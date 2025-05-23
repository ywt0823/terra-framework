package com.terra.framework.nova.common.component;

import com.terra.framework.nova.common.annotation.AIComponent;
import com.terra.framework.nova.common.annotation.AIParameter;
import com.terra.framework.nova.common.annotation.ComponentType;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zeus
 * @date 2025年05月23日 10:05
 * @description AiCommonComponet
 */
@Slf4j
public class AiCommonComponent {


    private final Random random = new Random();

    /**
     * 计算数学表达式
     *
     * @param expression 数学表达式
     * @return 计算结果
     */
    @AIComponent(
        name = "calculate",
        description = "计算数学表达式，支持基础运算符：+, -, *, /, % 和括号"
    )
    public double calculate(
        @AIParameter(description = "数学表达式，例如 2 * (3 + 4)") String expression
    ) {
        log.info("Calculating expression: {}", expression);
        try {
            // 简单起见，这里仅使用Java自带的脚本引擎
            // 实际使用中应当使用更健壮的解析器
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            if (engine == null) {
                throw new IllegalStateException("JavaScript引擎不可用");
            }

            Object result = engine.eval(expression);
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            } else {
                throw new IllegalArgumentException("表达式未返回数值结果");
            }
        } catch (Exception e) {
            log.error("计算表达式时出错: {}", expression, e);
            throw new IllegalArgumentException("无法计算表达式: " + e.getMessage());
        }
    }

    /**
     * 获取当前日期时间
     *
     * @param format 格式化字符串
     * @return 格式化后的日期时间
     */
    @AIComponent(
        name = "get_current_datetime",
        description = "获取当前的日期和时间，可以指定格式"
    )
    public String getCurrentDateTime(
        @AIParameter(
            description = "日期时间格式，例如 yyyy-MM-dd HH:mm:ss",
            required = false,
            defaultValue = "yyyy-MM-dd HH:mm:ss"
        ) String format
    ) {
        log.info("Getting current date time with format: {}", format);
        try {
            LocalDateTime now = LocalDateTime.now();
            return now.format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            log.error("格式化日期时间出错: {}", format, e);
            return LocalDateTime.now().toString();
        }
    }

    /**
     * 获取两个日期之间的天数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天数差
     */
    @AIComponent(
        name = "days_between",
        description = "计算两个日期之间的天数差"
    )
    public long daysBetween(
        @AIParameter(description = "开始日期，格式为 yyyy-MM-dd") String startDate,
        @AIParameter(description = "结束日期，格式为 yyyy-MM-dd") String endDate
    ) {
        log.info("Calculating days between: {} and {}", startDate, endDate);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            return java.time.temporal.ChronoUnit.DAYS.between(start, end);
        } catch (Exception e) {
            log.error("计算日期差错误", e);
            throw new IllegalArgumentException("无法计算日期: " + e.getMessage());
        }
    }

    /**
     * 分析文本中的关键词频率
     *
     * @param text     文本内容
     * @param keywords 要分析的关键词列表，逗号分隔
     * @return 分析结果
     */
    @AIComponent(
        name = "analyze_keywords",
        description = "分析文本中指定关键词的出现频率",
        types = {ComponentType.FUNCTION} // 仅作为函数使用
    )
    public String analyzeKeywords(
        @AIParameter(description = "需要分析的文本内容") String text,
        @AIParameter(description = "要分析的关键词列表，用逗号分隔") String keywords
    ) {
        log.info("Analyzing keywords in text of length: {}", text.length());
        StringBuilder result = new StringBuilder();
        String[] keywordArray = keywords.split(",");

        for (String keyword : keywordArray) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) continue;

            Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            int count = 0;
            while (matcher.find()) {
                count++;
            }

            result.append(keyword).append(": ").append(count).append("次\n");
        }

        return result.toString();
    }

    /**
     * 读取文件内容
     *
     * @param filePath 文件路径
     * @return 文件内容
     */
    @AIComponent(
        name = "read_file_content",
        description = "读取指定文件的内容",
        types = {ComponentType.TOOL} // 仅作为工具使用
    )
    public String readFileContent(
        @AIParameter(description = "文件的路径") String filePath
    ) {
        log.info("Reading file content: {}", filePath);
        try {
            Path path = Paths.get(filePath);
            return Files.readString(path);
        } catch (Exception e) {
            log.error("读取文件出错: {}", filePath, e);
            throw new IllegalArgumentException("无法读取文件: " + e.getMessage());
        }
    }

    /**
     * 列出目录内容
     *
     * @param directoryPath 目录路径
     * @return 文件列表
     */
    @AIComponent(
        name = "list_directory",
        description = "列出指定目录中的文件和子目录"
    )
    public List<String> listDirectory(
        @AIParameter(description = "目录路径") String directoryPath
    ) {
        log.info("Listing directory: {}", directoryPath);
        List<String> result = new ArrayList<>();
        try {
            File directory = new File(directoryPath);
            if (!directory.exists() || !directory.isDirectory()) {
                throw new IllegalArgumentException("指定路径不是一个有效的目录");
            }

            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    result.add(file.getName() + (file.isDirectory() ? "/" : ""));
                }
            }
            return result;
        } catch (Exception e) {
            log.error("列出目录出错: {}", directoryPath, e);
            throw new IllegalArgumentException("无法列出目录: " + e.getMessage());
        }
    }

    /**
     * 生成随机数
     *
     * @param min 最小值
     * @param max 最大值
     * @return 随机数
     */
    @AIComponent(
        name = "random_number",
        description = "生成指定范围内的随机整数"
    )
    public int randomNumber(
        @AIParameter(description = "随机数最小值") int min,
        @AIParameter(description = "随机数最大值") int max
    ) {
        log.info("Generating random number between {} and {}", min, max);
        if (min >= max) {
            throw new IllegalArgumentException("最小值必须小于最大值");
        }
        return random.nextInt(max - min + 1) + min;
    }
}
