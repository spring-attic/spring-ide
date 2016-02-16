/*******************************************************************************
 * Copyright (c) 2007, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.typehierarchy.TypeHierarchyEngine;

/**
 * Helper methods for examining a Java {@link IType}.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Pierre-Antoine Gregoire
 * @author Martin Lippert
 */
public final class Introspector {

	public enum Public {
		YES, NO, DONT_CARE
	}

	public enum Static {
		YES, NO, DONT_CARE
	}

	/**
	 * Utility method that handles property names.
	 * <p>
	 * See {@link java.beans.Introspector#decapitalize(String)} for the reverse operation done in the Java SDK.
	 * @see java.beans.Introspector#decapitalize(String)
	 */
	private static String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		if (name.length() > 1 && Character.isUpperCase(name.charAt(0))) {
			return name;
		}
		char chars[] = name.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	/**
	 * Returns <code>true</code> if the given Java type extends or is the specified class.
	 * @param type the Java type to be examined
	 * @param className the full qualified name of the class we are looking for
	 */
	public static boolean doesExtend(IType type, String className) {
		if (System.getProperty(TypeHierarchyEngine.ENABLE_PROPERTY, "true").equals("true")) {
			return SpringCore.getTypeHierarchyEngine().doesExtend(type, className);
		}
		else {
			return hasSuperType(type, className, false);
		}
	}

	/**
	 * Returns <code>true</code> if the given Java type implements the specified interface.
	 * @param type the Java type to be examined
	 * @param interfaceName the full qualified name of the interface we are looking for
	 */
	public static boolean doesImplement(IType type, String interfaceName) {
		if (System.getProperty(TypeHierarchyEngine.ENABLE_PROPERTY, "true").equals("true")) {
			return SpringCore.getTypeHierarchyEngine().doesImplement(type, interfaceName);
		}
		else {
			return hasSuperType(type, interfaceName, true);
		}
	}

	/**
	 * Returns a list of all constructors from given type.
	 */
	public static Set<IMethod> findAllConstructors(IType type) throws JavaModelException {
		Map<String, IMethod> allConstructors = new HashMap<String, IMethod>();
		while (type != null) {
			for (IMethod method : getMethods(type)) {
				String key = method.getElementName() + method.getSignature();
				if (!allConstructors.containsKey(key) && method.isConstructor()) {
					allConstructors.put(key, method);
				}
			}
			type = getSuperType(type);
		}
		return new HashSet<IMethod>(allConstructors.values());
	}

	/**
	 * Finds all {@link IMethod}s in the given {@link IType}'s hierarchy that match the given filter.
	 * <p>
	 * Note: calling this method is equivalent to calling {@link #findAllMethods(IType, String, IMethodFilter)}.
	 * @since 2.0.2
	 */
	public static Set<IMethod> findAllMethods(IType type, IMethodFilter filter) {
		return findAllMethods(type, "", filter);
	}

	/**
	 * Finds all {@link IMethod}s in the given {@link IType}'s hierarchy that match the given filter, applying the
	 * prefix.
	 * @since 2.0.2
	 */
	public static Set<IMethod> findAllMethods(IType type, String prefix, IMethodFilter filter) {
		Set<IMethod> methods = new LinkedHashSet<IMethod>();
		try {
			if (type != null && type.isInterface()) {
				Set<IType> types = new HashSet<IType>();
				types.add(type);
				for (IMethod method : getMethods(type)) {
					if (!method.isConstructor() && filter.matches(method, prefix)) {
						methods.add(method);
					}
				}
				for (IType interfaceType : getAllImplementedInterfaces(type)) {
					methods.addAll(findAllMethods(interfaceType, prefix, filter));
				}
			}
			while (type != null) {
				for (IMethod method : getMethods(type)) {
					if (!method.isConstructor() && filter.matches(method, prefix)) {
						methods.add(method);
					}
				}
				type = getSuperType(type);
			}
		}
		catch (JavaModelException e) {
			// don't do anything here
		}
		return methods;
	}

	public static Set<IAnnotation> getAllAnnotations(IType type) throws JavaModelException {
		Set<IAnnotation> annotations = new LinkedHashSet<IAnnotation>();
		while (type != null && !type.isInterface()) {
			annotations.addAll(Arrays.asList(type.getAnnotations()));
			type = getSuperType(type);
		}
		return annotations;
	}

	/**
	 * Returns a list of all methods from given type with specified features.
	 */
	public static Set<IMethod> findAllMethods(IType type, String methodPrefix, int argCount, Public publics,
			Static statics) throws JavaModelException {
		return findAllMethods(type, methodPrefix, argCount, publics, statics, false);
	}

