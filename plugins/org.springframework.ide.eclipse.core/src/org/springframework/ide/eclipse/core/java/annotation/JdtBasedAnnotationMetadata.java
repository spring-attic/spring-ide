/*******************************************************************************
 * Copyright (c) 2009 - 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.annotation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.ClassUtils;

/**
 * {@link IAnnotationMetadata} implementation that uses Eclipse JDT core model information for
 * extracting the metadata.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.2.2
 */
public class JdtBasedAnnotationMetadata implements IAnnotationMetadata {

	private final IType type;

	private Set<Annotation> classAnnotations = new HashSet<Annotation>();

	private Map<IMethod, Set<Annotation>> methodAnnotations = new LinkedHashMap<IMethod, Set<Annotation>>();

	private Map<IField, Set<Annotation>> fieldAnnotations = new LinkedHashMap<IField, Set<Annotation>>();

	public JdtBasedAnnotationMetadata(IType type) {
		this.type = type;
		init();
	}

	/**
	 * @Controller("/index.htm") -> value = /index.htm
	 * 
	 * @Controller({"/index1.htm" , "/index2.htm"}) -> value = /index1.htm, /index2.htm
	 * 
	 * @Controller({ RequestMapping.GET, RequestMapping.POST})
	 * @Controller({ org.swf.RequestMapping.GET, org.swf.RequestMapping.POST}) -> value =
	 * RequestMapping.GET, RequestMapping.POST
	 * 
	 * @Controller(RequestMapping.GET)
	 * @Controller(org.swf.RequestMapping.GET) -> value = RequestMapping.GET
	 */
	private void init() {
		try {
			for (IAnnotation annotation : Introspector.getAllAnnotations(type)) {
				classAnnotations.add(processAnnotation(annotation));
			}
			for (IMethod method : Introspector.getAllMethods(type)) {
				Set<Annotation> modelAnnotations = new LinkedHashSet<Annotation>();
				for (IAnnotation annotation : method.getAnnotations()) {
					modelAnnotations.add(processAnnotation(annotation));
				}
				if (modelAnnotations.size() <= 0) {
					// If no annotations found on concrete method, look for them on the interface
					modelAnnotations = processInterfaceMethods(method);
				}
				if (modelAnnotations.size() > 0) {
					methodAnnotations.put(method, modelAnnotations);
				}
			}
			for (IMethod method : Introspector.getAllConstructors(type)) {
				Set<Annotation> modelAnnotations = new LinkedHashSet<Annotation>();
				for (IAnnotation annotation : method.getAnnotations()) {
					modelAnnotations.add(processAnnotation(annotation));
				}
				if (modelAnnotations.size() > 0) {
					methodAnnotations.put(method, modelAnnotations);
				}
			}
			for (IField field : Introspector.getAllFields(type)) {
				Set<Annotation> modelAnnotations = new LinkedHashSet<Annotation>();
				for (IAnnotation annotation : field.getAnnotations()) {
					modelAnnotations.add(processAnnotation(annotation));
				}
				if (modelAnnotations.size() > 0) {
					fieldAnnotations.put(field, modelAnnotations);
				}
			}
		}
		catch (JavaModelException e) {

		}
	}

	private Annotation processAnnotation(IAnnotation annotation, IType itype) {
		Annotation modelAnnotation;
		if (itype.isBinary()) {
			modelAnnotation = new Annotation(annotation.getElementName());
		}
		else {
			modelAnnotation = new Annotation(JdtUtils.resolveClassName(annotation.getElementName(), itype));
		}
		try {
			for (IMemberValuePair member : annotation.getMemberValuePairs()) {
				StringBuilder builder = new StringBuilder();
				// First check if we have an array
				if (member.getValue() != null && member.getValue().getClass().isArray()) {
					Object[] values = (Object[]) member.getValue();
					for (int i = 0; i < values.length; i++) {
						processStringValue(member, builder, values[i].toString());
						if (i < values.length - 1) {
							builder.append(", ");
						}
					}
				}
				// If not process the value directly
				else if (member.getValue() != null) {
					processStringValue(member, builder, member.getValue().toString());
				}

				modelAnnotation.addMember(new AnnotationMemberValuePair(("value".equals(member
						.getMemberName()) ? null : member.getMemberName()), builder.toString()));

			}
		}
		catch (JavaModelException e) {
			// 
		}
		return modelAnnotation;
	}
	
	private Annotation processAnnotation(IAnnotation annotation) {
		return processAnnotation(annotation, type);
	}
	
