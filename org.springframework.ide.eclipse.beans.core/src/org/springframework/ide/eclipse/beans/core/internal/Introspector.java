/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.beans.core.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.core.StringUtils;

/**
 * Helper methods for examining a Java type.
 * @see IType
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Pierre-Antoine Grégoire
 */
public final class Introspector {

	/** static check does not matter */
	public static final int STATIC_IRRELVANT = 0;

	/** static check does matter; only none statics are should be found */
	public static final int STATIC_NO = 2;

	/** static check does matter; only statics are should be found */
	public static final int STATIC_YES = 1;

	/**
	 * Returns a map of method name + <code>IMethod</code> for all setters
	 * with specified features.
	 */
	public static Collection findAllMethods(IType type, String methodPrefix,
			int argCount, boolean isPublic, int staticFlag)
			throws JavaModelException {
		Map allMethods = new HashMap();
		while (type != null) {
			IMethod[] methods = type.getMethods();
			for (int i = 0; i < methods.length; i++) {
				IMethod method = methods[i];
				int flags = method.getFlags();
				String key = method.getElementName() + method.getSignature();
				if (!allMethods.containsKey(key)
						&& Flags.isPublic(flags) == isPublic
						&& (staticFlag == STATIC_IRRELVANT
								|| (staticFlag == STATIC_YES
										&& Flags.isStatic(flags))
										|| (staticFlag == STATIC_NO
												&& !Flags.isStatic(flags)))
						&& (argCount == -1
								|| method.getNumberOfParameters() == argCount)
						&& method.getElementName().startsWith(methodPrefix)) {
					allMethods.put(key, method);
				}
			}
			type = getSuperType(type);
		}
		return allMethods.values();
	}

	/**
	 * Returns a map of method name + <code>IMethod</code> for all setters
	 * with specified features.
	 */
	public static Collection findAllConstructors(IType type)
			throws JavaModelException {
		Map allConstructors = new HashMap();
		while (type != null) {
			IMethod[] methods = type.getMethods();
			for (int i = 0; i < methods.length; i++) {
				IMethod method = methods[i];
				String key = method.getElementName() + method.getSignature();
				if (!allConstructors.containsKey(key)
						&& method.isConstructor()) {
					allConstructors.put(key, method);
				}
			}
			type = getSuperType(type);
		}
		return allConstructors.values();
	}

	/**
	 * Returns a map of method name + <code>IMethod</code> for all setters
	 * with given prefix and no arguments.
	 */
	public static Collection findAllNoParameterMethods(IType type,
			String prefix) throws JavaModelException {
		if (prefix == null) {
			prefix = "";
		}
		return findAllMethods(type, prefix, 0, true, STATIC_IRRELVANT);
	}

	/**
	 * Finds a target methodName with specific number of arguments on the type
	 * hierarchy of given type.
	 * 
	 * @param type
	 *            The Java type object on which to retrieve the method
	 * @param methodName
	 *            Name of the method
	 * @param argCount
	 *            Number of arguments for the desired method
	 * @param isPublic
	 *            true if public method is requested
	 * @param staticFlag
	 *            one of the <code>STATIC_xxx</code> constants 
	 */
	public static IMethod findMethod(IType type, String methodName,
			int argCount, boolean isPublic, int staticFlag)
			throws JavaModelException {
		while (type != null) {
			IMethod[] methods = type.getMethods();
			for (int i = 0; i < methods.length; i++) {
				IMethod method = methods[i];
				int flags = method.getFlags();
				if (Flags.isPublic(flags) == isPublic
						&& (staticFlag == STATIC_IRRELVANT
								|| (staticFlag == STATIC_YES
										&& Flags.isStatic(flags))
										|| (staticFlag == STATIC_NO
												&& !Flags.isStatic(flags)))
						&& (argCount == -1
								|| method.getNumberOfParameters() == argCount)
						&& methodName.equals(method.getElementName())) {
					return method;
				}
			}
			type = getSuperType(type);
		}
		return null;
	}

	/**
	 * Returns a map of method name + <code>IMethod</code> for all setters
	 * with the given prefix.
	 */
	public static Collection findWritableProperties(IType type,
			String methodPrefix) throws JavaModelException {
		String base = StringUtils.capitalize(methodPrefix);
		return findAllMethods(type, "set" + base, 1, true, STATIC_NO);
	}

