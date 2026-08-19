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

import java.util.concurrent.atomic.AtomicLong;

import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.GuiPackage;

/**
 * Loads HTTP body text without blocking the UI: show raw content immediately,
 * optionally pretty-print compact JSON/XML on a background thread.
 */
final class HttpBodyEditorLoader {

    static final String TOOLBAR_CLIENT_KEY = "jmeter2.searchToolbar";

    private static final AtomicLong LOAD_GENERATION = new AtomicLong();

    private HttpBodyEditorLoader() {
    }

    static long nextLoadGeneration() {
        return LOAD_GENERATION.incrementAndGet();
    }

    static void loadContent(JSyntaxSearchToolBar toolbar, String raw) {
        JSyntaxTextArea editor = toolbar.getEditor();
        long generation = nextLoadGeneration();
        toolbar.cancelPendingFormat();
        editor.setInitialText(raw);
        toolbar.refreshBodyHints(raw);
        if (!BodyDataFormatter.shouldAutoFormat(raw)) {
            editor.refreshBodyEditorPresentation();
            return;
        }
        toolbar.showStatusMessage("search_body_formatting", true);
        scheduleFormat(toolbar, raw, generation, false);
    }

    static void formatManually(JSyntaxSearchToolBar toolbar) {
        String original = toolbar.getEditor().getText();
        if (StringUtils.isBlank(original)) {
            return;
        }
        long generation = nextLoadGeneration();
        toolbar.showStatusMessage("search_body_formatting", true);
        scheduleFormat(toolbar, original, generation, true);
    }

    private static void scheduleFormat(JSyntaxSearchToolBar toolbar, String source,
            long generation, boolean manual) {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return BodyDataFormatter.formatIfPossible(source);
            }

            @Override
            protected void done() {
                if (generation != LOAD_GENERATION.get()) {
                    return;
                }
                toolbar.clearPendingFormatWorker(this);
                try {
                    String formatted = get();
                    JSyntaxTextArea editor = toolbar.getEditor();
                    if (formatted.equals(source)) {
                        if (manual) {
                            toolbar.showStatusMessage("search_body_format_failed", true);
                        } else {
                            toolbar.refreshBodyHints(source);
                        }
                        editor.refreshBodyEditorPresentation();
                        return;
                    }
                    int caret = editor.getCaretPosition();
                    editor.setInitialText(formatted);
                    editor.setCaretPosition(Math.min(caret, formatted.length()));
                    editor.refreshBodyEditorPresentation();
                    GuiPackage guiPackage = GuiPackage.getInstance();
                    if (guiPackage != null) {
                        guiPackage.markCurrentGuiDirty();
                    }
                    editor.requestFocusInWindow();
                    if (manual) {
                        toolbar.showStatusMessage("search_body_format_done", true);
                    } else {
                        toolbar.showStatusMessage("search_body_auto_formatted", true);
                    }
                } catch (Exception ignored) {
                    toolbar.refreshBodyHints(source);
                }
            }
        };
        toolbar.setPendingFormatWorker(worker);
        worker.execute();
    }
}
