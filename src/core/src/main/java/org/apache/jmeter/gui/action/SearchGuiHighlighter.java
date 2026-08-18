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

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * After a tree search hit, focus the matching text in the element editor (e.g. Body Data).
 */
final class SearchGuiHighlighter {

    private static final Logger log = LoggerFactory.getLogger(SearchGuiHighlighter.class);

    private SearchGuiHighlighter() {
    }

    static void focusMatchInCurrentGui(String searchText, boolean caseSensitive, boolean regexp) {
        if (StringUtils.isEmpty(searchText)) {
            return;
        }
        GuiPackage guiPackage = GuiPackage.getInstance();
        guiPackage.updateCurrentGui();
        JMeterGUIComponent gui = guiPackage.getCurrentGui();
        if (!(gui instanceof Component)) {
            return;
        }
        Component root = (Component) gui;
        List<JTextComponent> editors = new ArrayList<>();
        collectTextComponents(root, editors);
        JTextComponent best = null;
        int bestStart = -1;
        int bestEnd = -1;
        int bestScore = Integer.MIN_VALUE;
        for (JTextComponent editor : editors) {
            if (!editor.isEnabled()) {
                continue;
            }
            Match match = findMatch(editor.getText(), searchText, caseSensitive, regexp);
            if (match == null) {
                continue;
            }
            int score = editor instanceof JTextArea ? 1_000 : 0;
            score += Math.min(editor.getText().length(), 500);
            if (score > bestScore) {
                bestScore = score;
                best = editor;
                bestStart = match.start;
                bestEnd = match.end;
            }
        }
        if (best == null) {
            return;
        }
        final JTextComponent target = best;
        final int start = bestStart;
        final int end = bestEnd;
        SwingUtilities.invokeLater(() -> {
            activateTabForComponent(target);
            target.requestFocusInWindow();
            try {
                target.select(start, end);
                if (target instanceof JTextArea) {
                    ((JTextArea) target).setCaretPosition(start);
                }
            } catch (IllegalArgumentException ex) {
                log.debug("Unable to select search match in editor", ex);
            }
        });
    }

    private static void collectTextComponents(Component component, List<JTextComponent> editors) {
        if (component instanceof JTextComponent) {
            editors.add((JTextComponent) component);
            return;
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                collectTextComponents(child, editors);
            }
        }
    }

    private static void activateTabForComponent(Component component) {
        Component current = component;
        while (current != null) {
            Container parent = current.getParent();
            if (parent instanceof JTabbedPane) {
                JTabbedPane tabs = (JTabbedPane) parent;
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    Component tabComponent = tabs.getComponentAt(i);
                    if (tabComponent == current || SwingUtilities.isDescendingFrom(component, tabComponent)) {
                        tabs.setSelectedIndex(i);
                        return;
                    }
                }
            }
            current = parent;
        }
    }

    private static Match findMatch(String haystack, String needle, boolean caseSensitive, boolean regexp) {
        if (StringUtils.isEmpty(haystack)) {
            return null;
        }
        if (regexp) {
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            Matcher matcher = Pattern.compile(needle, flags).matcher(haystack);
            if (matcher.find()) {
                return new Match(matcher.start(), matcher.end());
            }
            return null;
        }
        int index = caseSensitive
                ? haystack.indexOf(needle)
                : StringUtils.indexOfIgnoreCase(haystack, needle);
        if (index < 0) {
            return null;
        }
        return new Match(index, index + needle.length());
    }

    private static final class Match {
        private final int start;
        private final int end;

        private Match(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
