/*******************************************************************************
 * Copyright (c) 2009, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.core.type.MethodMetadata;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.2.5
 */
public class JdtMethodMetadata implements MethodMetadata, JdtConnectedMetadata {

	private final Map<String, Map<String, Object>> annotationMap = new LinkedHashMap<String, Map<String, Object>>();

	private final IMethod method;

	private final IType type;

	public JdtMethodMetadata(IType type, IMethod method) {
		this.type = type;
		this.method = method;
		init();
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		return annotationMap.get(annotationType);
	}

	public Set<String> getAnnotationTypes() {
		return annotationMap.keySet();
	}

	public String getDeclaringClassName() {
		return this.method.getDeclaringType().getFullyQualifiedName('$');
	}

	public IJavaElement getJavaElement() {
		return method;
	}

	public String getMethodName() {
		return method.getElementName();
	}

	public String getMethodReturnType() {
		try {
			return JdtUtils.resolveClassName(method.getReturnType(), method.getDeclaringType());
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	public boolean hasAnnotation(String annotationType) {
		return annotationMap.containsKey(annotationType);
	}

	public boolean isAnnotated(String annotationType) {
		return annotationMap.containsKey(annotationType);
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

	private void init() {
		try {
			for (IAnnotation annotation : method.getAnnotations()) {
				JdtAnnotationUtils.processAnnotation(annotation, type, annotationMap);
			}
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
	}

}