	/**
	 * Returns a list of all methods from given type with specified features.
	 */
	public static Set<IMethod> findAllMethods(IType type, String methodPrefix, int argCount, Public publics,
			Static statics, boolean ignoreCase) throws JavaModelException {
		Map<String, IMethod> allMethods = new HashMap<String, IMethod>();
		while (type != null) {
			for (IMethod method : getMethods(type)) {
				checkMethod(type, methodPrefix, argCount, publics, statics, ignoreCase, allMethods, method);
			}

			type = getSuperType(type);
		}
		return new HashSet<IMethod>(allMethods.values());
	}

	private static void checkMethod(IType type, String methodPrefix, int argCount, Public publics, Static statics,
			boolean ignoreCase, Map<String, IMethod> allMethods, IMethod method) throws JavaModelException {
		int flags = method.getFlags();
		String key = method.getElementName() + method.getSignature();
		if (!allMethods.containsKey(key)
				&& !method.isConstructor()
				&& (publics == Public.DONT_CARE
						|| (publics == Public.YES && (Flags.isPublic(flags) || Flags.isInterface(type.getFlags()))) || (publics == Public.NO && (!Flags
						.isPublic(flags) && !Flags.isInterface(type.getFlags()))))
				&& (statics == Static.DONT_CARE || (statics == Static.YES && Flags.isStatic(flags)) || (statics == Static.NO && !Flags
						.isStatic(flags))) && (argCount == -1 || method.getNumberOfParameters() == argCount)
				&& checkMethodNamePrefix(method, type, methodPrefix, ignoreCase)) {
			allMethods.put(key, method);
		}
	}

	private static boolean checkMethodNamePrefix(IMethod method, IType type, String methodPrefix, boolean ignoreCase) {
		String methodName = JdtUtils.getMethodName(method);
		return ((!ignoreCase && methodName.startsWith(methodPrefix)))
				|| (ignoreCase && methodName.toLowerCase().startsWith(methodPrefix.toLowerCase()));
	}

	private static boolean checkMethodName(IMethod method, IType type, String methodName, boolean ignoreCase) {
		String realMethodName = JdtUtils.getMethodName(method);
		return ((!ignoreCase && realMethodName.equals(methodName)))
				|| (ignoreCase && realMethodName.toLowerCase().startsWith(methodName.toLowerCase()));
	}

	/**
	 * Returns a list of all methods from given type with given prefix and no arguments.
	 */
	public static Set<IMethod> findAllNoParameterMethods(IType type, String prefix) throws JavaModelException {
		if (prefix == null) {
			prefix = "";
		}
		return findAllMethods(type, prefix, 0, Public.DONT_CARE, Static.DONT_CARE);
	}

	/**
	 * Returns a list of all setters.
	 */
	public static Set<IMethod> findAllWritableProperties(IType type) throws JavaModelException {
		return findAllMethods(type, "set", 1, Public.YES, Static.NO);
	}

	/**
	 * Finds a target methodName with specific number of arguments on the type hierarchy of given type.
	 * @param type the Java type object on which to retrieve the method
	 * @param methodName the name of the method
	 * @param argCount the number of arguments for the desired method
	 * @param isPublic <code>true</code> if public method is requested
	 * @param statics one of the <code>Statics</code> constants
	 */
	public static IMethod findMethod(IType type, String methodName, int argCount, Public publics, Static statics)
			throws JavaModelException {
		return findMethod(type, methodName, argCount, publics, statics, null);
	}

	/**
	 * Finds a target methodName with specific number of arguments on the type hierarchy of given type.
	 * @param type the Java type object on which to retrieve the method
	 * @param methodName the name of the method
	 * @param argCount the number of arguments for the desired method
	 * @param isPublic <code>true</code> if public method is requested
	 * @param statics one of the <code>Statics</code> constants
	 */
	public static IMethod findMethod(IType type, String methodName, int argCount, Public publics, Static statics,
			TypeHierarchyEngine typeHierarchyEngine) throws JavaModelException {

		for (IType itrType = type; itrType != null; itrType = getSuperType(itrType, typeHierarchyEngine)) {
			IMethod method = findMethodOnType(itrType, methodName, argCount, publics, statics);
			if (method != null) {
				return method;
			}
		}
		for (IType interfaceType : getAllImplementedInterfaces(type, typeHierarchyEngine)) {
			IMethod method = findMethod(interfaceType, methodName, argCount, publics, statics);
			if (method != null) {
				return method;
			}
		}
		return null;
	}

