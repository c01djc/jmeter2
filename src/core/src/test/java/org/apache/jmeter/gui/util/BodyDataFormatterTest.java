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

package org.apache.jmeter.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BodyDataFormatterTest {

    @Test
    void skipsAutoFormatWhenAlreadyPrettyOrHuge() {
        assertFalse(BodyDataFormatter.shouldAutoFormat("{\n  \"a\": 1\n}"));
        StringBuilder huge = new StringBuilder(BodyDataFormatter.AUTO_FORMAT_MAX_CHARS + 10);
        huge.append('{');
        while (huge.length() < BodyDataFormatter.AUTO_FORMAT_MAX_CHARS + 5) {
            huge.append("\"k\":1,");
        }
        huge.append('}');
        assertFalse(BodyDataFormatter.shouldAutoFormat(huge.toString()));
        assertTrue(BodyDataFormatter.shouldAutoFormat("{\"a\":1,\"b\":2}"));
    }

    @Test
    void formatsCompactJson() {
        String pretty = BodyDataFormatter.formatIfPossible("{\"a\":1,\"b\":{\"c\":true}}");
        assertTrue(pretty.contains("\n"));
        assertTrue(pretty.contains("\"a\""));
        assertTrue(pretty.contains("\"c\""));
    }

    @Test
    void leavesInvalidJsonAlone() {
        String raw = "{not-json";
        assertEquals(raw, BodyDataFormatter.formatIfPossible(raw));
    }

    @Test
    void formatsSimpleXml() {
        String pretty = BodyDataFormatter.formatIfPossible("<root><child>1</child></root>");
        assertTrue(pretty.contains("\n"));
        assertTrue(pretty.contains("<child>"));
    }

    @Test
    void detectsJsonShape() {
        assertTrue(BodyDataFormatter.looksLikeJson("{\"x\":1}"));
        assertTrue(BodyDataFormatter.looksLikeJson("[1,2]"));
        assertFalse(BodyDataFormatter.looksLikeJson("plain"));
    }
}
