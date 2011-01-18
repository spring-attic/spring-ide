/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * Support class for implementing custom {@link IContentAssistProcessor}. Calculation of individual
 * content assist proposals is done via {@link IContentAssistCalculator} strategy interfaces
 * respectively.
 * <p>
 * Provides the {@link #registerContentAssistCalculator} methods for registering a
 * {@link IContentAssistCalculator} to handle a specific element.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public abstract class NamespaceContentAssistProcessorSupport extends AbstractContentAssistProcessor
		implements INamespaceContentAssistProcessor {

	/**
	 * Stores the {@link IContentAssistCalculator} keyed the return value of a call to
	 * {@link #createRegisteredName(String, String)}.
	 */
	private Map<String, IContentAssistCalculator> calculators = new HashMap<String, IContentAssistCalculator>();

	/**
	 * Empty implementation. Can be overridden by subclasses.
	 */
	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request, String prefix,
			String namespace, String namespacePrefix, Node attributeNode) {
		// no-op
	}

	/**
	 * Empty implementation. Can be overridden by subclasses.
	 */
	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request, IDOMNode node) {
		// no-op
	}

	/**
	 * Calculates content assist proposals for the given request by delegating the request to a
	 * located {@link IContentAssistCalculator} returned by
	 * {@link #locateContentAssistCalculator(String, String)}.
	 * <p>
	 * After delegating the calculation to a {@link IContentAssistCalculator} this implementation
	 * calls {@link #postComputeAttributeValueProposals} to allow for custom post processing.
	 */
	@Override
	protected final void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName, String namespace, String prefix) {
		if (matchString == null) {
			matchString = "";
		}

		String parentNodeName = null;
		String parentNamespaceUri = null;
		IDOMNode parentNode = (IDOMNode) node.getParentNode();
		if (parentNode != null) {
			parentNodeName = parentNode.getLocalName();
			parentNamespaceUri = parentNode.getNamespaceURI();
		}
		
		// make sure the for old-style DTDs we assume the default namespace
		if (parentNamespaceUri == null) {
			parentNamespaceUri = NamespaceUtils.DEFAULT_NAMESPACE_URI;
		}
		
		IContentAssistCalculator calculator = locateContentAssistCalculator(parentNamespaceUri,
				parentNodeName, node.getLocalName(), attributeName);

		IContentAssistContext context = new DefaultContentAssistContext(request, attributeName,
				matchString);
		IContentAssistProposalRecorder recorder = new DefaultContentAssistProposalRecorder(request);

		if (calculator != null) {
			calculator.computeProposals(context, recorder);
		}
		postComputeAttributeValueProposals(request, node, matchString, attributeName, namespace,
				prefix);
	}

	/**
	 * Template method called after delegating the content assist request to a stored
	 * {@link IContentAssistCalculator}. This method can be overridden by subclasses to allow custom
	 * handling of requests.
	 * @param request the content assist request
	 * @param node the current node
	 * @param matchString the string already entered by the user prior to triggering the content
	 * assist request
	 * @param attributeName the name of the attribute
	 * @param namespace the namespace of the attribute
	 * @param prefix the namespace prefix of the attribute
	 */
	protected void postComputeAttributeValueProposals(ContentAssistRequest request, IDOMNode node,
			String matchString, String attributeName, String namespace, String prefix) {
	}

	/**
	 * Locates a {@link IContentAssistCalculator} in the {@link #calculators} store for the given
	 * <code>nodeName</code> and <code>attributeName</code>.
	 */
	private IContentAssistCalculator locateContentAssistCalculator(String parentNamespaceUri,
			String parentNodeName, String nodeName, String attributeName) {
		String key = createRegisteredName(parentNamespaceUri, parentNodeName, nodeName,
				attributeName);
		if (this.calculators.containsKey(key)) {
			return this.calculators.get(key);
		}
		key = createRegisteredName(null, null, nodeName, attributeName);
		if (this.calculators.containsKey(key)) {
			return this.calculators.get(key);
		}
		key = createRegisteredName(null, null, null, attributeName);
		if (this.calculators.containsKey(key)) {
			return this.calculators.get(key);
		}
		return null;
	}

	/**
	 * Creates a name from the <code>nodeName</code> and <code>attributeName</code>.
	 * @param parentNamespaceUri the namespace uri of the parent node
	 * @param parentNodeName the local name of the parent node
	 * @param nodeName the local (non-namespace qualified) name of the element
	 * @param attributeName the local (non-namespace qualified) name of the attribute
	 */
	protected String createRegisteredName(String parentNamespaceUri, String parentNodeName,
			String nodeName, String attributeName) {
		StringBuilder builder = new StringBuilder();
		if (StringUtils.hasText(parentNamespaceUri)) {
			builder.append("/parentNamespaceUri=");
			builder.append(parentNamespaceUri);
		}
		else {
			builder.append("/parentNamespaceUri=");
			builder.append("*");
		}
		if (StringUtils.hasText(parentNodeName)) {
			builder.append("/parentNodeName=");
			builder.append(parentNodeName);
		}
		else {
			builder.append("/parentNodeName=");
			builder.append("*");
		}
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
	 * Subclasses can call this to register the supplied {@link IContentAssistCalculator} to handle
	 * the specified attribute. The attribute name is the local (non-namespace qualified) name.
	 */
	protected void registerContentAssistCalculator(String attributeName,
			IContentAssistCalculator calculator) {
		registerContentAssistCalculator(null, null, null, attributeName, calculator);
	}

	/**
	 * Subclasses can call this to register the supplied {@link IContentAssistCalculator} to handle
	 * the specified attribute. The attribute name is the local (non-namespace qualified) name.
	 */
	protected void registerContentAssistCalculator(String nodeName, String attributeName,
			IContentAssistCalculator calculator) {
		registerContentAssistCalculator(null, null, nodeName, attributeName, calculator);
	}

	/**
	 * Subclasses can call this to register the supplied {@link IContentAssistCalculator} to handle
	 * the specified attribute <b>only</b> for a given element. The attribute name is the local
	 * (non-namespace qualified) name.
	 */
	protected void registerContentAssistCalculator(String parentNamespaceUri,
			String parentNodeName, String nodeName, String attributeName,
			IContentAssistCalculator calculator) {
		this.calculators.put(createRegisteredName(parentNamespaceUri, parentNodeName, nodeName,
				attributeName), calculator);
	}

}
