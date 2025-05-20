package com.terra.framework.nova.agent.impl;

import com.terra.framework.nova.agent.*;
import com.terra.framework.nova.agent.memory.MemoryManager;
import com.terra.framework.nova.agent.tool.Tool;
import com.terra.framework.nova.agent.tool.ToolRegistry;
import com.terra.framework.nova.llm.service.AIService;
import com.terra.framework.nova.prompt.template.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 基于规划然后执行的代理实现
 * 先生成计划，然后按步骤执行
 *
 * @author terra-nova
 */
@Slf4j
public class PlanAndExecuteAgent implements Agent {
    
    private static final Pattern PLAN_PATTERN = Pattern.compile("计划:\\s*(.*?)(?=步骤|$)", Pattern.DOTALL);
    private static final Pattern STEPS_PATTERN = Pattern.compile("步骤(\\d+):\\s*(.*?)(?=步骤\\d+:|$)", Pattern.DOTALL);
    private static final Pattern ACTION_PATTERN = Pattern.compile("```\\s*json\\s*\\{([^}]*)\\}\\s*```", Pattern.DOTALL);
    private static final Pattern THOUGHT_PATTERN = Pattern.compile("思考:\\s*(.*?)(?=行动:|$)", Pattern.DOTALL);
    
    private final AIService aiService;
    private final ToolRegistry toolRegistry;
    private final AgentConfig config;
    private final PromptTemplate plannerTemplate;
    private final PromptTemplate executorTemplate;
    private final MemoryManager memoryManager;
    private final ScheduledExecutorService executorService;
    
    /**
     * 构造函数
     *
     * @param aiService AI服务
     * @param toolRegistry 工具注册表
     * @param config 代理配置
     * @param plannerTemplate 规划提示词模板
     * @param executorTemplate 执行提示词模板
     * @param memoryManager 记忆管理器
     * @param executorService 执行器服务
     */
    public PlanAndExecuteAgent(
            AIService aiService,
            ToolRegistry toolRegistry,
            AgentConfig config,
            PromptTemplate plannerTemplate,
            PromptTemplate executorTemplate,
            MemoryManager memoryManager,
            ScheduledExecutorService executorService) {
        this.aiService = aiService;
        this.toolRegistry = toolRegistry;
        this.config = config;
        this.plannerTemplate = plannerTemplate;
        this.executorTemplate = executorTemplate;
        this.memoryManager = memoryManager;
        this.executorService = executorService;
    }
    
    @Override
    public AgentResponse execute(String task, Map<String, Object> parameters) {
        try {
            AgentExecutionTrace trace = executeWithTrace(task, parameters);
            return trace.getResponse();
        } catch (Exception e) {
            log.error("Agent execution error", e);
            AgentResponse response = AgentResponse.builder()
                    .success(false)
                    .errorMessage("代理执行错误: " + e.getMessage())
                    .build();
            return response;
        }
    }
    
