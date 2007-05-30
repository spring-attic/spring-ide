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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springframework.ide.eclipse.core.model.validation.IValidator;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * {@link IWorkspaceRunnable} that triggers validation of a single
 * {@link IWebflowConfig}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowValidator implements IValidator {

	private Set<ValidationProblem> problems = new HashSet<ValidationProblem>();

	public static final String VALIDATOR_ID = Activator.PLUGIN_ID
			+ ".validator";

	public void cleanup(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		try {
			if (resource instanceof IFile
					&& WebflowModelUtils.isWebflowConfig(((IFile) resource))) {
				monitor.subTask("Deleting Spring Web Flow problem markers ["
						+ resource.getFullPath().toString().substring(1) + "]");
				WebflowModelUtils.deleteProblemMarkers(resource);
			}
		}
		finally {
			monitor.done();
		}
	}

	public Set<IResource> getAffectedResources(IResource resource, int kind)
			throws CoreException {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (WebflowModelUtils.isWebflowConfig(resource)) {
			resources.add(resource);
		}
		return resources;
	}

	protected void validate(IFile file, IProgressMonitor monitor) {

		monitor.subTask("Validating Spring Web Flow file ["
				+ file.getFullPath().toString() + "]");

		WebflowModelUtils.deleteProblemMarkers(file);

		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager()
					.getExistingModelForRead(file);
			if (model == null) {
				model = StructuredModelManager.getModelManager()
						.getModelForRead(file);

			}
			if (model != null) {
				IDOMDocument document = ((DOMModelImpl) model).getDocument();
				IWebflowState webflowState = new WebflowState(WebflowModelUtils
						.getWebflowConfig(file));
				webflowState.init((IDOMNode) document.getDocumentElement(),
						null);

				WebflowValidationContext validationContext = new WebflowValidationContext(
						file, webflowState, WebflowModelUtils
								.getWebflowConfig(file));
				WebflowValidationVisitor validationVisitor = new WebflowValidationVisitor(
						validationContext, ValidationRuleDefinitionFactory
								.getEnabledRuleDefinitions(VALIDATOR_ID, file
										.getProject()));
				webflowState.accept(validationVisitor, monitor);
				this.problems = validationContext.getProblems();

				if (!monitor.isCanceled()) {
					WebflowModelUtils.createMarkerFromProblemReporter(
							this.problems, file);
				}
			}
		}
		catch (Exception e) {
			Activator.log(e);
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
			monitor.done();
		}
	}

	public void validate(Set<IResource> affectedResources,
			IProgressMonitor monitor) throws CoreException {
		for (IResource affectedResource : affectedResources) {
			if (affectedResource instanceof IFile) {
				validate((IFile) affectedResource, monitor);
			}
		}
	}

	public Set<ValidationProblem> getProblems() {
		return this.problems;
	}

}
