package com.terra.framework.nova.llm.chain;

import com.terra.framework.nova.llm.model.base.LLMModel;

/**
 * Chain接口
 *
 * @param <I> 输入类型
 * @param <O> 输出类型
 */
public interface Chain<I, O> {

    /**
     * 运行Chain
     *
     * @param input 输入
     * @return 输出
     */
    O run(I input);

    /**
     * 初始化Chain
     *
     * @param model LLM模型
     */
    void init(LLMModel model);

    /**
     * 关闭Chain
     */
    default void close() {
        // 默认空实现
    }
}
