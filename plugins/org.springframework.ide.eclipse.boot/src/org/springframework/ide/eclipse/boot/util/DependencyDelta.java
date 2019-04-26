/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.util;

import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCIES;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCY;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.findChilds;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.getChild;

import java.io.StringReader;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.ide.eclipse.boot.core.MavenId;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableMap;

@SuppressWarnings("restriction")
public class DependencyDelta {

	private final Set<MavenId> removed;
	public final Map<MavenId, Optional<String>> added; // mavenId -> scope mapping

	public DependencyDelta(Set<MavenId> removed, Map<MavenId, Optional<String>> added) {
		super();
		this.removed = removed;
		this.added = added;
	}

	public static DependencyDelta create(String referencePom, String targetPom) throws Exception {
		Map<MavenId, Optional<String>> referenceDeps = parseDependencies(referencePom);
		Map<MavenId, Optional<String>> targetDeps = parseDependencies(targetPom);
		Set<MavenId> removed = new HashSet<>();
		Map<MavenId, Optional<String>> added = new LinkedHashMap<>(); //use linked hashmap. Not really important, but gives more predictable/stable ordering
		for (MavenId oldDep : referenceDeps.keySet()) {
			if (targetDeps.containsKey(oldDep)) {
				//it still exists
			} else {
				removed.add(oldDep);
			}
		}
		for (Entry<MavenId, Optional<String>> newDep : targetDeps.entrySet()) {
			if (referenceDeps.containsKey(newDep.getKey())) {
				//it already existed before
			} else {
				added.put(newDep.getKey(), newDep.getValue());
			}
		}
		return new DependencyDelta(removed, added);
	}

	private static Map<MavenId, Optional<String>> parseDependencies(String pomContents) throws Exception {
		ImmutableMap.Builder<MavenId, Optional<String>> builder = ImmutableMap.builder();
		Document pom = parsePom(pomContents);
		Element depsEl = getChild(
				pom.getDocumentElement(), DEPENDENCIES);
		List<Element> deps = findChilds(depsEl, DEPENDENCY);
		for (Element dep : deps) {
			String gid = PomUtils.getGroupId(dep);
			String aid = PomUtils.getArtifactId(dep);
			if (aid!=null && gid!=null) {
				builder.put(new MavenId(gid, aid), Optional.ofNullable(PomUtils.getScope(dep)));
			}
		}
		return builder.build();
	}

	private static Document parsePom(String pomContents) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		return dbFactory.newDocumentBuilder().parse(new InputSource(new StringReader(pomContents)));
	}

	public boolean isRemoved(MavenId id) {
		return removed.contains(id);
	}

}
