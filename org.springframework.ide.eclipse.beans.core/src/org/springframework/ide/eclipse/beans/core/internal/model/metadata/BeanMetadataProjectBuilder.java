/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansTypeHierachyState;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadataModel;
import org.springframework.ide.eclipse.core.java.ITypeStructureCache;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.TypeStructureState;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.project.IProjectContributorStateAware;

/**
 * {@link IProjectBuilder} that triggers the creation and lifecycle of {@link IBeanMetadata} stored
 * in the {@link IBeanMetadataModel}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataProjectBuilder implements IProjectBuilder, IProjectContributorStateAware {

	/** Internal state */
	private IProjectContributorState context = null;

	/** Map of affected beans that need re-processing */
	private Map<IBeansConfig, Set<IBean>> affectedBeans = new HashMap<IBeansConfig, Set<IBean>>();

	/**
	 * {@inheritDoc}
	 */
	public void build(Set<IResource> affectedResources, int kind, IProgressMonitor monitor)
			throws CoreException {
		monitor.subTask("Attaching Spring meta data");
		if (affectedResources.size() > 0) {
			Job job = new BeanMetadataBuilderJob(affectedBeans);
			job.schedule();
		}
		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanup(IResource resource, IProgressMonitor monitor) throws CoreException {
		if (BeansCoreUtils.isBeansConfig(resource) && resource instanceof IFile) {
			IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
			for (IBean bean : beansConfig.getBeans()) {
				BeansCorePlugin.getMetadataModel().clearBeanMetadata(bean);
				BeansCorePlugin.getMetadataModel().clearBeanProperties(bean);
			}
			// Notify that the model has changed.
			// ((BeansModel) BeansCorePlugin.getModel()).notifyListeners(beansConfig, Type.CHANGED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IResource> getAffectedResources(IResource resource, int kind) throws CoreException {
		Set<IResource> resources = new HashSet<IResource>();

		if (kind != IncrementalProjectBuilder.FULL_BUILD && resource instanceof IFile
				&& resource.getName().endsWith(JdtUtils.JAVA_FILE_EXTENSION)) {

			// Make sure that only a structural change to a java source file triggers a rebuild
			TypeStructureState structureManager = context.get(TypeStructureState.class);
			BeansTypeHierachyState hierachyManager = context.get(BeansTypeHierachyState.class);

			if (structureManager == null
					|| structureManager.hasStructuralChanges(resource,
							ITypeStructureCache.FLAG_ANNOTATION
									| ITypeStructureCache.FLAG_ANNOTATION_VALUE)) {
				for (IBean bean : hierachyManager.getBeansByContainingTypes(resource)) {
					IBeansConfig beansConfig = BeansModelUtils.getConfig(bean);
					resources.add(beansConfig.getElementResource());
					if (affectedBeans.containsKey(beansConfig)) {
						affectedBeans.get(beansConfig).add(bean);
					}
					else {
						Set<IBean> beans = new LinkedHashSet<IBean>();
						beans.add(bean);
						affectedBeans.put(beansConfig, beans);
					}
				}
			}
		}
		else if (BeansCoreUtils.isBeansConfig(resource, true)) {
			IBeansConfig beansConfig = (IBeansConfig) BeansModelUtils
					.getResourceModelElement(resource);
			if (beansConfig instanceof IImportedBeansConfig) {
				beansConfig = BeansModelUtils.getParentOfClass(beansConfig, BeansConfig.class);
			}
			for (IBeansImport beansImport : beansConfig.getImports()) {
				for (IImportedBeansConfig importedBeansConfig : beansImport
						.getImportedBeansConfigs()) {
					resources.add(importedBeansConfig.getElementResource());
					addBeans(importedBeansConfig);
				}
			}
			resources.add(beansConfig.getElementResource());
			addBeans(beansConfig);
		}
		return resources;
	}

	private void addBeans(IBeansConfig beansConfig) {
		if (affectedBeans.containsKey(beansConfig)) {
			affectedBeans.get(beansConfig).addAll(BeansModelUtils.getBeans(beansConfig));
		}
		else {
			Set<IBean> beans = BeansModelUtils.getBeans(beansConfig);
			affectedBeans.put(beansConfig, beans);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProjectContributorState(IProjectContributorState context) {
		this.context = context;
	}

}
