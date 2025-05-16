package com.terra.framework.nova.llm.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class DeepSeekTest {
    private static final String API_KEY = "sk-a3bb771d5ff04bceb72683e2a21d133f";
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";

    public static void main(String[] var0) {
        System.out.println("开始测试 DeepSeek API...");

        try {
            String var1 = "用一句话介绍一下你自己";
            String var2 = String.format("{\"model\": \"deepseek-chat\",\"messages\": [{\"role\": \"user\", \"content\": \"%s\"}],\"temperature\": 0.7,\"max_tokens\": 100}", var1);
            String var3 = sendRequest("https://api.deepseek.com/v1/chat/completions", var2);
            System.out.println("API响应:");
            System.out.println(var3);
            String var4 = extractContent(var3);
            System.out.println("\n提取的内容:");
            System.out.println(var4);
        } catch (Exception var5) {
            System.err.println("测试失败: " + var5.getMessage());
            var5.printStackTrace();
        }

    }

    private static String sendRequest(String var0, String var1) throws Exception {
        URL var2 = new URL(var0);
        HttpURLConnection var3 = (HttpURLConnection) var2.openConnection();
        var3.setRequestMethod("POST");
        var3.setRequestProperty("Content-Type", "application/json");
        var3.setRequestProperty("Authorization", "Bearer sk-a3bb771d5ff04bceb72683e2a21d133f");
        var3.setDoOutput(true);

        try (OutputStream var4 = var3.getOutputStream()) {
            byte[] var5 = var1.getBytes(StandardCharsets.UTF_8);
            var4.write(var5, 0, var5.length);
        }

        int var14 = var3.getResponseCode();
        if (var14 == 200) {
            try (BufferedReader var16 = new BufferedReader(new InputStreamReader(var3.getInputStream(), StandardCharsets.UTF_8))) {
                return (String) var16.lines().collect(Collectors.joining("\n"));
            }
        } else {
            BufferedReader var15 = new BufferedReader(new InputStreamReader(var3.getErrorStream(), StandardCharsets.UTF_8));

            try {
                String var6 = (String) var15.lines().collect(Collectors.joining("\n"));
                throw new Exception("HTTP错误: " + var14 + "\n" + var6);
            } catch (Throwable var12) {
                try {
                    var15.close();
                } catch (Throwable var8) {
                    var12.addSuppressed(var8);
                }

                throw var12;
            }
        }
    }

    private static String extractContent(String var0) {
        try {
            String var1 = "\"content\":";
            int var2 = var0.indexOf(var1);
            if (var2 == -1) {
                return "找不到content字段";
            } else {
                int var3 = var0.indexOf("\"", var2 + var1.length());
                if (var3 == -1) {
                    return "找不到content值的开始引号";
                } else {
                    int var4 = var3 + 1;

                    boolean var5;
                    for (var5 = false; var4 < var0.length(); ++var4) {
                        if (var0.charAt(var4) == '"' && var0.charAt(var4 - 1) != '\\') {
                            var5 = true;
                            break;
                        }
                    }

                    return !var5 ? "找不到content值的结束引号" : var0.substring(var3 + 1, var4);
                }
            }
        } catch (Exception var6) {
            return "提取失败: " + var6.getMessage();
        }
    }
}
