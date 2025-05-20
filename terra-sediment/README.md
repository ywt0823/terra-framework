# Terra Sediment 模块

Terra Sediment 是 Terra Framework 的基础工具模块，提供了各种通用工具类和组件，用于简化应用开发。

## 主要功能

- **加密/解密工具**：支持对称加密（AES、PBE）和非对称加密（RSA）
- **HTTP客户端**：基于Apache HttpClient封装的HTTP请求工具
- **JSON处理**：基于Jackson的JSON序列化/反序列化工具
- **日期时间工具**：简化日期时间操作的工具类
- **并发工具**：线程池和批处理等并发处理工具
- **布隆过滤器**：用于高效判断元素是否存在的数据结构
- **结果封装**：统一的API结果返回格式
- **锁机制**：抽象锁接口，支持不同的锁实现

## 最近优化内容

### 1. 加密模块优化
- 移除了硬编码的密钥，改为从环境变量和配置文件读取
- 添加了专门的加密异常类，提供更详细的错误信息
- 添加了密钥管理机制，支持开发和生产环境不同配置
- 增强了加密方法的安全性，包括更高的密钥长度和更严格的参数验证
- 添加了密钥生成工具方法

### 2. HTTP客户端优化
- 完全重构了HttpClient工具类，提高了性能和可维护性
- 添加了连接池管理，优化资源使用
- 加入了配置管理机制，支持自定义配置
- 改进了异常处理机制，提供更详细的错误信息
- 添加了自动重试和SSL验证选项
- 支持流式处理HTTP响应
- 采用Builder模式，使API更加灵活和易用

### 3. JSON工具优化
- 改进了底层架构，添加了缓存机制提高性能
- 增加了更多的工具方法，包括JSON合并、验证等
- 支持更复杂的类型转换和映射操作
- 优化了异常处理和日志记录

## 使用示例

### 加密工具
```java
// RSA加密
String encrypted = RSAUtils.getInstance().encrypt("要加密的内容");
String decrypted = RSAUtils.getInstance().decrypt(encrypted);

// AES加密
String encrypted = AESUtils.getInstance().encrypt("要加密的内容");
String decrypted = AESUtils.getInstance().decrypt(encrypted);
```

### HTTP客户端
```java
// 创建HTTP客户端实例
HttpClientUtils client = HttpClientUtils.create();

// 发送GET请求
JSONObject response = client.sendGet("https://api.example.com/data", StandardCharsets.UTF_8, "your-token");

// 发送POST请求（JSON）
JSONObject response = client.sendPostJson("https://api.example.com/data", jsonData, StandardCharsets.UTF_8, "your-token");

// 自定义配置
HttpClientUtils customClient = HttpClientUtils.builder()
        .connectTimeout(10000)
        .readTimeout(15000)
        .maxTotalConnections(300)
        .retryEnabled(true)
        .maxRetryCount(3)
        .build();
```

### JSON工具
```java
// 对象转JSON
String json = JsonUtils.objectCovertToJson(yourObject);

// JSON转对象
YourClass obj = JsonUtils.jsonCovertToObject(json, YourClass.class);

// 格式化JSON（用于调试）
String prettyJson = JsonUtils.objectCovertToPrettyJson(yourObject);

// 对象转Map
Map<String, Object> map = JsonUtils.objectToMap(yourObject);
```

## 配置说明

### 加密配置
加密模块支持从环境变量或配置文件获取密钥：

1. **环境变量**（优先级高）:
   - `TERRA_ENCRYPTION_RSA_PUBLIC_KEY` - RSA公钥
   - `TERRA_ENCRYPTION_RSA_PRIVATE_KEY` - RSA私钥
   - `TERRA_ENCRYPTION_AES_KEY` - AES密钥
   - `TERRA_ENCRYPTION_PBE_KEY` - PBE密钥
   - `TERRA_ENCRYPTION_PBE_SALT` - PBE盐值

2. **配置文件** (`encryption.properties`):
   ```properties
   encryption.rsa.public-key=YOUR_RSA_PUBLIC_KEY
   encryption.rsa.private-key=YOUR_RSA_PRIVATE_KEY
   encryption.aes.key=YOUR_AES_KEY_16BYTES
   encryption.pbe.key=YOUR_PBE_KEY
   encryption.pbe.salt=YOUR_PBE_SALT
   ```

3. **开发环境**:
   如果未配置密钥，将使用默认的开发密钥，但不推荐在生产环境使用。

## 注意事项

1. 该模块中的工具类大多采用单例模式或静态方法，无需重复创建实例
2. 加密工具类的密钥应在生产环境中配置，不要使用默认密钥
3. HTTP客户端在不再使用时应调用close()方法释放资源

