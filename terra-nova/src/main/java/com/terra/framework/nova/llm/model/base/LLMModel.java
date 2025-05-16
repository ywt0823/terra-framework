package com.terra.framework.nova.llm.model.base;

import java.util.concurrent.Flow.Publisher;

/**
 * LLM模型的核心接口
 */
public interface LLMModel {


    /**
     * 预测文本
     *
     * @param prompt 输入提示
     * @return 预测结果
     */
    String predict(String prompt);

    /**
     * 流式预测文本
     *
     * @param prompt 输入提示
     * @return 预测结果流
     */
    Publisher<String> predictStream(String prompt);

    /**
     * 初始化模型
     */
    void init();

    /**
     * 关闭模型
     */
    void close();
}
