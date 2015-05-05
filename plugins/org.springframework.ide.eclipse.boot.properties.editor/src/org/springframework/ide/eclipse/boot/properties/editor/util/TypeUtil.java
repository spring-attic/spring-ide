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

import static org.springframework.ide.eclipse.boot.properties.editor.util.ArrayUtils.firstElement;
import static org.springframework.ide.eclipse.boot.properties.editor.util.ArrayUtils.hasElements;
import static org.springframework.ide.eclipse.boot.properties.editor.util.ArrayUtils.lastElement;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.omg.CORBA.NO_PERMISSION;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.EnumValueParser;
import org.springframework.ide.eclipse.boot.util.StringUtil;

/**
 * Utilities to work with types represented as Strings as returned by
 * Spring config metadata apis.
 *
 * @author Kris De Volder
 */
public class TypeUtil {


	public static final Type INTEGER_TYPE = new Type("java.lang.Integer", null);

	private static final String STRING_TYPE_NAME = String.class.getName();
	private static final String INET_ADDRESS_TYPE_NAME = InetAddress.class.getName();

	public enum EnumCaseMode {
		LOWER_CASE, //convert enum names to lower case
		ORIGNAL,    //keep orignal enum name
		ALIASED     //use both lower-cased and original names as aliases of one another
	}

	private IJavaProject javaProject;

	public TypeUtil(IJavaProject jp) {
		//Note javaProject is allowed to be null, but only in unit testing context
		// (This is so some tests can be run without an explicit jp needing to be created)
		this.javaProject = jp;
	}


	private static final Map<String, String> PRIMITIVE_TYPE_NAMES = new HashMap<String, String>();
	static {
		PRIMITIVE_TYPE_NAMES.put("java.lang.Boolean", "boolean");
		PRIMITIVE_TYPE_NAMES.put("java.lang.Integer", "int");
		PRIMITIVE_TYPE_NAMES.put("java.lang.Long", "short");
		PRIMITIVE_TYPE_NAMES.put("java.lang.Short", "int");
		PRIMITIVE_TYPE_NAMES.put("java.lang.Double", "double");
		PRIMITIVE_TYPE_NAMES.put("java.lang.Float", "float");
		PRIMITIVE_TYPE_NAMES.put("java.lang.Character", "char");
	}

	private static final Set<String> ASSIGNABLE_TYPES = new HashSet<String>(Arrays.asList(
			"java.lang.Boolean",
			"java.lang.String",
			"java.lang.Short",
			"java.lang.Integer",
			"java.lang.Long",
			"java.lang.Double",
			"java.lang.Float",
			"java.lang.Character",
			INET_ADDRESS_TYPE_NAME,
			"java.lang.String[]"
	));

	private static final Set<String> ATOMIC_TYPES = new HashSet<String>(PRIMITIVE_TYPE_NAMES.keySet());
	static {
		ATOMIC_TYPES.add(INET_ADDRESS_TYPE_NAME);
		ATOMIC_TYPES.add(STRING_TYPE_NAME);
	}

	private static final Map<String, String[]> TYPE_VALUES = new HashMap<String, String[]>();
	static {
		TYPE_VALUES.put("java.lang.Boolean", new String[] { "true", "false" });
	}

	private static final Map<String,ValueParser> VALUE_PARSERS = new HashMap<String, ValueParser>();
	static {
		VALUE_PARSERS.put(Integer.class.getName(), new ValueParser() {
			public Object parse(String str) {
				return Integer.parseInt(str);
			}
		});
		VALUE_PARSERS.put(Long.class.getName(), new ValueParser() {
			public Object parse(String str) {
				return Long.parseLong(str);
			}
		});
		VALUE_PARSERS.put(Short.class.getName(), new ValueParser() {
			public Object parse(String str) {
				return Short.parseShort(str);
			}
		});
		VALUE_PARSERS.put(Double.class.getName(), new ValueParser() {
			public Object parse(String str) {
				return Double.parseDouble(str);
			}
		});
		VALUE_PARSERS.put(Float.class.getName(), new ValueParser() {
			public Object parse(String str) {
				return Float.parseFloat(str);
			}
		});
		VALUE_PARSERS.put(Boolean.class.getName(), new ValueParser() {
			public Object parse(String str) {
				//The 'more obvious' implementation is too liberal and accepts anything as okay.
				//return Boolean.parseBoolean(str);
				str = str.toLowerCase();
				if (str.equals("true")) {
					return true;
				} else if (str.equals("false")) {
					return false;
				}
				throw new IllegalArgumentException("Value should be 'true' or 'false'");
			}
		});
	}