## 核心功能

- **基础工具类**：
  - 字符串处理
  - 日期时间操作
  - 数值计算
  - 集合工具
  - 异常处理

- **安全工具**：
  - 加密解密
  - 哈希计算
  - 安全随机数
  - 密码处理
  - 签名验证

- **IO工具**：
  - 文件操作
  - 流处理
  - 资源加载
  - 序列化/反序列化
  - 编码转换

- **反射与类型**：
  - 反射工具
  - 类型转换
  - Bean属性复制
  - 动态代理
  - 注解处理

- **验证工具**：
  - 参数验证
  - 业务规则验证
  - 断言工具
  - 正则表达式
  - 格式检查

- **HTTP工具**：
  - REST客户端
  - URL处理
  - Cookie操作
  - 请求/响应处理
  - 内容类型工具

## 快速开始

### 引入依赖

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-sediment</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 使用字符串工具

```java
import com.terra.framework.sediment.text.StringUtils;

public class StringExample {
    
    public void demoStringUtils() {
        // 判空处理
        String nullStr = null;
        String emptyStr = "";
        String blankStr = "   ";
        String normalStr = "Hello";
        
        System.out.println(StringUtils.isEmpty(nullStr));     // true
        System.out.println(StringUtils.isEmpty(emptyStr));    // true
        System.out.println(StringUtils.isEmpty(blankStr));    // false
        System.out.println(StringUtils.isEmpty(normalStr));   // false
        
        System.out.println(StringUtils.isBlank(nullStr));     // true
        System.out.println(StringUtils.isBlank(emptyStr));    // true
        System.out.println(StringUtils.isBlank(blankStr));    // true
        System.out.println(StringUtils.isBlank(normalStr));   // false
        
        // 默认值处理
        System.out.println(StringUtils.defaultIfEmpty(nullStr, "Default"));  // Default
        System.out.println(StringUtils.defaultIfEmpty(emptyStr, "Default")); // Default
        System.out.println(StringUtils.defaultIfEmpty(normalStr, "Default")); // Hello
        
        // 字符串操作
        System.out.println(StringUtils.capitalize("hello"));  // Hello
        System.out.println(StringUtils.uncapitalize("Hello")); // hello
        System.out.println(StringUtils.abbreviate("Hello, world!", 10)); // Hello,...
        
        // 字符串分割与连接
        String[] parts = StringUtils.split("a,b,c", ",");
        System.out.println(StringUtils.join(parts, "-")); // a-b-c
        
        // 字符串替换
        System.out.println(StringUtils.replaceAll("Hello, {name}!", "{name}", "World")); // Hello, World!
    }
}
```

### 使用日期时间工具

```java
import com.terra.framework.sediment.time.DateUtils;
import com.terra.framework.sediment.time.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class DateTimeExample {
    
    public void demoDateUtils() {
        // 日期解析与格式化
        Date date = DateUtils.parseDate("2023-09-15", "yyyy-MM-dd");
        String formatted = DateUtils.formatDate(date, "yyyy年MM月dd日");
        System.out.println(formatted); // 2023年09月15日
        
        // 日期计算
        Date tomorrow = DateUtils.addDays(date, 1);
        Date nextMonth = DateUtils.addMonths(date, 1);
        Date nextYear = DateUtils.addYears(date, 1);
        
        // 日期比较
        boolean isBefore = DateUtils.isBefore(date, tomorrow);
        boolean isSameDay = DateUtils.isSameDay(date, date);
        
        // 获取特定日期
        Date startOfDay = DateUtils.getStartOfDay(date);
        Date endOfDay = DateUtils.getEndOfDay(date);
        Date startOfMonth = DateUtils.getStartOfMonth(date);
        Date endOfMonth = DateUtils.getEndOfMonth(date);
    }
    
    public void demoDateTimeUtils() {
        // Java 8 日期时间API支持
        LocalDateTime now = DateTimeUtils.now();
        LocalDate today = DateTimeUtils.today();
        
        // 格式化
        String formatted = DateTimeUtils.format(now, "yyyy-MM-dd HH:mm:ss");
        
        // 解析
        LocalDateTime dateTime = DateTimeUtils.parse("2023-09-15 14:30:00", "yyyy-MM-dd HH:mm:ss");
        
        // 转换
        Date legacyDate = DateTimeUtils.toDate(dateTime);
        LocalDateTime newDateTime = DateTimeUtils.toLocalDateTime(legacyDate);
        
        // 计算
        LocalDateTime plusHours = DateTimeUtils.plusHours(now, 2);
        LocalDateTime minusDays = DateTimeUtils.minusDays(now, 3);
        
        // 判断工作日
        boolean isWeekend = DateTimeUtils.isWeekend(today);
        boolean isWorkDay = DateTimeUtils.isWorkDay(today);
    }
}
```

