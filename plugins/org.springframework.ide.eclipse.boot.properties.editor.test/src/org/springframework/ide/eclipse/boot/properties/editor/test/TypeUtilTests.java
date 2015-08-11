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
package org.springframework.ide.eclipse.boot.properties.editor.test;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypedProperty;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.BeanPropertyNameMode;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode;

/**
 * @author Kris De Volder
 */
public class TypeUtilTests extends SpringPropertiesEditorTestHarness {

	private Type getPropertyType(Type type, String propName, EnumCaseMode enumMode, BeanPropertyNameMode beanMode) {
		List<TypedProperty> props = engine.getTypeUtil().getProperties(type, enumMode, beanMode);
		for (TypedProperty prop : props) {
			if (prop.getName().equals(propName)) {
				return prop.getType();
			}
		}
		return null;
	}

	public void testGetProperties() throws Exception {
		IProject p = createPredefinedProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);
		assertNotNull(jp.findType("demo.Color"));
		assertNotNull(jp.findType("demo.ColorData"));


		Type data = TypeParser.parse("demo.ColorData");

		assertType("java.lang.Double",
				getPropertyType(data, "wavelen"));
		assertType("java.lang.String",
				getPropertyType(data, "name"));
		assertType("demo.Color",
				getPropertyType(data, "next"));
		assertType("demo.ColorData",
				getPropertyType(data, "nested"));
		assertType("java.util.List<java.lang.String>",
				getPropertyType(data, "tags"));
		assertType("java.util.Map<java.lang.String,demo.ColorData>",
				getPropertyType(data, "mapped-children"));
		assertType("java.util.Map<demo.Color,demo.ColorData>",
				getPropertyType(data, "color-children"));

		//Also gets aliased as camelCased names?
		assertType("java.util.Map<demo.Color,demo.ColorData>",
				getPropertyType(data, "colorChildren"));
		assertType("java.util.Map<java.lang.String,demo.ColorData>",
				getPropertyType(data, "mappedChildren"));

		//Gets aliased names only if asked for it?
		assertType("java.util.Map<java.lang.String,demo.ColorData>",
				getPropertyType(data, "mappedChildren", EnumCaseMode.ORIGNAL, BeanPropertyNameMode.CAMEL_CASE));
		assertType(null,
				getPropertyType(data, "mappedChildren", EnumCaseMode.ORIGNAL, BeanPropertyNameMode.HYPHENATED));
		assertType(null,
				getPropertyType(data, "mapped-children", EnumCaseMode.ORIGNAL, BeanPropertyNameMode.CAMEL_CASE));
		assertType("java.util.Map<java.lang.String,demo.ColorData>",
				getPropertyType(data, "mapped-children", EnumCaseMode.ORIGNAL, BeanPropertyNameMode.HYPHENATED));

	}

	public void testGetEnumKeyedProperties() throws Exception {
		IProject p = createPredefinedProject("demo-enum");
		IJavaProject jp = JavaCore.create(p);
		useProject(jp);

		Type data = TypeParser.parse("java.util.Map<demo.Color,Something>");
		assertType("Something", getPropertyType(data, "red"));
		assertType("Something", getPropertyType(data, "green"));
		assertType("Something", getPropertyType(data, "blue"));
		assertType("Something", getPropertyType(data, "RED"));
		assertType("Something", getPropertyType(data, "GREEN"));
		assertType("Something", getPropertyType(data, "BLUE"));
		assertNull(getPropertyType(data, "not-a-color"));
	}

	private Type getPropertyType(Type type, String propName) {
		return getPropertyType(type, propName, EnumCaseMode.ALIASED, BeanPropertyNameMode.ALIASED);
	}

	private void assertType(String expectedType, Type actualType) {
		assertEquals(TypeParser.parse(expectedType), actualType);
	}

}
