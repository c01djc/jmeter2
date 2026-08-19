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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Pretty-print HTTP body data (JSON / XML) for the Body Data editor.
 */
public final class BodyDataFormatter {

    /**
     * Auto-format on node select is capped so large payloads do not freeze the EDT.
     * Manual「格式化」still formats any size.
     */
    static final int AUTO_FORMAT_MAX_CHARS = 100_000;

    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private BodyDataFormatter() {
    }

    /**
     * Auto-format when opening a sampler: only compact JSON/XML under {@link #AUTO_FORMAT_MAX_CHARS}.
     */
    public static String autoFormatIfCompact(String text) {
        if (!shouldAutoFormat(text)) {
            return text;
        }
        return formatIfPossible(text);
    }

    static boolean shouldAutoFormat(String text) {
        if (StringUtils.isBlank(text) || text.length() > AUTO_FORMAT_MAX_CHARS) {
            return false;
        }
        String trimmed = text.trim();
        if (!(looksLikeJson(trimmed) || looksLikeXml(trimmed))) {
            return false;
        }
        // Already multi-line / pretty — skip to keep tree switching snappy
        return trimmed.indexOf('\n') < 0 && trimmed.indexOf('\r') < 0;
    }

    /**
     * @return pretty-printed text when input looks like JSON or XML; otherwise the original text
     */
    public static String formatIfPossible(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        String trimmed = text.trim();
        if (looksLikeJson(trimmed)) {
            String pretty = prettyJson(trimmed);
            return pretty != null ? pretty : text;
        }
        if (looksLikeXml(trimmed)) {
            String pretty = prettyXml(trimmed);
            return pretty != null ? pretty : text;
        }
        return text;
    }

    public static boolean looksLikeJson(String trimmed) {
        char c = trimmed.charAt(0);
        return (c == '{' || c == '[') && (trimmed.endsWith("}") || trimmed.endsWith("]"));
    }

    public static boolean looksLikeXml(String trimmed) {
        return trimmed.charAt(0) == '<' && trimmed.indexOf('>') > 1;
    }

    static String prettyJson(String json) {
        try {
            JsonNode node = JSON.readTree(json);
            if (node == null || node.isMissingNode()) {
                return null;
            }
            return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            return null;
        }
    }

    static String prettyXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setExpandEntityReferences(false);
            try {
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            } catch (IllegalArgumentException ignored) {
                // Some JDK parsers do not support these attributes
            }
            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            document.normalizeDocument();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            try {
                transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            } catch (IllegalArgumentException ignored) {
                // Some JDK transformers do not support these attributes
            }
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    xml.trim().startsWith("<?xml") ? "no" : "yes");
            try {
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            } catch (IllegalArgumentException ignored) {
                // optional
            }
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String pretty = writer.toString().trim();
            // Some transformers ignore INDENT; fall back to a light manual break
            if (!pretty.contains("\n") && pretty.length() > 20) {
                pretty = pretty.replace("><", ">\n<");
            }
            return pretty;
        } catch (Exception e) {
            return null;
        }
    }
}
