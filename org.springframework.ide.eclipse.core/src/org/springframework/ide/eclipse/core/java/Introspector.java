/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.StringUtils;

/**
 * Helper methods for examining a Java {@link IType}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Pierre-Antoine Gregoire
 */
public final class Introspector {

	public enum Static {
		YES, NO, DONT_CARE
	}

	public enum Public {
		YES, NO, DONT_CARE
	}

	/**
	 * Returns a list of all methods from given type with specified features.
	 */
	public static Set<IMethod> findAllMethods(IType type, String methodPrefix,
			int argCount, Public publics, Static statics)
			throws JavaModelException {
		return findAllMethods(type, methodPrefix, argCount, publics, statics,
				false);
	}

	/**
	 * Returns a list of all methods from given type with specified features.
	 */
	public static Set<IMethod> findAllMethods(IType type, String methodPrefix,
			int argCount, Public publics, Static statics, boolean ignoreCase)
			throws JavaModelException {
		Map<String, IMethod> allMethods = new HashMap<String, IMethod>();
		while (type != null) {
			for (IMethod method : type.getMethods()) {
				int flags = method.getFlags();
				String key = method.getElementName() + method.getSignature();
				if (!allMethods.containsKey(key)
						&& (publics == Public.DONT_CARE
								|| (publics == Public.YES && Flags
										.isPublic(flags)) || (publics == Public.NO && !Flags
								.isPublic(flags)))
						&& (statics == Static.DONT_CARE
								|| (statics == Static.YES && Flags
										.isStatic(flags)) || (statics == Static.NO && !Flags
								.isStatic(flags)))
						&& (argCount == -1 || method.getNumberOfParameters() == argCount)
						&& ((!ignoreCase && method.getElementName().startsWith(
								methodPrefix)))
						|| (ignoreCase && method.getElementName().toLowerCase()
								.startsWith(methodPrefix.toLowerCase()))) {
					allMethods.put(key, method);
				}
			}
			type = getSuperType(type);
		}
		return new HashSet<IMethod>(allMethods.values());
	}

