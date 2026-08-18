# JMeter2 — independent distribution folder
#
# This is not an Apache JMeter official release. See ../README.md.
#
# Unpack / install the built product here (or use the zip that expands to jmeter2-<version>/).
# Source tree stays in the parent repo; this folder is for the runnable tool only.
#
# Build a distribution from the repo root:
#   gradlew :src:dist:distZip -PchecksumIgnore -Prelease
# Archive output:
#   src/dist/build/distributions/jmeter2-1.0.0.zip
# Unpack layout:
#   jmeter2-1.0.0/
#     bin/jmeter2.bat   (Windows — only launcher)
#     bin/jmeter2.sh    (Unix — only launcher)
#     lib/
#     ...
#
# Based on Apache JMeter 5.6.3. Product name: JMeter2. See CHANGELOG.md.
#
# JMeter2 runtime highlights:
# - Async .jtl writing (jmeter.save.saveservice.async=true)
# - Optional HttpClient5 sampler (Implementation=HttpClient5 or #jmeter.httpsampler=HttpClient5)
# - Share disabled tree nodes across VUs (jmeter.tree_clone.share_disabled=true)
# - Visualizer path avoids per-sample EDT flood by default
# - Larger CSV buffer + parallel report consumers for big JTLs
# - Lean default save settings (no response body/headers)
# - Single entry scripts: jmeter2.bat / jmeter2.sh only
#
# Fresh test unpack example:
#   jmeter2/release/jmeter2-1.0.0/bin/jmeter2.bat
#
# GUI polish notes:
# - Help → About / zh_CN template strings use JMeter2
# - Taskbar icon: icon-jmeter2.svg
# - Test Plan panel shows a Templates getting-started hint
# - HTML report: JMeter2 theme + 中文/EN toggle
# - Language switch retranslates default tree node names
# - Default language=zh_CN in user.properties (comment out to follow OS)
# - Template: JSON API（中文）; CI helper: bin/jmeter2-report.bat|.sh
