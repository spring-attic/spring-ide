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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;
import org.springframework.ide.eclipse.beans.core.BeansCoreImages;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadataProvider;
import org.springframework.ide.eclipse.beans.core.model.metadata.IMethodMetadata;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;

/**
 * {@link Job} implementation that handles loading and attaching {@link IBeanMetadata} for
 * {@link IBeansConfig}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataBuilderJob extends Job {

	/** The metadataProvider element in the extension point contribution */
	private static final String METADATA_PROVIDER_ELEMENT = "metadataProvider";

	/** The class attribute in the extension point contribution */
	private static final String CLASS_ATTRIBUTE = "class";

	/** The id of the metadata providers extension point */
	public static final String META_DATA_PROVIDERS_EXTENSION_POINT = BeansCorePlugin.PLUGIN_ID
			+ ".metadataproviders";

	/** Object identifying the job family */
	private static final Object CONTENT_FAMILY = new Object();

	/** Internal cache of the affected {@link IBean}s keyed by the containing {@link IBeansConfig} */
	private Map<IBeansConfig, Set<IBean>> affectedBeans;

	/**
	 * Constructor
	 * @param affectedBeans the list of affected {@link IBean} keyed by a corresponding
	 * {@link IBeansConfig}.
	 */
	public BeanMetadataBuilderJob(Map<IBeansConfig, Set<IBean>> affectedBeans) {
		super("Attaching Spring meta data");
		this.affectedBeans = affectedBeans;
		setPriority(Job.BUILD);
		setProperty(IProgressConstants.ICON_PROPERTY, BeansCoreImages.DESC_OBJS_ANNOTATATION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean belongsTo(Object family) {
		return CONTENT_FAMILY == family;
	}

	public boolean isCoveredBy(BeanMetadataBuilderJob other) {
		if (other.affectedBeans != null && this.affectedBeans != null) {
			Set<IBean> allBeans = new HashSet<IBean>();
			for (Set<IBean> beans : other.affectedBeans.values()) {
				allBeans.addAll(beans);
			}
			Set<IBean> allMyBeans = new HashSet<IBean>();
			for (Set<IBean> beans : this.affectedBeans.values()) {
				allMyBeans.addAll(beans);
			}
			return allBeans.containsAll(allMyBeans);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
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
			monitor.beginTask("Attaching Spring bean meta data", affectedBeans.size());
			int worked = 0;

			// Reading contributed IBeanMetadataProviders from the extension point
			IBeanMetadataProvider[] providers = getMetadataProviders();

			for (Map.Entry<IBeansConfig, Set<IBean>> entry : affectedBeans.entrySet()) {
				// Do some profiling
				long start = System.currentTimeMillis();
				IResource resource = entry.getKey().getElementResource();

				monitor.subTask("Attaching Spring bean meta data to file ["
						+ resource.getFullPath().toString() + "]");
				attachMetadata(entry.getKey(), entry.getValue(), monitor, providers);
				worked++;
				monitor.worked(worked);

				if (BeanMetadataModel.DEBUG) {
					System.out.println("Attaching meta data [" + resource.getFullPath().toString()
							+ "] took " + (System.currentTimeMillis() - start) + "ms");
				}
			}
		}
		finally {
			affectedBeans = null;
		}
		return Status.OK_STATUS;
	}

	/**
	 * Iterates over the provided list of {@link IBeanMetadataProvider}s and attaches
	 * {@link IBeanMetadata} and {@link IBeanProperty}s to the given {@link IBean} instance.
	 */
	protected void attachMetadata(IBeansConfig beansConfig, Set<IBean> beans,
			IProgressMonitor progressMonitor, IBeanMetadataProvider[] providers) {

		for (IBean bean : beans) {
			attachMetadataToBean(beansConfig, progressMonitor, providers, bean);
		}
		// Notify that the model has changed.
		((BeansModel) BeansCorePlugin.getModel()).notifyListeners(beansConfig, Type.CHANGED);
	}

	/**
	 * Attaches {@link IBeanMetadata} and {@link IBeanProperty} to a single {@link IBean}.
	 */
	private void attachMetadataToBean(final IBeansConfig beansConfig,
			final IProgressMonitor progressMonitor, IBeanMetadataProvider[] providers,
			final IBean bean) {
		// Reset meta data attachment before adding
		BeansCorePlugin.getMetadataModel().clearBeanMetadata(bean);
		BeansCorePlugin.getMetadataModel().clearBeanProperties(bean);

		Set<IBeanMetadata> beanMetaData = new LinkedHashSet<IBeanMetadata>();
		Set<IMethodMetadata> methodMetaData = new LinkedHashSet<IMethodMetadata>();

		final Set<IBeanMetadata> beanMetaDataSet = new LinkedHashSet<IBeanMetadata>();
		final Set<IBeanProperty> beanProperties = new LinkedHashSet<IBeanProperty>();
		for (final IBeanMetadataProvider provider : providers) {

			// make sure third-party extensions don't crash the build
			SafeRunner.run(new ISafeRunnable() {

				public void handleException(Throwable exception) {
					// nothing to do here
				}

				public void run() throws Exception {
					beanMetaDataSet.addAll(provider.provideBeanMetadata(bean, beansConfig,
							progressMonitor));
					beanProperties.addAll(provider.provideBeanProperties(bean, beansConfig,
							progressMonitor));
				}
			});

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
			BeansCorePlugin.getMetadataModel().setBeanMetadata(bean, beanMetaData, methodMetaData);
		}
		if (beanProperties.size() > 0) {
			BeansCorePlugin.getMetadataModel().setBeanProperties(bean, beanProperties);
		}
	}

	/**
	 * Returns the {@link IBeanMetadataProvider}s contributed to the Eclipse extension point
	 * registry.
	 */
	protected IBeanMetadataProvider[] getMetadataProviders() {
		List<IBeanMetadataProvider> providers = new ArrayList<IBeanMetadataProvider>();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				META_DATA_PROVIDERS_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (METADATA_PROVIDER_ELEMENT.equals(config.getName())
							&& config.getAttribute(CLASS_ATTRIBUTE) != null) {
						try {
							Object handler = config.createExecutableExtension(CLASS_ATTRIBUTE);
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
