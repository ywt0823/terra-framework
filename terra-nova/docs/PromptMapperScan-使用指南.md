# PromptMapperScan 使用指南

## 概述

`@PromptMapperScan` 是 Terra Nova 框架中的一个强大功能，允许开发者为不同的 PromptMapper 接口指定不同的 AI 模型和配置。这个功能类似于 MyBatis 的 `@MapperScan`，提供了灵活的配置管理和模型隔离。

## 主要特性

- **灵活的包扫描**：可以指定特定的包路径进行扫描
- **模型绑定**：为特定的 PromptMapper 指定使用的 AI 模型
- **配置隔离**：不同的扫描配置可以有不同的模型配置
- **多配置支持**：支持多个 `@PromptMapperScan` 注解
- **向后兼容**：保持现有功能不变

## 基本用法

### 1. 基本配置

```java
@Configuration
@PromptMapperScan(
    basePackages = "com.example.prompts",
    chatModel = "deepSeekChatModel",
    temperature = 0.7,
    maxTokens = 1000
)
public class PromptConfiguration {
}
```

### 2. 多模型配置

```java
@Configuration
@PromptMapperScan(
    basePackages = "com.example.prompts.creative",
    chatModel = "openAiChatModel",
    temperature = 0.9,
    modelName = "gpt-4",
    configId = "creative-config"
)
@PromptMapperScan(
    basePackages = "com.example.prompts.analytical",
    chatModel = "deepSeekChatModel",
    temperature = 0.3,
    modelName = "deepseek-chat",
    configId = "analytical-config"
)
public class MultiModelPromptConfiguration {
}
```

### 3. 使用类型指定模型

```java
@Configuration
@PromptMapperScan(
    basePackages = "com.example.prompts",
    chatModelClass = DeepSeekChatModel.class,
    temperature = 0.7
)
public class TypedPromptConfiguration {
}
```

## 注解参数详解

### 包路径配置

| 参数 | 说明 | 示例 |
|------|------|------|
| `value` | 扫描的基础包路径（`basePackages` 的别名） | `@PromptMapperScan("com.example.prompts")` |
| `basePackages` | 扫描的基础包路径 | `basePackages = {"com.example.prompts", "com.example.other"}` |
| `basePackageClasses` | 基础包类（扫描这些类所在的包） | `basePackageClasses = {MyPromptMapper.class}` |

### 模型配置

| 参数 | 说明 | 示例 |
|------|------|------|
| `chatModel` | 指定使用的 ChatModel Bean 名称 | `chatModel = "deepSeekChatModel"` |
| `chatModelClass` | 指定使用的 ChatModel Bean 类型 | `chatModelClass = DeepSeekChatModel.class` |
| `modelName` | 模型名称 | `modelName = "gpt-4"` |

### 生成参数

| 参数 | 说明 | 默认值 | 示例 |
|------|------|--------|------|
| `temperature` | 温度值（0.0-1.0） | -1（使用默认值） | `temperature = 0.7` |
| `maxTokens` | 最大 token 数 | -1（使用默认值） | `maxTokens = 1000` |
| `topP` | top-p 值（0.0-1.0） | -1（使用默认值） | `topP = 0.9` |

### 过滤配置

| 参数 | 说明 | 默认值 | 示例 |
|------|------|--------|------|
| `annotationClass` | 要扫描的注解类型 | `PromptMapper.class` | `annotationClass = MyPromptMapper.class` |
| `excludeClasses` | 排除的类型 | `{}` | `excludeClasses = {TestMapper.class}` |

### 其他配置

| 参数 | 说明 | 示例 |
|------|------|------|
| `configId` | 配置的唯一标识符 | `configId = "creative-config"` |

## 使用示例

### 创建 PromptMapper 接口

```java
@PromptMapper
public interface CreativePromptMapper {
    
    /**
     * 生成诗歌
     */
    String generatePoem(@Param("theme") String theme);
    
    /**
     * 创作故事
     */
    String createStory(@Param("character") String character, @Param("setting") String setting);
}

@PromptMapper
public interface AnalyticalPromptMapper {
    
    /**
     * 分析数据
     */
    String analyzeData(@Param("data") String data);
    
    /**
     * 生成报告
     */
    String generateReport(@Param("metrics") String metrics);
}
```

### 创建 XML 模板

#### creative-prompts.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<mapper namespace="com.example.prompts.creative.CreativePromptMapper">
    
    <prompt id="generatePoem" temperature="0.9">
        请创作一首关于${theme}的诗歌，要求有韵律感和情感表达。
    </prompt>
    
    <prompt id="createStory" max-tokens="2000">
        请创作一个以${character}为主角，发生在${setting}的短篇故事。
    </prompt>
    
</mapper>
```

#### analytical-prompts.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<mapper namespace="com.example.prompts.analytical.AnalyticalPromptMapper">
    
    <prompt id="analyzeData" temperature="0.3">
        请分析以下数据并给出结论：${data}
    </prompt>
    
    <prompt id="generateReport" max-tokens="1500">
        根据以下指标生成分析报告：${metrics}
    </prompt>
    
</mapper>
```

### 配置扫描

