/*******************************************************************************
 * Copyright (c) 2013, 2014 Spring IDE Developers
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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Type;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;

/**
 * Addition to the standard metadata reading visitor that connects this class metadata to the corresponding
 * JDT IType and the methods to IMethods
 *
 * @author Martin Lippert
 * @since 3.3.0
 */
public class JdtConnectedAnnotationMetadataReadingVisitor extends AnnotationMetadataReadingVisitor implements JdtConnectedMetadata {

	private final IType type;

	public JdtConnectedAnnotationMetadataReadingVisitor(ClassLoader classloader, IType type) {
		super(classloader);
		this.type = type;
	}

	public IJavaElement getJavaElement() {
		return this.type;
	}
	
	public JavaModelSourceLocation createSourceLocation() throws JavaModelException {
		return new JavaModelSourceLocation(type);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		IMethod method = getMethodFromSignature(name, desc);
		return new JdtConnectedMethodMetadataReadingVisitor(name, access, this.getClassName(), this.classLoader, this.methodMetadataSet, method, Type.getReturnType(desc).getClassName());
	}

	@Override
	public boolean isAnnotated(String annotationType) {
		return !ImportResource.class.getName().equals(annotationType) && super.isAnnotated(annotationType);
	}

	private IMethod getMethodFromSignature(final String name, final String desc) {
		if (System.getProperty("spring-tooling.scanning.verbose", "false").equals("true")) {
			System.out.println("spring-tooling.scanning - findMethodFromSignature - type: " + this.type.getFullyQualifiedName() + " - method: " + name + " - signature: " + desc);
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
				method = JdtUtils.getConstructor(type, parameters.toArray(new String[parameters.size()]));
			} else {
				if (System.getProperty("spring-tooling.scanning.verbose", "false").equals("true")) {
					System.out.println("spring-tooling.scanning - deep dive to find method - type: " + this.type.getFullyQualifiedName() + " - method: " + name + " - signature: " + desc);
				}
				method = JdtUtils.getMethod(type, name, parameters.toArray(new String[parameters.size()]), false);
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
			IMethod[] methods = type.getMethods();
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
			IMethod[] methods = type.getMethods();
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
