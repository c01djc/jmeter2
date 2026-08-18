# 如何参与 JMeter2

这是 **JMeter2** 独立 fork，不是 Apache JMeter 官方项目。

## 发现 bug

请在 **本仓库** 开 Issue，并尽量带上：

- JMeter2 版本（例如 1.0.0）和 Java 版本
- 测试计划（`.jmx`）和 `jmeter.log`
- 复现步骤

请不要把本 fork 的问题报到 [apache/jmeter](https://github.com/apache/jmeter/issues) 或官方邮件列表。上游问题请走官方渠道：https://jmeter.apache.org/issues.html

## 开发环境

Gradle 命令见 [gradle.md](gradle.md)。

### IntelliJ IDEA

需要 IntelliJ 2018.3.1 或更新。

1. 用 IDEA 打开 `build.gradle.kts`，选择 `Open as Project`
2. 勾选 `Create separate module per source set`
3. 勾选 `Use default gradle wrapper`

### Eclipse

`File → Import → Gradle project`。需要 Java 8 兼容 JDK，以及 Kotlin 插件。

也可生成 Eclipse 工程：

```
./gradlew eclipse
```

## 提交补丁

欢迎 Pull Request。提交前请尽量：

- 提交说明写清楚改了什么、为什么
- 避免无意义的 merge commit
- 能跑的测试请带上（`./gradlew check` 较重，至少手动验证相关功能）

## 文档

上游用户手册仍在 [xdocs](xdocs)。本 fork 的说明以根目录 [README.md](README.md) 和 [CHANGELOG.md](CHANGELOG.md) 为准。