```java
@Configuration
@PromptMapperScan(
    basePackages = "com.example.prompts.creative",
    chatModel = "openAiChatModel",
    temperature = 0.9,
    modelName = "gpt-4",
    configId = "creative-config"
)
@PromptMapperScan(
    basePackages = "com.example.prompts.analytical",
    chatModel = "deepSeekChatModel",
    temperature = 0.3,
    modelName = "deepseek-chat",
    configId = "analytical-config"
)
public class PromptConfiguration {
}
```

### 使用 PromptMapper

```java
@Service
public class ContentService {
    
    @Autowired
    private CreativePromptMapper creativeMapper;
    
    @Autowired
    private AnalyticalPromptMapper analyticalMapper;
    
    public String generateCreativeContent(String theme) {
        return creativeMapper.generatePoem(theme);
    }
    
    public String analyzeBusinessData(String data) {
        return analyticalMapper.analyzeData(data);
    }
}
```

## 配置优先级

配置优先级从高到低：

1. **XML 模板中的配置**：`<prompt temperature="0.8">`
2. **@PromptMapperScan 配置**：`@PromptMapperScan(temperature = 0.7)`
3. **全局默认配置**：`terra.nova.prompt.default-config.temperature=0.5`
4. **系统默认值**

## 高级特性

### 1. 排除特定类

```java
@PromptMapperScan(
    basePackages = "com.example.prompts",
    chatModel = "deepSeekChatModel",
    excludeClasses = {TestPromptMapper.class, MockPromptMapper.class}
)
```

### 2. 自定义注解类型

```java
@PromptMapperScan(
    basePackages = "com.example.prompts",
    annotationClass = MyCustomPromptMapper.class,
    chatModel = "openAiChatModel"
)
```

### 3. 使用 basePackageClasses

```java
@PromptMapperScan(
    basePackageClasses = {CreativePromptMapper.class, AnalyticalPromptMapper.class},
    chatModel = "deepSeekChatModel"
)
```

## 配置文件支持

### application.yml 配置

```yaml
terra:
  nova:
    prompt:
      # 传统扫描模式配置
      auto-scan: true
      auto-scan-base-packages: 
        - com.example.prompts
      default-chat-model: deepSeekChatModel
      
      # 模板文件路径
      mapper-locations: 
        - classpath*:/prompts/**/*.xml
        - classpath*:/templates/**/*.xml
      
      # 默认配置
      default-config:
        temperature: 0.7
        max-tokens: 1000
        top-p: 0.9
```

### application.properties 配置

```properties
# 传统扫描模式
terra.nova.prompt.auto-scan=true
terra.nova.prompt.auto-scan-base-packages=com.example.prompts
terra.nova.prompt.default-chat-model=deepSeekChatModel

# 模板文件路径
terra.nova.prompt.mapper-locations=classpath*:/prompts/**/*.xml

# 默认配置
terra.nova.prompt.default-config.temperature=0.7
terra.nova.prompt.default-config.max-tokens=1000
terra.nova.prompt.default-config.top-p=0.9
```

## 错误处理

### 1. 常见错误

#### ChatModel 未找到
```
IllegalStateException: ChatModel bean not found: openAiChatModel
```

**解决方案**：确保指定的 ChatModel Bean 存在于 Spring 上下文中。

#### 包路径未指定
```
IllegalArgumentException: Base packages must be specified for PromptMapperScan
```

**解决方案**：至少指定一个包路径。

#### 配置冲突
```
IllegalArgumentException: Cannot specify both chatModel and chatModelClass
```

**解决方案**：不要同时指定 `chatModel` 和 `chatModelClass`。

### 2. 调试技巧

#### 启用调试日志
```properties
logging.level.com.terra.framework.nova.prompt=DEBUG
```

#### 检查 Bean 注册
```java
@Component
public class PromptMapperDiagnostic {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @PostConstruct
    public void checkPromptMappers() {
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            if (beanName.contains("PromptMapper")) {
                System.out.println("Found PromptMapper bean: " + beanName);
            }
        }
    }
}
```

## 向后兼容性

### 传统扫描模式继续有效

```java
@TerraBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 混合使用

可以同时使用传统扫描和新的 `@PromptMapperScan`：

```java
@Configuration
@PromptMapperScan(
    basePackages = "com.example.prompts.special",
    chatModel = "specialChatModel"
)
public class MixedConfiguration {
    // 传统扫描会处理其他包
}
```

## 最佳实践

1. **按功能分包**：将不同类型的 PromptMapper 放在不同的包中
2. **使用 configId**：为每个扫描配置指定唯一的 ID，便于调试
3. **合理设置温度**：创意类任务使用较高温度，分析类任务使用较低温度
4. **配置隔离**：不同的业务域使用不同的模型配置
5. **模板组织**：将相关的模板放在同一个 XML 文件中

## 性能考虑

- 扫描结果会被缓存，不会影响运行时性能
- 模型实例会复用，减少内存开销
- 配置会在启动时预处理，避免运行时解析开销

## 故障排除

如果遇到问题，请检查：

1. 包路径是否正确
2. ChatModel Bean 是否存在
3. XML 模板文件是否能找到
4. 配置参数是否在有效范围内
5. 是否有冲突的 Bean 定义

更多信息请参考：
- [Terra Nova 框架文档](../README.md)
- [PromptMapper 基础用法](./PromptMapper-Guide.md)
- [配置参考](./Configuration-Reference.md) 