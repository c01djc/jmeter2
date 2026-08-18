# JMeter2 Changelog

## Unreleased

### Usability
- HTTP request **Body Data** has find / locate / replace (Ctrl+F in the editor, F3 / Shift+F3)
- Search Tree Next/Previous walks each match in the current editor (not only the next tree node)

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
