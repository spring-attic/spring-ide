/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.ui;

import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * An implementation of 'UserInteractions' that uses real Dialogs, for use in 'production'.
 *
 * @author Kris De Volder
 */
public class DefaultUserInteractions implements UserInteractions {

	public interface UIContext {
		Shell getShell();
	}

	private UIContext context;

	public DefaultUserInteractions(UIContext context) {
		this.context = context;
	}

	@Override
	public ILaunchConfiguration chooseConfigurationDialog(String dialogTitle, String message, List<ILaunchConfiguration> configs) {
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		try {
			ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
			dialog.setElements(configs.toArray());
			dialog.setTitle(dialogTitle);
			dialog.setMessage(message);
			dialog.setMultipleSelection(false);
			int result = dialog.open();
			labelProvider.dispose();
			if (result == Window.OK) {
				return (ILaunchConfiguration) dialog.getFirstResult();
			}
			return null;
		} finally {
			labelProvider.dispose();
		}
	}

	private Shell getShell() {
		return context.getShell();
	}

	@Override
	public IType chooseMainType(IType[] mainTypes, String dialogTitle,
			String message) {
		if (mainTypes.length==1) {
			return mainTypes[0];
		} else if (mainTypes.length>0) {
			IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
			try {
				ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
				dialog.setElements(mainTypes);
				dialog.setTitle(dialogTitle);
				dialog.setMessage(message);
				dialog.setMultipleSelection(false);
				int result = dialog.open();
				labelProvider.dispose();
				if (result == Window.OK) {
					return (IType) dialog.getFirstResult();
				}
				return null;
			} finally {
				labelProvider.dispose();
			}
		}
		return null;
	}

	@Override
	public void errorPopup(String title, String message) {
		MessageDialog.openError(getShell(), title, message);
	}

	@Override
	public void openLaunchConfigurationDialogOnGroup(ILaunchConfiguration conf, String launchGroup) {
		IStructuredSelection selection = new StructuredSelection(new Object[] {conf});
		DebugUITools.openLaunchConfigurationDialogOnGroup(getShell(), selection, launchGroup);
	}

}
