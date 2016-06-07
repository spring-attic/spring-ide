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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.BasicYValueHint;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.YValueHint;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

public class BootDashBuildpackHintProvider implements Provider<Collection<YValueHint>> {

	private BootDashViewModel model;

	public BootDashBuildpackHintProvider(BootDashViewModel model) {
		this.model = model;
	}

	@Override
	public Collection<YValueHint> get() {
		LiveSetVariable<RunTarget> runTargets = model.getRunTargets();
		Set<YValueHint> buildPacks = new HashSet<>();
		if (runTargets != null) {
			for (RunTarget target : runTargets.getValue()) {
				if (target instanceof CloudFoundryRunTarget) {
					CloudFoundryRunTarget cfTarget = (CloudFoundryRunTarget) target;
					try {
						List<CFBuildpack> targetBuildpacks = cfTarget.getBuildpacks();
						if (targetBuildpacks != null) {
							for (CFBuildpack existingBp : targetBuildpacks) {
								YValueHint ymlBuildpack = new BasicYValueHint(existingBp.getName(), cfTarget.getUrl());
								buildPacks.add(ymlBuildpack);
							}
						}
					} catch (Exception e) {
						Log.log(e);
					}
				}
			}
		}
		return buildPacks;
	}

}
