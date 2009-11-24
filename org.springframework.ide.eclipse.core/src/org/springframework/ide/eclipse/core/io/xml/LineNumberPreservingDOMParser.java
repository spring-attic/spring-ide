/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.io.xml;

import org.apache.xerces.dom.NodeImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.SAXException;

/**
 * Extended version of Xerces' DOM parser which adds line numbers (as DOM level 3 user data) to every node.
 * <p>
 * <b>Requires Xerces 2.7 or newer!!!</b>
 * </p>
 * @author Torsten Juergeleit
 * @author Christian Dupuis
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
			// String line = (String) ((NodeImpl) node).getUserData(key);
			try {
				String line = (String) ClassUtils.invokeMethod(node, "getUserData", new Object[] { key },
						new Class[] { String.class });
				if (line != null && line.length() > 0) {
					try {
						return Integer.parseInt(line);
					}
					catch (NumberFormatException e) {
						// ignore invalid user data
					}
				}
			}
			catch (Throwable e) {
				// silently ignore that we can't get line numbers
			}
		}
		return -1;
	}

	@Override
	public void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext, Augmentations augs)
			throws XNIException {
		this.locator = locator;
		super.startDocument(locator, encoding, namespaceContext, augs);
		addLineNumberToCurrentNode(START_LINE);
	}

	@Override
	public void endDocument(Augmentations augs) throws XNIException {
		addLineNumberToCurrentNode(END_LINE);
		super.endDocument(augs);
	}

	@Override
	public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		super.startElement(element, attributes, augs);
		addLineNumberToCurrentNode(START_LINE);
	}

	@Override
	public void endElement(QName element, Augmentations augs) throws XNIException {
		addLineNumberToCurrentNode(END_LINE);
		super.endElement(element, augs);
	}

	private void addLineNumberToCurrentNode(String key) throws XNIException {
		try {
			Node node = (Node) getProperty(CURRENT_ELEMENT_NODE);
			if (node instanceof NodeImpl) {
				String line = String.valueOf(locator.getLineNumber());
				// ((NodeImpl) node).setUserData(key, line, (UserDataHandler) null);
				try {
					ClassUtils.invokeMethod(node, "setUserData", new Object[] { key, line, (UserDataHandler) null },
							new Class[] { String.class, Object.class, UserDataHandler.class });
				}
				catch (Throwable e) {
					SpringCore.log(e);
				}
			}
		}
		catch (SAXException e) {
			throw new XNIException(e);
		}
	}
}
