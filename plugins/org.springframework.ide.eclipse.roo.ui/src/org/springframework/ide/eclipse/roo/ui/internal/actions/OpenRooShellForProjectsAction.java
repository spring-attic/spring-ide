/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.core.model.IRooInstall;
import org.springframework.ide.eclipse.roo.ui.RooUiActivator;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellView;


/**
 * An action to open a Roo Shell tab for a group of projects
 * @author Christian Dupuis
 * @since 2.1.0
 */
public class OpenRooShellForProjectsAction extends Action {

	private final RooShellView view;

	public OpenRooShellForProjectsAction(RooShellView view) {
		super("Open Roo Shell", RooUiActivator.getImageDescriptor("icons/full/obj16/new_console_obj.gif"));
		this.view = view;
	}

	@Override
	public void run() {
		RooProjectSelectionDialog dialog = new RooProjectSelectionDialog(view.getSite().getShell());
		if (dialog.open() == Dialog.OK) {
			for (IProject project : dialog.getSelectedProjects()) {
				view.openShell(project);
			}
		}
	}

	private class RooProjectSelectionDialog extends CheckedTreeSelectionDialog {

		public RooProjectSelectionDialog(Shell parent) {
			super(parent, new DecoratingLabelProvider(new RooProjectLabelProvider(), PlatformUI.getWorkbench()
					.getDecoratorManager().getLabelDecorator()), new RooProjectTreeContentProvider());
			setTitle("Roo Shell Project Selection");
			setMessage("Open Roo Shells for the following projects:");
			setInput(ResourcesPlugin.getWorkspace().getRoot());
		}

		public IProject[] getSelectedProjects() {
			Object[] result = getResult();
			ArrayList<IProject> projects = new ArrayList<IProject>();
			for (Object project : result) {
				projects.add((IProject) project);
			}
			return projects.toArray(new IProject[0]);
		}

	}

	private class RooProjectTreeContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IWorkspaceRoot) {
				Set<IProject> projects = new HashSet<IProject>();
				for (IProject project : ((IWorkspaceRoot) inputElement).getProjects()) {
					if (!view.getOpenProjects().contains(project) && project.isAccessible() && project.isOpen()
							&& SpringCoreUtils.hasNature(project, RooCoreActivator.NATURE_ID)) {
						projects.add(project);
					}
				}
				return projects.toArray(new IProject[projects.size()]);
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class RooProjectLabelProvider extends WorkbenchLabelProvider {
		@Override
		protected String decorateText(String input, Object element) {
			if (element instanceof IProject) {
				IRooInstall install = RooCoreActivator.getDefault().getInstallManager()
						.getRooInstall((IProject) element);
				if (install != null) {
					return input + " [" + install.getName() + "]";
				}
			}
			return super.decorateText(input, element);
		}

	}
}
