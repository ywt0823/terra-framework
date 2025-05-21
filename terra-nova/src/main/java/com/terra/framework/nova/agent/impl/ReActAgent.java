package com.terra.framework.nova.agent.impl;

import com.terra.framework.nova.agent.*;
import com.terra.framework.nova.agent.memory.MemoryManager;
import com.terra.framework.nova.agent.tool.Tool;
import com.terra.framework.nova.agent.tool.ToolRegistry;
import com.terra.framework.nova.llm.service.AIService;
import com.terra.framework.nova.prompt.template.PromptTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 基于ReAct模式的代理实现
 * 使用思考-行动-观察循环解决问题
 *
 * @author terra-nova
 */
@Slf4j
public class ReActAgent implements Agent {

    private static final Pattern ACTION_PATTERN = Pattern.compile("```\\s*json\\s*\\{([^}]*)\\}\\s*```", Pattern.DOTALL);
    private static final Pattern THOUGHT_PATTERN = Pattern.compile("思考:\\s*(.*?)(?=行动:|$)", Pattern.DOTALL);

    private final AIService aiService;
    private final ToolRegistry toolRegistry;
    private final AgentConfig config;
    private final PromptTemplate promptTemplate;
    private final MemoryManager memoryManager;
    private final ScheduledExecutorService executorService;

