package com.terra.framework.nova.llm.blend;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.llm.model.TokenUsage;

/**
 * 默认结果合并器实现
 *
 * @author terra-nova
 */
public class DefaultResultMerger implements ResultMerger {

    private static final Logger log = LoggerFactory.getLogger(DefaultResultMerger.class);

    /**
     * 合并策略
     */
    private final MergeStrategy strategy;

    /**
     * 随机数生成器
     */
    private final Random random = new Random();

    /**
     * 构造函数
     *
     * @param strategy 合并策略
     */
    public DefaultResultMerger(MergeStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public ModelResponse merge(List<ModelResponse> responses, Map<String, Integer> weights) {
        if (responses == null || responses.isEmpty()) {
            log.warn("没有响应可合并");
            return null;
        }

        if (responses.size() == 1) {
            return responses.get(0);
        }

        log.info("使用策略 {} 合并 {} 个模型响应", strategy, responses.size());

        // 过滤出非空响应
        List<ModelResponse> validResponses = responses.stream()
                .filter(r -> r != null && r.getContent() != null)
                .collect(Collectors.toList());

        if (validResponses.isEmpty()) {
            log.warn("没有有效的响应可合并");
            return null;
        }

        // 根据策略选择合并方法
        String mergedContent;
        switch (strategy) {
            case LONGEST:
                mergedContent = mergeLongest(validResponses);
                break;
            case SHORTEST:
                mergedContent = mergeShortest(validResponses);
                break;
            case CONCATENATE:
                mergedContent = mergeConcatenate(validResponses);
                break;
            case RANDOM:
                mergedContent = mergeRandom(validResponses);
                break;
            case WEIGHTED:
                mergedContent = mergeWeighted(validResponses, weights);
                break;
            case FIRST_SUCCESS:
                mergedContent = mergeFirstSuccess(validResponses);
                break;
            case LIST_FORMAT:
                mergedContent = mergeListFormat(validResponses);
                break;
            case INTERLEAVE:
                mergedContent = mergeInterleave(validResponses);
                break;
            case VOTING:
                mergedContent = mergeVoting(validResponses);
                break;
            case QUALITY_BASED:
                mergedContent = mergeQualityBased(validResponses);
                break;
            default:
                // 默认使用第一个有效响应
                mergedContent = validResponses.get(0).getContent();
        }

        // 构建合并响应
        ModelResponse mergedResponse = new ModelResponse();
        mergedResponse.setContent(mergedContent);
        mergedResponse.setModelId("blended-model");
        mergedResponse.setResponseId("blend-" + System.currentTimeMillis());
        mergedResponse.setCreatedAt(System.currentTimeMillis());

        // 合并令牌使用情况
        mergedResponse.setTokenUsage(mergeTokenUsage(validResponses));

        // 合并原始响应
        Map<String, Object> mergedRawResponse = new HashMap<>();
        mergedRawResponse.put("strategy", strategy.name());
        mergedRawResponse.put("model_count", validResponses.size());
        mergedRawResponse.put("source_responses", validResponses.stream()
                .map(ModelResponse::getRawResponse)
                .collect(Collectors.toList()));
        mergedResponse.setRawResponse(mergedRawResponse);

        return mergedResponse;
    }

    @Override
    public MergeStrategy getStrategy() {
        return strategy;
    }

    /**
     * 选择最长的响应
     */
    private String mergeLongest(List<ModelResponse> responses) {
        return responses.stream()
                .max(Comparator.comparingInt(r -> r.getContent().length()))
                .map(ModelResponse::getContent)
                .orElse("");
    }

    /**
     * 选择最短的响应
     */
    private String mergeShortest(List<ModelResponse> responses) {
        return responses.stream()
                .min(Comparator.comparingInt(r -> r.getContent().length()))
                .map(ModelResponse::getContent)
                .orElse("");
    }

    /**
     * 连接所有响应
     */
    private String mergeConcatenate(List<ModelResponse> responses) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < responses.size(); i++) {
            ModelResponse response = responses.get(i);
            sb.append("模型 ").append(i + 1).append(" 的回答:\n");
            sb.append(response.getContent());
            if (i < responses.size() - 1) {
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    /**
     * 随机选择一个响应
     */
    private String mergeRandom(List<ModelResponse> responses) {
        int index = random.nextInt(responses.size());
        return responses.get(index).getContent();
    }

    /**
     * 根据权重选择响应
     */
    private String mergeWeighted(List<ModelResponse> responses, Map<String, Integer> weights) {
        if (weights == null || weights.isEmpty()) {
            return mergeRandom(responses);
        }

        // 计算总权重
        int totalWeight = 0;
        for (ModelResponse response : responses) {
            String modelId = response.getModelId();
            totalWeight += weights.getOrDefault(modelId, 1);
        }

        // 随机值落在哪个区间
        int randomValue = random.nextInt(Math.max(1, totalWeight));
        int currentWeight = 0;

        for (ModelResponse response : responses) {
            String modelId = response.getModelId();
            currentWeight += weights.getOrDefault(modelId, 1);
            if (randomValue < currentWeight) {
                return response.getContent();
            }
        }

        // 防止权重计算错误
        return responses.get(0).getContent();
    }

    /**
     * 使用第一个成功的响应
     */
    private String mergeFirstSuccess(List<ModelResponse> responses) {
        return responses.get(0).getContent();
    }

    /**
     * 以列表格式合并
     */
    private String mergeListFormat(List<ModelResponse> responses) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < responses.size(); i++) {
            sb.append(i + 1).append(". ");
            sb.append(responses.get(i).getContent().trim());
            if (i < responses.size() - 1) {
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    /**
     * 交错合并（每个模型的内容交替组合）
     */
    private String mergeInterleave(List<ModelResponse> responses) {
        // 简化实现：按段落交错
        StringBuilder sb = new StringBuilder();

        // 将每个响应按段落分割
        List<String[]> paragraphsList = responses.stream()
                .map(r -> r.getContent().split("\\n\\s*\\n"))
                .collect(Collectors.toList());

        // 找出最长的段落数
        int maxParagraphs = paragraphsList.stream()
                .mapToInt(paragraphs -> paragraphs.length)
                .max()
                .orElse(0);

        // 交错合并段落
        for (int i = 0; i < maxParagraphs; i++) {
            for (String[] paragraphs : paragraphsList) {
                if (i < paragraphs.length) {
                    sb.append(paragraphs[i].trim());
                    sb.append("\n\n");
                }
            }
        }

        return sb.toString().trim();
    }

    /**
     * 投票选择（简单实现）
     */
    private String mergeVoting(List<ModelResponse> responses) {
        // 简化实现：假设响应是简单的分类结果，选择出现次数最多的
        Map<String, Integer> voteCount = new HashMap<>();

        for (ModelResponse response : responses) {
            String content = response.getContent().trim();
            voteCount.put(content, voteCount.getOrDefault(content, 0) + 1);
        }

        return voteCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(responses.get(0).getContent());
    }

    /**
     * 质量选择（简化实现）
     */
    private String mergeQualityBased(List<ModelResponse> responses) {
        // 实际应用中，可以基于更复杂的质量评估指标
        // 简化实现：根据内容长度和结构评估质量

        return responses.stream()
                .sorted(Comparator
                        // 优先选择长度适中的响应（太短可能信息不足，太长可能冗余）
                        .comparing((ModelResponse r) -> {
                            int length = r.getContent().length();
                            int optimalLength = 500; // 假设理想长度为500
                            return -Math.abs(length - optimalLength);
                        })
                        // 其次考虑段落数（更多的段落可能意味着更结构化）
                        .thenComparing(r -> r.getContent().split("\\n\\s*\\n").length))
                .findFirst()
                .map(ModelResponse::getContent)
                .orElse(responses.get(0).getContent());
    }

    /**
     * 合并令牌使用情况
     */
    private TokenUsage mergeTokenUsage(List<ModelResponse> responses) {
        int totalPromptTokens = 0;
        int totalCompletionTokens = 0;

        for (ModelResponse response : responses) {
            TokenUsage usage = response.getTokenUsage();
            if (usage != null) {
                totalPromptTokens += usage.getPromptTokens();
                totalCompletionTokens += usage.getCompletionTokens();
            }
        }

        return TokenUsage.of(totalPromptTokens, totalCompletionTokens);
    }
}
