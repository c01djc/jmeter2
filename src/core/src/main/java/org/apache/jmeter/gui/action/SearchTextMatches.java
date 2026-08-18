/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.action;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

/**
 * Find all non-overlapping matches of a search string or regex in text.
 */
public final class SearchTextMatches {

    public static final class Span {
        private final int start;
        private final int end;

        Span(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }

    private SearchTextMatches() {
    }

    public static List<Span> findAll(String haystack, String needle, boolean caseSensitive, boolean regexp) {
        if (StringUtils.isEmpty(haystack) || StringUtils.isEmpty(needle)) {
            return new ArrayList<>();
        }
        if (regexp) {
            return findAllRegex(haystack, needle, caseSensitive);
        }
        List<Span> result = new ArrayList<>();
        int from = 0;
        int needleLength = needle.length();
        while (from <= haystack.length() - needleLength) {
            int index = caseSensitive
                    ? haystack.indexOf(needle, from)
                    : StringUtils.indexOfIgnoreCase(haystack, needle, from);
            if (index < 0) {
                break;
            }
            result.add(new Span(index, index + needleLength));
            from = index + Math.max(needleLength, 1);
        }
        return result;
    }

    private static List<Span> findAllRegex(String haystack, String needle, boolean caseSensitive) {
        try {
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            Matcher matcher = Pattern.compile(needle, flags).matcher(haystack);
            List<Span> result = new ArrayList<>();
            while (matcher.find()) {
                if (matcher.start() == matcher.end()) {
                    continue;
                }
                result.add(new Span(matcher.start(), matcher.end()));
            }
            return result;
        } catch (PatternSyntaxException e) {
            return new ArrayList<>();
        }
    }
}
