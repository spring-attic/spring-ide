/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.springframework.ide.eclipse.beans.core.model.metadata.IAnnotationMemberValuePair;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.type.asm.ClassMetadataReadingVisitor;
import org.springframework.util.ClassUtils;

/**
 * ASM based {@link ClassVisitor} that reads and stores all
 * {@link java.lang.annotation.Annotation}s from classes and methods.
 * Furthermore this implementation saves all annotation members as well.
 * @author Christian Dupuis
 * @since 2.0.5
 */
/**
 * TODO CD extract interface to be used by
 * {@link AbstractAnnotationReadingMetadataProvider}
 */
public class AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor {

	private static EmptyVisitor EMPTY_VISITOR = new EmptyVisitor();

	private Set<Annotation> classAnnotations = new HashSet<Annotation>();

	private Map<IMethod, Set<Annotation>> methodAnnotations = new LinkedHashMap<IMethod, Set<Annotation>>();

	private IType type;

	public void setType(IType type) {
		this.type = type;
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

	public boolean hasTypeLevelAnnotation(String... annotationClasses) {
		Set<String> foundAnnoatationClasses = getTypeLevelAnnotationClasses();
		for (String annotationClass : annotationClasses) {
			if (foundAnnoatationClasses.contains(annotationClass)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
		final String annotationClass = Type.getType(desc).getClassName();
		final Annotation annotation = new Annotation(annotationClass);
		classAnnotations.add(annotation);
		return new AnnotationMemberVisitor(annotation);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc,
			final String signature, String[] exceptions) {
		return new EmptyVisitor() {
			@Override
			public AnnotationVisitor visitAnnotation(final String annotationDesc, boolean visible) {
				final String annotationClass = Type.getType(annotationDesc).getClassName();
				final IMethod method = getMethodFromSignature(name, desc);

				if (method != null) {
					final Set<Annotation> methodAnnotations = getAnnotationSet(method);
					final Annotation annotation = new Annotation(annotationClass);
					methodAnnotations.add(annotation);
					return new AnnotationMemberVisitor(annotation);
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

	private IMethod getMethodFromSignature(final String name, final String desc) {
		Type[] parameterTypes = Type.getArgumentTypes(desc);
		List<String> parameters = new ArrayList<String>();
		if (parameterTypes != null && parameterTypes.length > 0) {
			for (Type parameterType : parameterTypes) {
				parameters.add(parameterType.getClassName());
			}
		}
		final IMethod method = JdtUtils.getMethod(type, name, parameters
				.toArray(new String[parameters.size()]));
		return method;
	}

	private static class AnnotationMemberVisitor extends EmptyVisitor {

		private final Annotation annotation;

		public AnnotationMemberVisitor(Annotation annotation) {
			this.annotation = annotation;
		}

		@Override
		public void visit(String name, Object value) {
			if ("value".equals(name)) {
				annotation.addMember(new AnnotationMemberValuePair(null, value.toString()));
			}
			else {
				annotation.addMember(new AnnotationMemberValuePair(name, value.toString()));
			}
		}

		@Override
		public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
			return EMPTY_VISITOR;
		}

		@Override
		public AnnotationVisitor visitArray(final String name) {
			final Set<String> values = new LinkedHashSet<String>();
			return new EmptyVisitor() {

				@Override
				public void visit(String arg0, Object arg1) {
					values.add(arg1.toString());
				}

				@Override
				public void visitEnd() {
					StringBuilder buf = new StringBuilder();
					for (String value : values) {
						buf.append(value);
						buf.append(", ");
					}
					String value = buf.toString();
					if (value.length() > 0) {
						value = value.substring(0, value.length() - 2);
					}
					if (name.equals("value")) {
						annotation.addMember(new AnnotationMemberValuePair(null, value));
					}
					else {
						annotation.addMember(new AnnotationMemberValuePair(name, value));
					}
				}

				@Override
				public void visitEnum(String enumName, String type, String enumValue) {
					String className = Type.getType(type).getClassName();
					values.add(ClassUtils.getShortName(className) + "." + enumValue);
				}
			};
		}

		@Override
		public void visitEnum(String name, String type, String enumValue) {
			String className = Type.getType(type).getClassName();
			annotation.addMember(new AnnotationMemberValuePair(name, ClassUtils
					.getShortName(className)
					+ "." + enumValue));
		}
	}

	public static class Annotation {

		private String annotationClass;

		private Set<IAnnotationMemberValuePair> members;

		public Annotation(String annotationClass) {
			this.annotationClass = annotationClass;
			this.members = new LinkedHashSet<IAnnotationMemberValuePair>();
		}

		public void addMember(IAnnotationMemberValuePair member) {
			this.members.add(member);
		}

		public String getAnnotationClass() {
			return annotationClass;
		}

		public Set<IAnnotationMemberValuePair> getMembers() {
			return members;
		}
	}
}