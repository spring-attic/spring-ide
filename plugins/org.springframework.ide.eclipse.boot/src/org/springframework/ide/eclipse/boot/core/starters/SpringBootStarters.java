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
package org.springframework.ide.eclipse.boot.core.starters;

import java.net.URL;
import java.util.HashMap;

import org.springframework.ide.eclipse.boot.core.StarterId;
import org.springframework.ide.eclipse.boot.core.dialogs.InitializrDependencySpec;
import org.springframework.ide.eclipse.boot.core.dialogs.InitializrDependencySpec.DependencyInfo;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec.DependencyGroup;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;

/**
 * A model containing data / knowledge about the 'starters' for a given spring boot version.
 * <p>
 * This knowledge is obtained from the 'intilizr' web service (start.spring.io).
 *
 * @author Kris De Volder
 */
public class SpringBootStarters {

	private String bootVersion; //TODO: Actually, this is not used and the info provided by this model is not version-specific.
	private URL intializerUrl;
	private URLConnectionFactory urlConnectionFactory;

	private InitializrDependencySpec dependencySpec;
	private InitializrServiceSpec initializrSpec;
	private HashMap<String, StarterId> idToMavenId;

	public SpringBootStarters(String bootVersion, URL initializerUrl, URLConnectionFactory urlConnFac) throws Exception {
		this.bootVersion = bootVersion;
		this.intializerUrl = initializerUrl;
		this.urlConnectionFactory = urlConnFac;

		this.initializrSpec = InitializrServiceSpec.parseFrom(urlConnectionFactory, initializerUrl);
		this.dependencySpec = InitializrDependencySpec.parseFrom(urlConnectionFactory, initializerUrl+"/dependencies");
	}

	public DependencyGroup[] getDependencyGroups() {
		return initializrSpec.getDependencies();
	}

	public StarterId getMavenId(String findId) {
		if (idToMavenId==null) {
			idToMavenId = new HashMap<String, StarterId>();
			for (DependencyInfo dep : dependencySpec.getDependencies()) {
				String id = dep.getId();
				String groupId = dep.getGroupId();
				String artifactId = dep.getArtifactId();
				if (id!=null && groupId!=null && artifactId!=null) {
					idToMavenId.put(dep.getId(), new StarterId(groupId, artifactId));
				}
			}
		}
		return idToMavenId.get(findId);
	}

}