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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.jorphan.gui.JMeterUIDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists UI font zoom between JMeter2 sessions (same mechanism as Look&amp;Feel).
 * Must be restored <em>before</em> {@link JMeterUIDefaults#install()} so fonts pick up the scale.
 */
public final class UiZoomPreferences {

    private static final Logger log = LoggerFactory.getLogger(UiZoomPreferences.class);

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

    /**
     * @return true when the user has previously saved a zoom level
     */
    public static boolean hasSavedScale() {
        return PREFS.get(SCALE_KEY, null) != null;
    }

    public static void saveScale(float scale) {
        PREFS.putDouble(SCALE_KEY, clamp(scale));
        try {
            PREFS.flush();
        } catch (BackingStoreException e) {
            log.warn("Could not persist UI zoom scale {}", scale, e);
        }
    }

    /**
     * Apply the last saved zoom. Call before Look and Feel / font install.
     */
    public static void restoreSavedScale() {
        float saved = getSavedScale();
        JMeterUIDefaults.INSTANCE.setScale(saved);
        if (Math.abs(saved - 1.0f) > 0.01f) {
            log.info("Restored UI zoom scale: {}", saved);
        }
    }
}