    @Override
    public AgentExecutionTrace executeWithTrace(String task, Map<String, Object> parameters) {
        AgentExecutionTrace trace = AgentExecutionTrace.builder()
                .steps(new ArrayList<>())
                .observations(new ArrayList<>())
                .build();
        
        List<AgentStep> steps = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        Map<String, Integer> toolUsage = new HashMap<>();
        String finalAnswer = null;
        
        // 初始化上下文
        Map<String, Object> context = new HashMap<>(parameters != null ? parameters : Collections.emptyMap());
        context.put("task", task);
        context.put("tools", getToolDescriptions());
        
        // 超时控制
        AtomicBoolean timeout = new AtomicBoolean(false);
        executorService.schedule(() -> {
            timeout.set(true);
            log.warn("Agent execution timeout after {} ms", config.getTimeoutMs());
        }, config.getTimeoutMs(), TimeUnit.MILLISECONDS);
        
        try {
            // 第一阶段：生成计划
            log.info("Generating plan for task: {}", task);
            String plan = generatePlan(task, context);
            trace.getObservations().add("生成计划: " + plan);
            List<String> planSteps = extractPlanSteps(plan);
            
            if (planSteps.isEmpty()) {
                trace.getObservations().add("未能生成有效计划");
                throw new RuntimeException("未能生成有效计划");
            }
            
            // 第二阶段：逐步执行计划
            int stepNumber = 1;
            String previousStepResult = "";
            
            for (String planStep : planSteps) {
                if (timeout.get()) {
                    trace.getObservations().add("执行超时");
                    break;
                }
                
                log.info("Executing plan step {}: {}", stepNumber, planStep);
                AgentStep step = AgentStep.builder()
                        .id(String.valueOf(stepNumber))
                        .thought("执行计划步骤 " + stepNumber + ": " + planStep)
                        .stepTimeMs(0)
                        .build();
                
                long stepStartTime = System.currentTimeMillis();
                
                // 构建执行提示词
                Map<String, Object> executorVars = new HashMap<>(context);
                executorVars.put("plan", plan);
                executorVars.put("current_step", planStep);
                executorVars.put("previous_steps", formatPreviousSteps(steps));
                executorVars.put("previous_result", previousStepResult);
                executorVars.put("step_number", stepNumber);
                
                String executorPrompt = executorTemplate.render(executorVars);
                
                // 获取LLM响应
                String llmResponse = aiService.generateText(
                        executorPrompt,
                        config.getModelId(),
                        config.getLlmParameters()
                );
                
                // 解析思考和动作
                ThoughtActionPair pair = parseThoughtAction(llmResponse);
                step.setThought(pair.getThought());
                step.setAction(pair.getAction());
                
                // 更新工具使用统计
                if (pair.getAction().getType() == AgentActionType.TOOL_CALL) {
                    String toolName = pair.getAction().getTool();
                    toolUsage.put(toolName, toolUsage.getOrDefault(toolName, 0) + 1);
                }
                
                // 执行动作
                if (pair.getAction().getType() == AgentActionType.FINAL_ANSWER) {
                    Object answerObj = pair.getAction().getParameters().get("answer");
                    finalAnswer = answerObj != null ? answerObj.toString() : "";
                    step.setStepTimeMs(System.currentTimeMillis() - stepStartTime);
                    steps.add(step);
                    break;
                } else if (pair.getAction().getType() == AgentActionType.TOOL_CALL) {
                    try {
                        String toolResult = executeTool(pair.getAction());
                        step.setActionResult(toolResult);
                        previousStepResult = toolResult;
                        // 更新上下文
                        context.put("last_tool_result", toolResult);
                    } catch (Exception e) {
                        String errorMsg = "工具执行错误: " + e.getMessage();
                        step.setActionResult(errorMsg);
                        previousStepResult = errorMsg;
                        context.put("last_tool_result", errorMsg);
                        log.error("Tool execution error", e);
                    }
                }
                
                step.setStepTimeMs(System.currentTimeMillis() - stepStartTime);
                steps.add(step);
                stepNumber++;
                
                // 如果是最后一个计划步骤，且没有明确的最终答案，生成一个
                if (stepNumber > planSteps.size() && finalAnswer == null) {
                    finalAnswer = generateFinalAnswer(task, context, steps);
                    trace.getObservations().add("根据执行结果生成最终答案");
                }
            }
            
        } catch (Exception e) {
            log.error("Error during plan-and-execute", e);
            trace.getObservations().add("执行错误: " + e.getMessage());
        }
        
        // 创建响应
        AgentResponse response = AgentResponse.builder()
                .success(finalAnswer != null)
                .output(finalAnswer != null ? finalAnswer : "未能完成任务，请检查执行痕迹或重试")
                .toolUsage(toolUsage)
                .metadata(Map.of("taskTimeMs", (System.currentTimeMillis() - startTime)))
                .build();
        
        // 完成执行痕迹
        trace.setSteps(steps);
        trace.setResponse(response);
        trace.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        
        return trace;
    }
    
