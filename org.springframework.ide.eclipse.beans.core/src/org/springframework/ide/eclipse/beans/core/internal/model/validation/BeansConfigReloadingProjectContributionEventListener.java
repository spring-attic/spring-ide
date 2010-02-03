/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
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
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.java.ITypeStructureCache;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.TypeStructureState;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;
import org.springframework.ide.eclipse.core.project.IProjectContributionEventListener;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectContributionEventListenerAdapter;

/**
 * {@link IProjectContributionEventListener} implementation that handles resetting of {@link IBeansConfig}s based on
 * changes to the resource tree.
 * @author Christian Dupuis
 * @since 2.2.5
 */
public class BeansConfigReloadingProjectContributionEventListener extends ProjectContributionEventListenerAdapter {

	/** The annotation-config element */
	private static final String ANNOTATION_CONFIG_ELEMENT_NAME = "annotation-config";

	/** The component-scan element */
	private static final String COMPONENT_SCAN_ELEMENT_NAME = "component-scan";

	/** The context namespace URI */
	private static final String CONTEXT_NAMESPACE_URI = "http://www.springframework.org/schema/context";

	private TypeStructureState structureState = null;

	/** Internal cache of {@link IBeansConfig} instances that should be reloaded */
	private final Set<IBeansConfig> configs = new HashSet<IBeansConfig>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project,
			IProgressMonitor monitor) {
		// Get the type structure cache for this build
		structureState = state.get(TypeStructureState.class);
		if (structureState == null) {
			structureState = new TypeStructureState();
		}

		try {
			if (kind != IncrementalProjectBuilder.FULL_BUILD) {
				if (delta == null) {
					ResourceTreeVisitor visitor = new ResourceTreeVisitor();
					project.accept(visitor);
				}
				else {
					ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
					delta.accept(visitor);
				}
			}
			else {
				// Reset all for full clean build
				IBeansProject beansProject = BeansCorePlugin.getModel().getProject(project);
				if (beansProject != null) {
					configs.addAll(beansProject.getConfigs());
				}
			}
		}
		catch (CoreException e) {
			BeansCorePlugin.log(e);
		}

		// Trigger reloading and reload before validation infrastructure kicks in
		if (configs.size() > 0) {
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
			subMonitor.beginTask("Initializing Spring Model", configs.size());
			for (IBeansConfig config : configs) {
				subMonitor.subTask("Loading '" + config.getElementResource().getFullPath().toString().substring(1)
						+ "'");
				((BeansConfig) config).reload();
				config.getBeans();
				subMonitor.worked(1);
			}
			subMonitor.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish(int kind, IResourceDelta delta, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions, IProjectContributorState state, IProject project) {
		// Send update event
		if (configs.size() > 0) {
			((BeansModel) BeansCorePlugin.getModel()).notifyListeners(BeansCorePlugin.getModel().getProject(project),
					Type.CHANGED);
		}
	}

	/**
	 * Check if the given <code>resource</code> affects the {@link IBeansConfig}s.
	 * <p>
	 * If that is the case the {@link IBeansConfig} and all configs from {@link IBeansConfigSet}s are reset.
	 */
	private void checkResource(IResource resource) {
		// Only reset if the resource represents a Java source file and the types have structural changes
		if (resource.getName().endsWith(JdtUtils.JAVA_FILE_EXTENSION)
				&& structureState.hasStructuralChanges(resource, ITypeStructureCache.FLAG_ANNOTATION
						| ITypeStructureCache.FLAG_ANNOTATION_VALUE)) {

			// Reset configs that use component-scanning and annotation-config
			for (IBeansProject beansProject : BeansCorePlugin.getModel().getProjects()) {
				if (JdtUtils.isJavaProject(beansProject.getProject())
						&& JdtUtils.getJavaProject(beansProject.getProject()).isOnClasspath(resource)) {
					for (IBeansConfig config : beansProject.getConfigs()) {
						for (IBeansComponent component : config.getComponents()) {
							if (component.getElementSourceLocation() instanceof XmlSourceLocation) {
								XmlSourceLocation location = (XmlSourceLocation) component.getElementSourceLocation();
								if (COMPONENT_SCAN_ELEMENT_NAME.equals(location.getLocalName())
										&& CONTEXT_NAMESPACE_URI.equals(location.getNamespaceURI())) {
									propagateToConfigsFromConfigSet(config, false);
								}
								else if (ANNOTATION_CONFIG_ELEMENT_NAME.equals(location.getLocalName())
										&& CONTEXT_NAMESPACE_URI.equals(location.getNamespaceURI())) {
									propagateToConfigsFromConfigSet(config, false);
								}
							}
						}
					}
				}
			}
		}
		else if (BeansCoreUtils.isBeansConfig(resource, true)) {
			IBeansConfig bc = BeansCorePlugin.getModel().getConfig((IFile) resource, true);
			// Resources are loaded by isBeansConfig from the top; so the resourceChanged check here is not sufficient
			if (bc.resourceChanged()) {
				if (bc instanceof IImportedBeansConfig) {
					propagateToConfigsFromConfigSet(BeansModelUtils.getParentOfClass(bc, BeansConfig.class), false);
				}
				else {
					propagateToConfigsFromConfigSet(bc, false);
				}
			}
			else {
				propagateToConfigsFromConfigSet(bc, true);
			}
		}
	}

	private void propagateToConfigsFromConfigSet(IBeansConfig config, boolean onlyImportsCheck) {
		// Add config to make sure that in case on config set is configured
		if (!onlyImportsCheck) {
			configs.add(config);
		}

		for (IBeansProject beansProject : BeansCorePlugin.getModel().getProjects()) {
			if (!onlyImportsCheck) {
				for (IBeansConfigSet configSet : beansProject.getConfigSets()) {
					if (configSet.hasConfig((IFile) config.getElementResource())) {
						configs.addAll(configSet.getConfigs());
					}
				}
			}
			for (IBeansConfig bc : beansProject.getConfigs()) {
				for (IBeansImport beansImport : bc.getImports()) {
					for (IImportedBeansConfig importedBeansConfig : beansImport.getImportedBeansConfigs()) {
						if (config.getElementResource().equals(importedBeansConfig.getElementResource())) {
							configs.add(bc);
						}
					}
				}
			}
		}
	}

	/**
	 * Create a list of affected resources from a resource delta.
	 */
	class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		private Set<IResource> resources;

		public ResourceDeltaVisitor() {
			this.resources = new LinkedHashSet<IResource>();
		}

		public Set<IResource> getResources() {
			return resources;
		}

		public boolean visit(IResourceDelta aDelta) throws CoreException {
			boolean visitChildren = false;

			IResource resource = aDelta.getResource();
			if (resource instanceof IProject) {

				// Only check projects with Spring beans nature
				visitChildren = SpringCoreUtils.isSpringProject(resource);
			}
			else if (resource instanceof IFolder) {
				visitChildren = true;
			}
			else if (resource instanceof IFile) {
				switch (aDelta.getKind()) {
				case IResourceDelta.ADDED:
				case IResourceDelta.CHANGED:
					checkResource(resource);
					visitChildren = true;
					break;

				case IResourceDelta.REMOVED:
					checkResource(resource);
					break;
				}
			}
			return visitChildren;
		}
	}

	/**
	 * Create a list of affected resources from a resource tree.
	 */
	class ResourceTreeVisitor implements IResourceVisitor {

		private Set<IResource> resources;

		public ResourceTreeVisitor() {
			this.resources = new LinkedHashSet<IResource>();
		}

		public Set<IResource> getResources() {
			return resources;
		}

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				checkResource(resource);
			}
			return true;
		}
	}
}
