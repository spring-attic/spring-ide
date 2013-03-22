/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.MethodMetadata;
import org.springframework.ide.eclipse.core.java.annotation.Annotation;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMemberValuePair;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMetadataReadingVisitor;
import org.springframework.util.ClassUtils;

/**
 * @author Martin Lippert
 * @since 3.2.0
 */
public class JdtAnnotationMetadataAdapter implements IJdtAnnotationMetadata {

	private final AnnotationMetadataReadingVisitor visitor;
	private final IType type;

	public JdtAnnotationMetadataAdapter(IType type, AnnotationMetadataReadingVisitor visitor) {
		this.type = type;
		this.visitor = visitor;
	}

	public IType getType() {
		return this.type;
	}

	public String getClassName() {
		return visitor.getClassName();
	}

	public boolean isInterface() {
		return visitor.isInterface();
	}

	public boolean isAbstract() {
		return visitor.isAbstract();
	}

	public boolean isConcrete() {
		return visitor.isConcrete();
	}

	public boolean isFinal() {
		return visitor.isFinal();
	}

	public boolean isIndependent() {
		return visitor.isIndependent();
	}

	public boolean hasEnclosingClass() {
		return visitor.hasEnclosingClass();
	}

	public String getEnclosingClassName() {
		return visitor.getEnclosingClassName();
	}

	public boolean hasSuperClass() {
		return visitor.hasSuperClass();
	}

	public String getSuperClassName() {
		return visitor.getSuperClassName();
	}

	public String[] getInterfaceNames() {
		return visitor.getInterfaceNames();
	}

	public String[] getMemberClassNames() {
		return visitor.getMemberClassNames();
	}

	public Set<String> getAnnotationTypes() {
		return visitor.getTypeLevelAnnotationClasses();
	}

	public Set<String> getMetaAnnotationTypes(String annotationType) {
		return Collections.emptySet();
	}

	public boolean hasAnnotation(String annotationType) {
		return visitor.hasTypeLevelAnnotations(annotationType);
	}

	public boolean hasMetaAnnotation(String metaAnnotationType) {
		return false;
	}

	public boolean isAnnotated(String annotationType) {
		return !ImportResource.class.getName().equals(annotationType) && visitor.hasTypeLevelAnnotations(annotationType);
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		return getAnnotationAttributes(annotationType, false);
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType, boolean classValuesAsString) {
		Annotation annotation = visitor.getTypeLevelAnnotation(annotationType);
		
		if (annotation == null) return null;
		
		Map<String, Object> result = new AnnotationAttributes();
		Set<AnnotationMemberValuePair> members = annotation.getMembers();
		for (AnnotationMemberValuePair pair : members) {
			
			if (classValuesAsString && pair.getValueAsObject() instanceof Class) {
				result.put(pair.getName(), ClassUtils.getQualifiedName((Class<?>) pair.getValueAsObject()));
			}
			else if (classValuesAsString && pair.getValueAsObject() instanceof Class[]) {
				Class[] classes = (Class[]) pair.getValueAsObject();
				String[] classNames = new String[classes.length];
				for (int i = 0; i < classes.length; i++) {
					classNames[i] = ClassUtils.getQualifiedName(classes[i]);
				}
				result.put(pair.getName(), classNames);
			}
			else {
				result.put(pair.getName(), pair.getValueAsObject());
			}
		}
		return result;
	}
	
	public boolean hasAnnotatedMethods(String annotationType) {
		return visitor.hasMethodLevelAnnotations(annotationType);
	}

	public Set<MethodMetadata> getAnnotatedMethods(String annotationType) {
		Set<MethodMetadata> result = new HashSet<MethodMetadata>();
		Map<IMethod, Annotation> methodLevelAnnotations = visitor.getMethodLevelAnnotations(annotationType);
		for (IMethod method : methodLevelAnnotations.keySet()) {
			Annotation annotation = methodLevelAnnotations.get(method);
			result.add(new JdtMethodMetadataAdapter(method, annotation));
		}
		
		return result;
	}

}
