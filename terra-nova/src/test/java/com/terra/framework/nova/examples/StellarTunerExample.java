package com.terra.framework.nova.examples;

import com.terra.framework.nova.model.client.ModelClient;
import com.terra.framework.nova.model.router.ModelRouter;
import com.terra.framework.nova.model.router.RoutingContext;
import com.terra.framework.nova.tuner.ParameterTuner;
import com.terra.framework.nova.tuner.TuningContext;
import com.terra.framework.nova.tuner.TuningMetrics;
import com.terra.framework.nova.tuner.TuningResult;
import com.terra.framework.nova.tuner.service.TunerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

/**
 * StellarTuner参数调优示例
 *
 * @author terra-nova
 */
@Slf4j
@SpringBootApplication
public class StellarTunerExample {
    
    public static void main(String[] args) {
        SpringApplication.run(StellarTunerExample.class, args);
    }
    
    @Bean
    public CommandLineRunner tunerDemo(TunerService tunerService, ModelRouter modelRouter) {
        return args -> {
            log.info("=== StellarTuner参数调优示例 ===");
            
            // 展示可用的调优器
            log.info("可用的调优器：");
            tunerService.getAllTuners().forEach(tuner -> 
                log.info("- {}", tuner.getName()));
            
            // 创建调优上下文
            log.info("创建调优上下文...");
            TuningContext context = tunerService.createContext(
                    TuningContext.TaskType.GENERATION,
                    "文本生成任务",
                    "gpt-3.5-turbo",
                    "openai",
                    TuningContext.OptimizationGoal.BALANCED,
                    "写一篇关于人工智能的短文"
            );
            
            log.info("创建调优上下文成功: {}", context.getContextId());
            
            // 创建初始参数
            Map<String, Object> initialParams = new HashMap<>();
            initialParams.put("temperature", 0.7);
            initialParams.put("max_tokens", 200);
            initialParams.put("top_p", 0.9);
            initialParams.put("frequency_penalty", 0.0);
            initialParams.put("presence_penalty", 0.0);
            
            // 开始交互式调优
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            String currentTuner = "heuristic";
            Map<String, Object> currentParams = new HashMap<>(initialParams);
            
            while (!exit) {
                printMenu(context, currentTuner, currentParams);
                String choice = scanner.nextLine();
                
                switch (choice) {
                    case "1":
                        // 切换调优器
                        log.info("请选择调优器: [heuristic, bayesian]");
                        String newTuner = scanner.nextLine();
                        if (tunerService.getTunerByName(newTuner) != null) {
                            currentTuner = newTuner;
                            log.info("已切换到调优器: {}", currentTuner);
                        } else {
                            log.warn("无效的调优器: {}", newTuner);
                        }
                        break;
                        
                    case "2":
                        // 优化参数
                        log.info("正在优化参数...");
                        currentParams = tunerService.tuneParameters(currentParams, context.getContextId(), currentTuner);
                        log.info("优化后的参数: {}", currentParams);
                        break;
                        
                    case "3":
                        // 模拟调用模型
                        log.info("正在模拟调用模型...");
                        simulateModelCall(currentParams, context, currentTuner, tunerService, modelRouter);
                        break;
                        
                    case "4":
                        // 查看当前状态
                        log.info("=== 当前状态 ===");
                        log.info("上下文ID: {}", context.getContextId());
                        log.info("任务类型: {}", context.getTaskType());
                        log.info("优化目标: {}", context.getOptimizationGoal());
                        log.info("当前迭代: {}/{}", context.getIteration(), context.getMaxIterations());
                        log.info("当前调优器: {}", currentTuner);
                        log.info("当前参数: {}", currentParams);
                        break;
                        
                    case "5":
                        // 重置调优器
                        log.info("正在重置调优器: {}", currentTuner);
                        tunerService.resetTuner(currentTuner);
                        currentParams = new HashMap<>(initialParams);
                        context = tunerService.createContext(
                                TuningContext.TaskType.GENERATION,
                                "文本生成任务",
                                "gpt-3.5-turbo",
                                "openai",
                                TuningContext.OptimizationGoal.BALANCED,
                                "写一篇关于人工智能的短文"
                        );
                        log.info("重置完成，新上下文ID: {}", context.getContextId());
                        break;
                        
                    case "6":
                        // 退出
                        exit = true;
                        break;
                        
                    default:
                        log.warn("无效选项，请重试");
                }
                
                if (!exit) {
                    log.info("按回车键继续...");
                    scanner.nextLine();
                }
            }
            
            log.info("示例结束");
        };
    }
    
