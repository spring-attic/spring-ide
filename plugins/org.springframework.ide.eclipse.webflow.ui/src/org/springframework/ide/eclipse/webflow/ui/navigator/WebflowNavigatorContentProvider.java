/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelListener;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * This class is a content provider for the {@link CommonNavigator} which knows
 * about the web flow core model's {@link IWebflowConfig}.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class WebflowNavigatorContentProvider implements ICommonContentProvider,
		IWebflowModelListener {

	private String providerID;

	private StructuredViewer viewer;

	public void init(ICommonContentExtensionSite config) {
		providerID = config.getExtension().getId();
		Activator.getModel().registerModelChangeListener(this);
	}

	public void saveState(IMemento aMemento) {
	}

	public void restoreState(IMemento aMemento) {
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ISpringProject) {
			if (hasChildren(parentElement)) {
				return new Object[] { Activator.getModel().getProject(
						((ISpringProject) parentElement).getProject()) };
			}
		}
		else if (parentElement instanceof IWebflowProject) {
			return WebflowModelUtils.getFiles(
					((IWebflowProject) parentElement).getProject()).toArray();
		}
		else if (parentElement instanceof IFile) {
			IFile file = (IFile) parentElement;
			if (WebflowModelUtils.isWebflowConfig(file)) {
				return new Object[] { WebflowModelUtils.getWebflowConfig(file) };
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
		else if (element instanceof IFile
				&& WebflowModelUtils.isWebflowConfig((IFile) element)) {
			return WebflowModelUtils.getWebflowConfig(((IFile) element))
					.getProject();
		}
		else if (element instanceof IWebflowProject) {
			return SpringCore.getModel().getProject(
					((IWebflowProject) element).getProject());
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ISpringProject) {
			return Activator.getModel().getProject(
					((ISpringProject) element).getProject()).getConfigs()
					.size() > 0;
		}
		else if (element instanceof IWebflowProject) {
			return ((IWebflowProject) element).getConfigs().size() > 0;
		}
		else if (element instanceof IFile) {
			IFile file = (IFile) element;
			return WebflowModelUtils.isWebflowConfig(file);
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
		if (project != null) {
			IProject p = project.getProject();
			if (org.springframework.ide.eclipse.webflow.ui.Activator.PROJECT_EXPLORER_CONTENT_PROVIDER_ID
					.equals(providerID)) {
				refreshViewerForElement(p);
				refreshViewerForElement(JdtUtils.getJavaProject(p));
			}
			else if (org.springframework.ide.eclipse.webflow.ui.Activator.SPRING_EXPLORER_CONTENT_PROVIDER_ID
					.equals(providerID)) {
				refreshViewerForElement(SpringCore.getModel().getProject(p));
			}
		}
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
						if (element instanceof IWebflowModel) {
							viewer.refresh();
						}
						else {
							viewer.refresh(element);
						}
					}
				});
			}
		}
	}

	@Override
	public String toString() {
		return String.valueOf(providerID);
	}
}
