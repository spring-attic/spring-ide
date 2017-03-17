/*******************************************************************************
 * Copyright (c) 2013, 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.asm.Type;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MethodMetadataReadingVisitor;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.java.JavaModelMethodSourceLocation;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;

/**
 * Addition to the standard class method metadata reading visitor that connects this method metadata to the corresponding
 * JDT IMethod
 *
 * @author Martin Lippert
 * @since 3.3.0
 */
public class JdtConnectedMethodMetadataReadingVisitor extends MethodMetadataReadingVisitor implements JdtConnectedMetadata {

	private final String desc;
	private final String name;
	private final IType mainType;

	private IMethod method;

	public JdtConnectedMethodMetadataReadingVisitor(IType mainType, String name, int access, String desc, String declaringClassName, ClassLoader classLoader,
			Set<MethodMetadata> methodMetadataMap, String returnType) {
		super(name, access, declaringClassName, returnType, classLoader, methodMetadataMap);

		this.mainType = mainType;
		this.desc = desc;
		this.name = name;
		this.method = null;
	}

	public IJavaElement getJavaElement() {
		if (method == null) {
			method = getMethodFromSignature(name, desc);
		}
		return this.method;
	}
	
	public JavaModelSourceLocation createSourceLocation() throws JavaModelException {
		IJavaElement javaElement = getJavaElement();
		if (javaElement == null) {
			throw new NullPointerException("java element not found for: " + this.name + " - with desc: " + this.desc + " - on main type: " + mainType != null ? mainType.getElementName() : "null");
		}
		else {
			return new JavaModelMethodSourceLocation(javaElement, getReturnTypeName());
		}
	}

	private IMethod getMethodFromSignature(final String name, final String desc) {
		if (System.getProperty("spring-tooling.scanning.verbose", "false").equals("true")) {
			System.out.println("spring-tooling.scanning - findMethodFromSignature - type: " + this.mainType.getFullyQualifiedName() + " - method: " + name + " - signature: " + desc);
		}
		
		Type[] parameterTypes = Type.getArgumentTypes(desc);

		IMethod method = null;
		if (isConstructor(name)) {
			method = quickCheckForConstructor(parameterTypes);
		} else {
			method = quickCheckForMethod(name, parameterTypes);
		}

		if (method == null) {
			List<String> parameters = new ArrayList<String>();
			if (parameterTypes != null && parameterTypes.length > 0) {
				for (Type parameterType : parameterTypes) {
					parameters.add(parameterType.getClassName());
				}
			}

			if (isConstructor(name)) {
				method = JdtUtils.getConstructor(mainType, parameters.toArray(new String[parameters.size()]));
			} else {
				if (System.getProperty("spring-tooling.scanning.verbose", "false").equals("true")) {
					System.out.println("spring-tooling.scanning - deep dive to find method - type: " + this.mainType.getFullyQualifiedName() + " - method: " + name + " - signature: " + desc);
				}
				method = JdtUtils.getMethod(mainType, name, parameters.toArray(new String[parameters.size()]), false);
			}
		}
		return method;
	}

	private boolean isConstructor(String name) {
		return "<init>".equals(name);
	}

	private IMethod quickCheckForMethod(String name, Type[] parameterTypes) {
		IMethod result = null;
		try {
			IMethod[] methods = mainType.getMethods();
			for (IMethod method : methods) {
				if (method.getElementName().equals(name) && method.getParameterTypes().length == parameterTypes.length) {
					if (result == null) {
						result = method;
					} else {
						return null;
					}
				}

			}
		} catch (JavaModelException e) {
		}
		return result;
	}

	private IMethod quickCheckForConstructor(Type[] parameterTypes) {
		IMethod result = null;
		try {
			IMethod[] methods = mainType.getMethods();
			for (IMethod method : methods) {
				if (method.isConstructor() && method.getParameterTypes().length == parameterTypes.length) {
					if (result == null) {
						result = method;
					} else {
						return null;
					}
				}

			}
		} catch (JavaModelException e) {
		}
		return result;
	}

}
