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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;
import org.springframework.ide.eclipse.beans.core.BeansCoreImages;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadataProvider;
import org.springframework.ide.eclipse.beans.core.model.metadata.IMethodMetadata;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;

/**
 * {@link Job} implementation that handles loading and attaching
 * {@link IBeanMetadata} for {@link IBeansConfig}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataBuilderJob extends Job {

	public static final String META_DATA_PROVIDERS_EXTENSION_POINT = BeansCorePlugin.PLUGIN_ID
			+ ".metadataproviders";

	public static final Object CONTENT_FAMILY = new Object();

	private Set<IResource> affectedResources;

	private Map<IProject, ClassReaderFactory> classReaderFactoryCache = 
		new HashMap<IProject, ClassReaderFactory>();

	public BeanMetadataBuilderJob(Set<IResource> affectedResources) {
		super("Attaching Spring meta data");
		this.affectedResources = affectedResources;
		setPriority(Job.BUILD);
		setProperty(IProgressConstants.ICON_PROPERTY, BeansCoreImages.DESC_OBJS_ANNOTATATION);
	}

	@Override
	public boolean belongsTo(Object family) {
		return CONTENT_FAMILY == family;
	}

	private ClassReaderFactory getClassReaderFactory(IProject project) {
		synchronized (this) {
			if (!classReaderFactoryCache.containsKey(project)) {
				classReaderFactoryCache.put(project, new CachingClassReaderFactory(JdtUtils
						.getClassLoader(project, false)));
			}
			return classReaderFactoryCache.get(project);
		}
	}

	public boolean isCoveredBy(BeanMetadataBuilderJob other) {
		if (other.affectedResources != null && this.affectedResources != null) {
			for (IResource resource : affectedResources) {
				if (!other.affectedResources.contains(resource)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		try {
			// Remove similar jobs from the chain 
			synchronized (getClass()) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				Job[] buildJobs = Job.getJobManager().find(CONTENT_FAMILY);
				for (int i = 0; i < buildJobs.length; i++) {
					Job curr = buildJobs[i];
					if (curr != this && curr instanceof BeanMetadataBuilderJob) {
						BeanMetadataBuilderJob job = (BeanMetadataBuilderJob) curr;
						if (job.isCoveredBy(this)) {
							curr.cancel();
						}
					}
				}
			}
			monitor.beginTask("Attaching Spring bean meta data", affectedResources.size());
			int worked = 0;
			for (IResource resource : affectedResources) {
				if (BeansCoreUtils.isBeansConfig(resource) && resource instanceof IFile) {
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}

					// Do some profiling
					long start = System.currentTimeMillis();

					monitor.subTask("Attaching Spring bean meta data to file ["
							+ resource.getFullPath().toString() + "]");
					attachMetaData(BeansCorePlugin.getModel().getConfig((IFile) resource), monitor,
							getClassReaderFactory(resource.getProject()));
					worked++;
					monitor.worked(worked);
					if (BeanMetadataModel.DEBUG) {
						System.out.println("Attaching meta data ["
								+ resource.getFullPath().toString() + "] took "
								+ (System.currentTimeMillis() - start) + "ms");
					}
				}
			}
		}
		finally {
			affectedResources = null;
		}
		return Status.OK_STATUS;
	}

	protected void attachMetaData(IBeansConfig beansConfig, IProgressMonitor progressMonitor,
			ClassReaderFactory classReaderFactory) {

		IBeanMetadataProvider[] providers = getMetadataProviders();
		for (IBean bean : beansConfig.getBeans()) {
			attachMetaDataToBean(beansConfig, progressMonitor, classReaderFactory, providers, bean);
		}
		for (IBeansComponent beanComponent : beansConfig.getComponents()) {
			for (IBean bean : beanComponent.getBeans()) {
				attachMetaDataToBean(beansConfig, progressMonitor, classReaderFactory, providers,
						bean);
			}
		}

		// Notify that the model has changed.
		((BeansModel) BeansCorePlugin.getModel()).notifyListeners(beansConfig, Type.CHANGED);

	}

	private void attachMetaDataToBean(IBeansConfig beansConfig, IProgressMonitor progressMonitor,
			ClassReaderFactory classReaderFactory, IBeanMetadataProvider[] providers, IBean bean) {
		// Reset meta data attachment before adding
		BeansCorePlugin.getMetaDataModel().clearBeanMetaData(bean);
		Set<IBeanMetadata> beanMetaData = new HashSet<IBeanMetadata>();
		Set<IMethodMetadata> methodMetaData = new HashSet<IMethodMetadata>();
		for (IBeanMetadataProvider provider : providers) {
			Set<IBeanMetadata> beanMetaDataSet = provider.provideBeanMetadata(bean, beansConfig,
					progressMonitor, classReaderFactory);
			for (IBeanMetadata metaData : beanMetaDataSet) {
				if (metaData instanceof IMethodMetadata) {
					methodMetaData.add((IMethodMetadata) beanMetaData);
				}
				else {
					beanMetaData.add(metaData);
				}
			}
		}
		if (beanMetaData.size() > 0 || methodMetaData.size() > 0) {
			BeansCorePlugin.getMetaDataModel().setBeanMetaData(bean, beanMetaData, methodMetaData);
		}
	}

	protected IBeanMetadataProvider[] getMetadataProviders() {
		List<IBeanMetadataProvider> providers = new ArrayList<IBeanMetadataProvider>();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				META_DATA_PROVIDERS_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if ("metadataProvider".equals(config.getName())
							&& config.getAttribute("class") != null) {
						try {
							Object handler = config.createExecutableExtension("class");
							if (handler instanceof IBeanMetadataProvider) {
								IBeanMetadataProvider entityResolver = (IBeanMetadataProvider) handler;
								providers.add(entityResolver);
							}
						}
						catch (CoreException e) {
							BeansCorePlugin.log(e);
						}
					}
				}
			}
		}
		return providers.toArray(new IBeanMetadataProvider[providers.size()]);
	}
}
