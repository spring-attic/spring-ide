/*******************************************************************************
 * Copyright (c) 2012, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.core.type.MethodMetadata;
import org.springframework.ide.eclipse.core.java.annotation.Annotation;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMemberValuePair;

/**
 * @author Martin Lippert
 * @since 3.2.0
 */
public class JdtMethodMetadataAdapter implements MethodMetadata, JdtConnectedMetadata {
	
	private IMethod method;
	private Annotation annotation;

	public JdtMethodMetadataAdapter(IMethod method, Annotation annotation) {
		this.method = method;
		this.annotation = annotation;
	}

	public IJavaElement getJavaElement() {
		return this.method;
	}

	public String getMethodName() {
		return method.getElementName();
	}

	public String getDeclaringClassName() {
		return this.method.getDeclaringType().getFullyQualifiedName('$');
	}

	public boolean isFinal() {
		try {
			return Flags.isFinal(method.getFlags());
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
	}

	public boolean isOverridable() {
		try {
			return (!isStatic() && !isFinal() && !Flags.isPrivate(method.getFlags()));
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
	}

	public boolean isStatic() {
		try {
			return Flags.isStatic(method.getFlags());
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
	}

	public boolean isAnnotated(String annotationType) {
		return annotation.getAnnotationClass().equals(annotationType);
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		if (!annotation.getAnnotationClass().equals(annotationType)) {
			return null;
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		Set<AnnotationMemberValuePair> members = annotation.getMembers();
		for (AnnotationMemberValuePair pair : members) {
			result.put(pair.getName(), pair.getValueAsObject());
		}
		return result;
	}

}
