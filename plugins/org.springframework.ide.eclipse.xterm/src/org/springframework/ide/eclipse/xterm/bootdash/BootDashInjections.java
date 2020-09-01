package org.springframework.ide.eclipse.xterm.bootdash;

import org.springframework.ide.eclipse.boot.dash.di.EclipseBeanLoader.Contribution;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;

public class BootDashInjections implements Contribution {

	@Override
	public void applyBeanDefinitions(SimpleDIContext context) throws Exception {
		context.defInstance(BootDashActions.Factory.class, DockerBootDashActions.factory);
	}
}