	private static IMethod findMethodOnType(IType type, String methodName, int argCount, Public publics, Static statics)
			throws JavaModelException {
		for (IMethod method : getMethods(type)) {
			int flags = method.getFlags();
			if ((publics == Public.DONT_CARE
					|| (publics == Public.YES && (Flags.isPublic(flags) || Flags.isInterface(type.getFlags()))) || (publics == Public.NO && (!Flags
					.isPublic(flags) && !Flags.isInterface(type.getFlags()))))
					&& (statics == Static.DONT_CARE || (statics == Static.YES && Flags.isStatic(flags)) || (statics == Static.NO && !Flags
							.isStatic(flags)))
					&& (argCount == -1 || method.getNumberOfParameters() == argCount)
					&& checkMethodName(method, type, methodName, false)) {
				return method;
			}
		}

		return null;
	}

	/**
	 * Returns a list of all getters with the given prefix.
	 */
	public static Set<IMethod> findReadableProperties(IType type, String methodPrefix) throws JavaModelException {
		String base = capitalize(methodPrefix);
		return findAllMethods(type, "get" + base, 0, Public.YES, Static.NO);
	}

	/**
	 * Returns a list of all getters with the given prefix.
	 */
	public static Set<IMethod> findReadableProperties(IType type, String methodPrefix, boolean ignoreCase)
			throws JavaModelException {
		String base = capitalize(methodPrefix);
		return findAllMethods(type, "get" + base, 0, Public.YES, Static.NO, ignoreCase);
	}

	/**
	 * Returns a list of all setters with the given prefix.
	 */
	public static Set<IMethod> findWritableProperties(IType type, String methodPrefix) throws JavaModelException {
		String base = capitalize(methodPrefix);
		return findAllMethods(type, "set" + base, 1, Public.YES, Static.NO);
	}

	/**
	 * Returns a list of all setters with the given prefix.
	 */
	public static Set<IMethod> findWritableProperties(IType type, String methodPrefix, boolean ignoreCase)
			throws JavaModelException {
		String base = capitalize(methodPrefix);
		return findAllMethods(type, "set" + base, 1, Public.YES, Static.NO, ignoreCase);
	}

	public static Set<IType> getAllImplementedInterfaces(IType type) {
		return getAllImplementedInterfaces(type, null);
	}

	public static Set<IType> getAllImplementedInterfaces(IType type, TypeHierarchyEngine typeHierarchyEngine) {
		Set<IType> allInterfaces = new HashSet<IType>();
		try {
			while (type != null) {
				String[] interfaces = type.getSuperInterfaceTypeSignatures();
				if (interfaces != null) {
					for (String iface : interfaces) {
						String fqin = JdtUtils.resolveClassNameBySignature(iface, type);
						IType interfaceType = type.getJavaProject().findType(fqin);
						if (interfaceType != null) {
							allInterfaces.add(interfaceType);
						}

					}
				}
				type = getSuperType(type, typeHierarchyEngine);
			}
		}
		catch (JavaModelException e) {
			// BeansCorePlugin.log(e);
		}
		return allInterfaces;
	}

	private static boolean implementsInterface(IType type, String className) {
		try {
			while (type != null) {
				String[] interfaces = type.getSuperInterfaceTypeSignatures();
				if (interfaces != null) {
					for (String iface : interfaces) {
						String fqin = JdtUtils.resolveClassNameBySignature(iface, type);
						IType interfaceType = type.getJavaProject().findType(fqin);
						if (interfaceType != null && interfaceType.getFullyQualifiedName().equals(className)) {
							return true;
						}

					}
				}
				type = getSuperType(type);
			}
		}
		catch (JavaModelException e) {
			// BeansCorePlugin.log(e);
		}
		return false;
	}

	/**
	 * Returns <strong>all</strong> methods of the given {@link IType} instance.
	 * @param type the type
	 * @return set of {@link IMethod}
	 * @throws JavaModelException
	 */
	public static Set<IMethod> getAllMethods(IType type) throws JavaModelException {
		return getAllMethods(type, true);
	}

	/**
	 * Returns <strong>all</strong> methods of the given {@link IType} instance.
	 * @param type the type
	 * @param includeHierarchy indicates if methods from superclasses should be included
	 * @return set of {@link IMethod}
	 * @throws JavaModelException
	 * @since 3.2.0
	 */
	public static Set<IMethod> getAllMethods(IType type, boolean includeHierarchy) throws JavaModelException {
		Map<String, IMethod> allMethods = new HashMap<String, IMethod>();
		while (type != null) {
			for (IMethod method : getMethods(type)) {
				String key = method.getElementName() + method.getSignature();
				if (!allMethods.containsKey(key) && !method.isConstructor()) {
					allMethods.put(key, method);
				}
			}
			if (!includeHierarchy) break;
			type = getSuperType(type);
		}
		return new HashSet<IMethod>(allMethods.values());
	}

