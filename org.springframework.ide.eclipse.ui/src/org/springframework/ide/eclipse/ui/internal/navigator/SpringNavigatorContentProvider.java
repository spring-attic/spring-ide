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
package org.springframework.ide.eclipse.ui.internal.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringModel;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * This {@link ICommonContentProvider} knows about the Spring projects.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class SpringNavigatorContentProvider implements ICommonContentProvider,
		IModelChangeListener {

	private boolean refresh;
	private StructuredViewer viewer;
	private String providerID;

	public SpringNavigatorContentProvider() {
		this(true);
	}

	public SpringNavigatorContentProvider(boolean refresh) {
		this.refresh = refresh;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof StructuredViewer) {
			this.viewer = (StructuredViewer) viewer;
			if (refresh) {
				if (oldInput == null && newInput != null) {
					SpringCore.getModel().addChangeListener(this);
				} else if (oldInput != null && newInput == null) {
					SpringCore.getModel().removeChangeListener(this);
				}
			}
		} else {
			this.viewer = null;
		}
	}

	public void dispose() {
		if (refresh) {
			SpringCore.getModel().removeChangeListener(this);
		}
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IWorkingSet) {
			return IModelElement.NO_CHILDREN;
		}
		return SpringCore.getModel().getProjects().toArray();
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Object[] getChildren(Object parentElement) {

		// Show virtual folder "Spring Elements" only in project explorer
		if (SpringUIPlugin.PROJECT_EXPLORER_CONTENT_PROVIDER_ID
				.equals(providerID)) {
			IProject project = SpringCoreUtils.getAdapter(parentElement,
					IProject.class);
			if (project != null) {
				ISpringProject springProject = SpringCore.getModel()
						.getProject(project);
				if (springProject != null) {
					return new Object[] { springProject };
				}
			}
		}
		return IModelElement.NO_CHILDREN;
	}

	public Object getParent(Object element) {
		return null;
	}

	public void init(ICommonContentExtensionSite config) {
		providerID = config.getExtension().getId();
	}

	public void saveState(IMemento aMemento) {
	}

	public void restoreState(IMemento aMemento) {
	}

	public void elementChanged(ModelChangeEvent event) {
		IModelElement element = event.getElement();

		// For events of type ADDED or REMOVED refresh the parent of the changed
		// model element
		if (event.getType() == Type.CHANGED) {
			refreshViewerForElement(element);
		} else {
			if (element.getElementParent() instanceof ISpringModel) {
				refreshViewerForElement(ResourcesPlugin.getWorkspace().getRoot());
			}
			else {
				refreshViewerForElement(element.getElementParent());
			}
		}
	}

	protected final StructuredViewer getViewer() {
		return viewer;
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
			} else {
				ctrl.getDisplay().asyncExec(new Runnable() {
					public void run() {

						// Abort if this happens after disposes
						Control ctrl = viewer.getControl();
						if (ctrl == null || ctrl.isDisposed()) {
							return;
						}
						if (element instanceof ISpringModel) {
							viewer.refresh();
						} else {
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
