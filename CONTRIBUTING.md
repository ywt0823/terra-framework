# 贡献指南

感谢您对 Terra Framework 的关注！我们欢迎所有形式的贡献，包括但不限于：

- 提交问题和建议
- 改进文档
- 修复 bug
- 添加新功能
- 优化性能

## 开发流程

1. Fork 项目仓库
2. 克隆你的 Fork 到本地
3. 创建新的特性分支
4. 进行开发和测试
5. 提交变更
6. 推送到你的 Fork
7. 创建 Pull Request

## 环境设置

1. 安装 JDK 21 或更高版本
2. 安装 Maven 3.8.x 或更高版本
3. 配置 Git hooks：
   ```bash
   git config core.hooksPath .githooks
   chmod +x .githooks/pre-commit
   ```

## 代码规范

- 遵循项目的 `.editorconfig` 配置
- 通过 Checkstyle 检查
- 通过 SpotBugs 检查
- 保持代码覆盖率在 80% 以上

## 提交规范

提交信息格式：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type

- feat: 新功能
- fix: Bug 修复
- docs: 文档更新
- style: 代码格式（不影响代码运行的变动）
- refactor: 重构（既不是新增功能，也不是修改 bug 的代码变动）
- test: 测试相关
- chore: 构建过程或辅助工具的变动

### Scope

影响的范围，比如：
- bedrock
- crust
- strata
- geyser
- stream
- sediment
- starter

### Subject

简短描述，不超过 50 个字符

### Body

详细描述，说明代码变更的动机，以及与以前行为的对比

### Footer

- 关闭 Issue：`Closes #123, #456`
- Breaking Changes: 破坏性变更说明

## Pull Request 规范

1. PR 标题要简洁明了
2. 描述中要包含以下信息：
   - 解决的问题
   - 实现方案
   - 影响范围
   - 测试情况
3. 所有测试必须通过
4. 代码质量检查必须通过
5. 至少需要一个维护者的审查通过

## 测试规范

1. 单元测试
   - 测试类名：`*Test.java`
   - 测试方法名：`test[被测试的方法名][测试场景]`
   - 使用断言进行验证

2. 集成测试
   - 测试类名：`*IT.java`
   - 测试真实的组件交互

## 文档规范

1. 及时更新 README.md
2. 为新功能编写使用文档
3. 添加必要的注释
4. 保持文档的实时性

## 发布流程

1. 更新版本号
2. 更新 CHANGELOG.md
3. 创建发布分支
4. 执行测试和构建
5. 创建标签
6. 发布到 Maven 仓库

## 问题反馈

如果你在开发过程中遇到问题，可以：

1. 查看现有的 Issues
2. 创建新的 Issue，并提供：
   - 问题描述
   - 复现步骤
   - 期望行为
   - 实际行为
   - 环境信息
   - 相关日志

## 联系方式

- 项目负责人：[Your Name]
- 邮箱：[Your Email]
- 微信群：[Group Info] 
