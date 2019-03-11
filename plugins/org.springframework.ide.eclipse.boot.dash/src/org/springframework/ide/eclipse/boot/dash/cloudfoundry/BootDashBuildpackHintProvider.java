/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.inject.Provider;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.BuildpackHintGenerator.BuildpackInfo;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YValueHint;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

import com.google.common.collect.ImmutableSet;

public class BootDashBuildpackHintProvider implements Provider<Collection<YValueHint>> {

	private BootDashViewModel model;

	private BuildpackHintGenerator hintGenerator;

	public BootDashBuildpackHintProvider(BootDashViewModel model, BuildpackHintGenerator hintGenerator) {
		this.model = model;
		this.hintGenerator = hintGenerator;
	}

	@Override
	public Collection<YValueHint> get() {
		LiveSetVariable<RunTarget> runTargets = model.getRunTargets();

		Collection<YValueHint> hints = new HashSet<>();

		if (runTargets != null) {
			ImmutableSet<RunTarget> targetValues = runTargets.getValue();
			List<BuildpackInfo> buildPackInfos = getBuildpackInfos(targetValues);

			if (hintGenerator != null) {
				hints = hintGenerator.getHints(buildPackInfos);
			}
		}
		return hints;
	}

	private List<BuildpackInfo> getBuildpackInfos(ImmutableSet<RunTarget> targetValues) {
		List<BuildpackInfo> buildpackInfos = new ArrayList<>();

		// Create the buildpack -> apiLabels map
		for (RunTarget target : targetValues) {
			if (target instanceof CloudFoundryRunTarget) {
				BuildpackInfo buildpackInfo = getBuildpackInfo(target);
				if (buildpackInfo != null) {
					buildpackInfos.add(buildpackInfo);
				}
			}
		}
		return buildpackInfos;
	}

	private BuildpackInfo getBuildpackInfo(RunTarget target) {
		try {
			return new BuildpackInfo((CloudFoundryRunTarget) target);
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}
}
