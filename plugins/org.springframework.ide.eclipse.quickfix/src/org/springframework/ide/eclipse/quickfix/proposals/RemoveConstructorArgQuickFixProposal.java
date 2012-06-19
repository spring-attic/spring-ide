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
package org.springframework.ide.eclipse.quickfix.proposals;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Quick fix proposal for removing <constructor-arg> from a bean
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class RemoveConstructorArgQuickFixProposal extends BeanAttributeQuickFixProposal {

	private final int numAdditionalParams;

	private final IDOMNode beanNode;

	private final String label;

	public RemoveConstructorArgQuickFixProposal(int offset, int length, boolean missingEndQuote,
			int numAdditionalParams, IDOMNode beanNode, String label) {
		super(offset, length, missingEndQuote);
		this.numAdditionalParams = numAdditionalParams;
		this.beanNode = beanNode;
		this.label = label;
	}

	@Override
	public void applyQuickFix(IDocument document) {
		IStructuredModel model = null;
		try {
			if (document instanceof IStructuredDocument) {
				model = StructuredModelManager.getModelManager().getModelForEdit((IStructuredDocument) document);
				model.beginRecording(this);
			}

			NodeList childNodes = beanNode.getChildNodes();
			int numRemoved = 0;
			for (int i = childNodes.getLength() - 1; i >= 0; i--) {
				Node child = childNodes.item(i);
				String nodeName = child.getNodeName();
				if (nodeName != null && nodeName.equals(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG)) {
					beanNode.removeChild(child);
					numRemoved++;
					if (numRemoved >= numAdditionalParams) {
						break;
					}
				}
			}

			new FormatProcessorXML().formatNode(beanNode);
		}

		finally {
			if (model != null) {
				model.endRecording(this);
				model.releaseFromEdit();
				model = null;
			}
		}
	}

	public String getDisplayString() {
		return label;
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_REMOVE);
	}

}
