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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.ide.eclipse.editor.support.yaml.schema.BasicYValueHint;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YValueHint;

public class BuildpackHintGenerator {
	private static final String REMOVE_PATTERN = "api.run";

	public static final List<YValueHint> DEFAULT_BUILDPACK_VALUES = Arrays.asList(new YValueHint[] {
			createHint("java_buildpack"), createHint("ruby_buildpack"), createHint("staticfile_buildpack"),
			createHint("nodejs_buildpack"), createHint("python_buildpack"), createHint("php_buildpack"),
			createHint("liberty_buildpack"), createHint("binary_buildpack"), createHint("go_buildpack") });

	public Collection<YValueHint> getHints(List<BuildpackInfo> infos) {
		Set<String> allApiLabels = new HashSet<>();
		Map<String, Set<String>> buildPacksWithApiLabels = new HashMap<>();

		Set<YValueHint> hints = new HashSet<>();
		// Hint creation description:
		// - Create hints with api labels ONLY if the buildpack is NOT found in
		// all target apis. The purpose of the label is to show which apis the
		// buildpac is applicable
		// - Example: if there are three targets: pivotal.io, pez.io,
		// custompcf.io, and "java_buildpack" is applicable
		// to only pivotal.io and pez.io, the hint label will be:
		// "java_buildpack (pivotal.io, pez.io).
		// - Example: same three targets, but "nodejs_buildpack" is applicable
		// to ALL three apis, then the label will simply
		// be "nodejs_buildpack" with no additional API information

		// Create the buildpack -> apiLabels map
		if (infos != null) {
			for (BuildpackInfo info : infos) {

				String apiLabel = getApiLabel(info.getTargetUrl());
				allApiLabels.add(apiLabel);

				Collection<String> targetBuildpacks = info.getBuildpacks();
				if (targetBuildpacks != null) {
					for (String buildPack : targetBuildpacks) {
						Set<String> applicableApis = buildPacksWithApiLabels.get(buildPack);
						if (applicableApis == null) {
							applicableApis = new HashSet<>();
							buildPacksWithApiLabels.put(buildPack, applicableApis);
						}
						applicableApis.add(apiLabel);
					}
				}
			}
		}

		// create the hints
		for (Entry<String, Set<String>> entry : buildPacksWithApiLabels.entrySet()) {
			String buildpack = entry.getKey();
			String label = getLabel(buildpack, entry.getValue(), allApiLabels);

			YValueHint hint = createHint(buildpack, label);
			hints.add(hint);
		}

		if (hints.isEmpty()) {
			return DEFAULT_BUILDPACK_VALUES;
		}

		return hints;
	}

	public static YValueHint createHint(String value, String label) {
		return label != null ? new BasicYValueHint(value, label) : new BasicYValueHint(value);
	}

	public static YValueHint createHint(String value) {
		return createHint(value, null);
	}

	protected String getLabel(String buildpack, Set<String> apiLabelsForBuildpack, Set<String> allApiLabels) {
		// If the list of apis for the buildpack equals the total number of apis, do not add an additional label as it
		// means the buildpack is applicable to all apis
		if (apiLabelsForBuildpack == null || apiLabelsForBuildpack.size() == allApiLabels.size()
				|| apiLabelsForBuildpack.size() == 0) {
			return buildpack;
		}

		StringBuffer buf = new StringBuffer();
		buf.append(buildpack);
		buf.append(' ');
		buf.append('(');
		int i = 0;
		for (String bp : apiLabelsForBuildpack) {
			buf.append(bp);
			if (i < apiLabelsForBuildpack.size() - 1) {
				buf.append(',');
				buf.append(' ');
			}
			i++;
		}

		buf.append(')');
		return buf.toString();
	}

	protected String getApiLabel(String url) {
		if (url != null) {
			try {
				URI uri = new URI(url);

				String toTrim = uri.getAuthority();
				if (toTrim == null) {
					toTrim = uri.toString();
				}
				int removePatternOffset = REMOVE_PATTERN.length() + 1;

				if (toTrim != null && toTrim.startsWith(REMOVE_PATTERN) && removePatternOffset < toTrim.length()) {
					return toTrim.substring(removePatternOffset, toTrim.length());
				}
			} catch (URISyntaxException e) {
				// Ignore. return original URL.
			}
		}
		return url;
	}

	public static class BuildpackInfo {

		private final String targetUrl;
		private final Collection<String> buildpacks;

		public BuildpackInfo(CloudFoundryRunTarget target) throws Exception {
			this(target.getUrl(), target.getBuildpackValues());
		}

		public BuildpackInfo(String targetUrl, Collection<String> buildpacks) {
			this.targetUrl = targetUrl;
			this.buildpacks = buildpacks;
		}

		public String getTargetUrl() {
			return targetUrl;
		}

		public Collection<String> getBuildpacks() {
			return buildpacks;
		}
	}
}