	/**
	 * Returns <strong>all</strong> constructors of the given {@link IType} instance.
	 * @param type the type
	 * @return set of {@link IMethod}
	 * @throws JavaModelException
	 */
	public static Set<IMethod> getAllConstructors(IType type) throws JavaModelException {
		Map<String, IMethod> allMethods = new HashMap<String, IMethod>();
		for (IMethod method : getMethods(type)) {
			String key = method.getElementName() + method.getSignature();
			if (!allMethods.containsKey(key) && method.isConstructor()) {
				allMethods.put(key, method);
			}
		}
		return new HashSet<IMethod>(allMethods.values());
	}

	/**
	 * Returns <strong>all</strong> fields of the given {@link IType} instance.
	 * @param type the type
	 * @return set of {@link IMethod}
	 * @throws JavaModelException
	 */
	public static Set<IField> getAllFields(IType type) throws JavaModelException {
		return getAllFields(type, true);
	}

	/**
	 * Returns <strong>all</strong> fields of the given {@link IType} instance.
	 * @param type the type
	 * @param includeHierarchy should include fields from superclasses or not
	 * @return set of {@link IMethod}
	 * @throws JavaModelException
	 * @since 3.2.0
	 */
	public static Set<IField> getAllFields(IType type, boolean includeHierarchy) throws JavaModelException {
		Map<String, IField> allFields = new HashMap<String, IField>();
		while (type != null) {
			for (IField field : type.getFields()) {
				String key = field.getHandleIdentifier();
				if (!allFields.containsKey(key)) {
					allFields.put(key, field);
				}
			}
			if (!includeHierarchy) break;
			type = getSuperType(type);
		}
		return new HashSet<IField>(allFields.values());
	}

	public static IMethod getReadableProperty(IType type, String propertyName) throws JavaModelException {
		String base = capitalize(propertyName);
		return findMethod(type, "get" + base, 0, Public.YES, Static.NO);
	}

	/**
	 * Returns the super type of the given type.
	 */
	public static IType getSuperType(IType type) throws JavaModelException {
		TypeHierarchyEngine typeHierarchyEngine = System.getProperty(TypeHierarchyEngine.ENABLE_PROPERTY, "true").equals("true")
				? SpringCore.getTypeHierarchyEngine() : null;

		return getSuperType(type, typeHierarchyEngine);
	}

	/**
	 * Returns the super type of the given type.
	 * This is using the type hierarchy engine that is passed as parameter, if not null
	 */
	public static IType getSuperType(IType type, TypeHierarchyEngine typeHierarchyEngine) throws JavaModelException {
		if (type == null) {
			return null;
		}
		String name = type.getSuperclassName();
		if (name == null && !type.getFullyQualifiedName().equals(Object.class.getName())) {
			name = Object.class.getName();
		}
		if (name != null) {
			if (type.isBinary()) {
				return type.getJavaProject().findType(name);
			}
			else if (typeHierarchyEngine != null) {
				String supertype = typeHierarchyEngine.getSupertype(type);
				if (supertype != null) {
					return type.getJavaProject().findType(supertype);
				}
			}
			else {
				String resolvedName = JdtUtils.resolveClassName(name, type);
				if (resolvedName != null) {
					return type.getJavaProject().findType(resolvedName);
				}
			}
		}
		return null;
	}

	public static IMethod getWritableProperty(IType type, String propertyName) throws JavaModelException {
		return getWritableProperty(type, propertyName, null);
	}

	public static IMethod getWritableProperty(IType type, String propertyName, TypeHierarchyEngine typeHierarchyEngine) throws JavaModelException {
		String base = capitalize(propertyName);
		return findMethod(type, "set" + base, 1, Public.YES, Static.NO, typeHierarchyEngine);
	}

	public static Set<IMethod> getConstructors(IType type, int argCount, boolean isNonPublicAllowed) throws JavaModelException {
		IMethod[] methods = getMethods(type);
		
		Set<IMethod> ctors = new LinkedHashSet<IMethod>();
		if (argCount > 0) {
			for (IMethod method : methods) {
				if (method.isConstructor()) {
					if (method.getNumberOfParameters() == argCount) {
						if (isNonPublicAllowed || Flags.isPublic(method.getFlags())) {
							ctors.add(method);
						}
					}
				}
			}
		}
		return ctors;
	}

