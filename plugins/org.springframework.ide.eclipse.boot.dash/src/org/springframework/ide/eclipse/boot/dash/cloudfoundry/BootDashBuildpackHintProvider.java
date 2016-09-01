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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Provider;

import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.editor.support.yaml.schema.BasicYValueHint;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YValueHint;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

import com.google.common.collect.ImmutableSet;

public class BootDashBuildpackHintProvider implements Provider<Collection<YValueHint>> {

	private BootDashViewModel model;

	private static final String REMOVE_PATTERN = "api.run";

	public static final YValueHint[] DEFAULT_BUILDPACK_VALUES = new YValueHint[] { createHint("java_buildpack"),
			createHint("ruby_buildpack"), createHint("staticfile_buildpack"), createHint("nodejs_buildpack"),
			createHint("python_buildpack"), createHint("php_buildpack"), createHint("liberty_buildpack"),
			createHint("binary_buildpack"), createHint("go_buildpack") };

	public BootDashBuildpackHintProvider(BootDashViewModel model) {
		this.model = model;
	}

	@Override
	public Collection<YValueHint> get() {
		LiveSetVariable<RunTarget> runTargets = model.getRunTargets();
		Set<YValueHint> buildPacks = new LinkedHashSet<>();
		if (runTargets != null) {
			ImmutableSet<RunTarget> targetValues = runTargets.getValue();
			for (RunTarget target : targetValues) {
				if (target instanceof CloudFoundryRunTarget) {
					CloudFoundryRunTarget cfTarget = (CloudFoundryRunTarget) target;
					try {
						Collection<String> targetBuildpacks = cfTarget.getBuildpackValues();
						if (targetBuildpacks != null) {
							for (String existingBp : targetBuildpacks) {
								YValueHint ymlBuildpack = createHint(existingBp, getLabel(existingBp, cfTarget));
								buildPacks.add(ymlBuildpack);
							}
						}
					} catch (Exception e) {
						Log.log(e);
					}
				}
			}
		}

		if (buildPacks.isEmpty()) {
			return Arrays.asList(DEFAULT_BUILDPACK_VALUES);
		}
		return buildPacks;
	}

	public static YValueHint createHint(String value, String label) {
		return  label != null ? new BasicYValueHint(value, label) : new BasicYValueHint(value);
	}

	public static YValueHint createHint(String value) {
		return createHint(value, null);
	}

	protected String getLabel(String buildpack, CloudFoundryRunTarget target) {
		if (target.isPWS()) {
			return buildpack;
		}

		return buildpack + " (" + getUrlSegmentForLabel(target.getUrl()) + ')';
	}

	protected String getUrlSegmentForLabel(String url) {
		if (url != null) {
			try {
				URI uri = new URI(url);
				String authority = uri.getAuthority();

				int removePatternOffset = REMOVE_PATTERN.length() + 1;

				if (authority != null && authority.startsWith(REMOVE_PATTERN)
						&& removePatternOffset < authority.length()) {
					return authority.substring(removePatternOffset, authority.length());
				}
			} catch (URISyntaxException e) {
				// Ignore. return original URL.
			}
		}
		return url;
	}
}
