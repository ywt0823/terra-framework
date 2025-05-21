# 统一AI组件开发指南

Terra-Nova框架提供了统一的AI组件系统，可以让开发者便捷地创建既能作为Agent工具，又能作为LLM函数调用的组件。本指南详细介绍如何使用这套统一的注解系统开发AI组件。

## 1. 概述

Terra-Nova框架中的统一组件系统通过以下两个核心注解来支持组件开发：

- `@AIComponent` - 标记方法作为AI组件（可同时支持Tool和Function）
- `@AIParameter` - 标记组件参数

这种统一设计的核心优势：

- **一次开发，双重用途**：同一套代码可同时用作Agent工具和LLM函数调用
- **简化开发流程**：不再需要两套注解和处理逻辑
- **减少冗余代码**：避免为相同功能创建多个实现
- **统一配置管理**：通过集中配置管理组件行为

## 2. 快速开始

### 2.1 添加依赖

确保你的项目引入了Terra-Nova框架：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-nova</artifactId>
    <version>${terra-nova.version}</version>
</dependency>
```

### 2.2 使用注解创建组件

在任何Spring Bean中，使用`@AIComponent`注解来创建组件：

```java
@Component
public class MyAIComponents {

    @AIComponent(
        name = "calculate",
        description = "计算数学表达式的结果"
    )
    public double calculate(
        @AIParameter(description = "要计算的数学表达式") String expression
    ) {
        // 实现计算逻辑
        return evaluateExpression(expression);
    }
    
    private double evaluateExpression(String expression) {
        // 实现表达式求值
        return 0.0; // 仅作示例
    }
}
```

### 2.3 配置组件使用场景

通过`types`属性，指定组件使用场景：

```java
@AIComponent(
    name = "weather_report",
    description = "获取指定城市的天气信息",
    types = {ComponentType.TOOL} // 仅作为Agent工具使用
)
public String getWeatherReport(
    @AIParameter(description = "城市名称") String city
) {
    // 实现获取天气的逻辑
    return "晴天，温度25℃"; // 仅作示例
}
```

可选值：
- `ComponentType.TOOL` - 仅作为Agent工具
- `ComponentType.FUNCTION` - 仅作为LLM函数调用
- `{ComponentType.TOOL, ComponentType.FUNCTION}` - 两者都支持（默认）

## 3. 注解详解

### 3.1 @AIComponent 注解

用于标记一个方法作为AI组件。

| 属性 | 类型 | 说明 | 默认值 |
|-----|-----|------|-------|
| name | String | 组件名称，如果未指定则使用方法名 | "" |
| description | String | 组件功能描述 | 必填 |
| types | ComponentType[] | 使用场景，指定作为Tool或Function | {ComponentType.TOOL, ComponentType.FUNCTION} |
| category | String | 分类（主要用于Function） | "default" |

### 3.2 @AIParameter 注解

用于标记组件的参数。

| 属性 | 类型 | 说明 | 默认值 |
|-----|-----|------|-------|
| name | String | 参数名称，如果未指定则使用方法参数名 | "" |
| description | String | 参数描述 | 必填 |
| type | String | 参数类型，如果未指定将根据Java类型推断 | "" |
| required | boolean | 是否为必需参数 | true |
| defaultValue | String | 默认值，仅用于非必需参数 | "" |
| enumValues | String[] | 可选值列表，仅在Function模式下使用 | {} |
| validationRules | String[] | 参数验证规则，仅在Function模式下使用 | {} |

## 4. 类型映射

系统会自动将Java类型映射为相应的组件参数类型：

| Java类型 | 映射类型 |
|---------|---------|
| String | "string" |
| Integer/int, Long/long | "number" |
| Boolean/boolean | "boolean" |
| Double/double, Float/float | "number" |
| 数组类型, List 等 | "array" |
| 其他对象类型 | "object" |

## 5. 高级用法

### 5.1 参数验证与默认值

为非必需参数设置默认值：

```java
@AIComponent(
    name = "format_date",
    description = "格式化日期"
)
public String formatDate(
    @AIParameter(description = "要格式化的日期") LocalDate date,
    @AIParameter(
        description = "日期格式",
        required = false,
        defaultValue = "yyyy-MM-dd"
    ) String format
) {
    // 实现格式化逻辑
    return date.format(DateTimeFormatter.ofPattern(format));
}
```

### 5.2 使用场景特定配置

某些属性仅在特定场景下生效：

- `enumValues` - 仅在Function模式下使用，用于提供参数的可选值列表
- `validationRules` - 仅在Function模式下使用，用于定义参数验证规则

```java
@AIComponent(
    name = "get_weather",
    description = "获取天气信息"
)
public String getWeather(
    @AIParameter(description = "城市名") String city,
    @AIParameter(
        name = "format",
        description = "返回格式",
        required = false,
        defaultValue = "text",
        enumValues = {"text", "json", "html"}
    ) String format
) {
    // 实现逻辑
    return "晴天，温度25℃";
}
```

## 6. 配置项

在`application.yml`中可以配置组件系统：

```yaml
terra:
  nova:
    component:
      enabled: true                # 是否启用组件系统
      base-packages:               # 基础包路径
        - com.example.myapp
      auto-register: true          # 是否自动注册组件
      tool:
        enabled: true              # 是否启用工具功能
        legacy-annotation-support: true # 是否保留传统的@AITool注解支持
      function:
        enabled: true              # 是否启用函数功能
        legacy-annotation-support: true # 是否保留传统的@AIFunction注解支持
        validate-parameter-types: true  # 是否验证参数类型
