/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.yaml;

import java.util.Collections;

import org.springframework.ide.eclipse.editor.support.yaml.path.KeyAliases;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;

public class ApplicationYamlStructureProvider {

	private static final KeyAliases KEY_ALIASES = new KeyAliases() {
		@Override
		public Iterable<String> getKeyAliases(String base) {
			String camelCased = StringUtil.hyphensToCamelCase(base, false);
			if (!camelCased.equals(base)) {
				return Collections.singletonList(camelCased);
			}
			return Collections.emptyList();
		}
	};

	public static final YamlStructureProvider INSTANCE = YamlStructureProvider.withAliases(KEY_ALIASES);

}
