/*
 * JMeter2 report i18n — EN / 中文
 * Persists in localStorage: jmeter2.report.lang
 */
(function (window, document) {
    "use strict";

    var STORAGE_KEY = "jmeter2.report.lang";
    var DEFAULT_LANG = "zh";

    var DICT = {
        en: {
            dashboard: "Dashboard",
            charts: "Charts",
            custom_graphs: "Custom Graphs",
            overtime: "Over Time",
            throughput: "Throughput",
            response_times: "Response Times",
            test_report_info: "Test and Report information",
            source_file: "Source file",
            file: "File:",
            start_time: "Start Time",
            start_time_colon: "Start Time:",
            end_time: "End Time",
            end_time_colon: "End Time:",
            filter_computing: "Filter for computing",
            filter_display: "Filter for display",
            apdex: "APDEX (Application Performance Index)",
            requests_summary: "Requests Summary",
            statistics: "Statistics",
            errors: "Errors",
            top5_errors: "Top 5 Errors by sampler",
            pass: "PASS",
            fail: "FAIL",
            requests: "Requests",
            executions: "Executions",
            response_times_ms: "Response Times (ms)",
            throughput_group: "Throughput",
            network: "Network (KB/sec)",
            label: "Label",
            samples: "#Samples",
            fail_count: "FAIL",
            error_pct: "Error %",
            average: "Average",
            min: "Min",
            max: "Max",
            median: "Median",
            transactions_s: "Transactions/s",
            received: "Received",
            sent: "Sent",
            apdex_col: "Apdex",
            t_col: "T (Toleration threshold)",
            f_col: "F (Frustration threshold)",
            type_of_error: "Type of error",
            number_of_errors: "Number of errors",
            pct_in_errors: "% in errors",
            pct_in_all: "% in all samples",
            sample: "Sample",
            zoom: "Zoom",
            zoom_colon: "Zoom :",
            save_png: "Save as PNG",
            rt_over_time: "Response Times Over Time",
            rt_pct_over_time: "Response Time Percentiles Over Time (successful responses)",
            active_threads_over_time: "Active Threads Over Time",
            bytes_throughput_over_time: "Bytes Throughput Over Time",
            latencies_over_time: "Latencies Over Time",
            connect_time_over_time: "Connect Time Over Time",
            avg_response_time_ms: "Average response time in ms",
            avg_response_times_ms: "Average response times in ms",
            avg_latencies_ms: "Average response latencies in ms",
            avg_connect_ms: "Average Connect Time in ms",
            response_time_ms: "Response Time in ms",
            response_times_in_ms: "Response times in ms",
            number_active_threads: "Number of active threads",
            bytes_per_sec: "Bytes / sec",
            hits_per_sec: "Number of hits / sec",
            responses_per_sec: "Number of responses / sec",
            transactions_per_sec: "Number of transactions / sec",
            number_of_responses: "Number of responses",
            percentiles: "Percentiles",
            percentile_value_ms: "Percentile value in ms",
            response_times_ranges: "Response times ranges",
            global_req_per_sec: "Global number of requests per second",
            median_rt_ms: "Median Response Time in ms",
            median_latency_ms: "Median Latency in ms",
            elapsed_time: "Elapsed Time (granularity: {g})",
            connect_time_g: "Connect Time (granularity: {g})",
            hits_per_second: "Hits Per Second",
            codes_per_second: "Codes Per Second",
            transactions_per_second: "Transactions Per Second",
            total_tps: "Total TPS",
            rt_vs_request: "Response Time vs Request",
            latency_vs_request: "Latency vs Request",
            rt_percentiles: "Response Time Percentiles",
            synthetic_rt_dist: "Synthetic Response Time Distribution",
            time_vs_threads: "Time vs Threads",
            rt_distribution: "Response Time Distribution"
        },
        zh: {
            dashboard: "仪表盘",
            charts: "图表",
            custom_graphs: "自定义图",
            overtime: "随时间变化",
            throughput: "吞吐量",
            response_times: "响应时间",
            test_report_info: "测试与报告信息",
            source_file: "源文件",
            file: "文件：",
            start_time: "开始时间",
            start_time_colon: "开始时间：",
            end_time: "结束时间",
            end_time_colon: "结束时间：",
            filter_computing: "计算过滤",
            filter_display: "显示过滤",
            apdex: "APDEX（应用性能指数）",
            requests_summary: "请求汇总",
            statistics: "统计",
            errors: "错误",
            top5_errors: "按取样器 Top 5 错误",
            pass: "成功",
            fail: "失败",
            requests: "请求",
            executions: "执行",
            response_times_ms: "响应时间 (ms)",
            throughput_group: "吞吐量",
            network: "网络 (KB/秒)",
            label: "标签",
            samples: "样本数",
            fail_count: "失败",
            error_pct: "错误%",
            average: "平均",
            min: "最小",
            max: "最大",
            median: "中位数",
            transactions_s: "事务/秒",
            received: "接收",
            sent: "发送",
            apdex_col: "Apdex",
            t_col: "T（可容忍阈值）",
            f_col: "F（失望阈值）",
            type_of_error: "错误类型",
            number_of_errors: "错误数",
            pct_in_errors: "占错误%",
            pct_in_all: "占总样本%",
            sample: "样本",
            zoom: "缩放",
            zoom_colon: "缩放：",
            save_png: "保存为 PNG",
            rt_over_time: "响应时间随时间变化",
            rt_pct_over_time: "响应时间百分位随时间变化（成功响应）",
            active_threads_over_time: "活动线程数随时间变化",
            bytes_throughput_over_time: "字节吞吐量随时间变化",
            latencies_over_time: "延迟随时间变化",
            connect_time_over_time: "连接时间随时间变化",
            avg_response_time_ms: "平均响应时间 (ms)",
            avg_response_times_ms: "平均响应时间 (ms)",
            avg_latencies_ms: "平均延迟 (ms)",
            avg_connect_ms: "平均连接时间 (ms)",
            response_time_ms: "响应时间 (ms)",
            response_times_in_ms: "响应时间 (ms)",
            number_active_threads: "活动线程数",
            bytes_per_sec: "字节/秒",
            hits_per_sec: "命中数/秒",
            responses_per_sec: "响应数/秒",
            transactions_per_sec: "事务数/秒",
            number_of_responses: "响应数",
            percentiles: "百分位",
            percentile_value_ms: "百分位值 (ms)",
            response_times_ranges: "响应时间区间",
            global_req_per_sec: "全局每秒请求数",
            median_rt_ms: "中位响应时间 (ms)",
            median_latency_ms: "中位延迟 (ms)",
            elapsed_time: "经过时间（粒度：{g}）",
            connect_time_g: "连接时间（粒度：{g}）",
            hits_per_second: "每秒命中数",
            codes_per_second: "每秒响应码",
            transactions_per_second: "每秒事务数",
            total_tps: "总 TPS",
            rt_vs_request: "响应时间 vs 请求",
            latency_vs_request: "延迟 vs 请求",
            rt_percentiles: "响应时间百分位",
            synthetic_rt_dist: "综合响应时间分布",
            time_vs_threads: "时间 vs 线程",
            rt_distribution: "响应时间分布"
        }
    };

    var EXACT = {
        "Dashboard": "dashboard", "仪表盘": "dashboard",
        "Charts": "charts", "图表": "charts",
        "Customs Graphs": "custom_graphs", "Custom Graphs": "custom_graphs", "自定义图": "custom_graphs",
        "Over Time": "overtime", "随时间变化": "overtime",
        "Throughput": "throughput", "吞吐量": "throughput",
        "Response Times": "response_times", "响应时间": "response_times",
        "Test and Report information": "test_report_info", "测试与报告信息": "test_report_info",
        "Source file": "source_file", "源文件": "source_file",
        "File:": "file", "文件：": "file", "文件:": "file",
        "Start Time": "start_time", "开始时间": "start_time",
        "Start Time:": "start_time_colon", "开始时间：": "start_time_colon", "开始时间:": "start_time_colon",
        "End Time": "end_time", "结束时间": "end_time",
        "End Time:": "end_time_colon", "结束时间：": "end_time_colon", "结束时间:": "end_time_colon",
        "Filter for computing": "filter_computing", "计算过滤": "filter_computing",
        "Filter for display": "filter_display", "显示过滤": "filter_display",
        "APDEX (Application Performance Index)": "apdex", "APDEX（应用性能指数）": "apdex",
        "Requests Summary": "requests_summary", "请求汇总": "requests_summary",
        "Statistics": "statistics", "统计": "statistics",
        "Errors": "errors", "错误": "errors",
        "Top 5 Errors by sampler": "top5_errors", "按取样器 Top 5 错误": "top5_errors",
        "PASS": "pass", "成功": "pass",
        "FAIL": "fail", "失败": "fail",
        "Zoom": "zoom", "缩放": "zoom",
        "Zoom :": "zoom_colon", "Zoom:": "zoom_colon", "缩放：": "zoom_colon", "缩放:": "zoom_colon",
        "Save as PNG": "save_png", "保存为 PNG": "save_png",
        "Response Times Over Time": "rt_over_time", "响应时间随时间变化": "rt_over_time",
        "Response Time Percentiles Over Time (successful responses)": "rt_pct_over_time",
        "响应时间百分位随时间变化（成功响应）": "rt_pct_over_time",
        "Active Threads Over Time": "active_threads_over_time", "活动线程数随时间变化": "active_threads_over_time",
        "Bytes Throughput Over Time": "bytes_throughput_over_time", "字节吞吐量随时间变化": "bytes_throughput_over_time",
        "Latencies Over Time": "latencies_over_time", "延迟随时间变化": "latencies_over_time",
        "Connect Time Over Time": "connect_time_over_time", "连接时间随时间变化": "connect_time_over_time",
        "Hits Per Second": "hits_per_second", "每秒命中数": "hits_per_second",
        "Codes Per Second": "codes_per_second", "每秒响应码": "codes_per_second",
        "Transactions Per Second": "transactions_per_second", "每秒事务数": "transactions_per_second",
        "Total TPS": "total_tps", "总 TPS": "total_tps",
        "Response Time vs Request": "rt_vs_request", "响应时间 vs 请求": "rt_vs_request",
        "Latencies vs Request": "latency_vs_request", "Latency vs Request": "latency_vs_request", "延迟 vs 请求": "latency_vs_request",
        "Response Time Percentiles": "rt_percentiles", "响应时间百分位": "rt_percentiles",
        "Synthetic Response Times Distribution": "synthetic_rt_dist",
        "Synthetic Response Time Distribution": "synthetic_rt_dist",
        "综合响应时间分布": "synthetic_rt_dist",
        "Time vs Threads": "time_vs_threads", "时间 vs 线程": "time_vs_threads",
        "Response Time Distribution": "rt_distribution", "响应时间分布": "rt_distribution",
        "Label": "label", "标签": "label",
        "#Samples": "samples", "样本数": "samples",
        "Error %": "error_pct", "错误%": "error_pct",
        "Average": "average", "平均": "average",
        "Min": "min", "最小": "min",
        "Max": "max", "最大": "max",
        "Median": "median", "中位数": "median",
        "Transactions/s": "transactions_s", "事务/秒": "transactions_s",
        "Received": "received", "接收": "received",
        "Sent": "sent", "发送": "sent",
        "Apdex": "apdex_col",
        "T (Toleration threshold)": "t_col", "T（可容忍阈值）": "t_col",
        "F (Frustration threshold)": "f_col", "F（失望阈值）": "f_col",
        "Type of error": "type_of_error", "错误类型": "type_of_error",
        "Number of errors": "number_of_errors", "错误数": "number_of_errors",
        "% in errors": "pct_in_errors", "占错误%": "pct_in_errors",
        "% in all samples": "pct_in_all", "占总样本%": "pct_in_all",
        "Sample": "sample", "样本": "sample",
        "Requests": "requests", "请求": "requests",
        "Executions": "executions", "执行": "executions",
        "Response Times (ms)": "response_times_ms", "响应时间 (ms)": "response_times_ms",
        "Network (KB/sec)": "network", "网络 (KB/秒)": "network"
    };

    function currentLang() {
        try {
            var stored = localStorage.getItem(STORAGE_KEY);
            if (stored === "en" || stored === "zh") {
                return stored;
            }
        } catch (e) { /* ignore */ }
        return DEFAULT_LANG;
    }

    function t(key, lang, vars) {
        lang = lang || currentLang();
        var pack = DICT[lang] || DICT.en;
        var value = pack[key] != null ? pack[key] : (DICT.en[key] || key);
        if (vars) {
            Object.keys(vars).forEach(function (k) {
                value = value.replace("{" + k + "}", vars[k]);
            });
        }
        return value;
    }

    function translateTitle(text) {
        if (text == null) {
            return text;
        }
        var trimmed = String(text).replace(/\s+/g, " ").trim();
        var pct = trimmed.match(/^(\d+)(th|nd|rd|st)\s*pct$/i);
        if (pct) {
            return currentLang() === "zh" ? (pct[1] + "% 分位") : trimmed;
        }
        var elapsed = trimmed.match(/^Elapsed Time \(granularity:\s*(.+)\)$/i)
            || trimmed.match(/^经过时间（粒度：(.+)）$/);
        if (elapsed) {
            return t("elapsed_time", null, { g: elapsed[1] });
        }
        var connect = trimmed.match(/^Connect Time \(granularity:\s*(.+)\)$/i)
            || trimmed.match(/^连接时间（粒度：(.+)）$/);
        if (connect) {
            return t("connect_time_g", null, { g: connect[1] });
        }
        var key = EXACT[trimmed];
        return key ? t(key) : trimmed;
    }

    function translateAxis(english) {
        var map = {
            "Average response time in ms": "avg_response_time_ms",
            "Average response times in ms": "avg_response_times_ms",
            "Average response latencies in ms": "avg_latencies_ms",
            "Average Connect Time in ms": "avg_connect_ms",
            "Response Time in ms": "response_time_ms",
            "Response times in ms": "response_times_in_ms",
            "Number of active threads": "number_active_threads",
            "Bytes / sec": "bytes_per_sec",
            "Number of hits / sec": "hits_per_sec",
            "Number of responses / sec": "responses_per_sec",
            "Number of transactions / sec": "transactions_per_sec",
            "Number of responses": "number_of_responses",
            "Percentiles": "percentiles",
            "Percentile value in ms": "percentile_value_ms",
            "Response times ranges": "response_times_ranges",
            "Global number of requests per second": "global_req_per_sec",
            "Median Response Time in ms": "median_rt_ms",
            "Median Latency in ms": "median_latency_ms"
        };
        if (/^Elapsed Time \(granularity:/.test(english)) {
            var g1 = english.replace(/^Elapsed Time \(granularity:\s*/, "").replace(/\)$/, "");
            return t("elapsed_time", null, { g: g1 });
        }
        if (/^Connect Time \(granularity:/.test(english)) {
            var g2 = english.replace(/^Connect Time \(granularity:\s*/, "").replace(/\)$/, "");
            return t("connect_time_g", null, { g: g2 });
        }
        var key = map[english];
        return key ? t(key) : english;
    }

    function apply(lang) {
        lang = lang || currentLang();
        var nodes = document.querySelectorAll("[data-i18n]");
        for (var i = 0; i < nodes.length; i++) {
            var el = nodes[i];
            var key = el.getAttribute("data-i18n");
            if (key) {
                el.textContent = t(key, lang);
            }
        }
        var keyed = document.querySelectorAll("[data-i18n-key]");
        for (var j = 0; j < keyed.length; j++) {
            var cell = keyed[j];
            cell.textContent = t(cell.getAttribute("data-i18n-key"), lang);
        }

        var candidates = document.querySelectorAll(
            "a, p, span, td, th, h4, .dashboard-title, .span-title, .modal-title, .legendLabel"
        );
        for (var k = 0; k < candidates.length; k++) {
            var node = candidates[k];
            if (node.getAttribute("data-i18n") || node.getAttribute("data-i18n-key")) {
                continue;
            }
            // Only leaf-ish text nodes (no element children except maybe icons handled separately)
            if (node.children && node.children.length > 0) {
                // Allow single FA icon + text by translating only when data-i18n-src set, or childless text parts
                var onlyIcon = true;
                for (var c = 0; c < node.children.length; c++) {
                    if (!node.children[c].classList || !node.children[c].classList.contains("fa")) {
                        if (!node.children[c].classList || !node.children[c].classList.contains("arrow")) {
                            // has non-icon child (e.g. span) — skip, let child handle
                            if (node.children[c].tagName === "SPAN" || node.children[c].tagName === "I") {
                                continue;
                            }
                            onlyIcon = false;
                        }
                    }
                }
                if (node.querySelector("span[data-i18n], ul, table, div")) {
                    continue;
                }
            }
            var raw = node.getAttribute("data-i18n-src");
            if (!raw) {
                raw = (node.textContent || "").replace(/\s+/g, " ").trim();
                if (!raw) {
                    continue;
                }
                node.setAttribute("data-i18n-src", raw);
            }
            var translated = translateTitle(raw);
            if (translated !== node.textContent.replace(/\s+/g, " ").trim()) {
                // Preserve leading icon markup if present
                var icon = node.querySelector(":scope > i.fa, :scope > .fa");
                if (icon && node.children.length <= 2) {
                    var arrow = node.querySelector(":scope > .arrow, :scope > span.fa.arrow");
                    node.textContent = "";
                    node.appendChild(icon);
                    node.appendChild(document.createTextNode(" " + translated + " "));
                    if (arrow) {
                        node.appendChild(arrow);
                    }
                } else if (!node.querySelector("i.fa, .fa, span, ul")) {
                    node.textContent = translated;
                } else if (node.children.length === 0) {
                    node.textContent = translated;
                }
            }
        }
    }

    function updateButtons(lang) {
        var buttons = document.querySelectorAll(".jm2-lang button[data-lang]");
        for (var i = 0; i < buttons.length; i++) {
            var btn = buttons[i];
            if (btn.getAttribute("data-lang") === lang) {
                btn.classList.add("active");
            } else {
                btn.classList.remove("active");
            }
        }
    }

    function setLang(lang, opts) {
        if (lang !== "en" && lang !== "zh") {
            lang = DEFAULT_LANG;
        }
        var prev = currentLang();
        try {
            localStorage.setItem(STORAGE_KEY, lang);
        } catch (e) { /* ignore */ }
        document.documentElement.setAttribute("lang", lang === "zh" ? "zh-CN" : "en");
        apply(lang);
        updateButtons(lang);
        if (typeof window.jm2OnLangChange === "function") {
            window.jm2OnLangChange(lang);
        }
        // Chart pages: axis labels are baked into Flot — reload after user toggle
        if (opts && opts.reload && prev !== lang) {
            window.location.reload();
        }
    }

    function bind() {
        var root = document.querySelector(".jm2-lang");
        if (!root) {
            return;
        }
        root.addEventListener("click", function (ev) {
            var btn = ev.target;
            if (!btn || !btn.getAttribute) {
                return;
            }
            var lang = btn.getAttribute("data-lang");
            if (lang) {
                var needsReload = !!document.querySelector(".flot-chart-content, #flotResponseTimesOverTime, #flot-modal-content");
                setLang(lang, { reload: needsReload });
            }
        });
    }

    function boot() {
        bind();
        var lang = currentLang();
        document.documentElement.setAttribute("lang", lang === "zh" ? "zh-CN" : "en");
        apply(lang);
        updateButtons(lang);
    }

    window.JM2I18n = {
        t: t,
        setLang: setLang,
        currentLang: currentLang,
        translateTitle: translateTitle,
        translateAxis: translateAxis,
        apply: apply,
        boot: boot
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", boot);
    } else {
        boot();
    }
})(window, document);