	/**
	 * Returns a map of method name + <code>IMethod</code> for all setters
	 * with the given prefix.
	 */
	public static Collection findReadableProperties(IType type,
			String methodPrefix) throws JavaModelException {
		String base = StringUtils.capitalize(methodPrefix);
		return findAllMethods(type, "get" + base, 0, true, STATIC_NO);
	}

	/**
	 * Returns super type of given type.
	 */
	protected static IType getSuperType(IType type) throws JavaModelException {
		String name = type.getSuperclassName();
		if (name != null) {
			if (type.isBinary()) {
				return type.getJavaProject().findType(name);
			} else {
				String[][] resolvedNames = type.resolveType(name);
				if (resolvedNames != null && resolvedNames.length > 0) {
					String resolvedName = StringUtils.concatenate(
							resolvedNames[0][0], resolvedNames[0][1], ".");
					return type.getJavaProject().findType(resolvedName);
				}
			}
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if the given type has a public constructor
	 * with the specified number of arguments. If a constructor with no
	 * arguments is requested then the absence of a constructor (the JVM adds
	 * an implicit constructor here) results in <code>true</code>.
	 * 
	 * @param type
	 *            The Java type object on which to retrieve the method
	 * @param argCount
	 *            Number of arguments for the constructor
	 * @param isNonPublicAllowed
	 *            Set to <code>true</code> if non-public constructurs are
	 *            recognized too
	 */
	public static boolean hasConstructor(IType type, int argCount,
			boolean isNonPublicAllowed) throws JavaModelException {
		IMethod[] methods = type.getMethods();

		// First check for implicit constructor
		if (argCount == 0) {

			// Check if the methods do contain constuctors
			boolean hasExplicitConstructor = false;
			for (int i = 0; i < methods.length; i++) {
				IMethod method = methods[i];
				if (method.isConstructor()) {
					hasExplicitConstructor = true;
				}
			}
			if (!hasExplicitConstructor) {
				return true;
			}
		}

		// Now look for appropriate constructor
		for (int i = 0; i < methods.length; i++) {
			IMethod method = methods[i];
			if (method.isConstructor()) {
				if (method.getNumberOfParameters() == argCount) {
					if (isNonPublicAllowed
							|| Flags.isPublic(method.getFlags())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if the given type has a public setter (one-argument method
	 * named "set" + property name with an uppercase first character) for the
	 * specified property.
	 * 
	 * @param type
	 *            The Java type object on which to retrieve the method
	 * @param propertyName
	 *            Name of the property
	 */
	public static boolean hasWritableProperty(IType type, String propertyName)
			throws JavaModelException {
		String base = StringUtils.capitalize(propertyName);
		return (findMethod(type, "set" + base, 1, true, STATIC_NO) != null);
	}

	/**
	 * Returns true if the given type has a public setter (one-argument method
	 * named "set" + property name with an uppercase first character) for the
	 * specified property.
	 * 
	 * @param type
	 *            The Java type object on which to retrieve the method
	 * @param propertyName
	 *            Name of the property
	 */
	public static IMethod getWritableProperty(IType type, String propertyName)
			throws JavaModelException {
		String base = StringUtils.capitalize(propertyName);
		return findMethod(type, "set" + base, 1, true, STATIC_NO);
	}

	public static IMethod getReadableProperty(IType type, String propertyName)
			throws JavaModelException {
		String base = StringUtils.capitalize(propertyName);
		return findMethod(type, "get" + base, 0, true, STATIC_NO);
	}

	/**
	 * Returns <code>true</code> if the given name is a valid JavaBeans
	 * property name. This normally means that a property name starts with a
	 * lower case character, but in the (unusual) special case when there is
	 * more than one character and both the first and second characters are
	 * upper case, then an upper case character is valid too.
	 * <p>
	 * Thus "fooBah" corresponds to "FooBah" and "x" to "X", but "URL" stays
	 * the same as "URL".
	 * <p>
	 * This conforms to section "8.8 Capitalization of inferred names" of the
	 * JavaBeans specs.
	 * 
	 * @param name
	 *            The name to be checked.
	 */
	public static boolean isValidPropertyName(String name) {
		if (name == null || name.length() == 0) {
			return false;
		}
		if (name.length() == 1 && Character.isUpperCase(name.charAt(0))) {
			return false;
		}
		if (name.length() > 1 && Character.isUpperCase(name.charAt(0))
				&& Character.isLowerCase(name.charAt(1))) {
			return false;
		}
		return true;
	}
}
