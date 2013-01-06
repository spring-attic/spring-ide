/*******************************************************************************
 * Copyright (c) 2009, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.internal.provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Utility class for the autowiring support.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.2.7
 */
public abstract class AutowireUtils {

	public static IJavaElement getJavaElement(IResource project, Member member, int index) {
		IType type = JdtUtils.getJavaType(project.getProject(), member.getDeclaringClass().getName());
		IJavaElement source = null;
		if (member instanceof Method) {
			source = JdtUtils.getMethod(type, ((Method) member).getName(), ((Method) member).getParameterTypes());
		}
		else if (member instanceof Field) {
			source = JdtUtils.getField(type, ((Field) member).getName());
		}
		else if (member instanceof Constructor<?>) {
			source = JdtUtils.getConstructor(type, ((Constructor<?>) member).getParameterTypes());
		}
		return source;
	}
	
}	
