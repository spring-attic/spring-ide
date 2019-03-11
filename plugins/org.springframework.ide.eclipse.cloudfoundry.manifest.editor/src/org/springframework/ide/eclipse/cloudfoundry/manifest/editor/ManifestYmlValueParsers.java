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
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.editor.support.util.ValueParseException;
import org.springframework.ide.eclipse.editor.support.util.ValueParser;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Methods and constants to create/get parsers for some atomic types
 * used in manifest yml schema.
 *
 * @author Kris De Volder
 */
public class ManifestYmlValueParsers {

	public static final ValueParser MEMORY = new ValueParser() {

		private final ImmutableSet<String> GIGABYTE = ImmutableSet.of("G", "GB");
		private final ImmutableSet<String> MEGABYTE = ImmutableSet.of("M", "MB");
		private final Set<String> UNITS = Sets.union(GIGABYTE, MEGABYTE);

		@Override
		public Object parse(String str) throws Exception {
			str = str.trim();
			String unit = getUnit(str.toUpperCase());
			if (unit==null) {
				throw new ValueParseException(
						"'"+str+"' doesn't end with a valid unit of Memory ('M', 'MB', 'G' or 'GB')"
				);
			}
			str = str.substring(0, str.length()-unit.length());
			int unitSize = GIGABYTE.contains(unit)?1024:1;
			int value = Integer.parseInt(str);
			if (value<0) {
				throw new ValueParseException("Negative value is not allowed");
			}
			return value * unitSize;
		}

		private String getUnit(String str) {
			for (String u : UNITS) {
				if (str.endsWith(u)) {
					return u;
				}
			}
			return null;
		}
	};

}
