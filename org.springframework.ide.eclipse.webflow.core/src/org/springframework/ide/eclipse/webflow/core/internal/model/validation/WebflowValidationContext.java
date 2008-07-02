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
package org.springframework.ide.eclipse.webflow.core.internal.model.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidationContext;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.NamedNodeMap;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowValidationContext extends AbstractValidationContext {

	private final IWebflowConfig webflowConfig;

	private boolean isVersion1 = true;

	public WebflowValidationContext(IWebflowState state, IWebflowConfig webflowConfig) {
		super(state, null);
		this.webflowConfig = webflowConfig;
		determineVersion();
	}

	public IWebflowConfig getWebflowConfig() {
		return webflowConfig;
	}

	public boolean isVersion1() {
		return isVersion1;
	}

	private void determineVersion() {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getExistingModelForRead(
					getRootElement().getElementResource());
			if (model == null) {
				model = StructuredModelManager.getModelManager().getModelForRead(
						(IFile) getRootElement().getElementResource());

			}
			if (model != null) {
				IDOMDocument document = ((DOMModelImpl) model).getDocument();
				NamedNodeMap attributes = document.getDocumentElement().getAttributes();
				IDOMAttr schemaLocationNode = (IDOMAttr) attributes.getNamedItemNS(
						"http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
				String content = schemaLocationNode.getValue();
				isVersion1 = !content
						.contains("http://www.springframework.org/schema/webflow/spring-webflow-2.0.xsd");
			}
		}
		catch (Exception e) {
			if (model != null) {
				model.releaseFromRead();
			}
		}
	}

}
