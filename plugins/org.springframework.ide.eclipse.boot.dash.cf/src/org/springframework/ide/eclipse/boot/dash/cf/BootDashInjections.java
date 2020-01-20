package org.springframework.ide.eclipse.boot.dash.cf;

import org.springframework.ide.eclipse.boot.dash.cf.actions.CfBootDashActions;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cf.ui.DefaultCfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CfUserInteractions;
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
		context.defInstance(RunTargetTypeFactory.class, CloudFoundryRunTargetType.factory);
		context.def(CfUserInteractions.class, DefaultCfUserInteractions::new);
		context.defInstance(BootDashActions.Factory.class, CfBootDashActions.factory);
	}
}
