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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertiesMapper;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

public class ProjectAppStore {

	private final IPropertyStore modelStore;

	public ProjectAppStore(IPropertyStore modelStore) {
		this.modelStore = modelStore;
	}

	public synchronized Map<String, String> getMapping() {
		Map<String, String> existingProjectToAppMappings = new HashMap<String, String>();
		PropertiesMapper<Map<String, String>> propertiesMapper = new PropertiesMapper<Map<String, String>>();
		String storedVal = this.modelStore.get(CloudFoundryBootDashModel.APP_TO_PROJECT_MAPPING);
		if (storedVal != null) {
			Map<String, String> mappings = propertiesMapper.convert(storedVal);
			if (mappings != null) {
				existingProjectToAppMappings.putAll(mappings);
			}
		}
		return existingProjectToAppMappings;
	}

	public synchronized void storeProjectToAppMapping(Collection<CloudAppDashElement> updated) throws Exception {
		Map<String, String> projectsToApps = new HashMap<String, String>();
		if (updated != null) {
			for (BootDashElement element : updated) {
				IProject project = element.getProject();
				if (project != null) {
					projectsToApps.put(element.getName(), project.getName());
				}
			}
		}
		storeProjectToAppMapping(projectsToApps);
	}

	public void storeProjectToAppMapping(Map<String, String> projectsToApps) throws Exception {
		PropertiesMapper<Map<String, String>> propertiesMapper = new PropertiesMapper<Map<String, String>>();
		String asValue = propertiesMapper.convertToString(projectsToApps);
		modelStore.put(CloudFoundryBootDashModel.APP_TO_PROJECT_MAPPING, asValue);
	}
}