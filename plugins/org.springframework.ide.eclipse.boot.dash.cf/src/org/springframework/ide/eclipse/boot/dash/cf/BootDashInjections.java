package org.springframework.ide.eclipse.boot.dash.cf;

import org.springframework.ide.eclipse.boot.dash.di.EclipseBeanLoader.Contribution;
import org.springframework.ide.eclipse.boot.dash.cf.ui.DefaultCfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.DefaultBootDashModelContext;

/**
 * Contributes bean definitions to {@link DefaultBootDashModelContext}
 */
public class BootDashInjections implements Contribution {

	@Override
	public void applyBeanDefinitions(SimpleDIContext context) throws Exception {
		context.def(CfUserInteractions.class, DefaultCfUserInteractions::new);
	}
}
