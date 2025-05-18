package com.terra.framework.nova.test;

import com.terra.framework.nova.function.Function;
import com.terra.framework.nova.function.FunctionCall;
import com.terra.framework.nova.function.annotation.AIFunction;
import com.terra.framework.nova.function.annotation.AIParameter;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.service.EnhancedAIService;
import com.terra.framework.nova.prompt.service.PromptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class WeatherReportTest {

    @Autowired
    private EnhancedAIService aiService;

    @Autowired
    private PromptService promptService;

    @Component
    public static class WeatherQueryHandler {
        @AIFunction(
            name = "query_weather",
            description = "Query current weather information for a specific city"
        )
        public Map<String, Object> handle(
            @AIParameter(name = "city", description = "The name of the city to query weather for", required = true)
            String city
        ) {
            // 模拟天气数据
            Map<String, Object> weatherInfo = new HashMap<>();
            weatherInfo.put("city", city);
            weatherInfo.put("temperature", "25°C");
            weatherInfo.put("humidity", "65%");
            weatherInfo.put("weather", "Sunny");
            weatherInfo.put("wind", "3m/s");
            return weatherInfo;
        }
    }

    @Test
    public void testWeatherReport() {
        // 1. 设置城市
        String city = "Shanghai";

        // 2. 获取天气数据
        Map<String, Object> weatherInfo = queryWeather(city);

        // 3. 使用Prompt模板生成天气报告
        String prompt = promptService.render("weather_report", weatherInfo);

        // 4. 使用LLM生成自然语言报告
        String weatherReport = aiService.generateText(prompt);

        System.out.println("Weather Report:");
        System.out.println(weatherReport);
    }

    private Map<String, Object> queryWeather(String city) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("city", city);
        
        FunctionCall functionCall = new FunctionCall("query_weather", parameters);
        @SuppressWarnings("unchecked")
        Map<String, Object> weatherInfo = (Map<String, Object>) functionCall;
        return weatherInfo;
    }
} 