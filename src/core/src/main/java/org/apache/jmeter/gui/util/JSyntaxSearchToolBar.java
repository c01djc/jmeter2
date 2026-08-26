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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.gui.action.SearchTextMatches;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JFactory;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import net.miginfocom.swing.MigLayout;

/**
 * Search toolbar associated to {@link JSyntaxTextArea}
 * @since 5.0
 */
public final class JSyntaxSearchToolBar implements ActionListener {
    public static final Color LIGHT_RED = new Color(0xFF, 0x80, 0x80);

    public static final String FIND_ACTION = "Find";
    public static final String FIND_PREVIOUS_ACTION = "FindPrevious";
    public static final String REPLACE_ACTION = "Replace";
    public static final String REPLACE_ALL_ACTION = "ReplaceAll";

    private static final Color DIVIDER = new Color(0xD0, 0xD0, 0xD0);

    private JToolBar toolBar;

    private JComponent barComponent;

    private JTextField searchField;

    private JTextField replaceField;

    private JLabel statusLabel;

    private JCheckBox regexCB;

    private JCheckBox matchCaseCB;

    private Timer searchDebounceTimer;

    private SwingWorker<?, ?> pendingFormatWorker;

    /**
     * The component where we Search
     */
    private final JSyntaxTextArea dataField;

    private final boolean enableReplace;

    /**
     * @param dataField {@link JSyntaxTextArea} to use for searching
     */
    public JSyntaxSearchToolBar(JSyntaxTextArea dataField) {
        this(dataField, false);
    }

    /**
     * @param dataField {@link JSyntaxTextArea} to use for searching
     * @param enableReplace true to show replace field and replace buttons
     */
    public JSyntaxSearchToolBar(JSyntaxTextArea dataField, boolean enableReplace) {
        this.dataField = dataField;
        this.enableReplace = enableReplace;
        init();
        if (enableReplace) {
            installKeyBindings(dataField);
        }
    }

    /**
     * Body-data editor with find / locate / replace above the text.
     *
     * @param textArea target editor
     * @return panel containing the toolbar and scrollable text area
     */
    public static JPanel wrapWithFindReplace(JSyntaxTextArea textArea) {
        textArea.configureAsHttpBodyEditor();
        JSyntaxSearchToolBar bar = new JSyntaxSearchToolBar(textArea, true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.putClientProperty(HttpBodyEditorLoader.TOOLBAR_CLIENT_KEY, bar);
        panel.add(bar.getBarComponent(), BorderLayout.NORTH);
        panel.add(JTextScrollPane.getInstance(textArea), BorderLayout.CENTER);
        return panel;
    }

    /**
     * @param wrappedPanel panel returned by {@link #wrapWithFindReplace}
     * @return search toolbar for that body editor, or null
     */
    public static JSyntaxSearchToolBar getSearchToolBar(JComponent wrappedPanel) {
        Object value = wrappedPanel.getClientProperty(HttpBodyEditorLoader.TOOLBAR_CLIENT_KEY);
        return value instanceof JSyntaxSearchToolBar ? (JSyntaxSearchToolBar) value : null;
    }

    /**
     * Show raw body immediately; compact JSON/XML is pretty-printed in the background.
     */
    public void loadBodyContent(String raw) {
        HttpBodyEditorLoader.loadContent(this, raw);
    }

    private void init() {
        this.searchField = new JTextField();
        matchCaseCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_case"));
        searchField.addActionListener(e -> find(true));

        if (!enableReplace) {
            regexCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_regexp"));
            JFactory.small(searchField);
            JFactory.small(regexCB);
            JFactory.small(matchCaseCB);
            this.toolBar = new JToolBar();
            toolBar.setFloatable(false);
            JFactory.small(toolBar);
            toolBar.add(searchField);
            toolBar.add(createButton("search_text_button_find", FIND_ACTION, true));
            toolBar.add(matchCaseCB);
            toolBar.add(regexCB);
            this.barComponent = toolBar;
            return;
        }

        this.replaceField = new JTextField();
        replaceField.addActionListener(e -> replace(false));
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        // Body editor: find / replace / case only (no regex / format controls)
        JPanel panel = new JPanel(new MigLayout(
                "insets 8 10 8 10, fillx, gapx 8, gapy 6",
                "[][grow,fill][][][][right]"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER),
                BorderFactory.createEmptyBorder()));

        panel.add(new JLabel(JMeterUtils.getResString("search_text_field")));
        panel.add(searchField, "growx");
        panel.add(createButton("search_next", FIND_ACTION, false));
        panel.add(createButton("search_previous", FIND_PREVIOUS_ACTION, false));
        panel.add(matchCaseCB);
        panel.add(statusLabel, "wrap");

        panel.add(new JLabel(JMeterUtils.getResString("search_text_replace")));
        panel.add(replaceField, "growx");
        panel.add(createButton("search_replace_current", REPLACE_ACTION, false));
        panel.add(createButton("search_replace_all", REPLACE_ALL_ACTION, false), "wrap");

        this.toolBar = new JToolBar();
        toolBar.setFloatable(false);
        this.barComponent = panel;
        installSearchDebouncing();
    }

