/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemSeverity.ERROR;
import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemSeverity.WARNING;

import java.util.ArrayList;

/**
 * @author Kris De Volder
 */
public enum ProblemType {

	// Naming:
	//   YAML_* for all problems in .yml files.
	//   PROP_* for all problems in .properties files.
	// All enum values must start with one or the other (or some stuff will break!).

	PROP_INVALID_BEAN_NAVIGATION("Accessing a 'bean property' in a type that doesn't have properties (e.g. like String or Integer)"),
	PROP_INVALID_INDEXED_NAVIGATION("Accessing a property using [] in a type that doesn't support that"),
	PROP_EXPECTED_DOT_OR_LBRACK("Unexpected character found where a '.' or '[' was expected"),
	PROP_NO_MATCHING_RBRACK("Found a '[' but no matching ']'"),
	PROP_NON_INTEGER_IN_BRACKETS("Use of [..] navigation with non-integer value"),
	PROP_VALUE_TYPE_MISMATCH("Expecting a value of a certain type, but value doesn't parse as such"),
	PROP_INVALID_BEAN_PROPERTY("Accessing a named property in a type that doesn't provide a property accessor with that name"),
	PROP_UNKNOWN_PROPERTY(WARNING, "Property-key not found in any configuration metadata on the project's classpath"),

	YAML_SYNTAX_ERROR("Error parsing the input using snakeyaml"),
	YAML_UNKNOWN_PROPERTY(WARNING, "Property-key not found in the configuration metadata on the project's classpath"),
	YAML_VALUE_TYPE_MISMATCH("Expecting a value of a certain type, but value doesn't parse as such"),
	YAML_EXPECT_SCALAR("Expecting a 'scalar' value but found something more complex."),
	YAML_EXPECT_TYPE_FOUND_SEQUENCE("Found a 'sequence' node where a non 'list-like' type is expected"),
	YAML_EXPECT_TYPE_FOUND_MAPPING("Found a 'mapping' node where a type that can't be treated as a 'property map' is expected"),
	YAML_EXPECT_MAPPING("Expecting a 'mapping' node but found something else"),
	YAML_EXPECT_BEAN_PROPERTY_NAME("Expecting a 'bean property' name but found something more complex"),
	YAML_INVALID_BEAN_PROPERTY("Accessing a named property in a type that doesn't provide a property accessor with that name");

	private final ProblemSeverity defaultSeverity;
	private String description;
	private String label;

	private ProblemType(ProblemSeverity defaultSeverity, String description, String label) {
		this.description = description;
		this.defaultSeverity = defaultSeverity;
		this.label = label;
	}

	private ProblemType(ProblemSeverity defaultSeverity, String description) {
		this(defaultSeverity, description, null);
	}

	private ProblemType(String description) {
		this(ERROR, description);
	}

	public ProblemSeverity getDefaultSeverity() {
		return defaultSeverity;
	}

	public static final ProblemType[] FOR_YAML = withPrefix("YAML_");
	public static final ProblemType[] FOR_PROPERTIES = withPrefix("PROP_");

	public static ProblemType[] forProperties() {
		return withPrefix("PROP_");
	}

	private static ProblemType[] withPrefix(String prefix) {
		ProblemType[] allValues = values();
		ArrayList<ProblemType> values = new ArrayList<ProblemType>(allValues.length);
		for (ProblemType v : allValues) {
			if (v.toString().startsWith(prefix)) {
				values.add(v);
			}
		}
		return values.toArray(new ProblemType[values.size()]);
	}

	public String getLabel() {
		if (label==null) {
			label = createDefaultLabel();
		}
		return label;
	}

	public String getDescription() {
		return description;
	}

	private String createDefaultLabel() {
		String label = this.toString().substring(5).toLowerCase().replace('_', ' ');
		return Character.toUpperCase(label.charAt(0)) + label.substring(1);
	}
}