    /**
     * 生成任务执行计划
     *
     * @param task 任务描述
     * @param context 上下文
     * @return 计划文本
     */
    private String generatePlan(String task, Map<String, Object> context) {
        Map<String, Object> promptVars = new HashMap<>(context);
        promptVars.put("task", task);
        promptVars.put("tools", getToolDescriptions());
        
        String promptText = plannerTemplate.render(promptVars);
        
        String planResponse = aiService.generateText(
                promptText,
                config.getModelId(),
                config.getLlmParameters()
        );
        
        return planResponse;
    }
    
    /**
     * 从计划响应中提取步骤
     *
     * @param planResponse 计划响应文本
     * @return 步骤列表
     */
    private List<String> extractPlanSteps(String planResponse) {
        List<String> steps = new ArrayList<>();
        Matcher stepsMatcher = STEPS_PATTERN.matcher(planResponse);
        
        while (stepsMatcher.find()) {
            steps.add(stepsMatcher.group(2).trim());
        }
        
        // 如果没有找到格式化的步骤，尝试使用换行分割
        if (steps.isEmpty() && planResponse.contains("\n")) {
            String[] lines = planResponse.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("计划:") && !line.toLowerCase().contains("计划") && line.length() > 5) {
                    steps.add(line);
                }
            }
        }
        
        return steps;
    }
    
    /**
     * 基于步骤执行结果生成最终答案
     *
     * @param task 任务
     * @param context 上下文
     * @param steps 执行步骤
     * @return 最终答案
     */
    private String generateFinalAnswer(String task, Map<String, Object> context, List<AgentStep> steps) {
        Map<String, Object> promptVars = new HashMap<>(context);
        promptVars.put("task", task);
        promptVars.put("steps", formatPreviousSteps(steps));
        promptVars.put("generate_final_answer", true);
        
        String promptText = executorTemplate.render(promptVars);
        
        String response = aiService.generateText(
                promptText,
                config.getModelId(),
                config.getLlmParameters()
        );
        
        // 尝试提取明确的答案部分
        if (response.contains("答案:")) {
            String[] parts = response.split("答案:");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        
        return response.trim();
    }
    
    private String getToolDescriptions() {
        List<String> descriptions = new ArrayList<>();
        for (String toolId : config.getTools()) {
            Tool tool = toolRegistry.getTool(toolId);
            if (tool != null) {
                descriptions.add(String.format(
                        "%s: %s\n参数: %s",
                        tool.getName(),
                        tool.getDescription(),
                        formatToolParameters(tool)
                ));
            }
        }
        return String.join("\n\n", descriptions);
    }
    
    private String formatToolParameters(Tool tool) {
        if (tool.getParameters() == null || tool.getParameters().isEmpty()) {
            return "无";
        }
        return tool.getParameters().stream()
                .map(param -> String.format("%s (%s)%s", 
                        param.getName(), 
                        param.getType(), 
                        param.isRequired() ? " - 必填" : ""))
                .collect(Collectors.joining(", "));
    }
    
    private String formatPreviousSteps(List<AgentStep> steps) {
        if (steps.isEmpty()) {
            return "暂无执行步骤";
        }
        
        StringBuilder sb = new StringBuilder();
        for (AgentStep step : steps) {
            sb.append("步骤 ").append(step.getId()).append(":\n");
            sb.append("思考: ").append(step.getThought()).append("\n");
            AgentAction action = step.getAction();
            if (action != null) {
                sb.append("行动: ").append(action.getType())
                        .append(action.getType() == AgentActionType.TOOL_CALL ? 
                                " - " + action.getTool() + " " + formatParameters(action.getParameters()) : 
                                "")
                        .append("\n");
            }
            if (StringUtils.hasText(step.getActionResult())) {
                sb.append("结果: ").append(step.getActionResult()).append("\n");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    private String formatParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            
            sb.append(entry.getKey()).append(": ");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else {
                sb.append(value);
            }
        }
        sb.append("}");
        
        return sb.toString();
    }
    
    private static class ThoughtActionPair {
        private final String thought;
        private final AgentAction action;
        
        public ThoughtActionPair(String thought, AgentAction action) {
            this.thought = thought;
            this.action = action;
        }
        
        public String getThought() {
            return thought;
        }
        
        public AgentAction getAction() {
            return action;
        }
    }
    
    private ThoughtActionPair parseThoughtAction(String llmResponse) {
        String thought = extractThought(llmResponse);
        AgentAction action = extractAction(llmResponse);
        return new ThoughtActionPair(thought, action);
    }
    
    private String extractThought(String response) {
        Matcher thoughtMatcher = THOUGHT_PATTERN.matcher(response);
        if (thoughtMatcher.find()) {
            return thoughtMatcher.group(1).trim();
        }
        return response;
    }
    
    private AgentAction extractAction(String response) {
        Matcher actionMatcher = ACTION_PATTERN.matcher(response);
        if (actionMatcher.find()) {
            try {
                String jsonStr = "{" + actionMatcher.group(1) + "}";
                Map<String, Object> actionMap = parseJson(jsonStr);
                
                String actionType = (String) actionMap.getOrDefault("type", "TOOL_CALL");
                switch (actionType.toUpperCase()) {
                    case "FINAL_ANSWER":
                        return AgentAction.builder()
                                .type(AgentActionType.FINAL_ANSWER)
                                .parameters(actionMap)
                                .build();
                    case "TOOL_CALL":
                    default:
                        String tool = (String) actionMap.getOrDefault("tool", "");
                        if (!StringUtils.hasText(tool)) {
                            log.warn("Tool name not found in action: {}", jsonStr);
                            throw new RuntimeException("工具名称未指定");
                        }
                        
                        @SuppressWarnings("unchecked")
                        Map<String, Object> params = (Map<String, Object>) actionMap.getOrDefault("parameters", Collections.emptyMap());
                        return AgentAction.builder()
                                .type(AgentActionType.TOOL_CALL)
                                .tool(tool)
                                .parameters(params)
                                .build();
                }
            } catch (Exception e) {
                log.error("Failed to parse action", e);
            }
        }
        
        // 默认返回最终答案行动
        return AgentAction.builder()
                .type(AgentActionType.FINAL_ANSWER)
                .parameters(Map.of("answer", response))
                .build();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        // 简单的JSON解析，实际项目中应使用Jackson或Gson等库
        Map<String, Object> result = new HashMap<>();
        String processed = json.trim();
        
        if (processed.startsWith("{")) {
            processed = processed.substring(1);
        }
        if (processed.endsWith("}")) {
            processed = processed.substring(0, processed.length() - 1);
        }
        
        String[] entries = processed.split(",");
        for (String entry : entries) {
            entry = entry.trim();
            int colonPos = entry.indexOf(':');
            if (colonPos > 0) {
                String key = entry.substring(0, colonPos).trim();
                String value = entry.substring(colonPos + 1).trim();
                
                // 清理键名中的引号
                if (key.startsWith("\"") && key.endsWith("\"")) {
                    key = key.substring(1, key.length() - 1);
                }
                
                // 解析值
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    // 字符串
                    value = value.substring(1, value.length() - 1);
                    result.put(key, value);
                } else if (value.equals("true") || value.equals("false")) {
                    // 布尔值
                    result.put(key, Boolean.parseBoolean(value));
                } else if (value.startsWith("{") && value.endsWith("}")) {
                    // 嵌套对象
                    result.put(key, parseJson(value));
                } else {
                    // 数字或其他
                    try {
                        result.put(key, Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        try {
                            result.put(key, Double.parseDouble(value));
                        } catch (NumberFormatException e2) {
                            result.put(key, value);
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    private String executeTool(AgentAction action) {
        if (action.getType() != AgentActionType.TOOL_CALL) {
            throw new IllegalArgumentException("不是工具调用行动");
        }
        
        String toolName = action.getTool();
        if (!toolRegistry.hasTool(toolName)) {
            return "错误: 未找到工具 " + toolName;
        }
        
        try {
            Object result = toolRegistry.executeTool(toolName, action.getParameters());
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            log.error("Error executing tool: {}", toolName, e);
            return "工具执行错误: " + e.getMessage();
        }
    }
    
    @Override
    public AgentConfig getConfig() {
        return config;
    }
    
    @Override
    public void reset() {
        memoryManager.clear();
    }
} 