    /**
     * 构造函数
     *
     * @param aiService       AI服务
     * @param toolRegistry    工具注册表
     * @param config          代理配置
     * @param promptTemplate  提示词模板
     * @param memoryManager   记忆管理器
     * @param executorService 执行器服务
     */
    public ReActAgent(
        AIService aiService,
        ToolRegistry toolRegistry,
        AgentConfig config,
        PromptTemplate promptTemplate,
        MemoryManager memoryManager,
        ScheduledExecutorService executorService) {
        this.aiService = aiService;
        this.toolRegistry = toolRegistry;
        this.config = config;
        this.promptTemplate = promptTemplate;
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

        for (int i = 0; i < config.getMaxSteps(); i++) {
            if (timeout.get()) {
                trace.getObservations().add("执行超时");
                break;
            }

            AgentStep step = AgentStep.builder()
                .id(String.valueOf(i + 1))
                .stepTimeMs(0)
                .build();

            long stepStartTime = System.currentTimeMillis();

            // 构建提示词
            Map<String, Object> promptVars = new HashMap<>(context);
            promptVars.put("previous_steps", formatPreviousSteps(steps));

            String promptText = promptTemplate.render(promptVars);

            // 获取LLM响应
            String llmResponse = aiService.generateText(
                promptText,
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
                steps.add(step);
                break;
            } else if (pair.getAction().getType() == AgentActionType.TOOL_CALL) {
                try {
                    String toolResult = executeTool(pair.getAction());
                    step.setActionResult(toolResult);
                    // 更新上下文
                    context.put("last_tool_result", toolResult);
                } catch (Exception e) {
                    String errorMsg = "工具执行错误: " + e.getMessage();
                    step.setActionResult(errorMsg);
                    context.put("last_tool_result", errorMsg);
                    log.error("Tool execution error", e);
                }
            }

            step.setStepTimeMs(System.currentTimeMillis() - stepStartTime);
            steps.add(step);

            if (i == config.getMaxSteps() - 1) {
                trace.getObservations().add("达到最大步骤数限制");
            }
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
        return tool.getParameters().stream()
            .map(param -> String.format(
                "%s: %s%s",
                param.getName(),
                param.getDescription(),
                param.isRequired() ? " (必需)" : ""
            ))
            .collect(Collectors.joining(", "));
    }

    private String formatPreviousSteps(List<AgentStep> steps) {
        if (steps.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (AgentStep step : steps) {
            sb.append("步骤 ").append(step.getId()).append(":\n");
            sb.append("思考: ").append(step.getThought()).append("\n");
            sb.append("行动: \n```\n");

            if (step.getAction().getType() == AgentActionType.TOOL_CALL) {
                sb.append(String.format(
                    "{\n  \"tool\": \"%s\",\n  \"parameters\": %s\n}",
                    step.getAction().getTool(),
                    formatParameters(step.getAction().getParameters())
                ));
            } else if (step.getAction().getType() == AgentActionType.FINAL_ANSWER) {
                sb.append(String.format(
                    "{\n  \"answer\": \"%s\"\n}",
                    step.getAction().getParameters().get("answer")
                ));
            }

            sb.append("\n```\n");

            if (step.getActionResult() != null) {
                sb.append("观察: ").append(step.getActionResult()).append("\n\n");
            }
        }

        return sb.toString();
    }

    private String formatParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{\n");
        int i = 0;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            sb.append("    \"").append(entry.getKey()).append("\": ");

            if (entry.getValue() instanceof String) {
                sb.append("\"").append(entry.getValue()).append("\"");
            } else {
                sb.append(entry.getValue());
            }

            if (i < parameters.size() - 1) {
                sb.append(",\n");
            } else {
                sb.append("\n");
            }
            i++;
        }
        sb.append("  }");

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
        Matcher matcher = THOUGHT_PATTERN.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // 如果没有找到匹配的思考部分，将整个响应作为思考
        return response.trim();
    }

    private AgentAction extractAction(String response) {
        Matcher matcher = ACTION_PATTERN.matcher(response);
        if (matcher.find()) {
            try {
                String jsonContent = "{" + matcher.group(1) + "}";
                Map<String, Object> actionMap = parseJson(jsonContent);

                if (actionMap.containsKey("answer")) {
                    // 这是最终回答
                    Map<String, Object> params = new HashMap<>();
                    params.put("answer", actionMap.get("answer"));
                    return AgentAction.builder()
                        .type(AgentActionType.FINAL_ANSWER)
                        .parameters(params)
                        .build();
                } else if (actionMap.containsKey("tool")) {
                    // 这是工具调用
                    String toolName = actionMap.get("tool").toString();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parameters = (Map<String, Object>) actionMap.getOrDefault("parameters", Collections.emptyMap());

                    return AgentAction.builder()
                        .type(AgentActionType.TOOL_CALL)
                        .tool(toolName)
                        .parameters(parameters)
                        .build();
                }
            } catch (Exception e) {
                log.error("解析动作失败", e);
            }
        }

        // 默认返回错误处理动作
        return AgentAction.builder()
            .type(AgentActionType.ERROR_HANDLING)
            .parameters(Map.of("error", "无法解析动作"))
            .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        // 简单实现，实际项目中应使用Jackson或Gson等库
        if (json.contains("\"answer\"")) {
            String answer = json.replaceAll(".*\"answer\"\\s*:\\s*\"([^\"]*)\".*", "$1");
            return Map.of("answer", answer);
        } else if (json.contains("\"tool\"")) {
            String tool = json.replaceAll(".*\"tool\"\\s*:\\s*\"([^\"]*)\".*", "$1");

            // 解析参数
            Map<String, Object> parameters = new HashMap<>();
            if (json.contains("\"parameters\"")) {
                String paramsStr = json.replaceAll(".*\"parameters\"\\s*:\\s*\\{([^}]*)\\}.*", "$1");
                String[] paramPairs = paramsStr.split(",");
                for (String pair : paramPairs) {
                    if (pair.contains(":")) {
                        String[] keyValue = pair.split(":", 2);
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim().replaceAll("\"", "");
                        parameters.put(key, value);
                    }
                }
            }

            return Map.of(
                "tool", tool,
                "parameters", parameters
            );
        }

        return Collections.emptyMap();
    }

    private String executeTool(AgentAction action) {
        if (action.getType() != AgentActionType.TOOL_CALL) {
            return "不是有效的工具调用";
        }

        String toolName = action.getTool();
        Map<String, Object> parameters = action.getParameters();

        if (!toolRegistry.hasTool(toolName)) {
            return "找不到工具: " + toolName;
        }

        try {
            Object result = toolRegistry.executeTool(toolName, parameters);
            return result != null ? result.toString() : "工具执行完成，无返回结果";
        } catch (Exception e) {
            log.error("Tool execution error", e);
            return "工具执行错误: " + e.getMessage();
        }
    }

    @Override
    public AgentConfig getConfig() {
        return this.config;
    }

    @Override
    public void reset() {
        memoryManager.clear();
    }
}
