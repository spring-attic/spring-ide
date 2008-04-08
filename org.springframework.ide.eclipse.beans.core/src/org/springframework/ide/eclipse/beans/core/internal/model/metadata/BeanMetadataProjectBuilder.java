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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadataModel;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

/**
 * {@link IProjectBuilder} that triggers the creation and maintenance of
 * {@link IBeanMetadata} stored in the {@link IBeanMetadataModel}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataProjectBuilder implements IProjectBuilder {

	private static final String JAVA_FILE_EXTENSION = ".java";

	public void build(Set<IResource> affectedResources, int kind, IProgressMonitor monitor)
			throws CoreException {
		monitor.subTask("Attaching Spring meta data");
		if (affectedResources.size() > 0) {
			Job job = new BeanMetadataBuilderJob(affectedResources);
			job.schedule();
		}
		monitor.done();
	}

	public void cleanup(IResource resource, IProgressMonitor monitor) throws CoreException {
		if (BeansCoreUtils.isBeansConfig(resource) && resource instanceof IFile) {
			IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource);
			for (IBean bean : beansConfig.getBeans()) {
				BeansCorePlugin.getMetaDataModel().clearBeanMetaData(bean);
			}
			// Notify that the model has changed.
			((BeansModel) BeansCorePlugin.getModel()).notifyListeners(beansConfig, Type.CHANGED);
		}
	}

	public Set<IResource> getAffectedResources(IResource resource, int kind) throws CoreException {
		Set<IResource> files = new HashSet<IResource>();

		if (resource instanceof IFile && resource.getName().endsWith(JAVA_FILE_EXTENSION)) {
			for (IBeansConfig config :BeansModelUtils.getConfigsByContainingTypes(resource)) {
				files.add(config.getElementResource());
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
					files.add(importedBeansConfig.getElementResource());
				}
			}
			files.add(beansConfig.getElementResource());
		}
		return files;
	}

}
