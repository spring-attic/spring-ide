/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

/**
 * Properties view section for Cloud Foundry app elements
 *
 * @author Alex Boyko
 */
public class CloudFoundryAppPropertiesSection extends AbstractBdeGeneralPropertiesSection {

	@Override
	protected BootDashElementPropertyControl[] createPropertyControls() {
		return new BootDashElementPropertyControl[] {
				new RunStatePropertyControl(),
				new AppPropertyControl(),
				new ProjectPropertyControl(),
				new InstancesPropertyControl(),
				new UrlPropertyControl<>(BootDashElement.class, "URL:", ((e) -> e.getUrl())),
				new DefaultPathPropertyControl(),
				new ReadOnlyStringPropertyControl<>(CloudAppDashElement.class, "Healthcheck:", (e) -> e.getHealthCheck()),
				new ReadOnlyStringPropertyControl<>(CloudAppDashElement.class, "Healthcheck Http Endpoint:", (e) -> e.getHealthCheckHttpEndpoint()),
				new ReadOnlyStringPropertyControl<>(CloudAppDashElement.class, "Jmx Ssh Tunnel:", (e) -> e.getJmxSshTunnelStatus().getValue().getLabel()),
				new ReadOnlyStringPropertyControl<>(CloudAppDashElement.class, "Jmx Local Port:", (e) -> e.getCfJmxPort()>0 ? (""+e.getCfJmxPort()) : ""),
				new ReadOnlyStringPropertyControl<>(CloudAppDashElement.class, "Jmx URL:", (e) -> e.getJmxUrl()),
				new TagsPropertyControl()
		};
	}

}
