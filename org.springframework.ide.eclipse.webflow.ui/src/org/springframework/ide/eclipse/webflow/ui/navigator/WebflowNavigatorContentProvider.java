/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.ui.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.webflow.core.Activator;
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

	public static final String PROJECT_EXPLORER_CONTENT_PROVIDER_ID = org.springframework.ide.eclipse.webflow.ui.Activator.PLUGIN_ID
			+ ".navigator.projectExplorerContent";

	public static final String BEANS_EXPLORER_CONTENT_PROVIDER_ID = org.springframework.ide.eclipse.webflow.ui.Activator.PLUGIN_ID
			+ ".navigator.beansExplorerContent";

	private String providerID;

	private StructuredViewer viewer;

	public void init(ICommonContentExtensionSite config) {
		Activator.getModel().registerModelChangeListener(this);
		providerID = config.getExtension().getId();
	}

	public void saveState(IMemento aMemento) {
	}

	public void restoreState(IMemento aMemento) {
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IBeansProject) {
			IProject project = ((IBeansProject) parentElement).getProject();
			IWebflowProject webflowProject = Activator.getModel().getProject(
					project);
			return webflowProject.getConfigs().toArray();
		}
		else if (parentElement instanceof IWebflowConfig) {
			List<IResource> files = new ArrayList<IResource>();
			for (IBeansConfig bc : ((IWebflowConfig) parentElement)
					.getBeansConfigs()) {
				files.add(bc.getElementResource());
			}
			return files.toArray();
		}
		else if (parentElement instanceof IFile
				&& providerID.equals(PROJECT_EXPLORER_CONTENT_PROVIDER_ID)) {
			IFile file = (IFile) parentElement;
			IWebflowConfig config = Activator.getModel().getProject(
					file.getProject()).getConfig(file);
			if (config != null) {
				return new Object[] { config };
			}
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
		if (element instanceof IWebflowConfig) {
			IWebflowConfig config = (IWebflowConfig) element;
			if (config.getBeansConfigs() != null
					&& config.getBeansConfigs().size() > 0) {
				return true;
			}
		}
		else if (element instanceof IFile
				&& providerID.equals(PROJECT_EXPLORER_CONTENT_PROVIDER_ID)) {
			IFile file = (IFile) element;
			return Activator.getModel().getProject(file.getProject())
					.getConfig(file) != null;
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
