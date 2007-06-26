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
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidator;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * {@link IValidator} implementation that is responsible for validating the
 * {@link IWebflowModel}.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class WebflowValidator extends AbstractValidator {

	public static final String VALIDATOR_ID = Activator.PLUGIN_ID
			+ ".validator";

	public static final String MARKER_ID = Activator.PLUGIN_ID
			+ ".problemmarker";

	public Set<IResource> getAffectedResources(IResource resource, int kind)
			throws CoreException {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (WebflowModelUtils.isWebflowConfig(resource)) {
			resources.add(resource);
		}
		else if (JdtUtils.isClassPathFile(resource)) {
			IWebflowProject webflowProject = Activator.getModel().getProject(
					resource.getProject());
			if (webflowProject != null) {
				for (IWebflowConfig webflowConfig : webflowProject.getConfigs()) {
					resources.add(webflowConfig.getElementResource());
				}
			}

		}
		return resources;
	}

	@Override
	protected String getMarkerId() {
		return MARKER_ID;
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
		Set<IResourceModelElement> contextElements = new HashSet<IResourceModelElement>();
		contextElements.add(rootElement);
		return contextElements;
	}

	@Override
	protected IValidationContext createContext(
			IResourceModelElement rootElement,
			IResourceModelElement contextElement) {
		if (rootElement instanceof IWebflowState) {
			IWebflowState state = (IWebflowState) rootElement;
			IWebflowConfig config = WebflowModelUtils
					.getWebflowConfig((IFile) state.getElementResource());
			return new WebflowValidationContext(state, config);
		}
		return null;
	}

	@Override
	protected boolean supports(IModelElement element) {
		return element instanceof IWebflowModelElement;
	}
}
