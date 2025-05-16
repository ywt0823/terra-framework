package com.terra.framework.nova.llm.chain;

import com.terra.framework.nova.llm.core.LLMModel;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL Chain实现
 */
@Slf4j
public class SQLChain implements Chain<String, String> {

    private LLMModel model;
    private String databaseUrl;
    private String username;
    private String password;

    public SQLChain(String databaseUrl, String username, String password) {
        this.databaseUrl = databaseUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public void init(LLMModel model) {
        this.model = model;
        // TODO: 初始化数据库连接
    }

    @Override
    public String run(String input) {
        log.debug("Running SQL Chain with input: {}", input);
        
        // 1. 使用LLM将自然语言转换为SQL
        String prompt = buildSQLPrompt(input);
        String sqlResponse = model.predict(prompt);
        String sql = extractSQL(sqlResponse);
        
        // 2. 执行SQL查询
        // TODO: 实现SQL执行逻辑
        
        // 3. 使用LLM将SQL结果转换为自然语言
        String resultPrompt = buildResultPrompt(sql, "sql_result");
        return model.predict(resultPrompt);
    }

    private String buildSQLPrompt(String input) {
        return String.format("""
            请将以下自然语言转换为SQL查询语句:
            %s
            只返回SQL语句，不需要其他解释。
            """, input);
    }

    private String extractSQL(String response) {
        // TODO: 从LLM响应中提取SQL语句
        return response.trim();
    }

    private String buildResultPrompt(String sql, String result) {
        return String.format("""
            请将以下SQL查询结果转换为自然语言描述:
            SQL: %s
            结果: %s
            """, sql, result);
    }

    @Override
    public void close() {
        // TODO: 关闭数据库连接
    }
} 