    private void printMenu(TuningContext context, String currentTuner, Map<String, Object> currentParams) {
        System.out.println("\n=== StellarTuner菜单 ===");
        System.out.println("1. 切换调优器 (当前: " + currentTuner + ")");
        System.out.println("2. 优化参数 (迭代: " + context.getIteration() + "/" + context.getMaxIterations() + ")");
        System.out.println("3. 模拟调用模型");
        System.out.println("4. 查看当前状态");
        System.out.println("5. 重置调优器");
        System.out.println("6. 退出");
        System.out.print("请选择: ");
    }
    
    private void simulateModelCall(Map<String, Object> parameters, TuningContext context, String tunerName,
                                  TunerService tunerService, ModelRouter modelRouter) {
        log.info("使用参数: {}", parameters);
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 模拟模型调用
        String result;
        try {
            // 尝试使用实际模型客户端
            ModelClient client = modelRouter.route(new RoutingContext());
            if (client != null) {
                result = client.generate(context.getInputText(), parameters);
                log.info("模型返回结果: {}", result);
            } else {
                // 无可用模型客户端，使用模拟结果
                result = simulateModelResponse(parameters);
                log.info("模拟结果: {}", result);
            }
        } catch (Exception e) {
            log.error("调用模型失败", e);
            result = "模拟结果: " + context.getInputText() + " (模拟生成)";
        }
        
        // 记录结束时间
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 创建指标对象
        TuningMetrics metrics = TuningMetrics.builder()
                .responseTimeMs(duration)
                .tokenCount(estimateTokenCount(result))
                .qualityScore(calculateQualityScore(parameters, result))
                .relevanceScore(0.85)  // 模拟相关性评分
                .cost(calculateCost(parameters, estimateTokenCount(result)))
                .build();
        
        // 更新调优结果
        tunerService.updateWithResult(parameters, context.getContextId(), tunerName, result, metrics, false);
        
        log.info("调用完成: 耗时={}ms, 令牌数={}, 质量分数={}, 成本={}美分",
                metrics.getResponseTimeMs(), metrics.getTokenCount(),
                metrics.getQualityScore(), metrics.getCost());
    }
    
    private String simulateModelResponse(Map<String, Object> parameters) {
        // 根据参数模拟不同质量的响应
        double temperature = getDoubleValue(parameters, "temperature");
        int maxTokens = (int) getDoubleValue(parameters, "max_tokens");
        
        StringBuilder response = new StringBuilder();
        response.append("人工智能是一项革命性的技术，正在改变我们的生活方式、工作方式和思考方式。");
        
        // 温度越高，响应越随机
        if (temperature > 0.7) {
            response.append(" 它可能会带来未知的惊喜，也可能会引发意想不到的挑战。");
            response.append(" 在未来，AI可能会发展出自己的意识和情感，这是科幻小说中常见的场景。");
        } else {
            response.append(" 它通过学习大量数据，识别模式并作出决策。");
            response.append(" 目前，AI已广泛应用于医疗、金融、教育等各个领域。");
        }
        
        // max_tokens影响响应长度
        if (maxTokens > 150) {
            response.append(" 人工智能技术主要包括机器学习、深度学习、自然语言处理等分支。");
            response.append(" 近年来，大型语言模型的发展尤为迅速，如GPT、BERT和LLaMA等。");
            response.append(" 这些模型能够生成类人文本，回答问题，甚至创作内容。");
        }
        
        // 添加模拟随机性
        response.append(" [请求参数: temp=" + temperature + ", max_tokens=" + maxTokens + "]");
        
        return response.toString();
    }
    
    private int estimateTokenCount(String text) {
        // 简单估算：大约每4个字符1个token
        return text.length() / 4;
    }
    
    private double calculateQualityScore(Map<String, Object> parameters, String result) {
        // 模拟质量评分：参数越合理，分数越高
        double temperature = getDoubleValue(parameters, "temperature");
        double topP = getDoubleValue(parameters, "top_p");
        
        // 针对文本生成任务，温度在0.3-0.7之间较好
        double tempScore = 1.0 - Math.abs((temperature - 0.5) * 2);
        
        // topP在0.85-0.95之间较好
        double topPScore = 1.0 - Math.abs((topP - 0.9) * 5);
        
        // 结合长度因素
        double lengthScore = Math.min(1.0, result.length() / 300.0);
        
        // 加权平均
        return 0.4 * tempScore + 0.3 * topPScore + 0.3 * lengthScore;
    }
    
    private double calculateCost(Map<String, Object> parameters, int tokenCount) {
        // 模拟成本计算：每1000个token约0.002美元
        return tokenCount * 0.002 / 1000.0;
    }
    
    private double getDoubleValue(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value == null) {
            return 0.0;
        }
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
} 