	/**
	 * Returns <code>true</code> if the given type has a public constructor with the specified number of arguments. If a
	 * constructor with no arguments is requested then the absence of a constructor (the JVM adds an implicit
	 * constructor here) results in <code>true</code>.
	 * @param type the Java type object on which to retrieve the method
	 * @param argCount the number of arguments for the constructor
	 * @param isNonPublicAllowed <code>true</code> if non-public constructurs are recognized too
	 */
	public static boolean hasConstructor(IType type, int argCount, boolean isNonPublicAllowed)
			throws JavaModelException {
		IMethod[] methods = getMethods(type);

		// First check for implicit constructor
		if (argCount == 0) {

			// Check if the methods do contain constuctors
			boolean hasExplicitConstructor = false;
			for (IMethod method : methods) {
				if (method.isConstructor()) {
					hasExplicitConstructor = true;
				}
			}
			if (!hasExplicitConstructor) {
				return true;
			}
		}

		// Now look for appropriate constructor
		for (IMethod method : methods) {
			if (method.isConstructor()) {
				if (method.getNumberOfParameters() == argCount) {
					if (isNonPublicAllowed || Flags.isPublic(method.getFlags())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean hasSuperType(IType type, String className, boolean isInterface) {
		if (type != null && type.exists() && className != null && className.length() > 0) {
			try {
				if (!isInterface) {
					while (type != null) {
						if (className.equals(type.getFullyQualifiedName())) {
							return true;
						}
						type = getSuperType(type);
					}
				}
				else {
					if (implementsInterface(type, className)) {
						return true;
					}
				}
			}
			catch (JavaModelException e) {
				SpringCore.log(e);
			}
		}
		return false;
	}

	public static boolean hasSuperType(IType type, String className) {
		return hasSuperType(type, className, false);
	}

	/**
	 * Returns true if the given type has a public setter (one-argument method named "set" + property name with an
	 * uppercase first character) for the specified property.
	 * @param type the Java type object on which to retrieve the method
	 * @param propertyName the name of the property
	 */
	public static boolean hasWritableProperty(IType type, String propertyName) throws JavaModelException {
		return hasWritableProperty(type, propertyName, null);
	}

	/**
	 * Returns true if the given type has a public setter (one-argument method named "set" + property name with an
	 * uppercase first character) for the specified property.
	 * @param type the Java type object on which to retrieve the method
	 * @param propertyName the name of the property
	 */
	public static boolean hasWritableProperty(IType type, String propertyName, TypeHierarchyEngine typeHierarchyEngine) throws JavaModelException {
		String base = capitalize(propertyName);
		return (findMethod(type, "set" + base, 1, Public.YES, Static.NO, typeHierarchyEngine) != null);
	}

	/**
	 * Returns <code>true</code> if the given name is a valid JavaBeans property name. This normally means that a
	 * property name starts with a lower case character, but in the (unusual) special case when there is more than one
	 * character and both the first and second characters are upper case, then an upper case character is valid too.
	 * <p>
	 * Thus "fooBah" corresponds to "FooBah" and "x" to "X", but "URL" stays the same as "URL".
	 * <p>
	 * This conforms to section "8.8 Capitalization of inferred names" of the JavaBeans specs.
	 * @param name the name to be checked
	 */
	public static boolean isValidPropertyName(String name) {
		if (name == null || name.length() == 0) {
			return false;
		}
		if (name.length() == 1 && Character.isUpperCase(name.charAt(0))) {
			return false;
		}
		if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isLowerCase(name.charAt(1))) {
			return false;
		}
		return true;
	}

	public static IMethod[] getMethods(IType type) throws JavaModelException {
		if (type == null) {
			return new IMethod[0];
		}
		if (type.isStructureKnown()) {
			IMethod[] methods = type.getMethods();
			
			if (JdtUtils.isAjdtProject(type.getResource())) {
				Set<IMethod> itdMethods = AjdtUtils.getDeclaredMethods(type);
				if (itdMethods.size() > 0) {
					int i = methods.length;
					IMethod[] allMethods = new IMethod[methods.length + itdMethods.size()];
					System.arraycopy(methods, 0, allMethods, 0, methods.length);
					for (IMethod method : itdMethods) {
						allMethods[i++] = method;
					}
					
					methods = allMethods;
				}
			}

			return methods;
		}
		return new IMethod[0];
	}

}
