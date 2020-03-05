/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Predicates for testing file paths. Paths should be always separated with '/' and folders assumed to have trailing '/'
 *
 * @author Alex Boyko
 *
 */
public class PathSelectors {

	public static Predicate<String> rootFiles() {
		return path -> {
			return path.indexOf('/') < 0;
		};
	}

	public static Predicate<String> pattern(String glob) {
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
		return path -> {
			String[] pathArray = path.split("/");
			Path p = null;
			if (pathArray.length == 1) {
				p = Paths.get(pathArray[0]);
			} else if (pathArray.length > 1) {
				p = Paths.get(pathArray[0], Arrays.copyOfRange(pathArray, 1, pathArray.length));
			}
			if (p == null) {
				return false;
			} else {
				boolean matches = pathMatcher.matches(p);
				return matches;
			}
		};
	}

}
