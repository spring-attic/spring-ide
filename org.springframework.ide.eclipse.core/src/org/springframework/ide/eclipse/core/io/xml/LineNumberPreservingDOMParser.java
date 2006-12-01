/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.core.io.xml;

import org.apache.xerces.dom.NodeImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.SAXException;

/**
 * Extended version of Xerces' DOM parser which adds line numbers
 * (as DOM level 3 user data) to every node.
 * <p><b>Requires Xerces 2.7 or newer!!!</b></p>
 * @author Torsten Juergeleit
 */
public class LineNumberPreservingDOMParser extends DOMParser {

	private static final String START_LINE = "startLine";
	private static final String END_LINE = "endLine";

	private XMLLocator locator;

	public LineNumberPreservingDOMParser() throws SAXException {
		// To access current nodes we have to turn off a feature
		setFeature(DEFER_NODE_EXPANSION, false);
	}

	public static final int getStartLineNumber(Node node) {
		return getLineNumberFromUserData(node, START_LINE);
	}

	public static final int getEndLineNumber(Node node) {
		return getLineNumberFromUserData(node, END_LINE);
	}

	private static int getLineNumberFromUserData(Node node, String key) {
		if (node instanceof NodeImpl) {
			String line = (String) ((NodeImpl) node).getUserData(key);
			if (line != null && line.length() > 0) {
				try {
					return Integer.parseInt(line);
				} catch (NumberFormatException e) {
					// ignore invalid user data
				}
			}
		}
		return -1;
	}

	public void startDocument(XMLLocator locator, String encoding,
			NamespaceContext namespaceContext, Augmentations augs)
			throws XNIException {
		this.locator = locator;
		super.startDocument(locator, encoding, namespaceContext, augs);
		addLineNumberToCurrentNode(START_LINE);
	}

	public void endDocument(Augmentations augs) throws XNIException {
		addLineNumberToCurrentNode(END_LINE);
		super.endDocument(augs);
	}

	public void startElement(QName element, XMLAttributes attributes,
			Augmentations augs) throws XNIException {
		super.startElement(element, attributes, augs);
		addLineNumberToCurrentNode(START_LINE);
	}

	public void endElement(QName element, Augmentations augs)
			throws XNIException {
		addLineNumberToCurrentNode(END_LINE);
		super.endElement(element, augs);
	}

	private void addLineNumberToCurrentNode(String key) throws XNIException {
		try {
			Node node = (Node) getProperty(CURRENT_ELEMENT_NODE);
			if (node instanceof NodeImpl) {
				String line = String.valueOf(locator.getLineNumber());
				((NodeImpl) node)
						.setUserData(key, line, (UserDataHandler) null);
			}
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}
}
