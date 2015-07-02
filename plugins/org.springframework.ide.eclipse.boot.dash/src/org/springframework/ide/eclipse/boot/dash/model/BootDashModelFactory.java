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
package org.springframework.ide.eclipse.boot.dash.model;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;

public class BootDashModelFactory {

	public BootDashModel getModel(RunTarget target) {
		if (target instanceof CloudFoundryRunTarget) {
			return new CloudFoundryBootDashModel((CloudFoundryRunTarget) target);
		}
		return null;
	}

}
