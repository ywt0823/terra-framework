# terra-geyser

The `terra-geyser` module provides a powerful and flexible caching framework for the Terra Framework.

## Features

-   **Multi-Level Caching**: Out-of-the-box support for a two-level cache system, using a local in-memory cache (Caffeine) as L1 and a distributed cache (Redis) as L2.
-   **Extensible Factory Model**: Uses a `CacheFactory` pattern, making it easy to add new cache implementations.
-   **Annotation-Driven Caching**: Provides an `@AutoCache` annotation that can be placed on any method to automatically enable caching for it.
-   **Automatic Enhancement**: When used in conjunction with the `terra-strata` module, `terra-geyser` automatically caches database operations, significantly improving performance with minimal configuration.

## Architecture

`terra-geyser` is the core caching module. It contains all the generic components for caching, including:
-   `CacheFactory` and implementations (e.g., `CaffeineCacheFactory`, `RedisCacheFactory`).
-   `AutoCacheManager` for managing different cache instances.
-   `AutoCacheAspect` for applying caching logic via AOP.
-   Auto-configuration for Redis (`TerraRedisAutoConfiguration`) and the core caching system (`GeyserCacheAutoConfiguration`).

This module is self-contained and can be used independently to provide caching for any part of an application. Its true power is unlocked when combined with other `terra` modules like `terra-strata`.

## 核心功能

- **多级缓存**：
  - 本地缓存（JVM内存）
  - 分布式缓存（Redis等）
  - 多级联动缓存策略
  - 自动缓存降级

- **缓存管理**：
  - 缓存命名空间管理
  - 缓存条目生命周期控制
  - 缓存容量监控与限制
  - 手动/自动缓存刷新机制

- **缓存同步**：
  - 基于消息的缓存同步
  - 多节点缓存一致性保证
  - 缓存变更事件通知
  - 分布式锁支持的原子操作

- **缓存策略**：
  - 多种缓存淘汰策略（LRU, LFU, FIFO等）
  - 自适应缓存加载
  - 热点数据保护
  - 缓存预热机制

- **集成支持**：
  - 与Spring Cache无缝集成
  - 支持注解驱动的缓存操作
  - 支持Redis, Caffeine, Ehcache等多种缓存提供商
  - ORM查询结果缓存集成

## 快速开始

### 引入依赖

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-geyser</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 基础配置

在 `application.properties` 或 `application.yml` 中配置缓存：

```yaml
terra:
  geyser:
    enabled: true
    default-time-to-live: 3600  # 默认缓存过期时间（秒）
    default-max-size: 1000      # 默认最大缓存条目数
    local:
      enabled: true
      type: caffeine
    distributed:
      enabled: true
      type: redis
      namespace: "app:cache:"

spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

### 使用注解方式缓存

```java
import com.terra.framework.geyser.annotation.Cacheable;
import com.terra.framework.geyser.annotation.CacheEvict;
import com.terra.framework.geyser.annotation.CachePut;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    // 使用缓存注解，将查询结果缓存
    @Cacheable(
        cache = "products",
        key = "#id",
        condition = "#id != null",
        timeToLive = 1800  // 30分钟
    )
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "产品不存在"));
    }
    
    // 更新数据时更新缓存
    @CachePut(
        cache = "products",
        key = "#product.id"
    )
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }
    
    // 删除数据时清除缓存
    @CacheEvict(
        cache = "products",
        key = "#id"
    )
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    // 批量清除缓存
    @CacheEvict(
        cache = "products",
        allEntries = true
    )
    public void refreshAllProducts() {
        // 执行数据刷新逻辑
        log.info("All product cache cleared");
    }
}
```

### 使用缓存管理器

```java
import com.terra.framework.geyser.CacheManager;
import com.terra.framework.geyser.Cache;

@Service
public class CategoryService {
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // 手动管理缓存
    public List<Category> getCategoriesByParentId(Long parentId) {
        // 获取缓存实例
        Cache<Long, List<Category>> cache = cacheManager.getCache("categories");
        
        // 尝试从缓存获取
        List<Category> categories = cache.get(parentId);
        if (categories != null) {
            return categories;
        }
        
        // 缓存未命中，从数据库加载
        categories = categoryRepository.findByParentId(parentId);
        
        // 更新缓存
        cache.put(parentId, categories, 7200);  // 设置2小时过期
        
        return categories;
    }
    
