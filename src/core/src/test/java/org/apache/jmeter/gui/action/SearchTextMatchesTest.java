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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class SearchTextMatchesTest {

    @Test
    void findsEveryPlainOccurrence() {
        List<SearchTextMatches.Span> matches =
                SearchTextMatches.findAll("{\"password\":\"123456\",\"pin\":\"123\"}", "123", true, false);
        assertEquals(2, matches.size());
        String body = "{\"password\":\"123456\",\"pin\":\"123\"}";
        assertEquals("123", body.substring(matches.get(0).getStart(), matches.get(0).getEnd()));
        assertEquals("123", body.substring(matches.get(1).getStart(), matches.get(1).getEnd()));
        assertEquals(body.indexOf("123"), matches.get(0).getStart());
    }

    @Test
    void caseInsensitivePlainSearch() {
        List<SearchTextMatches.Span> matches =
                SearchTextMatches.findAll("Admin admin ADMIN", "admin", false, false);
        assertEquals(3, matches.size());
    }

    @Test
    void regexFindsDigitRuns() {
        List<SearchTextMatches.Span> matches =
                SearchTextMatches.findAll("a12b345c", "\\d+", true, true);
        assertEquals(2, matches.size());
        assertEquals("12", "a12b345c".substring(matches.get(0).getStart(), matches.get(0).getEnd()));
        assertEquals("345", "a12b345c".substring(matches.get(1).getStart(), matches.get(1).getEnd()));
    }

    @Test
    void emptyNeedleYieldsNoMatches() {
        assertEquals(0, SearchTextMatches.findAll("abc", "", true, false).size());
    }
}
