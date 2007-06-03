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
package org.springframework.ide.eclipse.beans.core.internal.model.validation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;

/**
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeansConfigValidator extends AbstractValidator {

	public static final String VALIDATOR_ID = BeansCorePlugin.PLUGIN_ID
			+ ".beansvalidator";

	public Set<IResource> getAffectedResources(IResource resource,
			int kind) throws CoreException {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (resource instanceof IFile) {

			// First check for a beans config file
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					(IFile) resource);
			if (config != null) {
				resources.add(resource);
			}
			else {

				// Now check for a bean class or source file
				IJavaElement element = JavaCore.create(resource);
				if (element != null && element.exists()) {
					if (element instanceof IClassFile) {
						IType type = ((IClassFile) element).getType();
						resources.addAll(getBeanConfigResources(type));
					}
					else if (element instanceof ICompilationUnit) {
						for (IType type : ((ICompilationUnit) element)
								.getTypes()) {
							resources.addAll(getBeanConfigResources(type));
						}
					}
				}
			}
		}
		return resources;
	}

	public void cleanup(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		if (resource instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
				(IFile) resource);
			if (config != null) {
				BeansModelUtils.deleteProblemMarkers(config);
			}
		}
	}

	@Override
	protected Set<ValidationRuleDefinition> getRuleDefinitions(
			IResource resource) {
		return ValidationRuleDefinitionFactory.getEnabledRuleDefinitions(
				VALIDATOR_ID, resource.getProject());
	}

	@Override
	protected Set<IResourceModelElement> getRootElements(IResource resource) {
		Set<IResourceModelElement> rootElements =
				new LinkedHashSet<IResourceModelElement>();
		IBeansConfig config = BeansCorePlugin.getModel().getConfig(
				(IFile) resource);
		if (config != null) {
			rootElements.addAll(BeansModelUtils.getConfigSets(config));
			if (rootElements.isEmpty()) {
				rootElements.add(config);
			}
		}
		return rootElements;
	}

	@Override
	protected IValidationContext createContext(IResource resource,
			IResourceModelElement rootElement) {
		if (resource instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel()
					.getConfig((IFile) resource);
			if (config != null) {
				return new BeansValidationContext(config, rootElement);
			}
		}
		return null;
	}

	@Override
	protected boolean supports(IModelElement element) {
		return (element instanceof IBeansModelElement);
	}

	@Override
	protected void createProblemMarker(IResource resource,
			ValidationProblem problem) {
//		WebflowModelUtils.createProblemMarker(resource, problem.getMessage(),
//				problem.getSeverity(), problem.getLine());
	}

	private List<IResource> getBeanConfigResources(IType beanClass) {
		List<IResource> resources = new ArrayList<IResource>();
		String className = beanClass.getFullyQualifiedName();
		for (IBeansConfig config : BeansCorePlugin.getModel().getConfigs(
				className)) {
			resources.add(config.getElementResource());
		}
		return resources;
	}
}
