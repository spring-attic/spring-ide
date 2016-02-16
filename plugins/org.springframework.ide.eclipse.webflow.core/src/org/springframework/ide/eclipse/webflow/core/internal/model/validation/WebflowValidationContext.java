/*******************************************************************************
 * Copyright (c) 2007 - 2013 Spring IDE Developers
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
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidationContext;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowState;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.util.StringUtils;
import org.w3c.dom.NamedNodeMap;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
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
				isVersion1 = content.contains("spring-webflow-1");
			}
		}
		catch (Exception e) {
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
			model = null;
		}
	}

	public IState getStateFromParentState(String stateId) {
		if (!isVersion1() && stateId != null && stateId.contains("#")) {
			IStructuredModel model = null;
			IState state = null;
			int i = stateId.lastIndexOf('#');
			String parentFlowId = stateId.substring(0, i);
			String parentStateId = stateId.substring(i + 1);

			IWebflowProject project = Activator.getModel().getProject(
					getRootElement().getElementResource().getProject());
			IWebflowConfig parentConfig = project.getConfig(parentFlowId);
			try {
				model = StructuredModelManager.getModelManager().getExistingModelForRead(
						parentConfig.getElementResource());
				if (model == null) {
					model = StructuredModelManager.getModelManager().getModelForRead(
							(IFile) parentConfig.getElementResource());
				}
				if (model != null) {
					IDOMDocument document = ((DOMModelImpl) model).getDocument();
					IWebflowState parentState = new WebflowState(parentConfig);
					parentState.init((IDOMNode) document.getDocumentElement(), null);

					state = WebflowModelXmlUtils.getStateById(parentState, parentStateId);
				}
			}
			catch (Exception e) {
			}
			finally {
				if (model != null) {
					model.releaseFromRead();
				}
				model = null;
			}

			if (state != null) {
				return state;
			}

		}
		return null;
	}

	public IState getStateFromParentFlow(String stateId, IWebflowConfig config) {
		if (!isVersion1()) {
			IStructuredModel model = null;
			String parent = null;
			try {
				model = StructuredModelManager.getModelManager().getExistingModelForRead(config.getElementResource());
				if (model == null) {
					model = StructuredModelManager.getModelManager().getModelForRead(
							(IFile) config.getElementResource());

				}
				if (model != null) {
					IDOMDocument document = ((DOMModelImpl) model).getDocument();
					NamedNodeMap attributes = document.getDocumentElement().getAttributes();
					if (attributes.getNamedItem("parent") != null) {
						IDOMAttr schemaLocationNode = (IDOMAttr) attributes.getNamedItem("parent");
						parent = schemaLocationNode.getValue();
					}
				}
			}
			catch (Exception e) {
			}
			finally {
				if (model != null) {
					model.releaseFromRead();
				}
				model = null;
			}

			if (parent != null) {
				for (Object p : StringUtils.commaDelimitedListToSet(parent)) {
					IState state = null;

					IWebflowProject project = Activator.getModel().getProject(
							getRootElement().getElementResource().getProject());
					IWebflowConfig parentConfig = project.getConfig((String) p);
					try {
						model = StructuredModelManager.getModelManager().getExistingModelForRead(
								parentConfig.getElementResource());
						if (model == null) {
							model = StructuredModelManager.getModelManager().getModelForRead(
									(IFile) parentConfig.getElementResource());
						}
						if (model != null) {
							IDOMDocument document = ((DOMModelImpl) model).getDocument();
							IWebflowState parentState = new WebflowState(parentConfig);
							parentState.init((IDOMNode) document.getDocumentElement(), null);

							state = WebflowModelXmlUtils.getStateById(parentState, stateId);
						}
					}
					catch (Exception e) {
					}
					finally {
						if (model != null) {
							model.releaseFromRead();
						}
						model = null;
					}

					if (state != null) {
						return state;
					}
					else {
						return getStateFromParentFlow(stateId, parentConfig);
					}
				}
			}
		}
		return null;
	}

}
