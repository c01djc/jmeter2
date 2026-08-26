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

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists main window size / position between JMeter2 sessions.
 */
public final class UiWindowPreferences {

    private static final Logger log = LoggerFactory.getLogger(UiWindowPreferences.class);

    private static final Preferences PREFS = Preferences.userNodeForPackage(UiWindowPreferences.class);

    private static final String KEY_X = "window.x";
    private static final String KEY_Y = "window.y";
    private static final String KEY_W = "window.width";
    private static final String KEY_H = "window.height";
    private static final String KEY_STATE = "window.state";

    private UiWindowPreferences() {
    }

    /**
     * Remember normal-state bounds without flushing (called while resizing/moving).
     */
    public static void rememberNormalBounds(Rectangle bounds) {
        if (bounds == null || bounds.width < 200 || bounds.height < 150) {
            return;
        }
        PREFS.putInt(KEY_X, bounds.x);
        PREFS.putInt(KEY_Y, bounds.y);
        PREFS.putInt(KEY_W, bounds.width);
        PREFS.putInt(KEY_H, bounds.height);
    }

    /**
     * Save current geometry. When maximized / iconified, keep the last normal size.
     */
    public static void save(JFrame frame) {
        if (frame == null) {
            return;
        }
        int extended = frame.getExtendedState();
        boolean normal = (extended & Frame.MAXIMIZED_BOTH) == 0
                && (extended & Frame.ICONIFIED) == 0;
        if (normal) {
            rememberNormalBounds(frame.getBounds());
        }
        int stateToStore = extended & ~Frame.ICONIFIED;
        PREFS.putInt(KEY_STATE, stateToStore);
        flushQuietly();
    }

    /**
     * @return true when a previous geometry was applied
     */
    public static boolean restore(JFrame frame) {
        if (frame == null || PREFS.get(KEY_W, null) == null) {
            return false;
        }
        int w = PREFS.getInt(KEY_W, 0);
        int h = PREFS.getInt(KEY_H, 0);
        int x = PREFS.getInt(KEY_X, 0);
        int y = PREFS.getInt(KEY_Y, 0);
        int state = PREFS.getInt(KEY_STATE, Frame.NORMAL);
        if (w < 200 || h < 150) {
            return false;
        }
        Rectangle proposed = new Rectangle(x, y, w, h);
        if (!isVisibleOnAnyScreen(proposed)) {
            return false;
        }
        frame.setBounds(proposed);
        if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
        return true;
    }

    private static boolean isVisibleOnAnyScreen(Rectangle bounds) {
        for (java.awt.GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getScreenDevices()) {
            for (GraphicsConfiguration gc : device.getConfigurations()) {
                if (gc.getBounds().intersects(bounds)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void flushQuietly() {
        try {
            PREFS.flush();
        } catch (BackingStoreException e) {
            log.warn("Could not persist window preferences", e);
        }
    }
}
