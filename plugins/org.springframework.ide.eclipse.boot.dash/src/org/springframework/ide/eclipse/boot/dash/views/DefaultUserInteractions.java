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
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.ToggleFiltersDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.ToggleFiltersDialogModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * An implementation of 'UserInteractions' that uses real Dialogs, for use in
 * 'production'.
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
	public ILaunchConfiguration chooseConfigurationDialog(final String dialogTitle, final String message,
			final List<ILaunchConfiguration> configs) {
		final LiveVariable<ILaunchConfiguration> chosen = new LiveVariable<ILaunchConfiguration>();
		context.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
				try {
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
					dialog.setElements(configs.toArray());
					dialog.setTitle(dialogTitle);
					dialog.setMessage(message);
					dialog.setMultipleSelection(false);
					int result = dialog.open();
					labelProvider.dispose();
					if (result == Window.OK) {
						chosen.setValue((ILaunchConfiguration) dialog.getFirstResult());
					}
				} finally {
					labelProvider.dispose();
				}
			}
		});
		return chosen.getValue();
	}

	private Shell getShell() {
		return context.getShell();
	}

	@Override
	public IType chooseMainType(final IType[] mainTypes, final String dialogTitle, final String message) {
		if (mainTypes.length == 1) {
			return mainTypes[0];
		} else if (mainTypes.length > 0) {
			// Take care the UI interactions don't bork if called from non-ui
			// thread.
			final LiveVariable<IType> chosenType = new LiveVariable<IType>();
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
					try {
						ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
						dialog.setElements(mainTypes);
						dialog.setTitle(dialogTitle);
						dialog.setMessage(message);
						dialog.setMultipleSelection(false);
						int result = dialog.open();
						labelProvider.dispose();
						if (result == Window.OK) {
							chosenType.setValue((IType) dialog.getFirstResult());
						}
					} finally {
						labelProvider.dispose();
					}
				}
			});
			return chosenType.getValue();
		}
		return null;
	}

	@Override
	public void errorPopup(final String title, final String message) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(getShell(), title, message);
			}
		});
	}

	@Override
	public void openLaunchConfigurationDialogOnGroup(final ILaunchConfiguration conf, final String launchGroup) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IStructuredSelection selection = new StructuredSelection(new Object[] { conf });
				DebugUITools.openLaunchConfigurationDialogOnGroup(getShell(), selection, launchGroup);
			}
		});
	}

	@Override
	public void openUrl(final String url) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (url != null) {
					UiUtil.openUrl(url);
				}
			}
		});
	}

	@Override
	public boolean confirmOperation(final String title, final String message) {
		final boolean[] confirm = { false };
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				confirm[0] = MessageDialog.openConfirm(getShell(), title, message);
			}
		});
		return confirm[0];
	}

	@Override
	public String updatePassword(final String userName, final String targetId) {
		final String[] password = new String[1];
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				UpdatePasswordDialog dialog = new UpdatePasswordDialog(getShell(), userName, targetId);
				if (dialog.open() == Window.OK) {
					password[0] = dialog.getPassword();
				}
			}
		});
		return password[0];
	}

	@Override
	public void openDialog(final ToggleFiltersDialogModel model) {
		final Shell shell = getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				ToggleFiltersDialog dlg = new ToggleFiltersDialog("Select Filters", model, shell);
				dlg.open();
			}
		});
	}

	@Override
	public String chooseFile(String title, String file) {
		FileDialog fileDialog = new FileDialog(getShell());
		fileDialog.setText(title);
		fileDialog.setFileName(file);

		String result = fileDialog.open();
		return result;
	}

	@Override
	public String inputText(String title, String message, String initialValue, IInputValidator validator) {
		InputDialog inputDialog = new InputDialog(getShell(), title, message, initialValue, validator);

		if (inputDialog.open() == Window.OK) {
			return inputDialog.getValue();
		}

		return null;
	}
}
