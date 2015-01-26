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
package org.springframework.ide.eclipse.boot.launch;

import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.DEFAULT_ENABLE_DEBUG_OUTPUT;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.getEnableDebugOutput;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.setEnableDebugOutput;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.boot.launch.util.LaunchConfigurationTabSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

/**
 * @author Kris De Volder
 */
public class EnableDebugSection extends LaunchConfigurationTabSection {

	private Button enableDebug;

	public EnableDebugSection(IPageWithSections owner) {
		super(owner);
	}

	@Override
	public void createContents(Composite page) {
		enableDebug = new Button(page, SWT.CHECK);
		enableDebug.setText("Enable debug output");
		enableDebug.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getDirtyState().setValue(true);
			}
		});
	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		enableDebug.setSelection(getEnableDebugOutput(conf));
		getDirtyState().setValue(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		setEnableDebugOutput(conf, enableDebug.getSelection());
		getDirtyState().setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		setEnableDebugOutput(conf, DEFAULT_ENABLE_DEBUG_OUTPUT);
	}

}