	public interface ValueParser {
		Object parse(String str);
	}

	public ValueParser getValueParser(Type type) {
		ValueParser simpleParser = VALUE_PARSERS.get(type.getErasure());
		if (simpleParser!=null) {
			return simpleParser;
		}
		String[] enumValues = getAllowedValues(type, EnumCaseMode.ALIASED);
		if (enumValues!=null) {
			//Note, technically if 'enumValues is empty array' this means something different
			// from when it is null. An empty array means a type that has no values, so
			// assigning anything to it is an error.
			return new EnumValueParser(niceTypeName(type), enumValues);
		}
		return null;
	}

	/**
	 * @return An array of allowed values for a given type. If an array is returned then
	 * *only* values in the array are valid and using any other value constitutes an error.
	 * This may return null if allowedValues list is unknown or the type is not characterizable
	 * as a simple enumaration of allowed values.
	 * @param caseMode determines whether Enum values are returned in 'lower case form', 'orignal form',
	 * or 'aliased' (meaning both forms are returned).
	 */
	public String[] getAllowedValues(Type enumType, EnumCaseMode caseMode) {
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
						boolean addOriginal = caseMode==EnumCaseMode.ORIGNAL||caseMode==EnumCaseMode.ALIASED;
						boolean addLowerCased = caseMode==EnumCaseMode.LOWER_CASE||caseMode==EnumCaseMode.ALIASED;
						for (int i = 0; i < fields.length; i++) {
							IField f = fields[i];
							if (f.isEnumConstant()) {
								String rawName = f.getElementName();
								if (addOriginal) {
									enums.add(rawName);
								}
								if (addLowerCased) {
									enums.add(StringUtil.upperCaseToHyphens(rawName));
								}
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

	public String niceTypeName(Type _type) {
		StringBuilder buf = new StringBuilder();
		niceTypeName(_type, buf);
		return buf.toString();
	}

	public void niceTypeName(Type type, StringBuilder buf) {
		String typeStr = type.getErasure();
		if (typeStr.startsWith("java.lang.")) {
			buf.append(typeStr.substring("java.lang.".length()));
		} else if (typeStr.startsWith("java.util.")) {
			buf.append(typeStr.substring("java.util.".length()));
		} else {
			buf.append(typeStr);
		}
		if (isEnum(type)) {
			String[] values = getAllowedValues(type, EnumCaseMode.ORIGNAL);
			if (values!=null && values.length>0) {
				buf.append("[");
				int max = Math.min(4, values.length);
				for (int i = 0; i < max; i++) {
					if (i>0) {
						buf.append(", ");
					}
					buf.append(values[i]);
				}
				if (max!=values.length) {
					buf.append(", ...");
				}
				buf.append("]");
			}
		} else if (type.isGeneric()) {
			Type[] params = type.getParams();
			buf.append("<");
			for (int i = 0; i < params.length; i++) {
				if (i>0) {
					buf.append(", ");
				}
				niceTypeName(params[i], buf);
			}
			buf.append(">");
		}
	}


	/**
	 * @return true if it is reasonable to navigate given type with '.' notation. This returns true
	 * by default except for some specific cases we assume are not 'dotable' such as Primitive types
	 * and String
	 */
	public boolean isDotable(Type type) {
		String typeName = type.getErasure();
		if (typeName.equals("java.lang.Object")) {
			//special case. Treat as 'non dotable' type. This mainly for stuff like logging.level
			// declared as Map<String,Object> so it would 'eat' the dots into the key.
			// also it makes sense to treat Object as 'non-dotable' since we cannot determine properties
			// for such an abstract type (as Object itself has no setters).
			return false;
		}
		return !isAtomic(type);
	}

	public boolean isAtomic(Type type) {
		String typeName = type.getErasure();
		return ATOMIC_TYPES.contains(typeName) || isEnum(type);
	}

	/**
	 * Check if it is valid to
	 * use the notation <name>[<index>]=<value> in property file
	 * for properties of this type.
	 */
	public static boolean isArrayLike(Type type) {
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

	public static boolean isMap(Type type) {
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

	public static Type getKeyType(Type mapOrArrayType) {
		if (isArrayLike(mapOrArrayType)) {
			return INTEGER_TYPE;
		} else {
			//assumed to be a map
			return firstElement(mapOrArrayType.getParams());
		}
	}

	public boolean isAssignableType(Type type) {
		return ASSIGNABLE_TYPES.contains(type.getErasure())
				|| isEnum(type)
				|| isAssignableList(type);
	}

	private boolean isAssignableList(Type type) {
		//TODO: isBracketable means 'isList' right now, but this may not be
		// the case in the future.
		if (isArrayLike(type)) {
			Type domainType = getDomainType(type);
			return isAtomic(domainType);
		}
		return false;
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

	private IType findType(Type beanType) {
		return findType(beanType.getErasure());
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

	private static final String[] NO_PARAMS = new String[0];

	/**
	 * Determine properties that are setable on object of given type.
	 * <p>
	 * Note that this may return both null or an empty list, but they mean
	 * different things. Null means that the properties on the object are not known,
	 * and therefore reconciling should not check property validity. On the other hand
	 * returning an empty list means that there are no properties. In this case,
	 * accessing properties is invalid and reconciler should show an error message
	 * for any property access.
	 *
	 * @return A list of known properties or null if the list of properties is unknown.
	 */
	public List<TypedProperty> getProperties(Type type, EnumCaseMode mode) {
		if (type==null) {
			return null;
		}
		if (!isDotable(type)) {
			//If dot navigation is not valid then really this is just like saying the type has no properties.
			return Collections.emptyList();
		}
		if (isMap(type)) {
			Type keyType = getKeyType(type);
			if (keyType!=null) {
				String[] keyValues = getAllowedValues(keyType, mode);
				if (hasElements(keyValues)) {
					Type valueType = getDomainType(type);
					ArrayList<TypedProperty> properties = new ArrayList<TypedProperty>(keyValues.length);
					for (String propName : keyValues) {
						properties.add(new TypedProperty(propName, valueType));
					}
					return properties;
				}
			}
		} else {
			String typename = type.getErasure();
			IType eclipseType = findType(typename);

			//TODO: handle type parameters.
			if (eclipseType!=null) {
				List<IMethod> setters = getSetterMethods(eclipseType);
				//TODO: setters inherited from super classes?
				if (setters!=null && !setters.isEmpty()) {
					ArrayList<TypedProperty> properties = new ArrayList<TypedProperty>(setters.size());
					for (IMethod m : setters) {
						Type propType = Type.fromSignature(m.getParameterTypes()[0], eclipseType);
						properties.add(new TypedProperty(setterNameToProperty(m.getElementName()), propType));
					}
					return properties;
				}
			}
		}
		return null;
	}

	private String setterNameToProperty(String name) {
		Assert.isLegal(name.startsWith("set"));
		String camelName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
		return StringUtil.camelCaseToHyphens(camelName);
	}

	private List<IMethod> getSetterMethods(IType eclipseType) {
		try {
			if (eclipseType!=null && eclipseType.isClass()) {
				IMethod[] allMethods = eclipseType.getMethods();
				if (ArrayUtils.hasElements(allMethods)) {
					ArrayList<IMethod> setters = new ArrayList<IMethod>();
					for (IMethod m : allMethods) {
						String mname = m.getElementName();
						if (mname.startsWith("set") && mname.length()>=4) {
							//Need at least 4 chars or the property name will be empty.
							String sig = m.getSignature();
							int numParams = Signature.getParameterCount(sig);
							if (numParams==1) {
								setters.add(m);
							}
						}
					}
					return setters;
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	public Map<String,Type> getPropertiesMap(Type type, EnumCaseMode mode) {
		//TODO: optimize, produce directly as a map instead of
		// first creating list and then coverting it.
		List<TypedProperty> list = getProperties(type, mode);
		if (list!=null) {
			Map<String, Type> map = new HashMap<String, Type>();
			for (TypedProperty p : list) {
				map.put(p.getName(), p.getType());
			}
			return map;
		}
		return null;
	}

	/**
	 * Maybe ne null in some contexts. In such context functionality will be limited because
	 * types can not be resolved.
	 */
	public IJavaProject getJavaProject() {
		return javaProject;
	}

	public IField getField(Type beanType, String propName) {
		IType type = findType(beanType);
		IField f = type.getField(StringUtil.hyphensToCamelCase(propName, false));
		if (f.exists()) {
			return f;
		}
		return null;
	}

	public IMethod getSetter(Type beanType, String propName) {
		try {
			String setterName = "set" + StringUtil.hyphensToCamelCase(propName, true);
			IType type = findType(beanType);
			for (IMethod m : type.getMethods()) {
				if (setterName.equals(m.getElementName())) {
					return m;
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	public IJavaElement getGetter(Type beanType, String propName) {
		String getterName = "get" + StringUtil.hyphensToCamelCase(propName, true);
		IType type = findType(beanType);

		IMethod m = type.getMethod(getterName, NO_PARAMS);
		if (m.exists()) {
			return m;
		}
		return null;
	}

}