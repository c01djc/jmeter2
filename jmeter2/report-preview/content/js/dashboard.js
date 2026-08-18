/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.setAttribute("data-i18n-key", "requests");
    cell.innerHTML = (window.JM2I18n ? JM2I18n.t("requests") : "Requests");
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.setAttribute("data-i18n-key", "executions");
    cell.innerHTML = (window.JM2I18n ? JM2I18n.t("executions") : "Executions");
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.setAttribute("data-i18n-key", "response_times_ms");
    cell.innerHTML = (window.JM2I18n ? JM2I18n.t("response_times_ms") : "Response Times (ms)");
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.setAttribute("data-i18n-key", "throughput_group");
    cell.innerHTML = (window.JM2I18n ? JM2I18n.t("throughput_group") : "Throughput");
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.setAttribute("data-i18n-key", "network");
    cell.innerHTML = (window.JM2I18n ? JM2I18n.t("network") : "Network (KB/sec)");
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        var rawTitle = info.titles[index];
        cell.setAttribute("data-i18n-src", rawTitle);
        cell.innerHTML = window.JM2I18n ? JM2I18n.translateTitle(rawTitle) : rawTitle;
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 98.82352941176471, "KoPercent": 1.1764705882352942};
    function buildPieDataset() {
        var failLabel = window.JM2I18n ? JM2I18n.t("fail") : "FAIL";
        var passLabel = window.JM2I18n ? JM2I18n.t("pass") : "PASS";
        return [
            {
                "label" : failLabel,
                "data" : data.KoPercent,
                "color" : "#e03131"
            },
            {
                "label" : passLabel,
                "data" : data.OkPercent,
                "color" : "#2f9e44"
            }];
    }
    function drawRequestsSummary() {
        $.plot($("#flot-requests-summary"), buildPieDataset(), {
            series : {
                pie : {
                    show : true,
                    radius : 1,
                    label : {
                        show : true,
                        radius : 3 / 4,
                        formatter : function(label, series) {
                            return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                                + label
                                + '<br/>'
                                + Math.round10(series.percent, -2)
                                + '%</div>';
                        },
                        background : {
                            opacity : 0.5,
                            color : '#000'
                        }
                    }
                }
            },
            legend : {
                show : true
            }
        });
    }
    drawRequestsSummary();
    window.jm2OnLangChange = function() {
        drawRequestsSummary();
        if (window.JM2I18n) {
            JM2I18n.apply();
        }
    };

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9882352941176471, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.0, 500, 1500, "JR-KO"], "isController": false}, {"data": [1.0, 500, 1500, "JR-OK"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 255, 3, 1.1764705882352942, 235.47450980392148, 100, 353, 232.0, 337.0, 339.2, 353.0, 4.2369361136495804, 0.07457474869153444, 0.03310106338788735], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["JR-KO", 3, 3, 100.0, 199.66666666666666, 100, 329, 170.0, 329.0, 329.0, 329.0, 0.07922465471254654, 0.0015473565373544248, 6.1894261494177E-4], "isController": false}, {"data": ["JR-OK", 252, 0, 0.0, 235.90079365079367, 101, 353, 232.0, 337.0, 339.35, 353.0, 4.187089806430174, 0.0736011880036554, 0.03271163911273573], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["400/Bad request", 3, 100.0, 1.1764705882352942], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 255, 3, "400/Bad request", 3, "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": ["JR-KO", 3, 3, "400/Bad request", 3, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

    if (window.JM2I18n) {
        JM2I18n.apply();
    }

});