    // 使用缓存加载器
    public Category getCategoryWithProducts(Long categoryId) {
        return cacheManager.getCache("categoryWithProducts")
            .get(categoryId, id -> {
                Category category = categoryRepository.findById(id).orElse(null);
                if (category != null) {
                    // 加载关联的产品数据
                    category.setProducts(productRepository.findByCategoryId(id));
                }
                return category;
            });
    }
}
```

### 使用多级缓存

```java
import com.terra.framework.geyser.MultiLevelCache;
import com.terra.framework.geyser.CacheLevel;

@Service
public class UserService {
    
    @Autowired
    private MultiLevelCache multiLevelCache;
    
    @Autowired
    private UserRepository userRepository;
    
    // 使用多级缓存存储用户信息
    public User getUserByUsername(String username) {
        return multiLevelCache.get(
            "users",       // 缓存名称
            username,      // 缓存键
            key -> userRepository.findByUsername(key),  // 数据加载器
            CacheLevel.ALL // 同时使用本地和分布式缓存
        );
    }
    
    // 选择性使用本地缓存
    public List<User> getActiveUsers() {
        return multiLevelCache.get(
            "activeUsers",
            "list",
            key -> userRepository.findByStatus(UserStatus.ACTIVE),
            CacheLevel.LOCAL  // 仅使用本地缓存
        );
    }
    
    // 选择性使用分布式缓存
    public User getUserProfile(Long userId) {
        return multiLevelCache.get(
            "userProfiles",
            userId,
            key -> {
                // 加载详细的用户资料
                User user = userRepository.findById(key).orElse(null);
                if (user != null) {
                    user.setPermissions(permissionRepository.findByUserId(key));
                    user.setPreferences(preferenceRepository.findByUserId(key));
                }
                return user;
            },
            CacheLevel.DISTRIBUTED  // 仅使用分布式缓存
        );
    }
}
```

### 缓存同步

```java
import com.terra.framework.geyser.event.CacheChangeEvent;
import com.terra.framework.geyser.event.CacheChangeListener;

// 监听缓存变更事件
@Component
public class ProductCacheListener implements CacheChangeListener {
    
    @Autowired
    private CacheManager cacheManager;
    
    @Override
    public void onCacheChange(CacheChangeEvent event) {
        if ("products".equals(event.getCacheName())) {
            log.info("Product cache changed: {}, key: {}", 
                    event.getChangeType(), event.getKey());
            
            // 执行相关业务逻辑，如更新统计信息
            if (event.getChangeType() == ChangeType.UPDATE || 
                event.getChangeType() == ChangeType.PUT) {
                Product product = (Product) event.getValue();
                // 处理产品更新
            }
        }
    }
}

// 手动触发缓存同步
@Service
public class ProductSyncService {
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private CacheSynchronizer cacheSynchronizer;
    
    public void syncProductCache(Long productId) {
        // 从权威数据源更新数据
        Product product = externalProductService.getLatestProductInfo(productId);
        
        // 更新本地数据库
        productRepository.save(product);
        
        // 同步所有节点的缓存
        cacheSynchronizer.syncCache("products", productId, product);
    }
}
```

## 扩展功能

Terra Geyser 提供了丰富的扩展点：

- 自定义缓存提供程序
- 自定义缓存键生成策略
- 自定义缓存加载和刷新策略
- 自定义缓存事件处理器
- 扩展监控和统计功能

## 配置选项

在 `application.properties` 或 `application.yml` 中可以进行详细配置：

```yaml
terra:
  geyser:
    enabled: true
    stats-enabled: true  # 启用缓存统计
    
    # 本地缓存配置
    local:
      enabled: true
      type: caffeine  # caffeine, ehcache
      default-max-size: 10000
      default-time-to-live: 3600  # 秒
      caches:
        products:
          max-size: 5000
          time-to-live: 1800  # 30分钟
        categories:
          max-size: 200
          time-to-live: 7200  # 2小时
    
    # 分布式缓存配置
    distributed:
      enabled: true
      type: redis  # redis, hazelcast
      namespace: "myapp:"  # 缓存键前缀
      default-time-to-live: 86400  # 1天
      connection-timeout: 2000  # 毫秒
      caches:
        user-sessions:
          time-to-live: 1800  # 30分钟
        app-config:
          time-to-live: -1  # 永不过期
    
    # 多级缓存配置
    multi-level:
      enabled: true
      sync-strategy: write-through  # write-through, write-behind, write-around
      local-first: true  # 优先查询本地缓存
      load-missing: true  # 本地缓存未命中时，自动从分布式缓存加载
    
    # 缓存同步配置
    sync:
      enabled: true
      strategy: pub-sub  # pub-sub, polling
      topic: "cache-sync"
      sync-interval: 100  # 毫秒
```

## 贡献指南

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。 