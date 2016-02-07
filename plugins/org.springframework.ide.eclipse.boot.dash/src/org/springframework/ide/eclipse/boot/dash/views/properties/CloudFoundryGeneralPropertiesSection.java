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
package org.springframework.ide.eclipse.boot.dash.views.properties;

/**
 * General properties view section for Cloud Foundry app elements
 *
 * @author Alex Boyko
 *
 */
public class CloudFoundryGeneralPropertiesSection extends AbstractBdeGeneralPropertiesSection {

	@Override
	protected BootDashElementPropertyControl[] createPropertyControls() {
		return new BootDashElementPropertyControl[] {
				new RunStatePropertyControl(),
				new AppPropertyControl(),
				new ProjectPropertyControl(),
				new InstancesPropertyControl(),
				new HostPropertyControl(),
				new DefaultPathPropertyControl(),
				new TagsPropertyControl(),
				new HealthCheckPropertyControl()
		};
	}

}
