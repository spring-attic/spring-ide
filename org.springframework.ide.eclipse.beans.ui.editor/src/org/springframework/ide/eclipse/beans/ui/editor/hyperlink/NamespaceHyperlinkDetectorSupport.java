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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Support class for implementing custom {@link IHyperlinkDetector}s.
 * Calculation of individual hyperlinks is done via {@link IHyperlinkCalculator}
 * strategy interfaces respectively.
 * <p>
 * Provides the {@link #registerHyperlinkCalculator} methods for registering a
 * {@link IHyperlinkCalculator} to handle a specific element.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class NamespaceHyperlinkDetectorSupport extends
		AbstractHyperlinkDetector {

	/**
	 * Stores the {@link IHyperlinkCalculator} keyed the return value of a call
	 * to {@link #createRegisteredName(String, String)}.
	 */
	private Map<String, IHyperlinkCalculator> calculators = new HashMap<String, IHyperlinkCalculator>();

	/**
	 * Calculates hyperlink for the given request by delegating the request to a
	 * located {@link IHyperlinkCalculator} returned by
	 * {@link #locateHyperlinkCalculator(String, String)}.
	 */
	public final IHyperlink createHyperlink(String name, String target,
			Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		IHyperlinkCalculator calculator = locateHyperlinkCalculator(node
				.getLocalName(), name);
		if (calculator != null) {
			return calculator.createHyperlink(name, target, node, parentNode,
					document, textViewer, hyperlinkRegion, cursor);
		}
		return null;
	}

	/**
	 * Creates a name from the <code>nodeName</code> and
	 * <code>attributeName</code>.
	 * @param nodeName the local (non-namespace qualified) name of the element
	 * @param attributeName the local (non-namespace qualified) name of the
	 * attribute
	 */
	protected String createRegisteredName(String nodeName, String attributeName) {
		StringBuilder builder = new StringBuilder();
		if (StringUtils.hasText(nodeName)) {
			builder.append("/nodeName=");
			builder.append(nodeName);
		}
		else {
			builder.append("/nodeName=");
			builder.append("*");
		}
		if (StringUtils.hasText(attributeName)) {
			builder.append("/attribute=");
			builder.append(attributeName);
		}
		return builder.toString();
	}

	/**
	 * Empty implementation. To be overridden by subclasses.
	 */
	public void init() {
	}

	/**
	 * Checks if a {@link IHyperlinkCalculator} for the given attribute has been
	 * registered.
	 */
	public final boolean isLinkableAttr(Attr attr) {
		return locateHyperlinkCalculator(attr.getOwnerElement().getLocalName(),
				attr.getLocalName()) != null;
	}

	/**
	 * Locates a {@link IContentAssistCalculator} in the {@link #calculators}
	 * store for the given <code>nodeName</code> and
	 * <code>attributeName</code>.
	 */
	private IHyperlinkCalculator locateHyperlinkCalculator(String nodeName,
			String attributeName) {
		String key = createRegisteredName(nodeName, attributeName);
		if (this.calculators.containsKey(key)) {
			return this.calculators.get(key);
		}
		key = createRegisteredName("*", attributeName);
		if (this.calculators.containsKey(key)) {
			return this.calculators.get(key);
		}
		return null;
	}

	/**
	 * Subclasses can call this to register the supplied
	 * {@link IHyperlinkCalculator} to handle the specified attribute. The
	 * attribute name is the local (non-namespace qualified) name.
	 */
	protected void registerHyperlinkCalculator(String attributeName,
			IHyperlinkCalculator calculator) {
		registerHyperlinkCalculator(null, attributeName, calculator);
	}

	/**
	 * Subclasses can call this to register the supplied
	 * {@link IHyperlinkCalculator} to handle the specified attribute
	 * <b>only</b> for a given element. The attribute name is the local
	 * (non-namespace qualified) name.
	 */
	protected void registerHyperlinkCalculator(String nodeName,
			String attributeName, IHyperlinkCalculator calculator) {
		this.calculators.put(createRegisteredName(nodeName, attributeName),
				calculator);
	}
}