    private void installSearchDebouncing() {
        searchDebounceTimer = new Timer(350, e -> {
            if (!searchField.getText().isEmpty()
                    && dataField.getDocument().getLength() < JSyntaxTextArea.HEAVY_BODY_CHARS) {
                // Preview matches while typing; never move focus out of the find box.
                find(true, false);
            }
        });
        searchDebounceTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchDebounceTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchDebounceTimer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // attribute change only
            }
        });
        matchCaseCB.addActionListener(e -> {
            // Re-search from the top so case option always takes effect immediately.
            dataField.setCaretPosition(0);
            searchDebounceTimer.restart();
        });
    }

    JSyntaxTextArea getEditor() {
        return dataField;
    }

    void cancelPendingFormat() {
        if (pendingFormatWorker != null && !pendingFormatWorker.isDone()) {
            pendingFormatWorker.cancel(true);
        }
        pendingFormatWorker = null;
    }

    void setPendingFormatWorker(SwingWorker<?, ?> worker) {
        cancelPendingFormat();
        pendingFormatWorker = worker;
    }

    void clearPendingFormatWorker(SwingWorker<?, ?> worker) {
        if (pendingFormatWorker == worker) {
            pendingFormatWorker = null;
        }
    }

    void showStatusMessage(String messageKey, boolean resourceKey) {
        if (statusLabel == null) {
            return;
        }
        statusLabel.setText(resourceKey ? JMeterUtils.getResString(messageKey) : messageKey);
    }

    void refreshBodyHints(String raw) {
        if (statusLabel == null) {
            return;
        }
        if (raw != null && (raw.length() >= JSyntaxTextArea.HEAVY_BODY_CHARS
                || raw.length() > BodyDataFormatter.AUTO_FORMAT_MAX_CHARS)) {
            showStatusMessage("search_body_hint_large", true);
        } else {
            statusLabel.setText(" ");
        }
    }

    private JComponent getBarComponent() {
        return barComponent;
    }

    private JButton createButton(String labelKey, String action, boolean small) {
        JButton button = new JButton(JMeterUtils.getResString(labelKey));
        button.setActionCommand(action);
        button.addActionListener(this);
        if (small) {
            JFactory.small(button);
        } else {
            button.setMargin(new Insets(3, 10, 3, 10));
        }
        return button;
    }

    private void installKeyBindings(JSyntaxTextArea area) {
        InputMap inputMap = area.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = area.getActionMap();
        inputMap.put(KeyStrokes.SEARCH_TREE, "jmeter2-find-in-editor");
        actionMap.put("jmeter2-find-in-editor", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.requestFocusInWindow();
                searchField.selectAll();
            }
        });
        KeyStroke f3 = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        KeyStroke shiftF3 = KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK);
        inputMap.put(f3, "jmeter2-find-next");
        actionMap.put("jmeter2-find-next", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
                find(true, false);
            }
        });
        inputMap.put(shiftF3, "jmeter2-find-prev");
        actionMap.put("jmeter2-find-prev", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
                find(false, false);
            }
        });
    }

    /**
     * Find-only toolbar used by result viewers. Must stay {@link JToolBar}
     * for binary compatibility with ApacheJMeter_components.
     *
     * @return search toolbar
     */
    public JToolBar getToolBar() {
        return toolBar;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand();
        if (FIND_ACTION.equals(command)) {
            find(true, false);
        } else if (FIND_PREVIOUS_ACTION.equals(command)) {
            find(false, false);
        } else if (REPLACE_ACTION.equals(command)) {
            replace(false);
        } else if (REPLACE_ALL_ACTION.equals(command)) {
            replace(true);
        }
    }

    private void find(boolean forward) {
        find(forward, true);
    }

    /**
     * @param forward search direction
     * @param allowEditorFocus when true (legacy find-only toolbar), may focus the editor
     */
    private void find(boolean forward, boolean allowEditorFocus) {
        String text = searchField.getText();
        toggleSearchField(searchField, true);
        if (text.isEmpty()) {
            updateStatus(0, 0);
            return;
        }

        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        boolean keepToolbarFocus = enableReplace
                && (focusOwner == searchField || focusOwner == replaceField);

        boolean found;
        if (enableReplace) {
            found = findInBody(text, forward, matchCaseCB.isSelected());
        } else {
            SearchContext context = createSearchContext(
                    text, forward, matchCaseCB.isSelected(), regexCB.isSelected());
            context.setReplaceWith("");
            found = SearchEngine.find(dataField, context).wasFound();
            if (!found && dataField.getDocument().getLength() > 0) {
                int saved = dataField.getCaretPosition();
                dataField.setCaretPosition(forward ? 0 : dataField.getDocument().getLength());
                found = SearchEngine.find(dataField, context).wasFound();
                if (!found) {
                    dataField.setCaretPosition(saved);
                }
            }
        }

        toggleSearchField(searchField, found);
        if (enableReplace) {
            if (dataField.getDocument().getLength() < JSyntaxTextArea.HEAVY_BODY_CHARS) {
                updateOccurrenceStatus();
            } else {
                updateStatus(0, 0);
            }
        }

        if (keepToolbarFocus) {
            // Selection in the body must not steal keystrokes from the find/replace fields.
            final JTextField keep = focusOwner == replaceField ? replaceField : searchField;
            SwingUtilities.invokeLater(keep::requestFocusInWindow);
        } else if (found && allowEditorFocus && !enableReplace) {
            dataField.requestFocusInWindow();
        }
    }

    /**
     * Body find uses {@link SearchTextMatches} so case sensitivity matches the checkbox
     * and status counter exactly.
     */
    private boolean findInBody(String needle, boolean forward, boolean matchCase) {
        List<SearchTextMatches.Span> spans = SearchTextMatches.findAll(
                dataField.getText(), needle, matchCase, false);
        if (spans.isEmpty()) {
            return false;
        }

        int selStart = dataField.getSelectionStart();
        int selEnd = dataField.getSelectionEnd();
        SearchTextMatches.Span target = null;

        if (forward) {
            for (SearchTextMatches.Span span : spans) {
                if (span.getStart() > selStart || (span.getStart() == selStart && span.getEnd() > selEnd)) {
                    target = span;
                    break;
                }
            }
            if (target == null) {
                target = spans.get(0);
            }
        } else {
            for (int i = spans.size() - 1; i >= 0; i--) {
                SearchTextMatches.Span span = spans.get(i);
                if (span.getStart() < selStart) {
                    target = span;
                    break;
                }
            }
            if (target == null) {
                target = spans.get(spans.size() - 1);
            }
        }

        selectBodySpan(target);
        return true;
    }

    private void selectBodySpan(SearchTextMatches.Span span) {
        dataField.select(span.getStart(), span.getEnd());
        if (dataField.getCaret() instanceof DefaultCaret) {
            ((DefaultCaret) dataField.getCaret()).setSelectionVisible(true);
        } else {
            dataField.getCaret().setSelectionVisible(true);
        }
        try {
            dataField.getCaret().setDot(span.getEnd());
            dataField.getCaret().moveDot(span.getStart());
        } catch (IllegalArgumentException ignored) {
            dataField.select(span.getStart(), span.getEnd());
        }
    }

    private void replace(boolean all) {
        String text = searchField.getText();
        if (text.isEmpty() || replaceField == null) {
            return;
        }
        boolean matchCase = matchCaseCB.isSelected();
        if (all) {
            replaceAllInBody(text, replaceField.getText(), matchCase);
        } else {
            String selected = dataField.getSelectedText();
            boolean selectionMatches = selected != null && (matchCase
                    ? selected.equals(text)
                    : selected.equalsIgnoreCase(text));
            if (!selectionMatches) {
                findInBody(text, true, matchCase);
                selected = dataField.getSelectedText();
                selectionMatches = selected != null && (matchCase
                        ? selected.equals(text)
                        : selected.equalsIgnoreCase(text));
            }
            if (selectionMatches) {
                dataField.replaceSelection(replaceField.getText());
            }
        }
        GuiPackage guiPackage = GuiPackage.getInstance();
        if (guiPackage != null) {
            guiPackage.markCurrentGuiDirty();
        }
        if (dataField.getDocument().getLength() < JSyntaxTextArea.HEAVY_BODY_CHARS) {
            updateOccurrenceStatus();
        } else {
            updateStatus(0, 0);
        }
        SwingUtilities.invokeLater(replaceField::requestFocusInWindow);
    }

    private void replaceAllInBody(String needle, String replacement, boolean matchCase) {
        List<SearchTextMatches.Span> spans = SearchTextMatches.findAll(
                dataField.getText(), needle, matchCase, false);
        if (spans.isEmpty()) {
            return;
        }
        StringBuilder out = new StringBuilder(dataField.getText());
        for (int i = spans.size() - 1; i >= 0; i--) {
            SearchTextMatches.Span span = spans.get(i);
            out.replace(span.getStart(), span.getEnd(), replacement);
        }
        int caret = dataField.getCaretPosition();
        dataField.setInitialText(out.toString());
        dataField.setCaretPosition(Math.min(caret, out.length()));
        dataField.refreshBodyEditorPresentation();
    }

    private void updateOccurrenceStatus() {
        String needle = searchField.getText();
        if (needle.isEmpty()) {
            updateStatus(0, 0);
            return;
        }
        List<SearchTextMatches.Span> spans = SearchTextMatches.findAll(
                dataField.getText(), needle, matchCaseCB.isSelected(), false);
        int current = 0;
        int caret = dataField.getSelectionStart();
        for (int i = 0; i < spans.size(); i++) {
            SearchTextMatches.Span span = spans.get(i);
            if (span.getStart() == caret || (span.getStart() <= caret && caret < span.getEnd())) {
                current = i + 1;
                break;
            }
            if (span.getStart() > caret && current == 0) {
                current = i + 1;
                break;
            }
        }
        if (current == 0 && !spans.isEmpty()) {
            current = 1;
        }
        updateStatus(current, spans.size());
        toggleSearchField(searchField, !spans.isEmpty());
    }

    private void updateStatus(int current, int total) {
        if (statusLabel == null) {
            return;
        }
        if (total <= 0) {
            statusLabel.setText(" ");
            return;
        }
        statusLabel.setText(MessageFormat.format(
                JMeterUtils.getResString("search_text_occurrence"), current, total));
    }

    void toggleSearchField(JTextField textToFindField, boolean matchFound) {
        if(!matchFound) {
            textToFindField.setBackground(LIGHT_RED);
            textToFindField.setForeground(Color.WHITE);
        } else {
            textToFindField.setBackground(Color.WHITE);
            textToFindField.setForeground(Color.BLACK);
        }
    }

    private SearchContext createSearchContext(String text, boolean forward, boolean matchCase,
            boolean isRegex) {
        SearchContext context = new SearchContext();
        context.setSearchFor(text);
        context.setMatchCase(matchCase);
        context.setRegularExpression(isRegex);
        context.setSearchForward(forward);
        // Mark-all paints every hit; a short needle in a huge JSON body freezes the EDT.
        context.setMarkAll(!enableReplace && dataField.getDocument().getLength() < JSyntaxTextArea.HEAVY_BODY_CHARS);
        context.setSearchSelectionOnly(false);
        context.setWholeWord(false);
        return context;
    }
}
