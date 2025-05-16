# Terra Bedrock

Terra Bedrock 是 Terra Framework 的核心基础设施模块，提供了开发企业级应用所需的关键基础功能。它是框架的基石，为其他模块提供了坚实的基础。

## 核心功能

- **通用异常处理**：统一的异常处理机制，包括业务异常、系统异常和第三方服务异常
- **统一响应格式**：标准化的API响应格式，支持成功/失败状态、业务码、数据和消息
- **安全框架**：可扩展的认证与授权机制，支持多种认证方式
- **切面支持**：提供日志、性能监控、缓存等常用切面
- **配置管理**：统一的配置加载和管理机制，支持多环境配置
- **事件机制**：强大的事件总线，支持同步和异步事件处理
- **任务调度**：可靠的定时任务和分布式任务调度支持
- **上下文管理**：统一的上下文管理，简化状态传递
- **分布式锁**：提供多种分布式锁实现，保证分布式环境下的数据一致性

## 快速开始

### 引入依赖

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-bedrock</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 使用统一响应格式

```java
import com.terra.framework.bedrock.core.Result;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return Result.success(user);
    }
    
    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        try {
            User created = userService.create(user);
            return Result.success(created);
        } catch (BusinessException e) {
            return Result.failure(e.getCode(), e.getMessage());
        }
    }
}
```

### 使用异常处理

```java
import com.terra.framework.bedrock.exception.BusinessException;
import com.terra.framework.bedrock.exception.SystemException;

public class UserService {
    
    public User findById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }
        return user;
    }
    
    public void validatePermission(User user, String operation) {
        if (!permissionService.hasPermission(user, operation)) {
            throw new BusinessException("NO_PERMISSION", "无操作权限")
                .withParam("user", user.getUsername())
                .withParam("operation", operation);
        }
    }
}
```

### 使用事件机制

```java
import com.terra.framework.bedrock.event.EventBus;
import com.terra.framework.bedrock.event.EventListener;

// 发布事件
public class UserService {
    
    @Autowired
    private EventBus eventBus;
    
    public User register(User user) {
        User saved = userRepository.save(user);
        eventBus.publish(new UserRegisteredEvent(saved));
        return saved;
    }
}

// 监听事件
@Component
public class UserEventListener {
    
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();
        // 发送欢迎邮件等操作
    }
}
```

## 扩展功能

Terra Bedrock 提供了丰富的扩展点，允许开发者根据具体需求定制功能：

- 自定义异常处理器
- 自定义认证和授权规则
- 自定义事件处理机制
- 自定义任务调度策略
- 自定义上下文传播方式

## 配置选项

在 `application.properties` 或 `application.yml` 中可以配置 Bedrock 的行为：

```yaml
terra:
  bedrock:
    exception:
      include-stacktrace: never  # 控制响应中是否包含堆栈信息
    security:
      enabled: true
      default-role: USER
    event:
      async-executor-size: 5     # 异步事件执行器线程池大小
    task:
      default-pool-size: 10      # 默认任务线程池大小
```

## 贡献指南

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。 