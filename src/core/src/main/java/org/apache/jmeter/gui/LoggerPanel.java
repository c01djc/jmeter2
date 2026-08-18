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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.jmeter.gui.logging.GuiLogEventListener;
import org.apache.jmeter.gui.logging.LogEventObject;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.logging.log4j.Level;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that shows log events
 */
public class LoggerPanel extends JPanel implements GuiLogEventListener {

    private static final long serialVersionUID = 4935188629475943230L;

    private static final Logger log = LoggerFactory.getLogger(LoggerPanel.class);

    private final JTextComponent textArea;

    // Limit length of log content
    // 0 means unlimited
    private static final int LOGGER_PANEL_MAX_LINES =
            JMeterUtils.getPropDefault("jmeter.loggerpanel.maxlength", 1000); // $NON-NLS-1$

    // Make panel handle event even if closed
    private static final boolean LOGGER_PANEL_RECEIVE_WHEN_CLOSED =
            JMeterUtils.getPropDefault("jmeter.loggerpanel.enable_when_closed", true); // $NON-NLS-1$

    private static final int LOGGER_PANEL_REFRESH_PERIOD =
            JMeterUtils.getPropDefault("jmeter.gui.refresh_period", 500); // $NON-NLS-1$

    /** Color log lines by level (ERROR/WARN/INFO/DEBUG). Default true. */
    private static final boolean LOGGER_PANEL_COLOR =
            JMeterUtils.getPropDefault("jmeter.loggerpanel.color", true); // $NON-NLS-1$

    private final Queue<LogEntry> events;

    private final boolean styled;

    private volatile boolean logChanged = false;

    private static final class LogEntry {
        private final String message;
        private final Level level;

        private LogEntry(String message, Level level) {
            this.message = message;
            this.level = level;
        }
    }

    /**
     * Pane for display JMeter log file
     */
    public LoggerPanel() {
        if (LOGGER_PANEL_MAX_LINES > 0) {
            events = new CircularFifoQueue<>(LOGGER_PANEL_MAX_LINES);
        } else {
            events = new ArrayDeque<>();
        }
        textArea = init();
        styled = LOGGER_PANEL_COLOR && textArea instanceof JTextPane;
    }

    private JTextComponent init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        this.setLayout(new BorderLayout());
        final JScrollPane areaScrollPane;
        final JTextComponent jTextComponent;

        if (LOGGER_PANEL_COLOR) {
            JTextPane pane = new JTextPane();
            pane.setEditable(false);
            pane.setMargin(new Insets(2, 2, 2, 2));
            Color bg = UIManager.getColor("TextArea.background");
            Color fg = UIManager.getColor("TextArea.foreground");
            if (bg != null) {
                pane.setBackground(bg);
            }
            if (fg != null) {
                pane.setForeground(fg);
            }
            pane.setFont(new JTextArea().getFont());
            areaScrollPane = new JScrollPane(pane);
            jTextComponent = pane;
        } else if (JMeterUtils.getPropDefault("loggerpanel.usejsyntaxtext", true)) {
            // JSyntax Text Area
            JSyntaxTextArea jSyntaxTextArea = JSyntaxTextArea.getInstance(15, 80, true);
            jSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            jSyntaxTextArea.setCodeFoldingEnabled(false);
            jSyntaxTextArea.setAntiAliasingEnabled(false);
            jSyntaxTextArea.setEditable(false);
            jSyntaxTextArea.setLineWrap(false);
            jSyntaxTextArea.setLanguage("text");
            jSyntaxTextArea.setMargin(new Insets(2, 2, 2, 2)); // space between borders and text
            areaScrollPane = JTextScrollPane.getInstance(jSyntaxTextArea);
            jTextComponent = jSyntaxTextArea;
        } else {
            // Plain text area
            JTextArea jTextArea = new JTextArea(15, 80);
            areaScrollPane = new JScrollPane(jTextArea);
            jTextComponent = jTextArea;
        }

        areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(areaScrollPane, BorderLayout.CENTER);

        initWorker();

        return jTextComponent;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.logging.GuiLogEventListener#processLogEvent(org.apache.jmeter.gui.logging.LogEventObject)
     */
    @Override
    public void processLogEvent(final LogEventObject logEventObject) {
        if(!LOGGER_PANEL_RECEIVE_WHEN_CLOSED && !GuiPackage.getInstance().getMenuItemLoggerPanel().getModel().isSelected()) {
            return;
        }

        String logMessage = logEventObject.toString();
        synchronized (events) {
            events.add(new LogEntry(logMessage, logEventObject.getLevel()));
        }

        logChanged = true;
    }

    private void initWorker() {
        Timer timer = new Timer(
            LOGGER_PANEL_REFRESH_PERIOD,
            e -> updateLogEntries());
        timer.start();
    }

    private void updateLogEntries() {
        if (!logChanged) {
            return;
        }
        logChanged = false;
        synchronized (textArea) {
            if (styled) {
                updateStyledLogEntries();
            } else {
                updatePlainLogEntries();
            }
        }
    }

    private void updatePlainLogEntries() {
        StringBuilder builder = new StringBuilder();
        synchronized (events) {
            for (LogEntry line : events) {
                builder.append(line.message);
            }
        }
        String logText = builder.toString();
        if (LOGGER_PANEL_MAX_LINES > 0) {
            textArea.setText(logText);
        } else {
            // Unlimited mode historically appended; rebuild from buffer instead
            textArea.setText(logText);
        }
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    private void updateStyledLogEntries() {
        JTextPane pane = (JTextPane) textArea;
        StyledDocument doc = pane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());
            synchronized (events) {
                for (LogEntry entry : events) {
                    doc.insertString(doc.getLength(), entry.message, styleForLevel(entry.level));
                }
            }
            pane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            log.debug("Unable to update styled logger panel", e);
        }
    }

    private static AttributeSet styleForLevel(Level level) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, colorForLevel(level));
        return attrs;
    }

    private static Color colorForLevel(Level level) {
        Color fallback = UIManager.getColor("TextArea.foreground");
        if (fallback == null) {
            fallback = Color.BLACK;
        }
        if (level == null) {
            return fallback;
        }
        boolean dark = isDarkBackground();
        if (level.isMoreSpecificThan(Level.ERROR)) {
            // ERROR / FATAL
            return dark ? new Color(0xFF6B6B) : new Color(0xC92A2A);
        }
        if (level.isMoreSpecificThan(Level.WARN)) {
            // WARN
            return dark ? new Color(0xFFD43B) : new Color(0xE67700);
        }
        if (level.isMoreSpecificThan(Level.INFO)) {
            // INFO
            return dark ? new Color(0x74C0FC) : new Color(0x1C7ED6);
        }
        // DEBUG / TRACE
        return dark ? new Color(0xADB5BD) : new Color(0x868E96);
    }

    private static boolean isDarkBackground() {
        Color bg = UIManager.getColor("TextArea.background");
        if (bg == null) {
            bg = UIManager.getColor("Panel.background");
        }
        if (bg == null) {
            return false;
        }
        // Perceived luminance
        double lum = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        return lum < 0.5;
    }

    /**
     * Clear panel content
     */
    public void clear() {
        synchronized (events) {
            events.clear();
        }
        logChanged = true;
        // Clear immediately for responsiveness
        synchronized (textArea) {
            textArea.setText("");
            Document doc = textArea.getDocument();
            if (doc instanceof StyledDocument) {
                try {
                    doc.remove(0, doc.getLength());
                } catch (BadLocationException e) {
                    log.debug("Unable to clear styled logger panel", e);
                }
            }
        }
    }
}
