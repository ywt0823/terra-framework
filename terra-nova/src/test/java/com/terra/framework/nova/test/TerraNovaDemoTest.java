package com.terra.framework.nova.test;

import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.service.AIService;
import com.terra.framework.nova.llm.service.BlenderService;
import com.terra.framework.nova.llm.service.EnhancedAIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.terra.framework.nova.llm.model.ModelType.DEEPSEEK;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TestConfig.class)
public class TerraNovaDemoTest {

    @Autowired
    private AIService aiService;

    @Autowired
    private EnhancedAIService enhancedAIService;


//    @Autowired
//    private PromptService promptService;

    @Test
    public void testBasicGeneration() {
        // 基本文本生成
        String response = aiService.generateText("Tell me a short story about a robot.", "deepseek-chat");
        assertNotNull(response);
        System.out.println("Basic Generation Response: " + response);
    }

    @Test
    public void testChatWithParameters() {
        // 带参数的对话生成
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", 0.7);
        parameters.put("max_tokens", 150);

        String response = aiService.chat(Arrays.asList(
            Message.ofSystem("You are a helpful assistant."),
            Message.ofUser("What are the benefits of using Spring Boot?")
        ), parameters);

        assertNotNull(response);
        System.out.println("Chat Response: " + response);
    }

    @Test
    public void testAsyncGeneration() {
        // 异步生成
        CompletableFuture<String> future = aiService.generateTextAsync(
            "Explain quantum computing in simple terms."
        );

        future.thenAccept(response -> {
            assertNotNull(response);
            System.out.println("Async Response: " + response);
        }).join();
    }

//    @Test
//    public void testPromptTemplate() {
//        // 使用提示词模板
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("topic", "artificial intelligence");
//        variables.put("tone", "professional");
//
//        String renderedPrompt = promptService.render("article_intro", variables);
//        String response = aiService.generateText(renderedPrompt);
//
//        assertNotNull(response);
//        System.out.println("Template-based Response: " + response);
//    }

    @Test
    public void testModelBlending() {
        // 模型混合调用
        BlenderService blenderService = enhancedAIService.getBlenderService();

        // 添加多个模型
        blenderService.addModel("deepseek:deepseek-chat", 60);
        blenderService.addModel("deepseek:deepseek-chat-instruct", 40);

        String response = enhancedAIService.generateTextWithBlending(
            "Explain the concept of machine learning."
        );

        assertNotNull(response);
        System.out.println("Blended Response: " + response);
    }
}
