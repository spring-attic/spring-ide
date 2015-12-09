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
package org.springframework.ide.eclipse.boot.core.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.IMavenCoordinates;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.MavenId;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;

public abstract class SpringBootProject implements ISpringBootProject {

	static final List<SpringBootStarter> NO_STARTERS = Collections.emptyList();

	private SpringBootStarters cachedInfos;

	@Override
	public List<SpringBootStarter> getKnownStarters() {
		SpringBootStarters infos = getStarterInfos();
		if (infos!=null) {
			List<String> knownIds = infos.getStarterIds();
			List<SpringBootStarter> starters = new ArrayList<>(knownIds.size());
			for (String id : knownIds) {
				SpringBootStarter starter = infos.getStarter(id);
				starters.add(starter);
			}
			return starters;
		}
		return NO_STARTERS;
	}

	@Override
	public SpringBootStarters getStarterInfos() {
		try {
			String bootVersion = getBootVersion();
			if (bootVersion!=null) {
				SpringBootStarters infos = cachedInfos;
				if (infos!=null && bootVersion.equals(infos.getBootVersion())) {
					return infos;
				}
				return cachedInfos = new SpringBootStarters(
						bootVersion,
						new URL(StsProperties.getInstance().get("spring.initializr.json.url")),
						WizardPlugin.getUrlConnectionFactory()
				);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	@Override
	public List<SpringBootStarter> getBootStarters() throws CoreException {
		SpringBootStarters infos = getStarterInfos();
		List<IMavenCoordinates> deps = getDependencies();
		ArrayList<SpringBootStarter> starters = new ArrayList<>();
		for (IMavenCoordinates dep : deps) {
			String aid = dep.getArtifactId();
			String gid = dep.getGroupId();
			if (aid!=null && gid!=null) {
				MavenId mavenId = new MavenId(gid, aid);
				SpringBootStarter starter = infos.getStarter(mavenId);
				if (starter!=null) {
					starters.add(starter);
				}
			}
		}
		return starters;
	}

	public boolean isKnownStarter(MavenId mavenId) {
		return getStarter(mavenId)!=null;
	}

	protected SpringBootStarter getStarter(MavenId mavenId) {
		SpringBootStarters infos = getStarterInfos();
		if (infos!=null) {
			return infos.getStarter(mavenId);
		}
		return null;
	}
}
