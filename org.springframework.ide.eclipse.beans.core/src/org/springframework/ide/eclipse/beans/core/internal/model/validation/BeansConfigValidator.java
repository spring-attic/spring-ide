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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationElementLifecycleManager;
import org.springframework.ide.eclipse.core.model.validation.IValidator;

/**
 * {@link IValidator} implementation that is responsible for validating the
 * {@link IBeansModelElement}s.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansConfigValidator extends AbstractValidator {

	public Set<IResource> deriveResources(Object object) {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (object instanceof ISpringProject) {
			object = BeansCorePlugin.getModel().getProject(
					((ISpringProject) object).getProject());
		}
		else if (object instanceof IFile) {
			object = BeansCorePlugin.getModel().getConfig((IFile) object);
		}
		if (object instanceof IBeansModelElement) {
			if (object instanceof IBeansProject) {
				for (IBeansConfig config : ((IBeansProject) object)
						.getConfigs()) {
					resources.add(config.getElementResource());
				}
			}
			else if (object instanceof IBeansConfigSet) {
				for (IBeansConfig config : ((IBeansConfigSet) object)
						.getConfigs()) {
					resources.add(config.getElementResource());
				}
			}
			else if (object instanceof IResourceModelElement) {
				resources.add(((IResourceModelElement) object)
						.getElementResource());
			}
		}
		return resources;
	}

	@Override
	public void cleanup(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		MarkerUtils.deleteAllMarkers(resource, getMarkerId());
	}

	public Set<IResource> getAffectedResources(IResource resource, int kind)
			throws CoreException {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (resource instanceof IFile) {

			// First check for a beans config file
			Set<IBeansConfig> configs = BeansCorePlugin.getModel().getConfigs(
					(IFile) resource, true);
			if (configs != null && configs.size() > 0) {
				for (IBeansConfig config : configs) {
					// Resolve imported config files to their root importing one
					if (config instanceof IImportedBeansConfig) {
						IBeansConfig importingConfig = BeansModelUtils
								.getParentOfClass(config, BeansConfig.class);
						if (importingConfig != null) {
							resources.add(importingConfig.getElementResource());
						}
					}
					else {
						resources.add(resource);
					}
				}
			}
			else if (JdtUtils.isClassPathFile(resource)) {
				IBeansProject beansProject = BeansCorePlugin.getModel()
						.getProject(resource.getProject());
				if (beansProject != null) {
					for (IBeansConfig beansConfig : beansProject.getConfigs()) {
						resources.add(beansConfig.getElementResource());
					}
				}
			}
			else {
				// Now check for bean classes and java structure
				for (IBeansConfig config :BeansModelUtils.getConfigsByContainingTypes(resource)) {
					resources.add(config.getElementResource());
				}
			}
		}
		return resources;
	}

	@Override
	protected IValidationContext createContext(
			IResourceModelElement rootElement,
			IResourceModelElement contextElement) {
		if (rootElement instanceof IBeansConfig) {
			return new BeansValidationContext((IBeansConfig) rootElement,
					contextElement);
		}
		return null;
	}

	@Override
	protected boolean supports(IModelElement element) {
		// Stop at imports because the contents is validated on the root config level
		return (element instanceof IBeansModelElement || element instanceof IBeansImport);
	}

	@Override
	protected IValidationElementLifecycleManager createValidationElementLifecycleManager() {
		return new BeanElementLifecycleManager();
	}

	private static class BeanElementLifecycleManager implements
			IValidationElementLifecycleManager {

		private IResourceModelElement rootElement = null;

		public void destory() {
			// nothing to do
		}

		public Set<IResourceModelElement> getContextElements() {
			Set<IResourceModelElement> contextElements = new LinkedHashSet<IResourceModelElement>();
			if (rootElement instanceof IBeansConfig) {
				contextElements.addAll(BeansModelUtils
						.getConfigSets(rootElement));
				if (contextElements.isEmpty()) {
					contextElements.add(rootElement);
				}
			}
			return contextElements;
		}

		public IResourceModelElement getRootElement() {
			return rootElement;
		}

		public void init(IResource resource) {
			if (resource instanceof IFile) {
				rootElement = BeansCorePlugin.getModel().getConfig(
						(IFile) resource);
			}

		}
	}
}
