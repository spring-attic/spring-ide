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
package org.springframework.ide.eclipse.roo.ui.internal.properties;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.core.internal.model.DefaultRooInstall;
import org.springframework.ide.eclipse.roo.core.model.IRooInstall;
import org.springframework.ide.eclipse.roo.ui.RooUiActivator;


/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class RooInstallPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private InstalledRooInstallBlock fJREBlock;

	public RooInstallPreferencePage() {
		super("Roo Installations");
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean isValid() {
		if (super.isValid()) {
			if (getCurrentDefaultVM() == null && fJREBlock.getJREs().length > 0) {
				setErrorMessage("Select a default Roo installation");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean performOk() {
		final boolean[] canceled = new boolean[] { false };
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				Set<IRooInstall> newInstalls = new LinkedHashSet<IRooInstall>();
				IRooInstall defaultVM = getCurrentDefaultVM();
				IRooInstall[] vms = fJREBlock.getJREs();
				for (IRooInstall vm : vms) {
					newInstalls.add(new DefaultRooInstall(vm.getHome(), vm.getName(), vm.equals(defaultVM)));
				}

				RooCoreActivator.getDefault().getInstallManager().setRooInstalls(newInstalls);
			}
		});

		if (canceled[0]) {
			return false;
		}

		// save column widths
		IDialogSettings settings = RooUiActivator.getDefault().getDialogSettings();
		fJREBlock.saveColumnSettings(settings, "com.springsource.sts.roo.ui.dialogsettings");

		return super.performOk();
	}

	private IRooInstall getCurrentDefaultVM() {
		return fJREBlock.getCheckedJRE();
	}

	@Override
	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);

		noDefaultAndApplyButton();

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		ancestor.setLayout(layout);

		SWTFactory
				.createWrapLabel(
						ancestor,
						"Add, edit or remove Roo installations. By default the checked Roo installation will be used to launch the Roo shell for newly create projects.",
						1, 300);
		SWTFactory.createVerticalSpacer(ancestor, 1);

		fJREBlock = new InstalledRooInstallBlock();
		fJREBlock.createControl(ancestor);
		fJREBlock.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				isValid();
			}
		});

		Control control = fJREBlock.getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		control.setLayoutData(data);

		fJREBlock.restoreColumnSettings(RooUiActivator.getDefault().getDialogSettings(),
				"com.springsource.sts.roo.ui.dialogsettings");

		fJREBlock.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setValid(false);

				IRooInstall install = getCurrentDefaultVM();
				if (install == null) {
					setErrorMessage("Select a default Roo installation");
				}
				else {
					setErrorMessage(null);
					setValid(true);
				}
			}
		});
		applyDialogFont(ancestor);
		return ancestor;
	}
}
