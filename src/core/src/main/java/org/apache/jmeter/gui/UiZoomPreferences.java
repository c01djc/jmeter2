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

import java.util.prefs.Preferences;

import org.apache.jorphan.gui.JMeterUIDefaults;

/**
 * Persists UI font zoom between JMeter2 sessions (same mechanism as Look&amp;Feel).
 */
public final class UiZoomPreferences {

    private static final Preferences PREFS = Preferences.userNodeForPackage(UiZoomPreferences.class);

    private static final String SCALE_KEY = "ui.font.scale";

    public static final float MIN_SCALE = 0.5f;

    public static final float MAX_SCALE = 3.0f;

    private UiZoomPreferences() {
    }

    public static float clamp(float scale) {
        return Math.max(MIN_SCALE, Math.min(scale, MAX_SCALE));
    }

    public static float getSavedScale() {
        return clamp((float) PREFS.getDouble(SCALE_KEY, 1.0d));
    }

    public static void saveScale(float scale) {
        PREFS.putDouble(SCALE_KEY, clamp(scale));
    }

    /**
     * Apply the last saved zoom after LaF / HiDPI setup.
     */
    public static void restoreSavedScale() {
        float saved = getSavedScale();
        if (Math.abs(saved - 1.0f) > 0.01f) {
            JMeterUIDefaults.INSTANCE.setScale(saved);
        }
    }
}