	private Set<Annotation> processInterfaceMethods(IMethod method) throws JavaModelException {
		Set<Annotation> modelAnnotations = new LinkedHashSet<Annotation>();
		Set<IType> interfaces = Introspector.getAllImplementedInterfaces(type);
		for (IType iface : interfaces) {
			IMethod[] interfaceMethod = iface.findMethods(method);
			if (interfaceMethod != null && interfaceMethod.length > 0) {
				for (IAnnotation annotation : interfaceMethod[0].getAnnotations()) {
					modelAnnotations.add(processAnnotation(annotation, iface));
				}
			}
		}
		return modelAnnotations;
	}


	private void processStringValue(IMemberValuePair member, StringBuilder builder, String value) {
		// class value
		if (member.getValueKind() == IMemberValuePair.K_CLASS) {
			String className = JdtUtils.resolveClassName(value, type);
			if (className != null) {
				builder.append(ClassUtils.getShortName(className));
			}
			else {
				builder.append(value);
			}
		}
		// enum value
		else if (member.getValueKind() == IMemberValuePair.K_QUALIFIED_NAME
				|| member.getValueKind() == IMemberValuePair.K_SIMPLE_NAME) {
			String tempValue = value;
			int i = tempValue.lastIndexOf('.');
			while (i > 0) {
				tempValue = tempValue.substring(0, i);
				String className = JdtUtils.resolveClassName(tempValue, type);
				if (className != null) {
					builder.append(ClassUtils.getShortName(className)).append(
							member.getValue().toString().substring(i));
					break;
				}
			}
			if (builder.length() == 0) {
				builder.append(value);
			}
		}
		else {
			builder.append(value);
		}
	}

	public Map<IMethod, Annotation> getMethodLevelAnnotations(String... annotationClasses) {
		Map<IMethod, Annotation> methodAnnotation = new HashMap<IMethod, Annotation>();
		for (Map.Entry<IMethod, Set<Annotation>> entry : methodAnnotations.entrySet()) {
			for (Annotation annotation : entry.getValue()) {
				for (String annotationClass : annotationClasses) {
					if (annotation.getAnnotationClass().equals(annotationClass)) {
						methodAnnotation.put(entry.getKey(), annotation);
					}
				}
			}
		}
		return methodAnnotation;
	}

	public boolean hasMethodLevelAnnotations(String... annotationClass) {
		List<String> annoatations = Arrays.asList(annotationClass);
		for (Map.Entry<IMethod, Set<Annotation>> entry : methodAnnotations.entrySet()) {
			for (Annotation annotation : entry.getValue()) {
				if (annoatations.contains(annotation.getAnnotationClass())) {
					return true;
				}
			}
		}
		return false;
	}

	public Map<IField, Annotation> getFieldLevelAnnotations(String... annotationClasses) {
		Map<IField, Annotation> fieldAnnotation = new HashMap<IField, Annotation>();
		for (Map.Entry<IField, Set<Annotation>> entry : fieldAnnotations.entrySet()) {
			for (Annotation annotation : entry.getValue()) {
				for (String annotationClass : annotationClasses) {
					if (annotation.getAnnotationClass().equals(annotationClass)) {
						fieldAnnotation.put(entry.getKey(), annotation);
					}
				}
			}
		}
		return fieldAnnotation;
	}
	
	public boolean hasFieldLevelAnnotations(String... annotationClass) {
		List<String> annoatations = Arrays.asList(annotationClass);
		for (Map.Entry<IField, Set<Annotation>> entry : fieldAnnotations.entrySet()) {
			for (Annotation annotation : entry.getValue()) {
				if (annoatations.contains(annotation.getAnnotationClass())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Set<String> getTypeLevelAnnotationClasses() {
		Set<String> annotationTypes = new LinkedHashSet<String>();
		for (Annotation annotation : classAnnotations) {
			annotationTypes.add(annotation.getAnnotationClass());
		}
		return annotationTypes;
	}

	public Annotation getTypeLevelAnnotation(String annotationClass) {
		for (Annotation annotation : classAnnotations) {
			if (annotation.getAnnotationClass().equals(annotationClass)) {
				return annotation;
			}
		}
		return null;
	}

	public boolean hasTypeLevelAnnotations(String... annotationClasses) {
		Set<String> foundAnnoatationClasses = getTypeLevelAnnotationClasses();
		for (String annotationClass : annotationClasses) {
			if (foundAnnoatationClasses.contains(annotationClass)) {
				return true;
			}
		}
		return false;
	}

}