### 使用集合工具

```java
import com.terra.framework.sediment.collection.CollectionUtils;
import com.terra.framework.sediment.collection.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionExample {
    
    public void demoCollectionUtils() {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Orange");
        
        // 判空
        System.out.println(CollectionUtils.isEmpty(list)); // false
        System.out.println(CollectionUtils.isNotEmpty(list)); // true
        
        // 转换
        List<Integer> lengths = CollectionUtils.transform(list, String::length);
        System.out.println(lengths); // [5, 6, 6]
        
        // 过滤
        List<String> filtered = CollectionUtils.filter(list, s -> s.length() > 5);
        System.out.println(filtered); // [Banana, Orange]
        
        // 查找
        String found = CollectionUtils.find(list, s -> s.startsWith("A"));
        System.out.println(found); // Apple
        
        // 分组
        Map<Integer, List<String>> grouped = CollectionUtils.groupBy(list, String::length);
        System.out.println(grouped); // {5=[Apple], 6=[Banana, Orange]}
    }
    
    public void demoMapUtils() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "John");
        map.put("age", 30);
        
        // 安全获取
        String name = MapUtils.getString(map, "name", "Unknown");
        int age = MapUtils.getInteger(map, "age", 0);
        boolean active = MapUtils.getBoolean(map, "active", false);
        
        // 判空
        System.out.println(MapUtils.isEmpty(map)); // false
        
        // 合并
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("active", true);
        defaults.put("role", "user");
        
        Map<String, Object> merged = MapUtils.merge(defaults, map);
        System.out.println(merged); // {name=John, age=30, active=true, role=user}
        
        // 过滤键
        Map<String, Object> filtered = MapUtils.filterKeys(map, key -> key.startsWith("a"));
        System.out.println(filtered); // {age=30}
    }
}
```

### 使用加密工具

```java
import com.terra.framework.sediment.security.AesUtils;
import com.terra.framework.sediment.security.RsaUtils;
import com.terra.framework.sediment.security.DigestUtils;

public class SecurityExample {
    
    public void demoEncryption() throws Exception {
        String plainText = "Hello, secure world!";
        
        // AES加密解密
        String key = AesUtils.generateKey();
        String encrypted = AesUtils.encrypt(plainText, key);
        String decrypted = AesUtils.decrypt(encrypted, key);
        
        System.out.println("AES原文: " + plainText);
        System.out.println("AES密文: " + encrypted);
        System.out.println("AES解密: " + decrypted);
        
        // RSA加密解密
        RsaUtils.KeyPair keyPair = RsaUtils.generateKeyPair();
        String publicKey = keyPair.getPublicKey();
        String privateKey = keyPair.getPrivateKey();
        
        String rsaEncrypted = RsaUtils.encrypt(plainText, publicKey);
        String rsaDecrypted = RsaUtils.decrypt(rsaEncrypted, privateKey);
        
        System.out.println("RSA原文: " + plainText);
        System.out.println("RSA密文: " + rsaEncrypted);
        System.out.println("RSA解密: " + rsaDecrypted);
        
        // 消息摘要
        String md5 = DigestUtils.md5(plainText);
        String sha256 = DigestUtils.sha256(plainText);
        
        System.out.println("MD5: " + md5);
        System.out.println("SHA-256: " + sha256);
        
        // HMAC
        String hmacKey = "secret";
        String hmacSha256 = DigestUtils.hmacSha256(plainText, hmacKey);
        
        System.out.println("HMAC-SHA256: " + hmacSha256);
    }
}
```

### 使用HTTP工具

```java
import com.terra.framework.sediment.http.HttpClient;
import com.terra.framework.sediment.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class HttpExample {
    
    public void demoHttpClient() {
        HttpClient client = HttpClient.builder()
            .connectTimeout(5000)
            .readTimeout(10000)
            .build();
        
        // GET请求
        HttpResponse response = client.get("https://api.example.com/users")
            .header("Authorization", "Bearer token123")
            .param("page", "1")
            .param("size", "10")
            .execute();
        
        System.out.println("状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody());
        
        // POST JSON请求
        Map<String, Object> user = new HashMap<>();
        user.put("name", "John Doe");
        user.put("email", "john@example.com");
        
        HttpResponse postResponse = client.post("https://api.example.com/users")
            .header("Content-Type", "application/json")
            .body(user)
            .execute();
        
        System.out.println("POST状态码: " + postResponse.getStatusCode());
        System.out.println("POST响应体: " + postResponse.getBody());
        
        // 下载文件
        client.get("https://example.com/files/report.pdf")
            .download("/path/to/save/report.pdf");
    }
}
```