```

## 7. 最佳实践

开发AI组件时，遵循以下最佳实践：

1. **明确命名和描述**：为组件和参数提供清晰的名称和描述
2. **参数类型明确**：尽量使用明确的参数类型，避免模糊的Object类型
3. **合理使用必需参数**：只将真正必需的参数标记为required=true
4. **提供默认值**：为非必需参数提供合理的默认值
5. **异常处理**：妥善处理异常，提供友好的错误信息
6. **功能单一**：每个组件应专注于单一功能
7. **性能考虑**：特别是用作Agent工具的组件，应注意执行效率

## 8. 迁移指南

旧的`@AITool`、`@AIToolParameter`和`@AIFunction`、`@AIParameter`注解系统已被完全移除。如果你之前使用了这些注解，请按照以下步骤迁移到新的统一注解系统：

### 8.1 迁移步骤

1. 将`@AITool`或`@AIFunction`替换为`@AIComponent`
2. 将`@AIToolParameter`或原函数的`@AIParameter`替换为新的`@AIParameter`
3. 根据需要设置`types`属性指定使用场景（默认同时启用TOOL和FUNCTION）

例如，从旧的工具注解：
```java
@AITool(name = "calc", description = "计算器")
public double calculate(
    @AIToolParameter(description = "表达式") String expr
) { ... }
```

迁移到：
```java
@AIComponent(name = "calc", description = "计算器")
public double calculate(
    @AIParameter(description = "表达式") String expr
) { ... }
```

或者从旧的函数注解：
```java
@AIFunction(name = "analyze", description = "分析文本", category = "text")
public String analyzeText(
    @com.terra.framework.nova.function.annotation.AIParameter(description = "文本内容") String text
) { ... }
```

迁移到：
```java
@AIComponent(name = "analyze", description = "分析文本", category = "text")
public String analyzeText(
    @AIParameter(description = "文本内容") String text
) { ... }
```

### 8.2 特殊场景迁移

#### 仅作为工具或仅作为函数使用

如果你希望组件仅作为工具或仅作为函数使用，可以通过`types`属性指定：

```java
// 仅作为工具使用
@AIComponent(
    name = "file_reader", 
    description = "读取文件内容",
    types = {ComponentType.TOOL}
)
public String readFile(
    @AIParameter(description = "文件路径") String filePath
) { ... }

// 仅作为函数使用
@AIComponent(
    name = "analyze_sentiment",
    description = "分析文本情感",
    types = {ComponentType.FUNCTION}
)
public String analyzeSentiment(
    @AIParameter(description = "文本内容") String text
) { ... }
```

#### 利用新参数功能

新的`@AIParameter`注解提供了更丰富的功能，利用这些功能来增强参数描述：

```java
@AIComponent(name = "search", description = "搜索内容")
public List<String> search(
    @AIParameter(description = "搜索关键词") String query,
    @AIParameter(
        description = "搜索类型",
        required = false,
        defaultValue = "web",
        enumValues = {"web", "image", "news", "video"}
    ) String type,
    @AIParameter(
        description = "结果数量",
        required = false,
        defaultValue = "10",
        validationRules = {
            "\"minimum\": 1",
            "\"maximum\": 50"
        }
    ) int limit
) { ... }
```

### 8.3 注意事项

- 统一注解系统提供了比旧注解更丰富的功能
- 所有已使用旧注解的代码必须迁移到新注解系统，否则将无法正常工作
- 进行迁移时，确保测试所有功能，以确保功能正常

## 9. 示例

### 9.1 基础计算工具

```java
@Component
public class MathComponents {
    
    @AIComponent(
        name = "add_numbers",
        description = "将两个数字相加"
    )
    public double add(
        @AIParameter(description = "第一个数字") double a,
        @AIParameter(description = "第二个数字") double b
    ) {
        return a + b;
    }
    
    @AIComponent(
        name = "calculate_expression",
        description = "计算数学表达式",
        category = "math"
    )
    public double calculate(
        @AIParameter(description = "数学表达式") String expression
    ) {
        // 使用第三方库进行表达式求值
        // 这里仅作示例
        return 0.0;
    }
}
```

### 9.2 文本处理工具

```java
@Component
public class TextComponents {
    
    @AIComponent(
        name = "word_count",
        description = "统计文本中的单词数量"
    )
    public int countWords(
        @AIParameter(description = "待分析的文本") String text
    ) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.split("\\s+").length;
    }
    
    @AIComponent(
        name = "text_summary",
        description = "生成文本摘要",
        types = {ComponentType.FUNCTION} // 仅作为函数
    )
    public String summarizeText(
        @AIParameter(description = "原始文本") String text,
        @AIParameter(
            description = "摘要长度（单词数）",
            required = false,
            defaultValue = "100"
        ) int length
    ) {
        // 实现摘要生成逻辑
        return "这是一个示例摘要...";
    }
}
``` 