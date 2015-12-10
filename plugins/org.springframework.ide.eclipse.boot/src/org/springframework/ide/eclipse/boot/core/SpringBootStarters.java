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
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.ide.eclipse.boot.core.dialogs.InitializrDependencySpec;
import org.springframework.ide.eclipse.boot.core.dialogs.InitializrDependencySpec.DependencyInfo;
import org.springframework.ide.eclipse.boot.core.dialogs.InitializrDependencySpec.RepoInfo;
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

	private InitializrDependencySpec dependencySpec;
	private InitializrServiceSpec initializrSpec;
	private HashMap<String, SpringBootStarter> byId;
	private HashMap<MavenId, SpringBootStarter> byMavenId;

	public SpringBootStarters(InitializrServiceSpec initializrSpec, InitializrDependencySpec dependencySpec) {
		this.dependencySpec = dependencySpec;
		this.initializrSpec = initializrSpec;
	}

	public SpringBootStarters(URL initializerUrl, URL dependencyUrl, URLConnectionFactory urlConnectionFactory) throws Exception {
		this(
				InitializrServiceSpec.parseFrom(urlConnectionFactory, initializerUrl),
				InitializrDependencySpec.parseFrom(urlConnectionFactory, dependencyUrl)
		);
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
		HashMap<String, IMavenCoordinates> bomsById = new HashMap<>();
		for (Entry<String, DependencyInfo> e : dependencySpec.getBoms().entrySet()) {
			DependencyInfo bomInfo = e.getValue();
			IMavenCoordinates bom = new MavenCoordinates(bomInfo.getGroupId(), bomInfo.getArtifactId(), bomInfo.getClassifier(), bomInfo.getVersion());
			bomsById.put(e.getKey(), bom);
		}

		if (byId==null) {
			byId = new HashMap<>();
			byMavenId = new HashMap<>();
			for (Entry<String, DependencyInfo> e : dependencySpec.getDependencies().entrySet()) {
				String id = e.getKey();
				DependencyInfo dep = e.getValue();
				String groupId = dep.getGroupId();
				String artifactId = dep.getArtifactId();
				String scope = dep.getScope();
				String bom = dep.getBom();
				if (id!=null && groupId!=null && artifactId!=null) {
					//ignore invalid looking entries. Should at least have an id, aid and gid
					SpringBootStarter starter = new SpringBootStarter(id, new MavenCoordinates(dep), scope, bomsById.get(bom));
					byId.put(id, starter);
					byMavenId.put(new MavenId(groupId, artifactId), starter);
				}
			}
		}
	}

	public String getBootVersion() {
		return dependencySpec.getBootVersion();
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

	public Map<String, RepoInfo> getRepos() {
		return dependencySpec.getRepositories();
	}

}