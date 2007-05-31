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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowValidator extends AbstractValidator {

	public static final String VALIDATOR_ID = Activator.PLUGIN_ID
			+ ".validator";

	public Set<IResource> getAffectedResources(IResource resource, int kind)
			throws CoreException {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (WebflowModelUtils.isWebflowConfig(resource)) {
			resources.add(resource);
		}
		return resources;
	}

	public void cleanup(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		if (resource instanceof IFile
				&& WebflowModelUtils.isWebflowConfig(((IFile) resource))) {
			monitor.subTask("Deleting problem markers from '"
					+ resource.getFullPath().toString().substring(1) + "'");
			WebflowModelUtils.deleteProblemMarkers(resource);
		}
	}

	protected String getId() {
		return VALIDATOR_ID;
	}

	protected IValidationContext createContext(IResource resource) {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			IStructuredModel model = null;
			try {
				model = StructuredModelManager.getModelManager()
						.getExistingModelForRead(resource);
				if (model == null) {
					model = StructuredModelManager.getModelManager()
							.getModelForRead(file);

				}
				if (model != null) {
					IDOMDocument document = ((DOMModelImpl) model)
							.getDocument();
					IWebflowState webflowState = new WebflowState(
							WebflowModelUtils.getWebflowConfig(file));
					webflowState.init((IDOMNode) document.getDocumentElement(),
							null);

					return new WebflowValidationContext(webflowState,
							WebflowModelUtils.getWebflowConfig(file));
				}
			}
			catch (Exception e) {
				Activator.log(e);
			}
			finally {
				if (model != null) {
					model.releaseFromRead();
				}
			}
		}
		return null;
	}

	@Override
	protected boolean supports(IModelElement element) {
		return element instanceof IWebflowModelElement;
	}

	@Override
	protected void createProblemMarker(IResource resource,
			ValidationProblem problem) {
		WebflowModelUtils.createProblemMarker(resource, problem.getMessage(),
				problem.getSeverity(), problem.getLine());
	}
}
