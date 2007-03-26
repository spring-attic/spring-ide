/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.io.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * A simple XML writer.
 */
public class XMLWriter extends PrintWriter {

	public static final String XML_VERSION =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	protected int tab;

	public XMLWriter(OutputStream output) throws UnsupportedEncodingException {
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
			printTag(name, null, true, false);
			print(getEscaped(String.valueOf(value)));
			printTag('/' + name, null, false, true);
		}
	}

	public void printTabulation() {
		for (int i = 0; i < tab; i++) {
			super.print('\t');
		}
	}

	public void printTag(String name, HashMap<String, ?> parameters) {
		printTag(name, parameters, true, true);
	}

	public void printTag(String name, HashMap<String, ?> parameters,
			boolean tab, boolean newLine) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<");
		buffer.append(name);
		if (parameters != null) {
			for (String key : parameters.keySet()) {
				buffer.append(" ");
				buffer.append(key);
				buffer.append("=\"");
				buffer.append(getEscaped(String.valueOf(parameters.get(key))));
				buffer.append("\"");
			}
		}
		buffer.append(">");
		if (tab) {
			printTabulation();
		}
		if (newLine) {
			println(buffer.toString());
		} else {
			print(buffer.toString());
		}
	}

	public void startTag(String name, HashMap<String, ?> parameters) {
		startTag(name, parameters, true);
	}

	public void startTag(String name, HashMap<String, ?> parameters,
			boolean newLine) {
		printTag(name, parameters, true, newLine);
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

	public static String getEscaped(String text) {
		StringBuffer result = new StringBuffer(text.length() + 10);
		for (int i = 0; i < text.length(); ++i) {
			appendEscapedChar(result, text.charAt(i));
		}
		return result.toString();
	}

	private static String getReplacement(char c) {
		// Encode special XML characters into the equivalent character
		// references. These five are defined by default for all XML documents.
		switch (c) {
		case '<':
			return "lt";
		case '>':
			return "gt";
		case '"':
			return "quot";
		case '\'':
			return "apos";
		case '&':
			return "amp";
		}
		return null;
	}
}
