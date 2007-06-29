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
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelContentProvider;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
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
			return new Object[] { beansProject };
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
			triggerDeferredLoading(parentElement);
			return IModelElement.NO_CHILDREN;
		}
		return super.getChildren(parentElement);
	}

	protected Object[] getConfigSetChildren(IBeansConfigSet configSet) {
		Set<ISourceModelElement> children = new LinkedHashSet<ISourceModelElement>();
		for (final IBeansConfig config : configSet.getConfigs()) {
			if (config instanceof ILazyInitializedModelElement
					&& !((ILazyInitializedModelElement) config).isInitialized()) {
				triggerDeferredLoading(config);
				break;
			}
			Object[] configChildren = getChildren(config);
			for (Object child : configChildren) {
				if (child instanceof IBean || child instanceof IBeansComponent) {
					children.add((ISourceModelElement) child);
				}
			}
		}
		return children.toArray();
	}

	private void triggerDeferredLoading(final Object config) {
		Job job = new Job("Loading model content from resource '"
				+ ((IResourceModelElement) config).getElementResource()
						.getFullPath().toString() + "'") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				superGetChildren(config);
				SpringUIUtils.getStandardDisplay().asyncExec(new Runnable() {
					public void run() {
						refreshViewerForElement(((IModelElement) config)
								.getElementParent());
					}

				});
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
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
}
