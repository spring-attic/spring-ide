/*
 * Copyright 2002-2004 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;

public class Introspector {

	public static List getConstructors(IType type) throws JavaModelException {
		List constructors = new ArrayList();
		IMethod[] methods = type.getMethods();
		for (int i = 0; i < methods.length; i++) {
			IMethod method = methods[i];
			if (method.isConstructor()) {
				constructors.add(method);
			}
		}
		return constructors;
	}

	public static boolean hasWritableProperty(IType type, String propertyName)
													 throws JavaModelException {
		if (propertyName == null || propertyName.length() == 0) {
			throw createException("bad property name");
		}
		String base = capitalize(propertyName);
		return (findMethod(type, "set" + base, 1) != null);
	}

    /**
     * Finds a target methodName with specific number of arguments on the type
     * hierarchie of given type.
     * 
     * @param type The Java type object on which to retrieve the method
     * @param methodName Name of the method
     * @param argCount Number of arguments for the desired method
     */
    public static IMethod findMethod(IType type, String methodName,
    								 int argCount) throws JavaModelException {
	    	while (type != null) {
	    		IMethod method = findPublicMethod(type, methodName, argCount);
	    		if (method != null) {
	    			return method;
	    		}
	   		type = getSuperClass(type);
	    	}
	    	return null;
	}

	/**
	 * Returns first public method defined in given type with specified method
	 * name and number of arguments.
	 */
    protected static IMethod findPublicMethod(IType type, String methodName,
    								   int argCount) throws JavaModelException {
	    	IMethod[] methods = type.getMethods();
	    	for (int i = 0; i < methods.length; i++) {
			IMethod method = methods[i];
			int flags = method.getFlags();
			if (Flags.isPublic(flags) && !Flags.isStatic(flags) &&
								   method.getNumberOfParameters() == argCount &&
								   methodName.equals(method.getElementName())) {
				return method;
			}
		}
		return null;
    }

	/**
	 * Returns super class of given type.
	 */
	protected static IType getSuperClass(IType type) {
		try {
			String name = type.getSuperclassName();
			if (name != null) {
				if (type.isBinary()) {
					return type.getJavaProject().findType(name);
				} else {
					String[][] resolvedType = type.resolveType(name);
					if (resolvedType != null) {
						name = resolvedType[0][0] + "." + resolvedType[0][1];
						return type.getJavaProject().findType(name);
					}
				}
			}
		} catch (JavaModelException e) {
			BeansCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * Returns given string with a leading uppercase character.
	 */
	protected static String capitalize(String s) {
		if (s.length() == 0) {
			return s;
		}
		char chars[] = s.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	protected static JavaModelException createException(String message) {
		return new JavaModelException(new CoreException(
							BeansCorePlugin.createErrorStatus(message, null)));
	}
}
