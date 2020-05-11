package org.springframework.ide.eclipse.boot.dash.docker;

import org.springframework.ide.eclipse.boot.dash.di.EclipseBeanLoader.Contribution;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.docker.target.DockerRunTargetType;

public class DockerBootDashInjections implements Contribution {

	@Override
	public void applyBeanDefinitions(SimpleDIContext c) throws Exception {
		c.def(DockerRunTargetType.class, DockerRunTargetType::new);
	}

}
