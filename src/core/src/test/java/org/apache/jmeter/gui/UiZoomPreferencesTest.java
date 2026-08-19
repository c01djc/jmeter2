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

package org.apache.jmeter.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jorphan.gui.JMeterUIDefaults;
import org.junit.jupiter.api.Test;

class UiZoomPreferencesTest {

    @Test
    void clampsAndRestoresScale() {
        UiZoomPreferences.saveScale(99f);
        assertEquals(UiZoomPreferences.MAX_SCALE, UiZoomPreferences.getSavedScale());
        UiZoomPreferences.saveScale(0.1f);
        assertEquals(UiZoomPreferences.MIN_SCALE, UiZoomPreferences.getSavedScale());
        UiZoomPreferences.saveScale(1.21f);
        assertEquals(1.21f, UiZoomPreferences.getSavedScale(), 0.001f);
        JMeterUIDefaults.INSTANCE.setScale(1.0f);
        UiZoomPreferences.restoreSavedScale();
        assertEquals(1.21f, JMeterUIDefaults.INSTANCE.getScale(), 0.001f);
        UiZoomPreferences.saveScale(1.0f);
    }
}
