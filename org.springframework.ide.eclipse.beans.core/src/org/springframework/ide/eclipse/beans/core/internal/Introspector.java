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
import org.springframework.util.StringUtils;

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

	public static boolean hasConstructor(IType type, int numberOfParameters)
													throws JavaModelException {
		List constructors = new ArrayList();
		IMethod[] methods = type.getMethods();
		for (int i = 0; i < methods.length; i++) {
			IMethod method = methods[i];
			if (method.isConstructor() &&
						method.getNumberOfParameters() == numberOfParameters) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasWritableProperty(IType type, String propertyName)
											  throws JavaModelException {
		if (propertyName == null || propertyName.length() == 0) {
			throw createException("bad property name");
		}
		String base = StringUtils.capitalize(propertyName);
		return (findMethod(type, "set" + base, 1, true, false) != null);
	}

    /**
     * Finds a target methodName with specific number of arguments on the type
     * hierarchy of given type.
     * 
     * @param type The Java type object on which to retrieve the method
     * @param methodName Name of the method
     * @param argCount Number of arguments for the desired method
     * @param isPublic true if public method is requested
     * @param isStatic true if static method is requested
     */
    public static IMethod findMethod(IType type, String methodName,
    								  int argCount, boolean isPublic,
								  boolean isStatic) throws JavaModelException {
	    	while (type != null) {
		    	IMethod[] methods = type.getMethods();
		    	for (int i = 0; i < methods.length; i++) {
				IMethod method = methods[i];
				int flags = method.getFlags();
				if (Flags.isPublic(flags) == isPublic &&
								 Flags.isStatic(flags) == isStatic &&
								 (argCount == -1 ||
								 method.getNumberOfParameters() == argCount) &&
								 methodName.equals(method.getElementName())) {
					return method;
				}
	    		}
	   		type = getSuperType(type);
	    	}
	    	return null;
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
					String resolvedName = concatenate(resolvedNames[0][0],
													  resolvedNames[0][1], ".");
					return type.getJavaProject().findType(resolvedName);
				}
			}
		}
		return null;
	}

	/**
	 * Returns concatenated text from given two texts delimited by given
	 * delimiter. Both texts can be empty or <code>null</code>.
	 */
	protected static String concatenate(String text1, String text2,
										String delimiter) {
		StringBuffer buf = new StringBuffer();
		if (text1 != null && text1.length() > 0) {
			buf.append(text1);
		}
		if (text2 != null && text2.length() > 0) {
			if (buf.length() > 0) {
				buf.append(delimiter);
			}
			buf.append(text2);
		}
		return buf.toString();
	}

	protected static JavaModelException createException(String message) {
		return new JavaModelException(new CoreException(
							BeansCorePlugin.createErrorStatus(message, null)));
	}
}
