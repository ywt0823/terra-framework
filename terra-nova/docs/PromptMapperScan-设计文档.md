# PromptMapperScan 功能设计文档

## 1. 项目概述

### 1.1 当前架构分析

Terra Nova 模块当前的 PromptMapper 功能架构：

- **@PromptMapper** 注解：标识接口为 Prompt Mapper
- **PromptMapperRegistrar**：负责扫描和注册 PromptMapper 接口
- **ClassPathPromptMapperScanner**：扫描指定包路径下的 PromptMapper 接口
- **PromptMapperFactoryBean**：创建 PromptMapper 接口的动态代理实例
- **PromptMapperProxy**：处理方法调用，查找模版并执行
- **PromptTemplateRegistry**：存储和管理XML模版
- **全局配置**：通过 TerraPromptAutoConfiguration 进行自动配置

### 1.2 改进目标

新增 `@PromptMapperScan` 注解，实现类似 MyBatis `@MapperScan` 的功能：

1. **灵活的包扫描**：可以指定特定的包路径进行扫描
2. **模型绑定**：为特定的 PromptMapper 指定使用的 AI 模型
3. **配置隔离**：不同的扫描配置可以有不同的模型配置
4. **多配置支持**：支持多个 `@PromptMapperScan` 注解
5. **向后兼容**：保持现有功能不变

## 2. 核心设计

### 2.1 @PromptMapperScan 注解设计

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(PromptMapperScannerRegistrar.class)
@Repeatable(PromptMapperScans.class)
public @interface PromptMapperScan {
    
    /**
     * 扫描的基础包路径
     */
    String[] value() default {};
    
    /**
     * 扫描的基础包路径（与value作用相同）
     */
    String[] basePackages() default {};
    
    /**
     * 扫描的基础包类（取类所在的包）
     */
    Class<?>[] basePackageClasses() default {};
    
    /**
     * 指定使用的 ChatModel Bean 名称
     */
    String chatModel() default "";
    
    /**
     * 指定使用的 ChatModel Bean 类型
     */
    Class<? extends ChatModel> chatModelClass() default ChatModel.class;
    
    /**
     * 模型相关的默认配置
     */
    String modelName() default "";
    
    /**
     * 默认温度值
     */
    double temperature() default -1;
    
    /**
     * 默认最大token数
     */
    int maxTokens() default -1;
    
    /**
     * 默认top-p值
     */
    double topP() default -1;
    
    /**
     * 包含的注解类型过滤器
     */
    Class<? extends Annotation> annotationClass() default PromptMapper.class;
    
    /**
     * 排除的类型过滤器
     */
    Class<?>[] excludeClasses() default {};
}
```

### 2.2 支持多个扫描配置

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface PromptMapperScans {
    PromptMapperScan[] value();
}
```

### 2.3 扫描配置数据结构

```java
public class PromptMapperScanConfiguration {
    private String[] basePackages;
    private String chatModelBeanName;
    private Class<? extends ChatModel> chatModelClass;
    private PromptConfig defaultConfig;
    private Class<? extends Annotation> annotationClass;
    private Class<?>[] excludeClasses;
    
    // getters and setters
}
```

## 3. 实现方案

### 3.1 新增组件

#### 3.1.1 PromptMapperScannerRegistrar

```java
public class PromptMapperScannerRegistrar implements ImportBeanDefinitionRegistrar {
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Set<AnnotationAttributes> scans = getPromptMapperScans(metadata);
        
        for (AnnotationAttributes scan : scans) {
            PromptMapperScanConfiguration config = parseConfiguration(scan);
            registerPromptMappers(config, registry);
        }
    }
    
    private void registerPromptMappers(PromptMapperScanConfiguration config, BeanDefinitionRegistry registry) {
        EnhancedPromptMapperScanner scanner = new EnhancedPromptMapperScanner(registry);
        scanner.setScanConfiguration(config);
        scanner.registerFilters();
        scanner.doScan(config.getBasePackages());
    }
}
```

#### 3.1.2 EnhancedPromptMapperScanner

```java
public class EnhancedPromptMapperScanner extends ClassPathBeanDefinitionScanner {
    
    private PromptMapperScanConfiguration scanConfiguration;
    
    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        
        if (!beanDefinitions.isEmpty()) {
            processBeanDefinitions(beanDefinitions);
        }
        
        return beanDefinitions;
    }
    
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        for (BeanDefinitionHolder holder : beanDefinitions) {
            AbstractBeanDefinition definition = (AbstractBeanDefinition) holder.getBeanDefinition();
            String mapperClassName = definition.getBeanClassName();
            
            // 配置 FactoryBean
            definition.setBeanClass(EnhancedPromptMapperFactoryBean.class);
            definition.getConstructorArgumentValues().addGenericArgumentValue(mapperClassName);
            
            // 注入扫描配置
            definition.getPropertyValues().add("scanConfiguration", scanConfiguration);
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }
    }
}
```

