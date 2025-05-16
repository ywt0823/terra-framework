# Terra Crust

Terra Crust 是 Terra Framework 的业务核心模块，专注于领域模型定义、业务逻辑实现和业务规则管理。它构建在 Terra Bedrock 之上，为企业级应用的核心业务功能提供支持。

## 核心功能

- **领域模型管理**：
  - 丰富的领域模型定义支持
  - 领域事件处理
  - 值对象与实体区分
  - 领域服务封装
 
- **业务规则引擎**：
  - 可配置的业务规则集
  - 规则评估与执行
  - 规则链和组合规则
  - 动态规则更新

- **状态机**：
  - 业务状态流转管理
  - 状态迁移事件触发
  - 状态约束检查
  - 基于状态的权限控制

- **业务流程**：
  - 流程定义与配置
  - 流程实例管理
  - 流程任务分配
  - 流程历史记录

- **权限模型**：
  - 基于领域的权限模型
  - 功能权限与数据权限分离
  - 动态权限计算
  - 多租户支持

## 快速开始

### 引入依赖

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-crust</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 定义领域模型

```java
import com.terra.framework.crust.domain.Entity;
import com.terra.framework.crust.domain.AggregateRoot;
import com.terra.framework.crust.domain.ValueObject;

// 值对象
@ValueObject
public class Address {
    private String province;
    private String city;
    private String street;
    private String zipCode;
    
    // 值对象是不可变的
    public Address(String province, String city, String street, String zipCode) {
        this.province = province;
        this.city = city;
        this.street = street;
        this.zipCode = zipCode;
    }
    
    // 省略getter方法和equals/hashCode
}

// 实体
@Entity
public class User {
    private UserId id;
    private String name;
    private String email;
    private Address address;
    
    // 实体通过ID标识唯一性
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

// 聚合根
@AggregateRoot
public class Order {
    private OrderId id;
    private UserId userId;
    private List<OrderItem> items;
    private OrderStatus status;
    
    public void addItem(Product product, int quantity) {
        // 业务逻辑和约束检查
        OrderItem item = new OrderItem(product.getId(), product.getPrice(), quantity);
        items.add(item);
    }
    
    public void submit() {
        if (items.isEmpty()) {
            throw new BusinessException("ORDER_EMPTY", "订单不能没有商品");
        }
        status = OrderStatus.SUBMITTED;
        // 发布领域事件
        DomainEvents.publish(new OrderSubmittedEvent(this));
    }
}
```

### 使用业务规则引擎

```java
import com.terra.framework.crust.rule.Rule;
import com.terra.framework.crust.rule.RuleEngine;

// 定义规则
@Component
public class OrderDiscountRule implements Rule<Order> {
    
    @Override
    public boolean evaluate(Order order) {
        // 判断订单是否满足打折条件
        return order.getTotalAmount().compareTo(new BigDecimal("1000")) >= 0;
    }
    
    @Override
    public void execute(Order order) {
        // 执行打折逻辑
        BigDecimal discount = order.getTotalAmount().multiply(new BigDecimal("0.1"));
        order.applyDiscount(discount);
    }
}

// 使用规则引擎
@Service
public class OrderService {
    
    @Autowired
    private RuleEngine ruleEngine;
    
    public void processOrder(Order order) {
        // 应用所有适用于订单的规则
        ruleEngine.applyRules(order);
        // 继续处理订单
    }
}
```

### 使用状态机

```java
import com.terra.framework.crust.statemachine.StateMachine;
import com.terra.framework.crust.statemachine.Transition;

@Service
public class OrderStateMachineConfig {
    
    @Bean
    public StateMachine<OrderStatus, OrderEvent> orderStateMachine() {
        StateMachine<OrderStatus, OrderEvent> machine = new StateMachine<>();
        
        // 配置状态迁移
        machine.addTransition(new Transition<>(
            OrderStatus.CREATED, 
            OrderEvent.SUBMIT, 
            OrderStatus.SUBMITTED,
            this::validateOrderForSubmission
        ));
        
        machine.addTransition(new Transition<>(
            OrderStatus.SUBMITTED, 
            OrderEvent.PAY, 
            OrderStatus.PAID
        ));
        
        // 更多状态迁移配置...
        
        return machine;
    }
    
    private boolean validateOrderForSubmission(Order order) {
        // 执行状态迁移的前置校验
        return !order.getItems().isEmpty();
    }
}

// 在服务中使用
@Service
public class OrderService {
    
    @Autowired
    private StateMachine<OrderStatus, OrderEvent> orderStateMachine;
    
    public void submitOrder(Order order) {
        // 触发状态迁移
        boolean success = orderStateMachine.trigger(order, OrderEvent.SUBMIT);
        if (!success) {
            throw new BusinessException("INVALID_STATE_TRANSITION", "订单状态迁移失败");
        }
    }
}
```

## 扩展功能

Terra Crust 提供了灵活的扩展机制：

- 自定义领域事件处理器
- 扩展业务规则类型
- 定制状态机行为
- 集成外部流程引擎
- 定义特定领域的权限计算逻辑

## 配置选项

在 `application.properties` 或 `application.yml` 中配置:

```yaml
terra:
  crust:
    domain:
      event:
        async: true  # 异步处理领域事件
    rule:
      enabled: true
      scan-packages: com.example.rules  # 自动扫描规则的包路径
    statemachine:
      strict-mode: true  # 严格模式下，无效的状态迁移会抛出异常
    tenant:
      enabled: false  # 是否启用多租户支持
```

## 贡献指南

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。 