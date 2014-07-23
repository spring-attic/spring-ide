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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Type;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor;
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
		return new JdtConnectedMethodMetadataReadingVisitor(type, name, access, desc, this.getClassName(), this.classLoader, this.methodMetadataSet, Type.getReturnType(desc).getClassName());
	}

	@Override
	public boolean isAnnotated(String annotationType) {
		return !ImportResource.class.getName().equals(annotationType) && super.isAnnotated(annotationType);
	}

}
