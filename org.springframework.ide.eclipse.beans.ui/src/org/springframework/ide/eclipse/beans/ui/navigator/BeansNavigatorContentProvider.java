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
package org.springframework.ide.eclipse.beans.ui.navigator;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.progress.IProgressConstants;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelContentProvider;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.ILazyInitializedModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * This class is a content provider for the {@link CommonNavigator} which knows
 * about the beans core model's {@link IModelElement} elements.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansNavigatorContentProvider extends BeansModelContentProvider
		implements ICommonContentProvider {

	private String providerID;

	@Override
	public Object[] getElements(Object inputElement) {
		if (BeansUIPlugin.PROJECT_EXPLORER_CONTENT_PROVIDER_ID
				.equals(providerID)) {
			return SpringCoreUtils.getSpringProjects().toArray();
		}
		return super.getElements(inputElement);
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof ISpringProject) {
			IBeansProject beansProject = BeansCorePlugin.getModel().getProject(
					((ISpringProject) parentElement).getProject());
			if (beansProject != null
					&& (!beansProject.getConfigs().isEmpty() || !beansProject
							.getConfigSets().isEmpty())) {
				return new Object[] { beansProject };
			}
		}
		else if (parentElement instanceof IBeansProject) {
			return getProjectChildren((IBeansProject) parentElement, false);
		}
		// check for lazy loading and/or long running elements; if a element is
		// marked to be long-running, execute the call to super.getChildren()
		// asynchronous and refresh the underlying viewer with the given parent
		// element (use parent because IBeansConfigSets need to be updated as
		// well)
		else if (parentElement instanceof ILazyInitializedModelElement
				&& !((ILazyInitializedModelElement) parentElement)
						.isInitialized()) {
			triggerDeferredElementLoading(parentElement,
					((IModelElement) parentElement).getElementParent());
			return IModelElement.NO_CHILDREN;
		}
		else if (parentElement instanceof IBeansImport) {
			return getImportChildren(parentElement);
		}
		return super.getChildren(parentElement);
	}

	protected Object[] getImportChildren(final Object parentElement) {
		Set<IImportedBeansConfig> importedBeansConfigs = ((IBeansImport) parentElement)
				.getImportedBeansConfigs();
		Set<Object> importedFiles = new LinkedHashSet<Object>();
		for (IBeansConfig bc : importedBeansConfigs) {
			if (bc.isElementArchived()) {
				importedFiles.add(new ZipEntryStorage(bc));
			}
			else {
				importedFiles.add(bc.getElementResource());
			}
		}
		return importedFiles.toArray(new Object[importedFiles.size()]);
	}

	@Override
	protected Object[] getConfigSetChildren(IBeansConfigSet configSet) {
		Set<ISourceModelElement> children = new LinkedHashSet<ISourceModelElement>();
		for (final IBeansConfig config : configSet.getConfigs()) {
			if (config instanceof ILazyInitializedModelElement
					&& !((ILazyInitializedModelElement) config).isInitialized()) {
				triggerDeferredElementLoading(config, configSet);
				continue;
			}
			getConfigChildren(children, config);
		}
		return children.toArray();
	}

	private void triggerDeferredElementLoading(final Object config,
			final Object parent) {
		// first check if a matching job is already scheduled
		synchronized (getClass()) {
			Job[] buildJobs = Job.getJobManager().find(
					ModelJob.MODEL_CONTENT_FAMILY);
			for (int i = 0; i < buildJobs.length; i++) {
				Job curr = buildJobs[i];
				if (curr instanceof ModelJob) {
					ModelJob job = (ModelJob) curr;
					if (job.isCoveredBy(config, parent)) {
						return;
					}
				}
			}
		}
		Job job = new ModelJob(config, parent, this);
		job.setPriority(Job.INTERACTIVE);
		job.setProperty(IProgressConstants.ICON_PROPERTY,
				BeansUIImages.DESC_OBJS_SPRING);
		job.schedule();
	}

	private Object[] superGetChildren(Object parentElement) {
		return super.getChildren(parentElement);
	}

	@Override
	public void elementChanged(ModelChangeEvent event) {
		IModelElement element = event.getElement();

		if (element instanceof IBeansProject) {
			IProject project = ((IBeansProject) element).getProject();
			if (BeansUIPlugin.PROJECT_EXPLORER_CONTENT_PROVIDER_ID
					.equals(providerID)) {
				refreshViewerForElement(project);
				refreshViewerForElement(JdtUtils.getJavaProject(project));
			}
			else if (BeansUIPlugin.SPRING_EXPLORER_CONTENT_PROVIDER_ID
					.equals(providerID)) {
				refreshViewerForElement(SpringCore.getModel().getProject(
						project));
			}
			else {
				super.elementChanged(event);
			}
		}
		else if (element instanceof IBeansConfig) {
			IBeansConfig config = (IBeansConfig) element;
			refreshViewerForElement(config.getElementResource());

			// For a changed Spring beans config in the Eclipse Project Explorer
			// refresh all corresponding bean classes
			if (BeansUIPlugin.PROJECT_EXPLORER_CONTENT_PROVIDER_ID
					.equals(providerID)) {
				refreshBeanClasses(config);
			}
		}
		else {
			super.elementChanged(event);
		}
	}

	/**
	 * Refreshes the config file and all bean classes of a given beans config
	 */
	protected void refreshBeanClasses(IBeansConfig config) {
		Set<String> classes = config.getBeanClasses();
		for (String clazz : classes) {
			IType type = JdtUtils.getJavaType(config.getElementResource()
					.getProject(), clazz);
			if (type != null) {
				refreshViewerForElement(type);
			}
		}
	}

	public void init(ICommonContentExtensionSite config) {
		providerID = config.getExtension().getId();
	}

	public void saveState(IMemento memento) {
	}

	public void restoreState(IMemento memento) {
	}

	@Override
	public String toString() {
		return String.valueOf(providerID);
	}

	/**
	 * Internal {@link Job} that handles loading of {@link IBeansConfig} as
	 * Eclipse progress
	 */
	private static class ModelJob extends Job {

		private final Object config;

		private final Object parent;

		private final BeansNavigatorContentProvider contentProvider;

		public static final Object MODEL_CONTENT_FAMILY = new Object();

		public ModelJob(Object config, Object parent,
				BeansNavigatorContentProvider contentProvider) {
			super("Loading model content");
			this.config = config;
			this.parent = parent;
			this.contentProvider = contentProvider;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Loading model content from resource '"
					+ ((IResourceModelElement) config).getElementResource()
							.getFullPath().toString() + "'", 2);
			synchronized (getClass()) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				Job[] buildJobs = Job.getJobManager()
						.find(MODEL_CONTENT_FAMILY);
				for (int i = 0; i < buildJobs.length; i++) {
					Job curr = buildJobs[i];
					if (curr != this && curr instanceof ModelJob) {
						ModelJob job = (ModelJob) curr;
						if (job.isCoveredBy(this)) {
							curr.cancel();
						}
					}
				}
			}
			contentProvider.superGetChildren(config);
			monitor.worked(1);
			SpringUIUtils.getStandardDisplay().asyncExec(new Runnable() {
				public void run() {
					contentProvider.refreshViewerForElement(config);
					contentProvider.refreshViewerForElement(parent);
					if (parent instanceof IBeansProject) {
						contentProvider.refreshViewerForElement(SpringCore
								.getModel().getProject(
										((IBeansProject) parent).getProject()));
					}
					if (BeansUIPlugin.PROJECT_EXPLORER_CONTENT_PROVIDER_ID
							.equals(contentProvider.providerID)
							&& config instanceof IResourceModelElement) {
						contentProvider
								.refreshViewerForElement(((IResourceModelElement) config)
										.getElementResource());
					}
				}
			});
			monitor.worked(2);
			monitor.done();
			return Status.OK_STATUS;
		}

		public boolean isCoveredBy(ModelJob other) {
			return other.parent.equals(parent) && other.config.equals(config);
		}

		public boolean isCoveredBy(Object otherConfig, Object otherParent) {
			return otherParent.equals(parent) && otherConfig.equals(config);
		}

		@Override
		public boolean belongsTo(Object family) {
			return MODEL_CONTENT_FAMILY == family;
		}
	}

}
