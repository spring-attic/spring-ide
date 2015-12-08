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
package org.springframework.ide.eclipse.boot.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
	private HashMap<String, SpringBootStarter> byId;
	private HashMap<MavenId, SpringBootStarter> byMavenId;

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

	public MavenId getMavenId(String findId) {
		ensureIndexes();
		SpringBootStarter starter = byId.get(findId);
		if (starter!=null) {
			return starter.getMavenId();
		}
		return null;
	}

	/**
	 * Ensures that the indexes 'byId' and 'byMavenId' have been created. Any method using
	 * one of the indexes should call this method first.
	 */
	private synchronized void ensureIndexes() {
		if (byId==null) {
			byId = new HashMap<>();
			byMavenId = new HashMap<>();
			for (DependencyInfo dep : dependencySpec.getDependencies()) {
				String id = dep.getId();
				String groupId = dep.getGroupId();
				String artifactId = dep.getArtifactId();
				String scope = dep.getScope();
				if (id!=null && groupId!=null && artifactId!=null) {
					MavenId mid = new MavenId(groupId, artifactId);
					//ignore invalid looking entries. Should at least have an id, aid and gid
					SpringBootStarter starter = new SpringBootStarter(id, mid, scope);
					byId.put(id, starter);
					byMavenId.put(mid, starter);
				}
			}
		}
	}

	public String getBootVersion() {
		return bootVersion;
	}

	public boolean contains(String id) {
		return getMavenId(id)!=null;
	}

	public List<String> getStarterIds() {
		ensureIndexes();
		return Collections.unmodifiableList(new ArrayList<>(byId.keySet()));
	}

	public synchronized String getId(MavenId mavenId) {
		ensureIndexes();
		SpringBootStarter starter = byMavenId.get(mavenId);
		if (starter!=null) {
			return starter.getId();
		}
		return null;
	}

	public SpringBootStarter getStarter(MavenId mavenId) {
		ensureIndexes();
		return byMavenId.get(mavenId);
	}

	public SpringBootStarter getStarter(String id) {
		ensureIndexes();
		return byId.get(id);
	}

}