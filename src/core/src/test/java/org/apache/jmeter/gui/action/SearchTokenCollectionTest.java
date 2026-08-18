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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.junit.jupiter.api.Test;

class SearchTokenCollectionTest {

    @Test
    void collectsNestedArgumentValues() {
        Arguments arguments = new Arguments();
        arguments.addArgument(new Argument("", "{\"password\":\"123456\"}", "="));

        List<String> tokens = arguments.getSearchableTokens();

        assertTrue(tokens.stream().anyMatch(t -> t.contains("password")),
                () -> "Expected nested argument value in searchable tokens but got: " + tokens);
    }
}
