/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.resources.BeansResourceChangeListener;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.java.ITypeStructureCache;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.TypeStructureState;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationElementLifecycleManager;
import org.springframework.ide.eclipse.core.model.validation.IValidationElementLifecycleManagerExtension;
import org.springframework.ide.eclipse.core.model.validation.IValidator;

/**
 * {@link IValidator} implementation that is responsible for validating the {@link IBeansModelElement}s.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0
 */
public class BeansConfigValidator extends AbstractValidator {

	private Set<String> affectedBeans = new LinkedHashSet<String>();

	public Set<IResource> deriveResources(Object object) {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (object instanceof ISpringProject) {
			object = BeansCorePlugin.getModel().getProject(((ISpringProject) object).getProject());
		}
		else if (object instanceof IFile) {
			object = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId((IFile) object));
		}
		if (object instanceof IBeansModelElement) {
			if (object instanceof IBeansProject) {
				for (IBeansConfig config : ((IBeansProject) object).getConfigs()) {
					resources.add(config.getElementResource());
				}
			}
			else if (object instanceof IBeansConfigSet) {
				for (IBeansConfig config : ((IBeansConfigSet) object).getConfigs()) {
					resources.add(config.getElementResource());
				}
			}
			else if (object instanceof IResourceModelElement) {
				resources.add(((IResourceModelElement) object).getElementResource());
			}
		}
		return resources;
	}

	@Override
	public void cleanup(IResource resource, IProgressMonitor monitor) throws CoreException {
		MarkerUtils.deleteAllMarkers(resource, getMarkerId());
	}

	public Set<IResource> getAffectedResources(IResource resource, int kind, int deltaKind) throws CoreException {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (resource instanceof IFile) {

			// First check for a beans config file
			Set<IBeansConfig> configs = BeansCorePlugin.getModel().getConfigs(BeansConfigFactory.getConfigId((IFile) resource), true);
			if (configs != null && configs.size() > 0) {
				for (IBeansConfig beansConfig : configs) {
					// Resolve imported config files to their root importing one
					if (beansConfig instanceof IImportedBeansConfig) {
						IBeansConfig importingConfig = BeansModelUtils.getParentOfClass(beansConfig, IBeansConfig.class);
						if (importingConfig != null) {
							resources.add(importingConfig.getElementResource());
							addBeans(importingConfig);
						}
					}
					else {
						resources.add(resource);
						addBeans(beansConfig);
					}
				}

				// Add resources that are in a config set with the changed resources
				propagateChangedResourceToConfigSets(resources);

			}
			else if (BeansResourceChangeListener.requiresRefresh((IFile) resource)) {
				propagateChangedResourceToProject(resource, resources);
			}
			else if (kind != IncrementalProjectBuilder.FULL_BUILD) {

				// Now check for bean classes and java structure
				TypeStructureState structureState = getProjectContributorState().get(TypeStructureState.class);
				BeansTypeHierachyState hierachyState = getProjectContributorState().get(BeansTypeHierachyState.class);

				if (structureState == null
						|| structureState.hasStructuralChanges(resource, ITypeStructureCache.FLAG_ANNOTATION
								| ITypeStructureCache.FLAG_ANNOTATION_VALUE | ITypeStructureCache.FLAG_TAB_BITS)) {

					// Capture removal of java source files
					if (deltaKind == IResourceDelta.REMOVED
							&& resource.getName().endsWith(JdtUtils.JAVA_FILE_EXTENSION)) {
						propagateChangedResourceToProject(resource, resources);
					}
					else {
						for (IBean bean : hierachyState.getBeansByContainingTypes(resource)) {
							IBeansConfig beansConfig = BeansModelUtils.getConfig(bean);
							// Resolve imported config files to their root importing one
							if (beansConfig instanceof IImportedBeansConfig) {
								IBeansConfig importingConfig = BeansModelUtils.getParentOfClass(beansConfig,
										IBeansConfig.class);
								if (importingConfig != null) {
									resources.add(importingConfig.getElementResource());
									affectedBeans.add(bean.getElementID());
								}
							}
							else {
								resources.add(beansConfig.getElementResource());
								affectedBeans.add(bean.getElementID());
							}
							
							// capture all beans if configuration class has changed
							if (isConfigurationBean(bean)) {
								addBeans(beansConfig);
							}
						}
					}
				}
			}
		}
		return resources;
	}

	private boolean isConfigurationBean(IBean bean) {
		IType beanType = BeansModelUtils.resolveBeanType(bean);
		if (beanType != null) {
			try {
				for(IAnnotation annotation : beanType.getAnnotations()) {
					if ("Configuration".equals(annotation.getElementName())) {
						return true;
					}
				}
			} catch (JavaModelException e) {
				// ignore, no annotations can be found
			}
		}
		
		return false;
	}

	/**
	 * Propagates a change to a particular resource to its project so that all {@link IBeansConfig}s will get
	 * revalidated.
	 */
	private void propagateChangedResourceToProject(IResource resource, Set<IResource> resources) {
		IBeansProject beansProject = BeansCorePlugin.getModel().getProject(resource.getProject());
		if (beansProject != null) {
			for (IBeansConfig beansConfig : beansProject.getConfigs()) {
				resources.add(beansConfig.getElementResource());
				addBeans(beansConfig);
			}
		}
	}

	/**
	 * Add resources that share a config set to the list.
	 */
	private void propagateChangedResourceToConfigSets(Set<IResource> resources) {
		for (IResource resource : new HashSet<IResource>(resources)) {
			IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId((IFile) resource));
			for (IBeansConfigSet beansConfigSet : BeansModelUtils.getConfigSets(beansConfig)) {
				for (IBeansConfig bc : beansConfigSet.getConfigs()) {
					if (!resources.contains(bc.getElementResource())) {
						resources.add(bc.getElementResource());
						addBeans(bc);
					}
				}
			}
			for (IBeansProject beansProject : BeansCorePlugin.getModel().getProjects()) {
				for (IBeansConfig bc : beansProject.getConfigs()) {
					for (IBeansImport beansImport : bc.getImports()) {
						for (IImportedBeansConfig importedBeansConfig : beansImport.getImportedBeansConfigs()) {
							if (resource.equals(importedBeansConfig.getElementResource())) {
								if (!resources.contains(bc.getElementResource())) {
									resources.add(bc.getElementResource());
									addBeans(bc);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected IValidationContext createContext(IResourceModelElement rootElement, IResourceModelElement contextElement) {
		if (rootElement instanceof IBeansConfig) {
			return new BeansValidationContext((IBeansConfig) rootElement, contextElement);
		}
		return null;
	}

	@Override
	protected boolean supports(IModelElement element) {
		// Validate only those beans that have been changed
		if (element instanceof IBean) {
			if (affectedBeans.contains(element.getElementID())) {
				return true;
			}
			else if (((IBean) element).isInnerBean()) {
				return supports(BeansModelUtils.getParentOfClass(element, IBean.class));
			}
		}
		// Stop at imports because the contents is validated on the root config level
		else if (element instanceof IBeansModelElement || element instanceof IBeansImport) {
			return true;
		}
		return false;
	}

	private void addBeans(IBeansConfig beansConfig) {
		for (IBean bean : BeansModelUtils.getBeans(beansConfig)) {
			affectedBeans.add(bean.getElementID());
		}
	}

	@Override
	protected IValidationElementLifecycleManager createValidationElementLifecycleManager() {
		return new BeanElementLifecycleManager();
	}

	private static class BeanElementLifecycleManager implements IValidationElementLifecycleManagerExtension {

		private IBeansConfig rootElement = null;

		@SuppressWarnings("unused")
		private int kind = -1;

		/**
		 * {@inheritDoc}
		 */
		public void destroy() {
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<IResourceModelElement> getContextElements() {
			Set<IResourceModelElement> contextElements = new LinkedHashSet<IResourceModelElement>();
			contextElements.addAll(BeansModelUtils.getConfigSets(rootElement));
			if (contextElements.isEmpty()) {
				contextElements.add(rootElement);
			}
			return contextElements;
		}

		/**
		 * {@inheritDoc}
		 */
		public IResourceModelElement getRootElement() {
			return rootElement;
		}

		/**
		 * {@inheritDoc}
		 */
		public void init(IResource resource) {
			if (resource instanceof IFile) {
				rootElement = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId((IFile) resource));
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void setKind(int kind) {
			this.kind = kind;
		}
	}

}
