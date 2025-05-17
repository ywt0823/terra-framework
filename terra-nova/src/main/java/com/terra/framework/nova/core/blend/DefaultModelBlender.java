package com.terra.framework.nova.core.blend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terra.framework.nova.core.model.AIModel;
import com.terra.framework.nova.core.model.Message;
import com.terra.framework.nova.core.model.ModelResponse;

/**
 * 默认模型混合器实现
 *
 * @author terra-nova
 */
public class DefaultModelBlender implements ModelBlender {

    private static final Logger log = LoggerFactory.getLogger(DefaultModelBlender.class);

    /**
     * 模型列表
     */
    private final List<AIModel> models = new ArrayList<>();

    /**
     * 模型权重映射（模型ID -> 权重值）
     */
    private final Map<String, Integer> modelWeights = new ConcurrentHashMap<>();

    /**
     * 结果合并器
     */
    private ResultMerger resultMerger;

    /**
     * 并行执行线程池
     */
    private final ExecutorService executorService;

    /**
     * 默认权重
     */
    private static final int DEFAULT_WEIGHT = 10;

    /**
     * 构造函数
     *
     * @param mergeStrategy 合并策略
     */
    public DefaultModelBlender(MergeStrategy mergeStrategy) {
        this.resultMerger = new DefaultResultMerger(mergeStrategy);
        // 线程池的大小取决于系统资源和预期的使用情况
        this.executorService = Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors())
        );
    }

    /**
     * 构造函数（使用默认合并策略）
     */
    public DefaultModelBlender() {
        this(MergeStrategy.WEIGHTED);
    }

    @Override
    public ModelResponse generateWithMultipleModels(String prompt, Map<String, Object> parameters) {
        if (models.isEmpty()) {
            throw new IllegalStateException("没有添加任何模型到混合器");
        }

        log.info("使用 {} 个模型执行混合生成", models.size());

        // 收集所有模型的响应
        List<ModelResponse> responses = models.stream()
                .map(model -> {
                    try {
                        log.debug("调用模型 {} 生成文本", model.getModelInfo().getModelId());
                        return model.generate(prompt, parameters);
                    } catch (Exception e) {
                        log.error("模型 {} 生成文本失败: {}", model.getModelInfo().getModelId(), e.getMessage());
                        return null;
                    }
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());

        // 合并结果
        return resultMerger.merge(responses, modelWeights);
    }

    @Override
    public CompletableFuture<ModelResponse> generateWithMultipleModelsAsync(String prompt, Map<String, Object> parameters) {
        if (models.isEmpty()) {
            CompletableFuture<ModelResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("没有添加任何模型到混合器"));
            return future;
        }

        log.info("使用 {} 个模型执行异步混合生成", models.size());

        // 创建所有模型的异步任务
        List<CompletableFuture<ModelResponse>> futures = models.stream()
                .map(model -> CompletableFuture.supplyAsync(() -> {
                    try {
                        log.debug("调用模型 {} 异步生成文本", model.getModelInfo().getModelId());
                        return model.generate(prompt, parameters);
                    } catch (Exception e) {
                        log.error("模型 {} 异步生成文本失败: {}", model.getModelInfo().getModelId(), e.getMessage());
                        return null;
                    }
                }, executorService))
                .collect(Collectors.toList());

        // 等待所有任务完成并合并结果
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<ModelResponse> responses = futures.stream()
                            .map(CompletableFuture::join)
                            .filter(response -> response != null)
                            .collect(Collectors.toList());

                    return resultMerger.merge(responses, modelWeights);
                });
    }

    @Override
    public ModelResponse chatWithMultipleModels(List<Message> messages, Map<String, Object> parameters) {
        if (models.isEmpty()) {
            throw new IllegalStateException("没有添加任何模型到混合器");
        }

        log.info("使用 {} 个模型执行混合聊天", models.size());

        // 收集所有模型的响应
        List<ModelResponse> responses = models.stream()
                .map(model -> {
                    try {
                        log.debug("调用模型 {} 聊天", model.getModelInfo().getModelId());
                        return model.chat(messages, parameters);
                    } catch (Exception e) {
                        log.error("模型 {} 聊天失败: {}", model.getModelInfo().getModelId(), e.getMessage());
                        return null;
                    }
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());

        // 合并结果
        return resultMerger.merge(responses, modelWeights);
    }

    @Override
    public CompletableFuture<ModelResponse> chatWithMultipleModelsAsync(List<Message> messages, Map<String, Object> parameters) {
        if (models.isEmpty()) {
            CompletableFuture<ModelResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("没有添加任何模型到混合器"));
            return future;
        }

        log.info("使用 {} 个模型执行异步混合聊天", models.size());

        // 创建所有模型的异步任务
        List<CompletableFuture<ModelResponse>> futures = models.stream()
                .map(model -> CompletableFuture.supplyAsync(() -> {
                    try {
                        log.debug("调用模型 {} 异步聊天", model.getModelInfo().getModelId());
                        return model.chat(messages, parameters);
                    } catch (Exception e) {
                        log.error("模型 {} 异步聊天失败: {}", model.getModelInfo().getModelId(), e.getMessage());
                        return null;
                    }
                }, executorService))
                .collect(Collectors.toList());

        // 等待所有任务完成并合并结果
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<ModelResponse> responses = futures.stream()
                            .map(CompletableFuture::join)
                            .filter(response -> response != null)
                            .collect(Collectors.toList());

                    return resultMerger.merge(responses, modelWeights);
                });
    }

    @Override
    public ModelBlender addModel(AIModel model, int weight) {
        if (model == null) {
            throw new IllegalArgumentException("模型不能为空");
        }

        if (weight <= 0) {
            throw new IllegalArgumentException("权重必须大于0");
        }

        String modelId = model.getModelInfo().getModelId();

        // 检查模型是否已存在
        for (AIModel existingModel : models) {
            if (existingModel.getModelInfo().getModelId().equals(modelId)) {
                // 更新权重
                modelWeights.put(modelId, weight);
                log.info("更新模型 {} 的权重为 {}", modelId, weight);
                return this;
            }
        }

        // 添加新模型
        models.add(model);
        modelWeights.put(modelId, weight);
        log.info("添加模型 {} 到混合器，权重为 {}", modelId, weight);

        return this;
    }

    @Override
    public ModelBlender addModel(AIModel model) {
        return addModel(model, DEFAULT_WEIGHT);
    }

    @Override
    public ModelBlender removeModel(String modelId) {
        models.removeIf(model -> model.getModelInfo().getModelId().equals(modelId));
        modelWeights.remove(modelId);
        log.info("从混合器中移除模型 {}", modelId);
        return this;
    }

    @Override
    public List<AIModel> getModels() {
        return new ArrayList<>(models);
    }

    @Override
    public ModelBlender setMergeStrategy(MergeStrategy mergeStrategy) {
        this.resultMerger = new DefaultResultMerger(mergeStrategy);
        log.info("设置结果合并策略为 {}", mergeStrategy);
        return this;
    }

    @Override
    public MergeStrategy getMergeStrategy() {
        return resultMerger.getStrategy();
    }

    /**
     * 关闭混合器，释放资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
