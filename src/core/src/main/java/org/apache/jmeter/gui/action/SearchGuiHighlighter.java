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
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * After a tree search hit, focus matching text in the element editor (e.g. Body Data)
 * and walk / replace individual occurrences.
 */
final class SearchGuiHighlighter {

    private static final Logger log = LoggerFactory.getLogger(SearchGuiHighlighter.class);

    private final List<LocatedMatch> matches = new ArrayList<>();
    private int index = -1;
    private String searchText;
    private boolean caseSensitive;
    private boolean regexp;

    void reset() {
        matches.clear();
        index = -1;
        searchText = null;
    }

    int getMatchCount() {
        return matches.size();
    }

    /**
     * @return 0-based index of the focused match, or -1
     */
    int getCurrentIndex() {
        return index;
    }

    /**
     * Collect matches in the current GUI and show the first or last one.
     *
     * @return true if any match is visible in the editor
     */
    boolean enterCurrentGui(String text, boolean caseSensitiveSearch, boolean regexpSearch, boolean showFirst) {
        this.searchText = text;
        this.caseSensitive = caseSensitiveSearch;
        this.regexp = regexpSearch;
        collectFromCurrentGui();
        if (matches.isEmpty()) {
            index = -1;
            return false;
        }
        index = showFirst ? 0 : matches.size() - 1;
        applyCurrent();
        return true;
    }

    /**
     * Move to the next/previous match in the current GUI.
     *
     * @return false when there is no further match in that direction
     */
    boolean navigate(boolean next) {
        if (matches.isEmpty()) {
            return false;
        }
        if (next) {
            if (index < matches.size() - 1) {
                index++;
                applyCurrent();
                return true;
            }
            return false;
        }
        if (index > 0) {
            index--;
            applyCurrent();
            return true;
        }
        return false;
    }

    /**
     * Replace the currently focused match in the editor.
     *
     * @return 1 if a replacement happened, otherwise 0
     */
    int replaceCurrent(String replaceWith) {
        if (index < 0 || index >= matches.size() || replaceWith == null || StringUtils.isEmpty(searchText)) {
            return 0;
        }
        LocatedMatch match = matches.get(index);
        JTextComponent editor = match.editor;
        String text = editor.getText();
        if (match.end > text.length() || match.start < 0) {
            collectFromCurrentGui();
            return 0;
        }
        editor.setCaretPosition(match.start);
        editor.moveCaretPosition(match.end);
        editor.replaceSelection(replaceWith);
        int caret = match.start + replaceWith.length();
        collectFromCurrentGui();
        index = -1;
        for (int i = 0; i < matches.size(); i++) {
            LocatedMatch next = matches.get(i);
            if (next.editor == editor && next.start >= caret) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            applyCurrent();
        }
        return 1;
    }

    private void collectFromCurrentGui() {
        matches.clear();
        if (StringUtils.isEmpty(searchText)) {
            return;
        }
        GuiPackage guiPackage = GuiPackage.getInstance();
        if (guiPackage == null || guiPackage.getCurrentNode() == null) {
            return;
        }
        JMeterGUIComponent gui = guiPackage.getGui(guiPackage.getCurrentElement());
        if (!(gui instanceof Component)) {
            return;
        }
        List<JTextComponent> editors = new ArrayList<>();
        collectTextComponents((Component) gui, editors);
        for (JTextComponent editor : editors) {
            if (!editor.isEnabled() || editor instanceof JPasswordField) {
                continue;
            }
            List<SearchTextMatches.Span> spans =
                    SearchTextMatches.findAll(editor.getText(), searchText, caseSensitive, regexp);
            for (SearchTextMatches.Span span : spans) {
                matches.add(new LocatedMatch(editor, span.getStart(), span.getEnd(), rank(editor)));
            }
        }
        matches.sort(Comparator
                .comparingInt((LocatedMatch m) -> m.rank).reversed()
                .thenComparingInt(m -> m.start));
    }

    private void applyCurrent() {
        if (index < 0 || index >= matches.size()) {
            return;
        }
        LocatedMatch match = matches.get(index);
        SwingUtilities.invokeLater(() -> selectMatch(match));
    }

    private static void selectMatch(LocatedMatch match) {
        JTextComponent target = match.editor;
        activateTabForComponent(target);
        target.requestFocusInWindow();
        try {
            target.getCaret().setSelectionVisible(true);
            target.setCaretPosition(match.start);
            target.moveCaretPosition(match.end);
            @SuppressWarnings("deprecation")
            Rectangle view = target.modelToView(match.start);
            if (view != null) {
                target.scrollRectToVisible(view);
            }
        } catch (BadLocationException | IllegalArgumentException ex) {
            log.debug("Unable to select search match in editor", ex);
        }
    }

    private static int rank(JTextComponent editor) {
        if (editor instanceof JSyntaxTextArea) {
            return 3;
        }
        if (editor instanceof JTextArea) {
            return 2;
        }
        return 1;
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

    private static final class LocatedMatch {
        private final JTextComponent editor;
        private final int start;
        private final int end;
        private final int rank;

        private LocatedMatch(JTextComponent editor, int start, int end, int rank) {
            this.editor = editor;
            this.start = start;
            this.end = end;
            this.rank = rank;
        }
    }
}
