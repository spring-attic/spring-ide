/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class, represents parsed info from a Resource, and provide method(s)
 * to display it somehow.
 */
public class SpringResource {

	public static final String FILE = "file";
	public static final String CLASS_PATH_RESOURCE = "class path resource";
	public static final String BEAN_DEFINITION_IN = "BeanDefinition defined in";
	public static final String URL = "URL";
	private static final String CF_CLASSPATH_PREFIX = "/home/vcap/app/";
	private static final String CLASS = ".class";

	private String type;
	private String path;

	private static final Pattern BRACKETS = Pattern.compile("\\[[^\\]]*\\]");

	private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
	private static final String REGEX_FQCN = ID_PATTERN + "(\\." + ID_PATTERN + ")*";

	public SpringResource(String resourceDefinition) {
		parse(resourceDefinition);
	}

	private void parse(String resourceDefinition) {
		Matcher matcher = BRACKETS.matcher(resourceDefinition);
		if (matcher.find()) {
			type = resourceDefinition.substring(0, matcher.start()).trim();
			path = resourceDefinition.substring(matcher.start() + 1, matcher.end() - 1);
			if (type.equals("file") && path.startsWith(CF_CLASSPATH_PREFIX)) {
				type = CLASS_PATH_RESOURCE;
				path = path.substring(CF_CLASSPATH_PREFIX.length());
			}
		} else if (Pattern.matches(REGEX_FQCN, resourceDefinition)) {
			// Resource is fully qualified Java type name
			type = CLASS_PATH_RESOURCE;
			path = resourceDefinition.replace('.', '/') + CLASS;
		} else {
			int beanDefInIndex = resourceDefinition.indexOf(BEAN_DEFINITION_IN);
			if (beanDefInIndex >= 0 && beanDefInIndex + BEAN_DEFINITION_IN.length() + 1 < resourceDefinition.length()) {
				String defInVal = resourceDefinition.substring(beanDefInIndex + BEAN_DEFINITION_IN.length() + 1);
				if (Pattern.matches(REGEX_FQCN, defInVal)) {
					// Resource is fully qualified Java type name
					type = BEAN_DEFINITION_IN;
					path = defInVal.replace('.', '/') + CLASS;
				}
			} else {
				path = resourceDefinition;
			}
		}
	}

	public String getResourcePath() {
		return path;
	}
	
	public String getClassName() {
		if (path != null && path.endsWith(".class")) {
			int index = path.lastIndexOf("/WEB-INF/classes/");
			int length = "/WEB-INF/classes/".length();
			if (index >= 0) {
				path = path.substring(index + length);
			}
			path = path.substring(0, path.lastIndexOf(".class"));
			path = path.replaceAll("\\\\|\\/", "."); // Tolerate both '/' and '\'.
			path = path.replace('$', '.'); // Replace inner classes '$' with JDT's '.'

			return path;
		}
		return null;
	}
}
