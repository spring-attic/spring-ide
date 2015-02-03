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

import org.springframework.ide.eclipse.boot.launch.util.ILaunchConfigurationTabModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

public abstract class LaunchTabSelectionModel<T> extends SelectionModel<T> implements ILaunchConfigurationTabModel {

	private LiveVariable<Boolean> dirtyState = new LiveVariable<Boolean>(false);

	public LaunchTabSelectionModel(LiveVariable<T> selection, LiveExpression<ValidationResult> validator) {
		super(selection, validator);
	}

	@Override
	public final LiveExpression<ValidationResult> getValidator() {
		return validator;
	}

	@Override
	public final LiveVariable<Boolean> getDirtyState() {
		return dirtyState;
	}

}
