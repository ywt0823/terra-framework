package com.terra.framework.nova.llm.chain;

import com.terra.framework.nova.llm.core.LLMModel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话Chain实现
 */
@Slf4j
public class ConversationChain implements Chain<String, String> {

    private LLMModel model;
    private final List<String> history = new ArrayList<>();
    private final int maxHistory;

    public ConversationChain(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    @Override
    public void init(LLMModel model) {
        this.model = model;
    }

    @Override
    public String run(String input) {
        log.debug("Running Conversation Chain with input: {}", input);
        
        // 构建带有历史记录的提示
        String prompt = buildPrompt(input);
        
        // 获取模型响应
        String response = model.predict(prompt);
        
        // 更新历史记录
        updateHistory(input, response);
        
        return response;
    }

    private String buildPrompt(String input) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加历史对话记录
        if (!history.isEmpty()) {
            prompt.append("以下是之前的对话历史：\n");
            for (String message : history) {
                prompt.append(message).append("\n");
            }
            prompt.append("\n现在请回答新的问题：\n");
        }
        
        prompt.append(input);
        return prompt.toString();
    }

    private void updateHistory(String input, String response) {
        history.add("用户: " + input);
        history.add("助手: " + response);
        
        // 如果历史记录超过最大限制，移除最早的记录
        while (history.size() > maxHistory * 2) {
            history.remove(0);
            history.remove(0);
        }
    }

    @Override
    public void close() {
        history.clear();
    }
} 