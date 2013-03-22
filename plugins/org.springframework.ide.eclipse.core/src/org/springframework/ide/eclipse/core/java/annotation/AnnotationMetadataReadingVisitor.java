/*******************************************************************************
 * Copyright (c) 2008, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Type;
import org.springframework.asm.commons.EmptyVisitor;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.type.asm.ClassMetadataReadingVisitor;

/**
 * ASM based {@link ClassVisitor} that reads and stores all
 * {@link java.lang.annotation.Annotation}s from classes and methods.
 * Furthermore this implementation saves all annotation members as well.
 * 
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0.5
 */
public class AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor implements IAnnotationMetadata {

	static EmptyVisitor EMPTY_VISITOR = new EmptyVisitor();

	private Map<String, Annotation> classAnnotations = new LinkedHashMap<String, Annotation>();
	private Map<IMethod, Set<Annotation>> methodAnnotations = new LinkedHashMap<IMethod, Set<Annotation>>();
	private Map<IField, Set<Annotation>> fieldAnnotations = new LinkedHashMap<IField, Set<Annotation>>();

	private Set<String> visitedMethods = new HashSet<String>();

	private IType type;
	private ClassLoader classloader;
	private boolean advancedValueProcessing;

	public AnnotationMetadataReadingVisitor() {
		this(false);
	}

	public AnnotationMetadataReadingVisitor(boolean advancedValueProcessing) {
		this.advancedValueProcessing = advancedValueProcessing;
	}

	public void setType(IType type) {
		this.type = type;
	}

	public void setClassloader(ClassLoader classloader) {
		this.classloader = classloader;
	}

	public Set<String> getTypeLevelAnnotationClasses() {
		return classAnnotations.keySet();
	}

	public Annotation getTypeLevelAnnotation(String annotationClass) {
		return classAnnotations.get(annotationClass);
	}

	public Map<IMethod, Annotation> getMethodLevelAnnotations(String... annotationClasses) {
		Map<IMethod, Annotation> result = new HashMap<IMethod, Annotation>();
		for (Map.Entry<IMethod, Set<Annotation>> entry : methodAnnotations.entrySet()) {
			for (Annotation annotation : entry.getValue()) {
				for (String annotationClass : annotationClasses) {
					if (annotation.getAnnotationClass().equals(annotationClass)) {
						result.put(entry.getKey(), annotation);
					}
				}
			}
		}
		return result;
	}

	public boolean hasMethodLevelAnnotations(String... annotationClasses) {
		List<String> annoatations = Arrays.asList(annotationClasses);
		for (Map.Entry<IMethod, Set<Annotation>> entry : methodAnnotations.entrySet()) {
			for (Annotation annotation : entry.getValue()) {
				if (annoatations.contains(annotation.getAnnotationClass())) {
					return true;
				}
			}
		}
		return false;
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

	public Map<IField, Annotation> getFieldLevelAnnotations(String... annotationClasses) {
		Map<IField, Annotation> result = new HashMap<IField, Annotation>();
		for (Map.Entry<IField, Set<Annotation>> entry : fieldAnnotations.entrySet()) {
			for (Annotation annotation : entry.getValue()) {
				for (String annotationClass : annotationClasses) {
					if (annotation.getAnnotationClass().equals(annotationClass)) {
						result.put(entry.getKey(), annotation);
					}
				}
			}
		}
		return result;
	}

	public boolean hasFieldLevelAnnotations(String... annotationClasses) {
		List<String> annoatations = Arrays.asList(annotationClasses);
		for (Map.Entry<IField, Set<Annotation>> entry : fieldAnnotations.entrySet()) {
			for (Annotation annotation : entry.getValue()) {
				if (annoatations.contains(annotation.getAnnotationClass())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
		final String annotationClass = Type.getType(desc).getClassName();
		if (!classAnnotations.containsKey(annotationClass)) {
			final Annotation annotation = new Annotation(annotationClass);
			classAnnotations.put(annotationClass, annotation);
			return new AnnotationMemberVisitor(annotation, this.classloader, advancedValueProcessing);
		} else {
			return EMPTY_VISITOR;
		}
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, String[] exceptions) {
		String methodKey = name + desc;
		if (!visitedMethods.contains(methodKey)) {
			visitedMethods.add(methodKey);

			return new EmptyVisitor() {
				@Override
				public AnnotationVisitor visitAnnotation(final String annotationDesc, boolean visible) {
					final String annotationClass = Type.getType(annotationDesc).getClassName();
					final IMethod method = getMethodFromSignature(name, desc);

					if (method != null) {
						final Set<Annotation> methodAnnotations = getAnnotationSet(method);
						final Annotation annotation = new Annotation(annotationClass);
						methodAnnotations.add(annotation);
						return new AnnotationMemberVisitor(annotation, classloader, advancedValueProcessing);
					}
					return EMPTY_VISITOR;
				}
			};
		}
		return EMPTY_VISITOR;
	}

	@Override
	public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, Object value) {
		return new EmptyVisitor() {
			@Override
			public AnnotationVisitor visitAnnotation(final String annotationDesc, boolean visible) {
				final String annotationClass = Type.getType(annotationDesc).getClassName();
				final IField field = getFieldFromSignature(name);

				if (field != null) {
					final Set<Annotation> fieldAnnotations = getAnnotationSet(field);
					final Annotation annotation = new Annotation(annotationClass);
					fieldAnnotations.add(annotation);
					return new AnnotationMemberVisitor(annotation, classloader, advancedValueProcessing);
				}
				return EMPTY_VISITOR;
			}

		};
	}

	private Set<Annotation> getAnnotationSet(IMethod method) {
		if (!methodAnnotations.containsKey(method)) {
			methodAnnotations.put(method, new LinkedHashSet<Annotation>());
		}
		return methodAnnotations.get(method);
	}

	private Set<Annotation> getAnnotationSet(IField field) {
		if (!fieldAnnotations.containsKey(field)) {
			fieldAnnotations.put(field, new LinkedHashSet<Annotation>());
		}
		return fieldAnnotations.get(field);
	}

	private IMethod getMethodFromSignature(final String name, final String desc) {
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

	private IField getFieldFromSignature(final String name) {
		IField field = quickCheckForField(name);
		if (field == null) {
			field = JdtUtils.getField(type, name, false);
		}
		return field;
	}

	private IField quickCheckForField(String name) {
		IField field = type.getField(name);
		return field != null && field.exists() ? field : null;
	}

}