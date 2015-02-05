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
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

/**
 * @author Kris De Volder
 */
public class EnableDebugLaunchTabModel extends LaunchTabSelectionModel<Boolean> {

	public static EnableDebugLaunchTabModel create() {
		LiveVariable<Boolean> enable = new LiveVariable<Boolean>();
		return new EnableDebugLaunchTabModel(enable, Validator.OK);
	}

	protected EnableDebugLaunchTabModel(LiveVariable<Boolean> selection,
			LiveExpression<ValidationResult> validator) {
		super(selection, validator);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		selection.setValue(getEnableDebugOutput(conf));
		getDirtyState().setValue(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		setEnableDebugOutput(conf, selection.getValue());
		getDirtyState().setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		setEnableDebugOutput(conf, DEFAULT_ENABLE_DEBUG_OUTPUT);
	}


}
