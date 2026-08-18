# JMeter2

<p align="center">
  <img src="xdocs/images/splash-jmeter2.png" alt="JMeter2 startup screen" width="720">
</p>

**JMeter2 is an independent fork. It is not an Apache Software Foundation release and is not affiliated with Apache JMeter.**

Based on [Apache JMeter](https://jmeter.apache.org/) 5.6.3 (Apache License 2.0).  
Current version: **1.0.0**

JMeter2 是团队内部使用中改出来的独立版本：官方功能不好用的地方做了调整，旧版本兼容性不好的地方做了更新。问题请提到 **本仓库**，不要去 Apache JMeter 的 JIRA / 邮件列表报这个 fork 的 bug。

官方项目：https://jmeter.apache.org/

## 相对上游改了什么

详见 [jmeter2/CHANGELOG.md](jmeter2/CHANGELOG.md)。摘要：

- 中文界面（默认 `zh_CN`）、HTML 报告中英切换
- 异步写 `.jtl`、大报告生成更稳
- 可选 HttpClient5 采样器
- 发行包名 `jmeter2-<version>`，入口只有 `jmeter2.bat` / `jmeter2.sh`

## 运行

需要 **Java 8+**（录制 HTTPS 建议用带 `keytool` 的 JDK）。

1. 下载 Release 里的 `jmeter2-1.0.0.zip` 并解压  
2. 进入 `jmeter2-1.0.0/bin`  
3. Windows 运行 `jmeter2.bat`，Linux / macOS 运行 `./jmeter2.sh`

无界面压测并出 HTML 报告：`jmeter2-report.bat` / `jmeter2-report.sh`。

路径里不要有空格。

## 从源码打包

构建需要 **JDK 17**（运行仍可用 Java 8）。

```bat
gradlew :src:dist:distZip -PchecksumIgnore -Prelease
```

产物：`src/dist/build/distributions/jmeter2-1.0.0.zip`

更多 Gradle 命令见 [gradle.md](gradle.md)。

## 反馈

- 本 fork 的问题 / 改进：本仓库 GitHub Issues  
- 上游 Apache JMeter：https://jmeter.apache.org/issues.html  

## 许可

沿用 [Apache License 2.0](LICENSE)。请保留 `LICENSE` 与 `NOTICE`。  
Java 包名仍为 `org.apache.jmeter`，以便兼容现有插件。