#### 3.1.3 EnhancedPromptMapperFactoryBean

```java
public class EnhancedPromptMapperFactoryBean<T> extends PromptMapperFactoryBean<T> {
    
    private PromptMapperScanConfiguration scanConfiguration;
    
    public void setScanConfiguration(PromptMapperScanConfiguration scanConfiguration) {
        this.scanConfiguration = scanConfiguration;
    }
    
    @Override
    public T getObject() {
        // 根据扫描配置解析 ChatModel
        ChatModel chatModel = resolveChatModel();
        
        // 创建增强的代理
        EnhancedPromptMapperProxy proxy = new EnhancedPromptMapperProxy(
            getApplicationContext(),
            getRegistry(),
            getMapperInterface(),
            chatModel,
            scanConfiguration
        );
        
        return (T) Proxy.newProxyInstance(
            getMapperInterface().getClassLoader(),
            new Class[]{getMapperInterface()},
            proxy
        );
    }
    
    private ChatModel resolveChatModel() {
        // 1. 优先使用指定的 Bean 名称
        if (StringUtils.hasText(scanConfiguration.getChatModelBeanName())) {
            return getApplicationContext().getBean(scanConfiguration.getChatModelBeanName(), ChatModel.class);
        }
        
        // 2. 使用指定的 Bean 类型
        if (scanConfiguration.getChatModelClass() != ChatModel.class) {
            return getApplicationContext().getBean(scanConfiguration.getChatModelClass());
        }
        
        // 3. 使用默认的 ChatModel
        return getDefaultChatModel();
    }
}
```

#### 3.1.4 EnhancedPromptMapperProxy

```java
public class EnhancedPromptMapperProxy extends PromptMapperProxy {
    
    private final PromptMapperScanConfiguration scanConfiguration;
    
    public EnhancedPromptMapperProxy(ApplicationContext applicationContext,
                                   PromptTemplateRegistry registry,
                                   Class<?> mapperInterface,
                                   ChatModel chatModel,
                                   PromptMapperScanConfiguration scanConfiguration) {
        super(applicationContext, registry, mapperInterface, chatModel);
        this.scanConfiguration = scanConfiguration;
    }
    
    @Override
    protected PromptConfig resolveEffectiveConfig(PromptConfig templateConfig) {
        // 合并配置：模版配置 > 扫描配置 > 默认配置
        PromptConfig effectiveConfig = templateConfig;
        
        if (scanConfiguration.getDefaultConfig() != null) {
            effectiveConfig = effectiveConfig.mergeWith(scanConfiguration.getDefaultConfig());
        }
        
        return effectiveConfig;
    }
}
```

### 3.2 配置属性增强

```java
@ConfigurationProperties(prefix = "terra.nova.prompt")
public class TerraPromptProperties {
    
    /**
     * 默认的模版扫描位置
     */
    private String[] mapperLocations = {"classpath*:/prompts/**/*.xml"};
    
    /**
     * 是否启用自动扫描（向后兼容）
     */
    private boolean autoScan = true;
    
    /**
     * 自动扫描的基础包
     */
    private String[] autoScanBasePackages = {};
    
    /**
     * 默认的 ChatModel Bean 名称
     */
    private String defaultChatModel = "";
    
    /**
     * 默认配置
     */
    private PromptConfig defaultConfig = new PromptConfig();
    
    // getters and setters
}
```

## 4. 使用方式

### 4.1 基本使用

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

### 4.2 多模型配置

```java
@Configuration
@PromptMapperScan(
    basePackages = "com.example.prompts.creative",
    chatModel = "openAiChatModel",
    temperature = 0.9,
    modelName = "gpt-4"
)
@PromptMapperScan(
    basePackages = "com.example.prompts.analytical", 
    chatModel = "deepSeekChatModel",
    temperature = 0.3,
    modelName = "deepseek-chat"
)
public class MultiModelPromptConfiguration {
}
```

### 4.3 使用 Class 指定模型

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

### 4.4 PromptMapper 接口定义

```java
@PromptMapper
public interface CreativePromptMapper {
    
    String generatePoem(@Param("theme") String theme);
    
    String createStory(@Param("character") String character, @Param("setting") String setting);
}
```

