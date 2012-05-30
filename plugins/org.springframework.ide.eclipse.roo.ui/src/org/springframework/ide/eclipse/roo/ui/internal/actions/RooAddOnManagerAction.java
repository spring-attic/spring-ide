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
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.springframework.ide.eclipse.roo.ui.internal.RooUiUtil;


/**
 * An action to open a Roo Shell tab for a group of projects
 * @author Christian Dupuis
 * @since 2.1.0
 */
public class RooAddOnManagerAction extends Action {

	private final RooShellView view;

	public RooAddOnManagerAction(RooShellView view) {
		super("Roo Add-on Manager", RooUiActivator.getImageDescriptor("icons/full/obj16/addon.gif"));
		this.view = view;
	}

	@Override
	public void run() {
		RooAddOnManagerActionDelegate delegate = new RooAddOnManagerActionDelegate();
		if (view.getActiveProject() != null) {
			delegate.selectionChanged(this, new StructuredSelection(view.getActiveProject()));
		}
		else {
			delegate.selectionChanged(this, new StructuredSelection(RooUiUtil.getAllRooProjects()));
		}
		delegate.run(view);
	}

}
