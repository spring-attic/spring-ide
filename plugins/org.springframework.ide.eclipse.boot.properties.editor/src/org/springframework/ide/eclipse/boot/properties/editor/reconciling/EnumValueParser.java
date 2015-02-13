/*******************************************************************************
 * Copyright (c) 2014-2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine.ValueParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;

/**
 * Parser for checking a 'Enum' type value in the {@link SpringPropertiesReconcileEngine}.
 *
 * @author Kris De Volder
 */
public class EnumValueParser extends ValueParser {

	private String typeName;
	private HashSet<String> values;

	public EnumValueParser(String typeName, String[] values) {
		this.typeName = typeName;
		this.values = new HashSet<String>(Arrays.asList(values));
	}

	@Override
	Object parse(String str) {
		if (values.contains(str)) {
			return str;
		} else {
			throw new IllegalArgumentException("'"+str+"' is not valid for Enum '"+typeName+"'");
		}
	}

}
