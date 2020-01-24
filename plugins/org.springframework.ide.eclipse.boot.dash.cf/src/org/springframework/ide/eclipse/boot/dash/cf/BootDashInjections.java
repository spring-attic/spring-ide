/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf;

import org.springframework.ide.eclipse.boot.dash.cf.actions.CfBootDashActions;
import org.springframework.ide.eclipse.boot.dash.cf.debug.SshDebugSupport;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cf.ui.DefaultCfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.di.EclipseBeanLoader.Contribution;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.DefaultBootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypeFactory;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;

/**
 * Contributes bean definitions to {@link DefaultBootDashModelContext}
 */
public class BootDashInjections implements Contribution {

	@Override
	public void applyBeanDefinitions(SimpleDIContext context) throws Exception {
		//TargetType
		context.defInstance(RunTargetTypeFactory.class, CloudFoundryRunTargetType.factory);

		//UI actions
		context.defInstance(BootDashActions.Factory.class, CfBootDashActions.factory);

		//internal
		context.def(CfUserInteractions.class, DefaultCfUserInteractions::new);
		context.defInstance(DebugSupport.class, SshDebugSupport.INSTANCE);
	}


}
