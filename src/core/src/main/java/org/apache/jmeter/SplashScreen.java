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

package org.apache.jmeter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Splash Screen
 * @since 3.2
 */
public class SplashScreen extends JDialog {
    private static final Logger log = LoggerFactory.getLogger(SplashScreen.class);

    private static final long serialVersionUID = 1L;

    private static final String SPLASH_PNG = "/org/apache/jmeter/images/splash-jmeter2.png";

    /** Display width of the splash artwork (height follows aspect ratio). */
    private static final int SPLASH_DISPLAY_WIDTH = 720;

    private static final Color SPLASH_BG = new Color(0x14, 0x18, 0x1C);

    private static final Color PROGRESS_FG = new Color(0xD1, 0x11, 0x23);

    private final JProgressBar progressBar = new JProgressBar(0, 100);

    /**
     * Constructor
     */
    public SplashScreen() {
        getContentPane().setBackground(SPLASH_BG);
        setLayout(new BorderLayout());
        add(loadLogo(), BorderLayout.CENTER);
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setForeground(PROGRESS_FG);
        progressBar.setBackground(SPLASH_BG);
        add(progressBar, BorderLayout.SOUTH);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setAutoRequestFocus(true);
        setUndecorated(true);
        pack();
        setLocationRelativeTo(null);
    }

    public static JComponent loadLogo() {
        JLabel logo = new JLabel();
        logo.setOpaque(true);
        logo.setBackground(SPLASH_BG);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setBorder(new EmptyBorder(0, 0, 0, 0));
        URL pngUrl = JMeterUtils.class.getResource(SPLASH_PNG);
        if (pngUrl != null) {
            ImageIcon raw = new ImageIcon(pngUrl);
            int width = raw.getIconWidth();
            int height = raw.getIconHeight();
            if (width > 0 && height > 0) {
                int targetW = Math.min(SPLASH_DISPLAY_WIDTH, width);
                int targetH = Math.max(1, height * targetW / width);
                Image scaled = raw.getImage().getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
                logo.setIcon(new ImageIcon(scaled));
                return logo;
            }
        }
        log.warn("Unable to find splash image {}", SPLASH_PNG);
        logo.setText("<html><div style='padding:28px 36px;color:#e8eaed;font-family:Segoe UI,Arial,sans-serif'>"
                + "<div style='font-size:42px;font-weight:700'><span style='color:#c0c0c0'>JMeter</span>"
                + "<span style='color:#D11123'>2</span></div>"
                + "<div style='font-size:14px;color:#8b939c;margin-top:8px'>Load testing, rebuilt.</div></div></html>");
        return logo;
    }

    /**
     * Show screen
     */
    public void showScreen() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    /**
     * Close splash
     */
    public void close() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }

    /**
     * @param progress Loading progress
     */
    public void setProgress(final int progress) {
        if (SwingUtilities.isEventDispatchThread()) {
            progressBar.setValue(progress);
        } else {
            SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
        }
    }
}
