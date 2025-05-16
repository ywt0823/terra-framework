# Terra Strata

Terra Strata 是 Terra Framework 的数据访问层模块，专注于提供统一、高效的数据访问能力。它封装了对关系型数据库、NoSQL数据库和其他数据源的访问，简化数据持久化和查询操作。

## 核心功能

- **ORM支持**：
  - 基于JPA/Hibernate的对象关系映射
  - MyBatis集成
  - 领域对象与数据库表的映射策略
 
- **多数据源管理**：
  - 动态数据源切换
  - 读写分离
  - 分片路由
  - 多租户数据隔离

- **事务管理**：
  - 声明式事务
  - 编程式事务
  - 分布式事务支持
  - 事务传播行为控制

- **数据访问模式**：
  - Repository模式实现
  - DAO模式支持
  - 查询对象(Query Object)模式
  - 规格模式(Specification Pattern)

- **查询增强**：
  - 动态条件查询
  - 分页与排序
  - 自定义SQL支持
  - 批量操作优化

- **审计与跟踪**：
  - 实体创建/修改时间自动记录
  - 操作用户追踪
  - 数据变更历史

## 快速开始

### 引入依赖

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-strata</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 配置数据源

在 `application.properties` 或 `application.yml` 中配置数据源：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

terra:
  strata:
    show-sql: true
    format-sql: true
    audit-enabled: true
```

### 定义实体类

```java
import com.terra.framework.strata.jpa.BaseEntity;
import com.terra.framework.strata.audit.Auditable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_product")
public class Product extends BaseEntity implements Auditable {
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // Getters and setters
}
```

### 创建Repository

```java
import com.terra.framework.strata.jpa.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends BaseRepository<Product, Long> {
    
    // 基本查询方法由BaseRepository提供
    
    // 自定义查询方法
    List<Product> findByStatus(ProductStatus status);
    
    Page<Product> findByPriceGreaterThan(BigDecimal price, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> search(String keyword);
    
    // 使用规格模式进行复杂查询
    List<Product> findAll(Specification<Product> spec);
}
```

### 使用动态查询

```java
import com.terra.framework.strata.query.Conditions;
import com.terra.framework.strata.query.QueryHelper;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private QueryHelper queryHelper;
    
    public Page<Product> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        // 使用条件构建器创建动态查询条件
        Conditions conditions = Conditions.create();
        
        if (criteria.getName() != null) {
            conditions.like("name", "%" + criteria.getName() + "%");
        }
        
        if (criteria.getMinPrice() != null) {
            conditions.greaterThanOrEqual("price", criteria.getMinPrice());
        }
        
        if (criteria.getMaxPrice() != null) {
            conditions.lessThanOrEqual("price", criteria.getMaxPrice());
        }
        
        if (criteria.getStatuses() != null && !criteria.getStatuses().isEmpty()) {
            conditions.in("status", criteria.getStatuses());
        }
        
        // 执行查询
        return queryHelper.findAll(productRepository, conditions, pageable);
    }
}
```

### 使用事务管理

```java
import com.terra.framework.strata.transaction.TransactionSupport;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private TransactionSupport transactionSupport;
    
    // 使用声明式事务
    @Transactional
    public Order createOrder(OrderCreateRequest request) {
        // 检查产品库存
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "产品不存在"));
            
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("INSUFFICIENT_STOCK", "库存不足");
        }
        
        // 扣减库存
        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);
        
        // 创建订单
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setAmount(product.getPrice().multiply(new BigDecimal(request.getQuantity())));
        order.setStatus(OrderStatus.CREATED);
        
        return orderRepository.save(order);
    }
    
    // 使用编程式事务
    public void processBatchOrders(List<OrderCreateRequest> requests) {
        transactionSupport.executeInTransaction(() -> {
            for (OrderCreateRequest request : requests) {
                try {
                    createOrder(request);
                } catch (BusinessException e) {
                    // 记录失败，但继续处理其他订单
                    log.error("Order creation failed: " + e.getMessage());
                }
            }
        });
    }
}
```

## 扩展功能

Terra Strata 提供多种扩展点，满足不同的数据访问需求：

- 自定义Repository实现
- 扩展审计功能
- 定制数据源路由策略
- 实现特定数据库的优化
- 集成其他ORM框架

## 配置选项

在 `application.properties` 或 `application.yml` 中可以进行详细配置：

```yaml
terra:
  strata:
    # 通用配置
    show-sql: true
    format-sql: true
    batch-size: 100
    
    # 审计配置
    audit:
      enabled: true
      creator-field: createdBy
      creation-time-field: createdTime
      updater-field: updatedBy
      update-time-field: updatedTime
    
    # 多数据源配置
    datasources:
      enabled: false
      default: master
      routing-strategy: standard  # standard, tenant, sharding
      datasources:
        master:
          url: jdbc:mysql://master:3306/mydb
          username: root
          password: password
        slave:
          url: jdbc:mysql://slave:3306/mydb
          username: readonly
          password: password
          read-only: true
          
    # 缓存配置
    cache:
      enabled: true
      type: caffeine  # redis, caffeine, ehcache
      ttl: 3600  # 默认缓存时间（秒）
```

## 贡献指南

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。 