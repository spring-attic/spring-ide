/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.core.contentassist.providers;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean.FactoryMethodContentAssistCalculator;
import org.springframework.ide.eclipse.config.core.contentassist.XmlBackedContentProposalProvider;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * An {@link XmlBackedContentProposalProvider} that uses
 * {@link FactoryMethodContentAssistCalculator} as its content assist
 * calculator.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public class FactoryMethodContentProposalProvider extends XmlBackedContentProposalProvider {

	private final String referenceNode;

	/**
	 * Constructs a content proposal provider for an XML attribute.
	 * 
	 * @param input the XML element to serve as the model for this proposal
	 * provider
	 * @param attrName the name of the attribute to compute proposals for
	 */
	public FactoryMethodContentProposalProvider(IDOMElement input, String attrName) {
		this(input, attrName, BeansSchemaConstants.ATTR_FACTORY_BEAN);
	}

	/**
	 * Constructs a content proposal provider for an XML attribute.
	 * 
	 * @param input the XML element to serve as the model for this proposal
	 * provider
	 * @param attrName the name of the attribute to compute proposals for
	 * @param referenceNode the name of the factory bean reference node
	 */
	public FactoryMethodContentProposalProvider(IDOMElement input, String attrName, String referenceNode) {
		super(input, attrName);
		this.referenceNode = referenceNode;
	}

	@Override
	protected IContentAssistCalculator createContentAssistCalculator() {
		return new FactoryMethodContentAssistCalculator() {
			@Override
			protected Node getFactoryBeanReferenceNode(NamedNodeMap attributes) {
				return attributes.getNamedItem(referenceNode);
			}
		};
	}

}
