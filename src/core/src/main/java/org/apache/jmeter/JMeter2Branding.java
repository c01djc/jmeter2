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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.weisj.darklaf.icons.ThemedSVGIcon;

/**
 * User-facing product branding for this JMeter fork.
 * Internal package names remain {@code org.apache.jmeter} for compatibility.
 */
public final class JMeter2Branding {

    private static final Logger log = LoggerFactory.getLogger(JMeter2Branding.class);

    public static final String APP_NAME = "JMeter2";

    public static final String APP_NAME_LOWER = "jmeter2";

    /** Fork author (GitHub username). */
    public static final String AUTHOR_NAME = "c01djc";

    public static final String AUTHOR_GITHUB_URL = "https://github.com/c01djc";

    /** Upstream Apache JMeter release this fork is based on. */
    public static final String UPSTREAM_BASE_VERSION = "5.6.3";

    private static final String APP_ICON_PNG = "/org/apache/jmeter/images/icon-jmeter2.png";

    private static final String APP_ICON_SVG = "/org/apache/jmeter/images/icon-jmeter2.svg";

    private JMeter2Branding() {
    }

    /**
     * Window / taskbar icons at common sizes (PNG preferred, SVG fallback).
     *
     * @return non-empty list of images suitable for {@code JFrame#setIconImages}
     */
    public static List<Image> loadAppIconImages() {
        List<Image> images = new ArrayList<>(5);
        for (int size : new int[] {16, 32, 48, 64, 128}) {
            Image img = loadAppIconImage(size);
            if (img != null) {
                images.add(img);
            }
        }
        if (images.isEmpty()) {
            ImageIcon fallback = JMeterUtils.getImage("icon-apache.png"); // $NON-NLS-1$
            if (fallback != null) {
                images.add(fallback.getImage());
            }
        }
        return images;
    }

    private static Image loadAppIconImage(int size) {
        Image png = loadPngIcon(size);
        if (png != null) {
            return png;
        }
        return loadSvgIcon(size);
    }

    private static Image loadPngIcon(int size) {
        URL pngUrl = JMeterUtils.class.getResource(APP_ICON_PNG);
        if (pngUrl == null) {
            return null;
        }
        try {
            BufferedImage src = ImageIO.read(pngUrl);
            if (src == null) {
                return null;
            }
            if (src.getWidth() == size && src.getHeight() == size) {
                return src;
            }
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(src, 0, 0, size, size, null);
            } finally {
                g.dispose();
            }
            return scaled;
        } catch (IOException | RuntimeException e) {
            log.debug("Unable to load JMeter2 PNG app icon at size {}", size, e);
            return null;
        }
    }

    private static Image loadSvgIcon(int size) {
        try {
            URL svgUrl = JMeterUtils.class.getResource(APP_ICON_SVG);
            if (svgUrl == null) {
                return null;
            }
            URI svgUri = svgUrl.toURI();
            Icon icon = new ThemedSVGIcon(svgUri, size, size);
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                icon.paintIcon(null, g, 0, 0);
            } finally {
                g.dispose();
            }
            return image;
        } catch (URISyntaxException | RuntimeException e) {
            log.debug("Unable to load JMeter2 SVG app icon at size {}", size, e);
            return null;
        }
    }
}
