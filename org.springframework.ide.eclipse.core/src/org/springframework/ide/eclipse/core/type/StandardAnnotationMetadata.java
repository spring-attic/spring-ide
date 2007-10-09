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
package org.springframework.ide.eclipse.core.type;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardClassMetadata;

/**
 * @author Juergen Hoeller
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class StandardAnnotationMetadata extends StandardClassMetadata implements
		AnnotationMetadata {

	public StandardAnnotationMetadata(Class introspectedClass) {
		super(introspectedClass);
	}

	public Set<String> getAnnotationTypes() {
		Set<String> types = new HashSet<String>();
		Annotation[] anns = getIntrospectedClass().getAnnotations();
		for (int i = 0; i < anns.length; i++) {
			types.add(anns[i].annotationType().getName());
		}
		return types;
	}

	public boolean hasAnnotation(String annotationType) {
		Annotation[] anns = getIntrospectedClass().getAnnotations();
		for (int i = 0; i < anns.length; i++) {
			if (anns[i].annotationType().getName().equals(annotationType)) {
				return true;
			}
		}
		return false;
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		Annotation[] anns = getIntrospectedClass().getAnnotations();
		for (int i = 0; i < anns.length; i++) {
			Annotation ann = anns[i];
			if (ann.annotationType().getName().equals(annotationType)) {
				return AnnotationUtils.getAnnotationAttributes(ann);
			}
		}
		return null;
	}

}
