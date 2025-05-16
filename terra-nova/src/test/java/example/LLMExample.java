package example;

import com.terra.framework.nova.llm.chain.Chain;
import com.terra.framework.nova.llm.core.ModelConfig;
import com.terra.framework.nova.llm.core.ModelType;
import com.terra.framework.nova.llm.manager.ChainConfig;
import com.terra.framework.nova.llm.manager.ChainManager;
import com.terra.framework.nova.llm.manager.ChainType;
import com.terra.framework.nova.llm.manager.ModelManager;

import java.util.Map;

/**
 * LLM使用示例
 */
public class LLMExample {

    public static void main(String[] args) {
        // 1. 创建模型管理器
        ModelManager modelManager = new ModelManager();

        // 2. 注册DeepSeek模型配置
        ModelConfig deepseekConfig = ModelConfig.builder()
            .type(ModelType.DEEPSEEK)
            .apiEndpoint("http://localhost:11434")
            .temperature(0.7d)
            .maxTokens(2048)
            .build();
        modelManager.registerConfig("deepseek", deepseekConfig);

        // 3. 创建Chain管理器
        ChainManager chainManager = new ChainManager(modelManager);

        // 4. 注册SQL Chain配置
        ChainConfig sqlChainConfig = ChainConfig.builder()
            .type(ChainType.SQL)
            .modelId("deepseek")
            .params(Map.of(
                "databaseUrl", "jdbc:mysql://localhost:3306/test",
                "username", "root",
                "password", "password"
            ))
            .build();
        chainManager.registerConfig("sql", sqlChainConfig);

        // 5. 注册对话Chain配置
        ChainConfig conversationChainConfig = ChainConfig.builder()
            .type(ChainType.CONVERSATION)
            .modelId("deepseek")
            .params(Map.of("maxHistory", "5"))
            .build();
        chainManager.registerConfig("conversation", conversationChainConfig);

        try {
            // 6. 使用SQL Chain
            Chain<String, String> sqlChain = chainManager.getChain("sql");
            String sqlResult = sqlChain.run("查询所有用户数量");
            System.out.println("SQL查询结果: " + sqlResult);

            // 7. 使用对话Chain
            Chain<String, String> conversationChain = chainManager.getChain("conversation");
            String response1 = conversationChain.run("你好，请介绍一下自己");
            System.out.println("对话响应1: " + response1);

            String response2 = conversationChain.run("你刚才说到了什么？");
            System.out.println("对话响应2: " + response2);

        } finally {
            // 8. 关闭资源
            chainManager.shutdown();
            modelManager.shutdown();
        }
    }
}
