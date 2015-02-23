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

/**
 * @author Kris De Volder
 */
public class TypeUtilTests extends SpringPropertiesEditorTestHarness {

	private Type getPropertyType(Type type, String propName) {
		List<TypedProperty> props = engine.getTypeUtil().getProperties(type);
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
	}

	private void assertType(String expectedType, Type actualType) {
		assertEquals(TypeParser.parse(expectedType), actualType);
	}

}
