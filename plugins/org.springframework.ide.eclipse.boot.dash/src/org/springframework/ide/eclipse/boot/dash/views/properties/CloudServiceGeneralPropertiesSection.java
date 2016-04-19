/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import java.util.function.Function;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudServiceInstanceDashElement;

/**
 * Properties section for cloud service elements
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class CloudServiceGeneralPropertiesSection extends AbstractBdeGeneralPropertiesSection {

	@Override
	protected BootDashElementPropertyControl[] createPropertyControls() {
		return new BootDashElementPropertyControl[] {
				//textProperty("Name:", (e) -> e.getName()),
				textProperty("Service:", (e) -> e.getService()),
				textProperty("Plan:", (e) -> e.getPlan()),
				textProperty("Description:",  (e) -> e.getDescription()),
				urlProperty("Docs:", (e) -> e.getDocumentationUrl()),
				urlProperty("URL:", (e) -> e.getUrl()),
				new TagsPropertyControl()
		};
	}

	private AbstractBdePropertyControl urlProperty(String label, Function<CloudServiceInstanceDashElement, String> getter) {
		return new UrlPropertyControl<>(CloudServiceInstanceDashElement.class, label, getter);
	}

	private AbstractBdePropertyControl textProperty(String label, Function<CloudServiceInstanceDashElement, String> getter) {
		return new BdeReadOnlyTextPropertyControl<>(CloudServiceInstanceDashElement.class, label, getter);
	}

}
