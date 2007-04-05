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
package org.springframework.ide.eclipse.webflow.ui.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelListener;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * This class is a content provider for the {@link CommonNavigator} which knows
 * about the web flow core model's {@link IWebflowConfig}.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowNavigatorContentProvider implements ICommonContentProvider,
		IWebflowModelListener {

	public static final String PROJECT_EXPLORER_CONTENT_PROVIDER_ID =
			org.springframework.ide.eclipse.webflow.ui.Activator.PLUGIN_ID
					+ ".navigator.projectExplorerContent";

	public static final String SPRING_EXPLORER_CONTENT_PROVIDER_ID =
			org.springframework.ide.eclipse.webflow.ui.Activator.PLUGIN_ID
					+ ".navigator.springExplorerContent";

	private StructuredViewer viewer;

	public void init(ICommonContentExtensionSite config) {
		Activator.getModel().registerModelChangeListener(this);
	}

	public void saveState(IMemento aMemento) {
	}

	public void restoreState(IMemento aMemento) {
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ISpringProject) {
			return WebflowModelUtils.getFiles(
					((ISpringProject) parentElement).getProject()).toArray();
		}
		else if (parentElement instanceof IProject) {
			return WebflowModelUtils.getFiles((IProject) parentElement)
					.toArray();
		}
		else if (parentElement instanceof IFile) {
			IFile file = (IFile) parentElement;
			if (WebflowModelUtils.isWebflowConfig(file)) {
				return new Object[] {
						WebflowModelUtils.getWebflowConfig(file) };
			}
		}
		else if (parentElement instanceof IWebflowConfig) {
			List<Object> children = new ArrayList<Object>();
			for (IModelElement config : ((IWebflowConfig) parentElement)
					.getBeansConfigs()) {
				if (config instanceof IBeansConfig) {
					children.add(((IBeansConfig) config).getElementResource());
				}
				else {
					children.add(config);
				}
			}
			return children.toArray();
		}
		return IModelElement.NO_CHILDREN;
	}

	public Object getParent(Object element) {
		if (element instanceof IWebflowModelElement) {
			return ((IWebflowModelElement) element).getElementParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ISpringProject) {
			return WebflowModelUtils.getFiles(
					((ISpringProject) element).getProject()).size() > 0;
		}
		else if (element instanceof IProject) {
			return (WebflowModelUtils.getFiles((IProject) element).size() > 0);
		}
		else if (element instanceof IFile) {
			IFile file = (IFile) element;
			return WebflowModelUtils.isWebflowConfig(file)
					|| BeansCoreUtils.isBeansConfig(file);
		}
		else if (element instanceof IWebflowConfig) {
			IWebflowConfig config = (IWebflowConfig) element;
			if (config.getBeansConfigs() != null
					&& config.getBeansConfigs().size() > 0) {
				return true;
			}
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return IModelElement.NO_CHILDREN;
	}

	public void dispose() {
		Activator.getModel().removeModelChangeListener(this);
	}

	public void modelChanged(IWebflowProject project) {
		IProject p = project.getProject();
		refreshViewerForElement(BeansCorePlugin.getModel().getProject(p));
	}

	public final void inputChanged(Viewer viewer, Object oldInput,
			Object newInput) {
		if (viewer instanceof StructuredViewer) {
			this.viewer = (StructuredViewer) viewer;
			if (oldInput == null && newInput != null) {
				Activator.getModel().registerModelChangeListener(this);
			}
			else if (oldInput != null && newInput == null) {
				Activator.getModel().removeModelChangeListener(this);
			}
		}
		else {
			this.viewer = null;
		}
	}

	protected final void refreshViewerForElement(final Object element) {
		if (viewer instanceof StructuredViewer && element != null) {

			// Abort if this happens after disposes
			Control ctrl = viewer.getControl();
			if (ctrl == null || ctrl.isDisposed()) {
				return;
			}

			// Are we in the UI thread?
			if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
				viewer.refresh(element);
			}
			else {
				ctrl.getDisplay().asyncExec(new Runnable() {
					public void run() {

						// Abort if this happens after disposes
						Control ctrl = viewer.getControl();
						if (ctrl == null || ctrl.isDisposed()) {
							return;
						}
						viewer.refresh(element);
					}
				});
			}
		}
	}
}
