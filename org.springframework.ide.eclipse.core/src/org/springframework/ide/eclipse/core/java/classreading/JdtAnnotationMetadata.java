/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.ide.eclipse.core.java.Introspector;

/**
 * @author Christian Dupuis
 * @since 2.2.5
 */
public class JdtAnnotationMetadata extends JdtClassMetadata implements AnnotationMetadata {

	private final IType type;

	private Map<String, Map<String, Object>> annotationMap = new HashMap<String, Map<String, Object>>();
	
	private Set<MethodMetadata> methodMetadata = new HashSet<MethodMetadata>();  

	public JdtAnnotationMetadata(IType type) {
		super(type);
		this.type = type;
		init();
	}

	public Set<MethodMetadata> getAnnotatedMethods() {
		return methodMetadata;
	}

	public Set<MethodMetadata> getAnnotatedMethods(String annotationType) {
		Set<MethodMetadata> annotationMetadata = new HashSet<MethodMetadata>();
		for (MethodMetadata method : methodMetadata) {
			if (method.isAnnotated(annotationType)) {
				annotationMetadata.add(method);
			}
		}
		return annotationMetadata;
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		return annotationMap.get(annotationType);
	}

	public Set<String> getAnnotationTypes() {
		return annotationMap.keySet();
	}

	public Set<String> getMetaAnnotationTypes(String annotationType) {
		return Collections.emptySet();
	}

	public boolean hasAnnotation(String annotationType) {
		return annotationMap.containsKey(annotationType);
	}

	public boolean hasMetaAnnotation(String metaAnnotationType) {
		return false;
	}
	
	private void init() {
		try {
			for (IAnnotation annotation : Introspector.getAllAnnotations(type)) {
				JdtAnnotationUtils.processAnnotation(annotation, type, annotationMap);
			}
			for (IMethod method : Introspector.getAllMethods(type)) {
				JdtMethodMetadata metadata = new JdtMethodMetadata(type, method);
				if (metadata.getAnnotationTypes().size() > 0) {
					methodMetadata.add(metadata);
				}
			}
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
	}
	
	public IType getType() {
		return type;
	}

	public boolean hasAnnotatedMethods(String annotationType) {
		for (MethodMetadata metadata : methodMetadata) {
			if (metadata.isAnnotated(annotationType)) {
				return true;
			}
		}
		return false;
	}

	public boolean isAnnotated(String annotationType) {
		return annotationMap.containsKey(annotationType);
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType, boolean classValuesAsString) {
		return getAnnotationAttributes(annotationType);
	}
	
}
