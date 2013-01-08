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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.type.asm.ClassMetadataReadingVisitor;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

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

	private static EmptyVisitor EMPTY_VISITOR = new EmptyVisitor();

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
		}
		else {
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
		}
		else {
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
			}
			else {
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
					}
					else {
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
					}
					else {
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

	private static class AnnotationMemberVisitor extends EmptyVisitor {

		private final Annotation annotation;
		private final ClassLoader classloader;
		private boolean advancedValueProcessing;

		public AnnotationMemberVisitor(Annotation annotation, ClassLoader classloader, boolean advancedValueProcessing) {
			this.annotation = annotation;
			this.classloader = classloader;
			this.advancedValueProcessing = advancedValueProcessing;
		}
		
		@Override
		public void visitEnd() {
			if (this.advancedValueProcessing) {
				try {
					Class<?> annotationClass = this.classloader.loadClass(annotation.getAnnotationClass());
					registerDefaultValues(annotationClass);
				} catch (ClassNotFoundException ex) {
				}
			}
		}

		private void registerDefaultValues(Class<?> annotationClass) {
			// Check declared default values of attributes in the annotation
			// type.
			Method[] annotationAttributes = annotationClass.getMethods();
			for (Method annotationAttribute : annotationAttributes) {
				String attributeName = annotationAttribute.getName();
				Object defaultValue = annotationAttribute.getDefaultValue();

				// special case for Enum values, load them from IDE classloader space to avoid conflicts between Spring framework
				// running in IDE and Spring framework classes from the projects classpath
				if (defaultValue != null && defaultValue.getClass().isEnum()) {
					try {
						Class<?> annotationClassInIdeSpace = this.getClass().getClassLoader().loadClass(annotation.getAnnotationClass());
						Method annotationAttributeInIdeSpace = annotationClassInIdeSpace.getMethod(annotationAttribute.getName(), annotationAttribute.getParameterTypes());
						defaultValue = annotationAttributeInIdeSpace.getDefaultValue();
					} catch (ClassNotFoundException ex) {
					} catch (SecurityException e) {
					} catch (NoSuchMethodException e) {
					}
				}

				if (defaultValue != null && !annotation.hasMember(attributeName)) {
					// if (defaultValue instanceof Annotation) {
					// defaultValue = AnnotationAttributes.fromMap(
					// AnnotationUtils.getAnnotationAttributes((Annotation)defaultValue,
					// false, true));
					// }
					// else if (defaultValue instanceof Annotation[]) {
					// Annotation[] realAnnotations = (Annotation[])
					// defaultValue;
					// AnnotationAttributes[] mappedAnnotations = new
					// AnnotationAttributes[realAnnotations.length];
					// for (int i = 0; i < realAnnotations.length; i++) {
					// mappedAnnotations[i] = AnnotationAttributes.fromMap(
					// AnnotationUtils.getAnnotationAttributes(realAnnotations[i],
					// false, true));
					// }
					// defaultValue = mappedAnnotations;
					// }
					annotation.addMember(new AnnotationMemberValuePair(attributeName, defaultValue.toString(), defaultValue));
				}
			}
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

//		@Override
//		public void visit(String name, Object value) {
//			annotation.addMember(new AnnotationMemberValuePair(name, value.toString(), value));
//		}

		@Override
		public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
			return EMPTY_VISITOR;
		}

		@Override
		public AnnotationVisitor visitArray(final String name) {
			final Set<Object> values = new LinkedHashSet<Object>();
			return new EmptyVisitor() {

				@Override
				public void visit(String arg0, Object arg1) {
					if (arg1 instanceof Type && advancedValueProcessing) {
						try {
							Class<?> clazz = classloader.loadClass(((Type) arg1).getClassName());
							values.add(clazz);
						} catch (ClassNotFoundException ex) {
						}
					} else {
						values.add(arg1);
					}
				}

				/**
				 * @Controller("/index.htm") -> value = /index.htm
				 * 
				 * @Controller({"/index1.htm" , "/index2.htm"}) -> value =
				 *                            /index1.htm, /index2.htm
				 * 
				 * @Controller({ RequestMapping.GET, RequestMapping.POST})
				 * @Controller({ org.swf.RequestMapping.GET,
				 *               org.swf.RequestMapping.POST}) -> value =
				 *               RequestMapping.GET, RequestMapping.POST
				 * 
				 * @Controller(RequestMapping.GET)
				 * @Controller(org.swf.RequestMapping.GET) -> value =
				 *                                         RequestMapping.GET
				 */
				@Override
				public void visitEnd() {
					StringBuilder buf = new StringBuilder();
					Class typeOfArray = null;
					for (Object value : values) {
						typeOfArray = value.getClass();
						buf.append(value.toString());
						buf.append(", ");
					}
					String value = buf.toString();
					if (value.length() > 0) {
						value = value.substring(0, value.length() - 2);
					}

					if (name.equals("value") && !advancedValueProcessing) {
						annotation.addMember(new AnnotationMemberValuePair(null, value, (Object[]) values.toArray((Object[]) Array.newInstance(
								typeOfArray, values.size()))));
					}
					else {
						annotation.addMember(new AnnotationMemberValuePair(name, value, (Object[]) values.toArray((Object[]) Array.newInstance(
								typeOfArray, values.size()))));
					}
				}

				@Override
				public void visitEnum(String enumName, String type, String enumValue) {
					String className = Type.getType(type).getClassName();
					Object value = ClassUtils.getShortName(className) + "." + enumValue;
					values.add(value);
				}
			};
		}

		@Override
		public void visitEnum(String name, String type, String enumValue) {
			String className = Type.getType(type).getClassName();
			Object valueAsObject = null;

			try {
				Class<?> enumType = this.getClass().getClassLoader().loadClass(className);
				Field enumConstant = ReflectionUtils.findField(enumType, enumValue);
				if (enumConstant != null) {
					valueAsObject = enumConstant.get(null);
				}
			} catch (ClassNotFoundException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
			
			annotation.addMember(new AnnotationMemberValuePair(name, ClassUtils.getShortName(className) + "." + enumValue, valueAsObject));
		}
	}

}