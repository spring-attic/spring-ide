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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
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

	@Override
	protected Set<ValidationRuleDefinition> getRuleDefinitions(
			IResource resource) {
		return ValidationRuleDefinitionFactory.getEnabledRuleDefinitions(
				VALIDATOR_ID, resource.getProject());
	}

	@Override
	protected IResourceModelElement getRootElement(IResource resource) {
		if (resource instanceof IFile) {
			return WebflowModelUtils.getWebflowState((IFile) resource);
		}
		return null;
	}

	@Override
	protected Set<IResourceModelElement> getContextElements(
			IResourceModelElement rootElement) {
		Set<IResourceModelElement> contextElements =
				new HashSet<IResourceModelElement>();
		contextElements.add(rootElement);
		return contextElements;
	}

	@Override
	protected IValidationContext createContext(
			IResourceModelElement rootElement,
			IResourceModelElement contextElement) {
		if (rootElement instanceof IWebflowState) {
			IWebflowState state = (IWebflowState) rootElement;
			IWebflowConfig config = WebflowModelUtils.getWebflowConfig(
					(IFile) state.getElementResource());
			return new WebflowValidationContext(state, config);
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
