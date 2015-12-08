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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.ide.eclipse.boot.core.MavenId;
import org.springframework.ide.eclipse.boot.core.dialogs.InitializrDependencySpec;
import org.springframework.ide.eclipse.boot.core.dialogs.InitializrDependencySpec.DependencyInfo;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.SimpleUriBuilder;
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

	private String bootVersion;
	private URLConnectionFactory urlConnectionFactory;

	private InitializrDependencySpec dependencySpec;
	private InitializrServiceSpec initializrSpec;
	private HashMap<String, MavenId> idToMavenId;
	private HashMap<MavenId, String> mavenIdToId;

	public SpringBootStarters(String bootVersion, URL initializerUrl, URLConnectionFactory urlConnFac) throws Exception {
		this.bootVersion = bootVersion;
		this.urlConnectionFactory = urlConnFac;

		this.initializrSpec = InitializrServiceSpec.parseFrom(urlConnectionFactory, initializerUrl);
		this.dependencySpec = InitializrDependencySpec.parseFrom(urlConnectionFactory, dependencyUrl(initializerUrl));
	}

	protected String dependencyUrl(URL initializerUrl) {
		SimpleUriBuilder builder = new SimpleUriBuilder(initializerUrl.toString()+"/dependencies");
		builder.addParameter("bootVersion", bootVersion);
		return builder.toString();
	}

	public DependencyGroup[] getDependencyGroups() {
		return initializrSpec.getDependencies();
	}

	public synchronized MavenId getMavenId(String findId) {
		if (idToMavenId==null) {
			idToMavenId = new HashMap<String, MavenId>();
			for (DependencyInfo dep : dependencySpec.getDependencies()) {
				String id = dep.getId();
				String groupId = dep.getGroupId();
				String artifactId = dep.getArtifactId();
				if (id!=null && groupId!=null && artifactId!=null) {
					idToMavenId.put(dep.getId(), new MavenId(groupId, artifactId));
				}
			}
		}
		return idToMavenId.get(findId);
	}

	public String getBootVersion() {
		return bootVersion;
	}

	public boolean contains(String id) {
		return getMavenId(id)!=null;
	}

	public List<String> getStarterIds() {
		getMavenId(""); // force intializationg of the idToMavenId index
		return Collections.unmodifiableList(new ArrayList<>(idToMavenId.keySet()));
	}

	public synchronized String getId(MavenId mavenId) {
		if (mavenIdToId==null) {
			mavenIdToId = new HashMap<>();
			for (DependencyInfo dep : dependencySpec.getDependencies()) {
				String id = dep.getId();
				String groupId = dep.getGroupId();
				String artifactId = dep.getArtifactId();
				if (id!=null && groupId!=null && artifactId!=null) {
					mavenIdToId.put(new MavenId(groupId, artifactId), id);
				}
			}
		}
		return mavenIdToId.get(mavenId);
	}

}