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
package org.springframework.ide.eclipse.boot.properties.editor.util;

import static org.springframework.ide.eclipse.boot.properties.editor.util.ArrayUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.core.BootActivator;

/**
 * Utilities to work with types represented as Strings as returned by
 * Spring config metadata apis.
 *
 * @author Kris De Volder
 */
public class TypeUtil {

	private IJavaProject javaProject;

	public TypeUtil(IJavaProject jp) {
		//Note javaProject is allowed to be null, but only in unit testing context
		// (This is so some tests can be run without an explicit jp needing to be created)
		this.javaProject = jp;
	}

	private static final Set<String> ASSIGNABLE_TYPES = new HashSet<String>(Arrays.asList(
			"java.lang.Boolean",
			"java.lang.String",
			"java.lang.Short",
			"java.lang.Integer",
			"java.lang.Long",
			"java.lan.Double",
			"java.lang.Float",
			"java.lang.Character",
			"java.util.List",
			"java.net.InetAddress",
			"java.lang.String[]"
	));

	private static final Map<String, String[]> TYPE_VALUES = new HashMap<String, String[]>();
	static {
		TYPE_VALUES.put("java.lang.Boolean", new String[] { "true", "false" });
	}

	/**
	 * @return An array of known values for a given type, or null if there's no
	 * list.
	 */
	public final String[] getValues(Type enumType) {
		if (enumType!=null) {
			try {
				String[] values = TYPE_VALUES.get(enumType.getErasure());
				if (values!=null) {
					return values;
				}
				IType type = findType(enumType.getErasure());
				if (type!=null && type.isEnum()) {
					IField[] fields = type.getFields();

					if (fields!=null) {
						ArrayList<String> enums = new ArrayList<String>(fields.length);
						for (int i = 0; i < fields.length; i++) {
							IField f = fields[i];
							if (f.isEnumConstant()) {
								enums.add(f.getElementName());
							}
						}
						return enums.toArray(new String[enums.size()]);
					}
				}
			} catch (Exception e) {
				BootActivator.log(e);
			}
		}
		return null;
	}


	/**
	 * Check if it is valid to
	 * use the notation <name>[<index>]=<value> in property file
	 * for properties of this type.
	 */
	public static boolean isBracketable(Type type) {
		//Note: map types are bracketable although the notation isn't really useful
		// for them (and a bit broken see https://github.com/spring-projects/spring-boot/issues/2386).

		//Note array types are no longer considered 'Bracketable'
		//see: STS-4031
		String erasure = type.getErasure();
		//Note: to be really correct we should use JDT infrastructure to resolve
		//type in project classpath instead of using Java reflection.
		//However, use reflection here is okay assuming types we care about
		//are part of JRE standard libraries. Using eclipse 'type hirearchy' would
		//also potentialy be very slow.
		try {
			Class<?> erasureClass = Class.forName(erasure);
			return List.class.isAssignableFrom(erasureClass);
		} catch (Exception e) {
			//type not resolveable assume its not 'array like'
			return false;
		}
	}

	public boolean isMap(Type type) {
		//Note: to be really correct we should use JDT infrastructure to resolve
		//type in project classpath instead of using Java reflection.
		//However, use reflection here is okay assuming types we care about
		//are part of JRE standard libraries. Using eclipse 'type hirearchy' would
		//also potentialy be very slow.
		String erasure = type.getErasure();
		try {
			Class<?> erasureClass = Class.forName(erasure);
			return Map.class.isAssignableFrom(erasureClass);
		} catch (Exception e) {
			//type not resolveable
			return false;
		}
	}

	/**
	 * Get domain type for a map or list generic type.
	 */
	public static Type getDomainType(Type type) {
		return lastElement(type.getParams());
	}

	public static Type getKeyType(Type mapType) {
		return firstElement(mapType.getParams());
	}

	public boolean isAssignableType(Type type) {
		return ASSIGNABLE_TYPES.contains(type.getErasure())
				|| isBracketable(type) //TODO?? Not all bracketable things are assignable are they?
				|| isEnum(type);
	}

	public boolean isEnum(Type type) {
		try {
			IType eclipseType = findType(type.getErasure());
			if (eclipseType!=null) {
				return eclipseType.isEnum();
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return false;
	}

	private IType findType(String typeName) {
		try {
			if (javaProject!=null) {
				return javaProject.findType(typeName);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	public static String formatJavaType(String type) {
		if (type!=null) {
			String primitive = TypeUtil.PRIMITIVE_TYPE_NAMES.get(type);
			if (primitive!=null) {
				return primitive;
			}
			if (type.startsWith(JAVA_LANG)) {
				return type.substring(JAVA_LANG_LEN);
			}
			return type;
		}
		return null;
	}


	private static final String JAVA_LANG = "java.lang.";
	private static final int JAVA_LANG_LEN = JAVA_LANG.length();

	private static final Map<String, String> PRIMITIVE_TYPE_NAMES = new HashMap<String, String>();

	private static final String STRING_TYPE_NAME = String.class.getName();

	static {
		TypeUtil.PRIMITIVE_TYPE_NAMES.put("java.lang.Boolean", "boolean");
		TypeUtil.PRIMITIVE_TYPE_NAMES.put("java.lang.Integer", "int");
		TypeUtil.PRIMITIVE_TYPE_NAMES.put("java.lang.Long", "short");
		TypeUtil.PRIMITIVE_TYPE_NAMES.put("java.lang.Short", "int");
		TypeUtil.PRIMITIVE_TYPE_NAMES.put("java.lang.Double", "double");
		TypeUtil.PRIMITIVE_TYPE_NAMES.put("java.lang.Float", "float");
		TypeUtil.PRIMITIVE_TYPE_NAMES.put("java.lang.Character", "char");
	}

	/**
	 * @return true if it is reasonable to navigate given type with '.' notation. This returns true
	 * by default except for some specific cases we assume are not 'dotable' such as Primitive types
	 * and String
	 */
	public static boolean isDotable(Type type) {
		String typeName = type.getErasure();
		boolean isSimple = typeName.equals(STRING_TYPE_NAME) || PRIMITIVE_TYPE_NAMES.containsKey(typeName);
		return !isSimple;
	}


	public List<TypedProperty> getProperties(Type type) {
		if (type!=null) {
			if (isMap(type)) {
				Type keyType = getKeyType(type);
				if (keyType!=null) {
					String[] keyValues = getValues(keyType);
					if (hasElements(keyValues)) {
						Type valueType = getDomainType(type);
						ArrayList<TypedProperty> properties = new ArrayList<TypedProperty>(keyValues.length);
						for (String propName : keyValues) {
							properties.add(new TypedProperty(propName, valueType));
						}
						return properties;
					}
				}
			}
		}
		return Collections.emptyList();
	}



}