### 4.5 XML 模版定义

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

## 5. 向后兼容性

### 5.1 自动扫描保持

现有的自动扫描机制继续有效：

```java
@TerraBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 5.2 配置属性兼容

```properties
# 现有配置继续有效
terra.nova.prompt.mapper-locations=classpath*:/prompts/**/*.xml
terra.nova.prompt.auto-scan=true
terra.nova.prompt.default-chat-model=deepSeekChatModel
```

## 6. 配置优先级

配置优先级（从高到低）：

1. **XML 模版中的配置**：`<prompt temperature="0.8">`
2. **@PromptMapperScan 配置**：`@PromptMapperScan(temperature = 0.7)`
3. **默认配置**：`terra.nova.prompt.default-config.temperature=0.5`
4. **系统默认值**

## 7. 错误处理

### 7.1 模型解析错误

```java
@Component
public class PromptMapperScanErrorHandler {
    
    public void handleChatModelNotFound(String modelName) {
        throw new PromptMapperScanException(
            "ChatModel '" + modelName + "' not found. Please check your configuration."
        );
    }
    
    public void handleInvalidConfiguration(String reason) {
        throw new PromptMapperScanException(
            "Invalid PromptMapperScan configuration: " + reason
        );
    }
}
```

### 7.2 配置验证

```java
public class PromptMapperScanConfigurationValidator {
    
    public void validate(PromptMapperScanConfiguration config) {
        // 验证包路径
        if (config.getBasePackages() == null || config.getBasePackages().length == 0) {
            throw new IllegalArgumentException("Base packages must be specified");
        }
        
        // 验证模型配置
        if (StringUtils.hasText(config.getChatModelBeanName()) && 
            config.getChatModelClass() != ChatModel.class) {
            throw new IllegalArgumentException("Cannot specify both chatModel and chatModelClass");
        }
        
        // 验证参数范围
        if (config.getDefaultConfig().getTemperature() != null) {
            double temp = config.getDefaultConfig().getTemperature();
            if (temp < 0 || temp > 1) {
                throw new IllegalArgumentException("Temperature must be between 0 and 1");
            }
        }
    }
}
```

## 8. 测试策略

### 8.1 单元测试

```java
@ExtendWith(MockitoExtension.class)
class PromptMapperScanTest {
    
    @Test
    void testScanConfiguration() {
        // 测试扫描配置解析
    }
    
    @Test 
    void testModelResolution() {
        // 测试模型解析
    }
    
    @Test
    void testConfigurationMerging() {
        // 测试配置合并
    }
}
```

### 8.2 集成测试

```java
@SpringBootTest
@PromptMapperScan(
    basePackages = "com.test.prompts",
    chatModel = "testChatModel",
    temperature = 0.8
)
class PromptMapperScanIntegrationTest {
    
    @Autowired
    private TestPromptMapper testMapper;
    
    @Test
    void testPromptExecution() {
        String result = testMapper.generateText("test");
        assertThat(result).isNotNull();
    }
}
```

## 9. 性能考虑

### 9.1 扫描性能优化

- 缓存扫描结果
- 并行扫描多个包
- 延迟初始化代理对象

### 9.2 运行时性能

- 配置预计算和缓存
- 模型实例复用
- 模版解析结果缓存

## 10. 未来扩展

### 10.1 动态配置

支持运行时动态修改配置：

```java
@Autowired
private PromptMapperConfigurationManager configManager;

public void updateConfiguration() {
    configManager.updateScanConfiguration("creativeMappers", newConfig);
}
```

### 10.2 配置中心集成

支持从配置中心读取配置：

```java
@PromptMapperScan(
    basePackages = "com.example.prompts",
    configSource = "nacos://prompt-config"
)
```

### 10.3 指标监控

集成监控指标：

```java
@PromptMapperScan(
    basePackages = "com.example.prompts",
    enableMetrics = true,
    metricsPrefix = "prompt.creative"
)
```

## 11. 总结

通过引入 `@PromptMapperScan` 注解，Terra Nova 模块将获得：

1. **更灵活的配置方式**：类似 MyBatis 的使用体验
2. **更好的模型管理**：支持多模型配置和隔离
3. **更强的扩展性**：支持自定义配置和过滤器
4. **更好的性能**：优化的扫描和缓存机制
5. **完整的向后兼容性**：现有代码无需修改

这个设计既保持了框架的易用性，又提供了企业级应用所需的灵活性和可扩展性。 