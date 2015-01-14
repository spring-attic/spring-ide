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
package org.springframework.ide.eclipse.propertiesfileeditor.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities to work with types represented as Strings as returned by 
 * Spring config metadata apis.
 * 
 * @author Kris De Volder
 */
public class TypeUtil {
	
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
	public static final String[] getValues(String type) {
		return TYPE_VALUES.get(type);
	}
	

	/**
	 * Check is a type is 'array like' meaning it is valid to
	 * use the notation <name>[<index>]=<value> in property file
	 * for properties of this type.
	 */
	public static boolean isArrayLikeType(String type) {
		if (type.endsWith("[]")) {
			return true;
		} else {
			String erasure = TypeUtil.typeErasure(type);
			//Note: to be really correct we should use JDT infrastructure to resolve 
			//type in project classpath instead of using Java reflection.
			//However, use reflection here is okay assuming types we care about 
			//are part of JRE standard libraries.
			try {
				Class<?> erasureClass = Class.forName(erasure);
				return List.class.isAssignableFrom(erasureClass);
			} catch (Exception e) {
				//type not resolveable assume its not 'array like'
				return false;
			}
		}
	}

	public static boolean isAssignableType(String type) {
		return ASSIGNABLE_TYPES.contains(TypeUtil.typeErasure(type)) || isArrayLikeType(type);
	}

	public static String typeErasure(String type) {
		int paramStarts = type.indexOf('<');
		if (paramStarts>=0) {
			return type.substring(0,paramStarts);
		}
		return type;
	}
	
	public static String formatJavaType(String type) {
		if (type!=null) {
			String primitive = TypeUtil.PRIMITIVE_TYPES.get(type);
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
	
	private static final Map<String, String> PRIMITIVE_TYPES = new HashMap<String, String>();
	static {
		TypeUtil.PRIMITIVE_TYPES.put("java.lang.Boolean", "boolean");
		TypeUtil.PRIMITIVE_TYPES.put("java.lang.Integer", "int");
		TypeUtil.PRIMITIVE_TYPES.put("java.lang.Long", "short");
		TypeUtil.PRIMITIVE_TYPES.put("java.lang.Short", "int");
		TypeUtil.PRIMITIVE_TYPES.put("java.lang.Double", "double");
		TypeUtil.PRIMITIVE_TYPES.put("java.lang.Float", "float");
		TypeUtil.PRIMITIVE_TYPES.put("java.lang.Character", "char");
	}

}