	/**
	 * Returns a list of all constructors from given type.
	 */
	public static Set<IMethod> findAllConstructors(IType type)
			throws JavaModelException {
		Map<String, IMethod> allConstructors = new HashMap<String, IMethod>();
		while (type != null) {
			for (IMethod method : type.getMethods()) {
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
	 * Returns a list of all methods from given type with given prefix and no
	 * arguments.
	 */
	public static Set<IMethod> findAllNoParameterMethods(IType type,
			String prefix) throws JavaModelException {
		if (prefix == null) {
			prefix = "";
		}
		return findAllMethods(type, prefix, 0, Public.DONT_CARE,
				Static.DONT_CARE);
	}

	/**
	 * Finds a target methodName with specific number of arguments on the type
	 * hierarchy of given type.
	 * @param type the Java type object on which to retrieve the method
	 * @param methodName the name of the method
	 * @param argCount the number of arguments for the desired method
	 * @param isPublic <code>true</code> if public method is requested
	 * @param statics one of the <code>Statics</code> constants
	 */
	public static IMethod findMethod(IType type, String methodName,
			int argCount, Public publics, Static statics)
			throws JavaModelException {
		while (type != null) {
			for (IMethod method : type.getMethods()) {
				int flags = method.getFlags();
				if ((publics == Public.DONT_CARE
						|| (publics == Public.YES && Flags.isPublic(flags)) || (publics == Public.NO && !Flags
						.isPublic(flags)))
						&& (statics == Static.DONT_CARE
								|| (statics == Static.YES && Flags
										.isStatic(flags)) || (statics == Static.NO && !Flags
								.isStatic(flags)))
						&& (argCount == -1 || method.getNumberOfParameters() == argCount)
						&& methodName.equals(method.getElementName())) {
					return method;
				}
			}
			type = getSuperType(type);
		}
		return null;
	}

	/**
	 * Returns a list of all setters with the given prefix.
	 */
	public static Set<IMethod> findWritableProperties(IType type,
			String methodPrefix) throws JavaModelException {
		String base = capitalize(methodPrefix);
		return findAllMethods(type, "set" + base, 1, Public.YES, Static.NO);
	}

	/**
	 * Returns a list of all setters.
	 */
	public static Set<IMethod> findAllWritableProperties(IType type) 
		throws JavaModelException {
		return findAllMethods(type, "set", 1, Public.YES, Static.NO);
	}

	/**
	 * Returns a list of all setters with the given prefix.
	 */
	public static Set<IMethod> findWritableProperties(IType type,
			String methodPrefix, boolean ignoreCase) throws JavaModelException {
		String base = capitalize(methodPrefix);
		return findAllMethods(type, "set" + base, 1, Public.YES, Static.NO,
				ignoreCase);
	}

	/**
	 * Returns a list of all getters with the given prefix.
	 */
	public static Set<IMethod> findReadableProperties(IType type,
			String methodPrefix) throws JavaModelException {
		String base = capitalize(methodPrefix);
		return findAllMethods(type, "get" + base, 0, Public.YES, Static.NO);
	}

	/**
	 * Returns a list of all getters with the given prefix.
	 */
	public static Set<IMethod> findReadableProperties(IType type,
			String methodPrefix, boolean ignoreCase) throws JavaModelException {
		String base = capitalize(methodPrefix);
		return findAllMethods(type, "get" + base, 0, Public.YES, Static.NO,
				ignoreCase);
	}

	/**
	 * Returns the super type of the given type.
	 */
	protected static IType getSuperType(IType type) throws JavaModelException {
		String name = type.getSuperclassName();
		if (name != null) {
			if (type.isBinary()) {
				return type.getJavaProject().findType(name);
			}
			String[][] resolvedNames = type.resolveType(name);
			if (resolvedNames != null && resolvedNames.length > 0) {
				String resolvedName = StringUtils.concatenate(
						resolvedNames[0][0], resolvedNames[0][1], ".");
				return type.getJavaProject().findType(resolvedName);
			}
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if the given type has a public constructor
	 * with the specified number of arguments. If a constructor with no
	 * arguments is requested then the absence of a constructor (the JVM adds an
	 * implicit constructor here) results in <code>true</code>.
	 * @param type the Java type object on which to retrieve the method
	 * @param argCount the number of arguments for the constructor
	 * @param isNonPublicAllowed <code>true</code> if non-public constructurs
	 * are recognized too
	 */
	public static boolean hasConstructor(IType type, int argCount,
			boolean isNonPublicAllowed) throws JavaModelException {
		IMethod[] methods = type.getMethods();

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

	/**
	 * Returns true if the given type has a public setter (one-argument method
	 * named "set" + property name with an uppercase first character) for the
	 * specified property.
	 * @param type the Java type object on which to retrieve the method
	 * @param propertyName the name of the property
	 */
	public static boolean hasWritableProperty(IType type, String propertyName)
			throws JavaModelException {
		String base = capitalize(propertyName);
		return (findMethod(type, "set" + base, 1, Public.YES, Static.NO) != null);
	}

	public static IMethod getWritableProperty(IType type, String propertyName)
			throws JavaModelException {
		String base = capitalize(propertyName);
		return findMethod(type, "set" + base, 1, Public.YES, Static.NO);
	}

	public static IMethod getReadableProperty(IType type, String propertyName)
			throws JavaModelException {
		String base = capitalize(propertyName);
		return findMethod(type, "get" + base, 0, Public.YES, Static.NO);
	}

	/**
	 * Returns <code>true</code> if the given name is a valid JavaBeans
	 * property name. This normally means that a property name starts with a
	 * lower case character, but in the (unusual) special case when there is
	 * more than one character and both the first and second characters are
	 * upper case, then an upper case character is valid too.
	 * <p>
	 * Thus "fooBah" corresponds to "FooBah" and "x" to "X", but "URL" stays the
	 * same as "URL".
	 * <p>
	 * This conforms to section "8.8 Capitalization of inferred names" of the
	 * JavaBeans specs.
	 * @param name the name to be checked
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

	/**
	 * Utility method that handles property names.
	 * <p>
	 * See {@link java.beans.Introspector#decapitalize(String)} for the reverse
	 * operation done in the Java SDK.
	 * @see java.beans.Introspector#decapitalize(String)
	 */
	private static String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		if (name.length() > 1 && Character.isUpperCase(name.charAt(1))
				&& Character.isLowerCase(name.charAt(0))) {
			return name;
		}
		char chars[] = name.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	public static Set<IType> getAllImplenentedInterfaces(IType type) {
		Set<IType> allInterfaces = new HashSet<IType>();
		try {
			while (type != null) {
				String[] interfaces = type.getSuperInterfaceTypeSignatures();
				if (interfaces != null) {
					for (String iface : interfaces) {
						String fqin = JdtUtils.resolveClassName(iface, type);
						IType interfaceType = type.getJavaProject().findType(
								fqin);
						if (interfaceType != null) {
							allInterfaces.add(interfaceType);
						}

					}
				}
				type = getSuperType(type);
			}
		}
		catch (JavaModelException e) {
			// BeansCorePlugin.log(e);
		}
		return allInterfaces;
	}

	/**
	 * Returns <code>true</code> if the given Java type extends or is the 
	 * specified class.
	 * @param type the Java type to be examined
	 * @param className the full qualified name of the class we are looking for
	 */
	public static boolean doesExtend(IType type, String className) {
		return hasSuperType(type, className, false);
	}
	
	/**
	 * Returns <code>true</code> if the given Java type implements the
	 * specified interface.
	 * @param type the Java type to be examined
	 * @param interfaceName the full qualified name of the interface we are
	 * looking for
	 */
	public static boolean doesImplement(IType type, String interfaceName) {
		return hasSuperType(type, interfaceName, true);
	}
	
	private static boolean hasSuperType(IType type, String className,
			boolean isInterface) {
		if (type != null && type.exists() && className != null
				&& className.length() > 0) {
			try {
				IType requiredType = type.getJavaProject().findType(className);
				if (requiredType != null
						&& ((isInterface && requiredType.isInterface()) 
								|| (!isInterface && !requiredType.isInterface()))) {
					ITypeHierarchy hierachy = SuperTypeHierarchyCache
							.getTypeHierarchy(type);
					return hierachy.contains(requiredType);
				}
			}
			catch (JavaModelException e) {
				SpringCore.log(e);
			}
		}
		return false;
	}

	/**
	 * Returns <strong>all</strong> methods of the given {@link IType}
	 * instance.
	 * @param type the type
	 * @return set of {@link IMethod}
	 * @throws JavaModelException
	 */
	public static Set<IMethod> getAllMethods(IType type)
			throws JavaModelException {
		Map<String, IMethod> allMethods = new HashMap<String, IMethod>();
		while (type != null) {
			for (IMethod method : type.getMethods()) {
				String key = method.getElementName() + method.getSignature();
				if (!allMethods.containsKey(key)) {
					allMethods.put(key, method);
				}
			}
			type = getSuperType(type);
		}
		return new HashSet<IMethod>(allMethods.values());
	}

}
