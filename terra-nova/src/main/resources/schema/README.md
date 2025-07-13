# Terra Prompt XML Schema 使用说明

## 概述

Terra Framework 提供了 DTD 和 XSD 文件来支持 Prompt XML 文件的编写，类似于 MyBatis 的做法。这些文件提供了：

- XML 结构验证
- IDE 智能提示和自动完成
- 属性值类型检查
- 文档说明

## 文件结构

```
terra-nova/src/main/resources/
├── dtd/
│   └── terra-prompt-1.0.dtd          # DTD 定义文件
├── xsd/
│   └── terra-prompt-1.0.xsd          # XSD 定义文件
├── META-INF/
│   └── catalog.xml                    # XML Catalog 映射文件
└── schema/
    └── README.md                      # 本说明文档
```

## 使用方式

### 1. 使用 DTD（推荐简单场景）

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE prompts SYSTEM "classpath:dtd/terra-prompt-1.0.dtd">
<prompts namespace="com.example.prompt.SimplePrompt"
         model="deepSeekChatModel"
         temperature="0.7">

    <prompt id="ask">
        ${question}
    </prompt>

</prompts>
```

### 2. 使用 XSD（推荐复杂场景）

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<prompts xmlns="http://terra.framework.com/schema/prompt"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://terra.framework.com/schema/prompt classpath:xsd/terra-prompt-1.0.xsd"
         namespace="com.example.prompt.SummaryPrompt"
         model="deepSeekChatModel"
         temperature="0.7"
         max-tokens="2000">

    <prompt id="summary">
        请为以下文章生成摘要：${content}
    </prompt>

</prompts>
```

## 支持的属性和类型

### prompts 元素

| 属性 | 类型 | 必需 | 说明 |
|------|------|------|------|
| namespace | string | 是 | 命名空间，对应Java接口全限定名 |
| model | string | 否 | 全局默认模型 |
| temperature | decimal(0.0-2.0) | 否 | 全局默认温度值 |
| max-tokens | positiveInteger | 否 | 全局默认最大令牌数 |
| top-p | decimal(0.0-1.0) | 否 | 全局默认top-p值 |

### prompt 元素

| 属性 | 类型 | 必需 | 说明 |
|------|------|------|------|
| id | string | 是 | prompt标识符，对应Java方法名 |
| model | string | 否 | 覆盖全局模型配置 |
| temperature | decimal(0.0-2.0) | 否 | 覆盖全局温度配置 |
| max-tokens | positiveInteger | 否 | 覆盖全局最大令牌数 |
| top-p | decimal(0.0-1.0) | 否 | 覆盖全局top-p配置 |

## IDE 支持

### IntelliJ IDEA

1. 确保 XML 文件位于 classpath 下
2. IDE 会自动识别 DTD/XSD 声明
3. 提供属性自动完成和值验证
4. 显示文档说明

### Eclipse

1. 项目右键 -> Properties -> XML Catalog
2. 添加 User Specified Entries
3. 映射 DTD/XSD 文件路径

### VS Code

1. 安装 XML 扩展
2. 配置 XML catalog 路径
3. 享受智能提示功能

## 验证规则

### 温度值 (temperature)
- 类型：decimal
- 范围：0.0 - 2.0
- 精度：最多2位小数

### Top-P值 (top-p)  
- 类型：decimal
- 范围：0.0 - 1.0
- 精度：最多2位小数

### 最大令牌数 (max-tokens)
- 类型：positiveInteger
- 范围：> 0

## 占位符语法

在 prompt 内容中使用 `${参数名}` 语法：

```xml
<prompt id="translate">
    请将以下内容翻译成${target_language}：
    
    ${text}
    
    要求准确、流畅。
</prompt>
```

对应的 Java 方法：

```java
String translate(@Param("text") String text, 
                @Param("target_language") String targetLanguage);
```

## 最佳实践

1. **命名空间**：使用完整的 Java 包名和接口名
2. **属性顺序**：建议按 id -> model -> temperature -> max-tokens -> top-p 顺序
3. **文档注释**：在 XML 中使用 `<!-- -->` 添加说明
4. **参数命名**：使用下划线分隔的小写命名（如 `target_language`）
5. **模型配置**：优先使用 Spring Bean 名称而非类名

## 故障排除

### 常见问题

1. **XML 验证失败**
   - 检查 DTD/XSD 文件路径
   - 确认属性值类型正确

2. **IDE 无智能提示**
   - 检查 XML Catalog 配置
   - 确认 schema 文件在 classpath 下

3. **运行时找不到模板**
   - 检查 namespace 是否匹配接口全限定名
   - 确认 XML 文件在正确的 classpath 位置

### 调试技巧

1. 开启 Terra 框架的 DEBUG 日志
2. 检查模板注册日志
3. 验证 XML 解析结果

## 版本历史

- v1.0: 初始版本，支持基本的 prompt 定义和配置 