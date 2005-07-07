/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.web.flow.core.internal;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * A simple XML writer.
 */
public class XmlWriter extends PrintWriter {

    public static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    protected int tab;

    public XmlWriter(OutputStream output) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(output, "UTF8"));
        tab = 0;
        println(XML_VERSION);
    }

    public void endTag(String name) {
        tab--;
        printTag('/' + name, null);
    }

    public void printSimpleTag(String name, Object value) {
        if (value != null) {
            printTag(name, null, true, false, false);
            print(getEscaped(String.valueOf(value)));
            printTag('/' + name, null, false, true, false);
        }
    }

    public void printComment(String name) {
        print("<!-- ");
        print(getEscaped(name));
        print(" -->");
    }

    public void printSimpleTag(String name, HashMap parameters, Object value) {
        if (value != null) {
            printTag(name, parameters, true, false, false);
            print(getEscaped(String.valueOf(value)));
            printTag('/' + name, null, false, true, false);
        }
    }

    public void printTabulation() {
        for (int i = 0; i < tab; i++)
            super.print('\t');
    }

    public void printTag(String name, HashMap parameters) {
        printTag(name, parameters, true, true, false);
    }

    public void printTag(String name, HashMap parameters, boolean tab,
            boolean newLine, boolean endTag) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<");
        buffer.append(name);
        if (parameters != null) {
            Enumeration enum = Collections.enumeration(parameters.keySet());
            while (enum.hasMoreElements()) {
                String key = (String) enum.nextElement();
                if (parameters.get(key) != null) {
                    buffer.append(" ");
                    buffer.append(key);
                    buffer.append("=\"");
                    buffer.append(getEscaped(String
                            .valueOf(parameters.get(key))));
                    buffer.append("\"");
                }
            }
        }
        if (!endTag) {
            buffer.append(">");
        } else {
            buffer.append("/>");
        }
        if (tab) {
            printTabulation();
        }
        if (newLine) {
            println(buffer.toString());
        } else {
            print(buffer.toString());
        }
    }

    public void startTag(String name, HashMap parameters) {
        startTag(name, parameters, true);
    }

    public void startTag(String name, HashMap parameters, boolean newLine) {
        printTag(name, parameters, true, newLine, false);
        tab++;
    }

    private static void appendEscapedChar(StringBuffer buffer, char c) {
        String replacement = getReplacement(c);
        if (replacement != null) {
            buffer.append('&');
            buffer.append(replacement);
            buffer.append(';');
        } else {
            buffer.append(c);
        }
    }

    public static String getEscaped(String s) {
        StringBuffer result = new StringBuffer(s.length() + 10);
        for (int i = 0; i < s.length(); ++i)
            appendEscapedChar(result, s.charAt(i));
        return result.toString();
    }

    private static String getReplacement(char c) {
        // Encode special XML characters into the equivalent character
        // references.
        // These five are defined by default for all XML documents.
        switch (c) {
        case '<':
            return "lt"; //$NON-NLS-1$
        case '>':
            return "gt"; //$NON-NLS-1$
        case '"':
            return "quot"; //$NON-NLS-1$
        case '\'':
            return "apos"; //$NON-NLS-1$
        case '&':
            return "amp"; //$NON-NLS-1$
        }
        return null;
    }
}