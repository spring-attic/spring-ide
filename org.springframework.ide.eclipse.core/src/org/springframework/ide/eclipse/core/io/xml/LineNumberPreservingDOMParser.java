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

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Extended version of Xerces' DOM parser which adds line numbers to an internal structure that can be queried in the
 * same thread.
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
		return NodeLineNumberAccessor.getLineNumber(node, key);
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
			if (node != null) {
				int line = locator.getLineNumber();
				NodeLineNumberAccessor.setLineNumber(node, line, key);
			}
		}
		catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	private static class NodeLineNumberAccessor {

		private static ThreadLocal<Map<Node, LineNumbers>> LINE_NUMBERS = new ThreadLocal<Map<Node, LineNumbers>>() {
			protected Map<Node, LineNumbers> initialValue() {
				return new WeakHashMap<Node, LineNumbers>();
			};
		};

		public static void setLineNumber(Node node, int line, String key) {
			LineNumbers lineNumbers = null;
			if (LINE_NUMBERS.get().containsKey(node)) {
				lineNumbers = LINE_NUMBERS.get().get(node);
			}
			else {
				lineNumbers = new LineNumbers();
				LINE_NUMBERS.get().put(node, lineNumbers);
			}

			if (START_LINE.equals(key)) {
				lineNumbers.setStart(line);
			}
			else if (END_LINE.equals(key)) {
				lineNumbers.setEnd(line);
			}
		}

		public static int getLineNumber(Node node, String key) {
			if (LINE_NUMBERS.get().containsKey(node)) {
				if (START_LINE.equals(key)) {
					return LINE_NUMBERS.get().get(node).getStart();
				}
				else if (END_LINE.equals(key)) {
					return LINE_NUMBERS.get().get(node).getEnd();
				}
			}
			return -1;
		}
	}

	private static class LineNumbers {

		private int start = -1;

		private int end = -1;

		public int getStart() {
			return start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getEnd() {
			return end;
		}

		public void setEnd(int end) {
			this.end = end;
		}
	}
}
