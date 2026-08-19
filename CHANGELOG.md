# JMeter2 Changelog

## Unreleased

## 1.0.1

Based on Apache JMeter 5.6.3.

### Packaging
- 发行包默认附带 jp@gc：Custom Thread Groups、Graphs Basic、Plugins Manager

### Usability
- HTTP 请求 **消息体数据** 可查找、定位、替换（Ctrl+F、F3 / Shift+F3）；不必依赖结构树搜索
- 结构树搜索的下一个 / 上一个会先在当前编辑器里逐处跳转
- 补全工具菜单等中文；打开脚本时把默认英文节点名翻成当前语言

## 1.0.0

Based on Apache JMeter 5.6.3.

### Usability
- Chinese/English toggle on HTML dashboard reports (persisted)
- Language switch retranslates default tree node names
- Default UI language `zh_CN`; launchers no longer force English
- Test Plan getting-started hint; template **JSON API（中文）**
- About / Help branding and zh_CN template strings for JMeter2
- Taskbar icon `icon-jmeter2.svg`
- CI helper: `bin/jmeter2-report.bat` / `jmeter2-report.sh`

### Reports
- Stronger JMeter2 dashboard theme (sidebar, accent, cards)
- Freemarker templates forced UTF-8 (fix Chinese garbled on Windows)
- Default report title: JMeter2 Dashboard

### Performance (from 2.0.x line)
- Async `.jtl` writes; lean CSV save defaults
- Optional HttpClient5 sampler (`HttpClient5`, default remains HttpClient4)
- Share disabled tree nodes across VUs
- Visualizer path avoids per-sample EDT flood by default
- Larger CSV buffer + parallel report consumers

### Packaging
- Dist name `jmeter2-<version>`; launchers `jmeter2.bat` / `jmeter2.sh` only

## 2.0.0

- Initial JMeter2 fork branding and independent distribution layout
