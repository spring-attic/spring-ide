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
package org.springframework.ide.eclipse.boot.properties.editor.util;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.CachingValueProvider;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.StsValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.eclipse.boot.util.JavaProjectUtil;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.editor.support.util.FuzzyMatcher;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;

/**
 * @author Kris De Volder
 */
public class ResourceHintProvider implements ValueProviderStrategy {

//	private static <T> Consumer<T> dbg(String msg) {
//		if (DEBUG) {
//			return (T x) -> {
//				System.out.println(msg+": "+x);
//			};
//		}
//		return (T x) -> {};
//	}

	private static final Pattern EXCLUDED = Pattern.compile(".*\\.java$");

	private static String[] CLASSPATH_PREFIXES = {
			"classpath:",
			"classpath*:"
	};

	private static final String[] URL_PREFIXES = new String[] {
			"classpath:",
			"classpath*:",
			"file:",
			"http://",
			"https://"
	};

	@Override
	public Flux<StsValueHint> getValues(IJavaProject javaProject, String query) {
		for (String prefix : CLASSPATH_PREFIXES) {
			if (query.startsWith(prefix)) {
				return classpathHints
				.getValues(javaProject, query.substring(prefix.length()))
				.map((hint) -> hint.prefixWith(prefix));
			}
		}
		return Flux.fromIterable(urlPrefixHints);
	}

	final private ImmutableList<StsValueHint> urlPrefixHints = ImmutableList.copyOf(
			Arrays.stream(URL_PREFIXES)
			.map(StsValueHint::create)
			.collect(Collectors.toList())
	);

	private ClasspathHints classpathHints = new ClasspathHints();

	private static class ClasspathHints extends CachingValueProvider {
		@Override
		protected Flux<StsValueHint> getValuesAsycn(IJavaProject javaProject, String query) {
			return getClasspathResourcePaths(javaProject)
			.map((path) -> path.toString())
			.filter((path) ->
				!EXCLUDED.matcher(path).matches() &&
				0!=FuzzyMatcher.matchScore(query, path)
			)
			.distinct()
			.map((path) -> StsValueHint.create(path));
		}

		private Flux<IPath> getClasspathResourcePaths(IJavaProject javaProject) {
			return Flux.fromArray(JavaProjectUtil.getSourceFolders(javaProject, true))
			.flatMap((sourceFolder) -> {
				int chopSegments = sourceFolder.getProjectRelativePath().segmentCount();
				return getResourcePaths(sourceFolder)
				.map((path) -> path.removeFirstSegments(chopSegments));
			});
		}

		private Flux<IPath> getResourcePaths(IContainer folder) {
			return getMembers(folder)
			.flatMap((IResource member) -> {
				Flux<IPath> allPaths = Flux.just(member.getProjectRelativePath());
				if (member instanceof IContainer) {
					allPaths = allPaths.concatWith(getResourcePaths((IContainer)member));
				}
				return allPaths;
			});
		}

		private Flux<IResource> getMembers(IContainer folder) {
			try {
				return Flux.fromArray(folder.members());
			} catch (CoreException e) {
				Log.log(e);
				return Flux.empty();
			}
		}
	}


}