## Spring Boot 自动装配

Terra Sediment 结合 Terra Bedrock 提供了完整的 Spring Boot 自动装配功能，只需引入 terra-bedrock 依赖即可自动配置并使用各种工具类。

### 自动装配配置

在 `application.properties` 或 `application.yml` 中可以进行配置：

```yaml
terra:
  # 加密工具配置
  encryption:
    enabled: true
    rsa:
      public-key: YOUR_RSA_PUBLIC_KEY
      private-key: YOUR_RSA_PRIVATE_KEY
    aes:
      key: YOUR_AES_KEY_16BYTES
    pbe:
      key: YOUR_PBE_KEY
      salt: YOUR_PBE_SALT

  # HTTP客户端配置
  httpclient:
    enabled: true
    connect-timeout: 5000
    socket-timeout: 10000
    request-max-num: 200
    max-per-route: 50
    retry-enabled: true
    max-retry-count: 3
    validate-ssl-certificate: true
    thread-pool-size: 10

  # JSON工具配置
  json:
    enabled: true
    date-format: "yyyy-MM-dd HH:mm:ss"
    ignore-unknown-properties: true
    include-null-values: false
    enable-cache: true
    cache-size: 16
    
  # 雪花算法配置
  snowflake:
    enabled: true
    max-sequence: 10000
```

### 使用自动装配的组件

通过自动装配功能，您可以直接在 Spring 组件中注入这些工具：

```java
@Service
public class SomeService {

    private final RSAUtils rsaUtils;
    private final AESUtils aesUtils;
    private final PBEUtils pbeUtils;
    private final HttpClientUtils httpClientUtils;
    private final JsonUtils jsonUtils;
    private final SnowflakeUtils snowflakeUtils;

    @Autowired
    public SomeService(RSAUtils rsaUtils, AESUtils aesUtils, PBEUtils pbeUtils,
                      HttpClientUtils httpClientUtils, JsonUtils jsonUtils,
                      SnowflakeUtils snowflakeUtils) {
        this.rsaUtils = rsaUtils;
        this.aesUtils = aesUtils;
        this.pbeUtils = pbeUtils;
        this.httpClientUtils = httpClientUtils;
        this.jsonUtils = jsonUtils;
        this.snowflakeUtils = snowflakeUtils;
    }

    public void doSomething() {
        // 使用加密工具
        String encrypted = rsaUtils.encrypt("sensitive data");
        
        // 使用HTTP客户端
        JSONObject response = httpClientUtils.sendGet("https://api.example.com", StandardCharsets.UTF_8, null);
        
        // 使用JSON工具
        MyObject obj = jsonUtils.jsonCovertToObject(response.toString(), MyObject.class);
        
        // 生成唯一ID
        Long id = snowflakeUtils.getSnowflakeId();
    }
}
```

使用自动装配的好处：
1. 更好地管理配置，通过application.yml集中配置
2. 让代码更加简洁，无需手动创建工具类实例
3. 更容易进行单元测试，可以方便地mock组件
4. 统一的生命周期管理，由Spring容器负责管理实例

## 更多工具

Terra Sediment 提供了众多工具类，包括但不限于：

- **ExceptionUtils**: 异常处理和转换
- **ValidationUtils**: 参数验证工具
- **AssertUtils**: 断言工具
- **ReflectionUtils**: 反射操作工具
- **BeanUtils**: Bean属性复制与转换
- **JsonUtils**: JSON序列化与反序列化
- **FileUtils**: 文件操作工具
- **StreamUtils**: 流处理工具
- **IdGenerator**: 唯一ID生成器
- **MoneyUtils**: 货币计算工具
- **IpUtils**: IP地址工具
- **UaUtils**: 用户代理分析工具
- **MaskUtils**: 数据脱敏工具
- **TreeUtils**: 树形结构处理工具
- **ExcelUtils**: Excel操作工具

## 贡献指南

Terra Sediment 模块欢迎各类工具的贡献，如果你有好用的工具类希望贡献到框架中，请遵循以下原则：

1. 工具类应该是无状态的，方法尽量设计为静态方法
2. 提供完善的单元测试，测试覆盖率不低于90%
3. 添加清晰的JavaDoc文档
4. 处理好异常情况和边界条件
5. 参考已有工具类的命名和设计风格

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。 