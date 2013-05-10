/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
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
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.SpringAsmInfo;
import org.springframework.asm.Type;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ide.eclipse.core.type.asm.EmptyAnnotationVisitor;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 3.2.0
 */
public class AnnotationMemberVisitor extends AnnotationVisitor {

	private final Annotation annotation;
	private final ClassLoader classloader;
	private boolean advancedValueProcessing;

	public AnnotationMemberVisitor(Annotation annotation, ClassLoader classloader, boolean advancedValueProcessing) {
		super(SpringAsmInfo.ASM_VERSION);
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

			// special case for Enum values, load them from IDE classloader
			// space to avoid conflicts between Spring framework
			// running in IDE and Spring framework classes from the projects
			// classpath
			if (defaultValue != null && defaultValue.getClass().isEnum()) {
				try {
					Class<?> annotationClassInIdeSpace = this.getClass().getClassLoader().loadClass(annotation.getAnnotationClass());
					Method annotationAttributeInIdeSpace = annotationClassInIdeSpace.getMethod(annotationAttribute.getName(),
							annotationAttribute.getParameterTypes());
					defaultValue = annotationAttributeInIdeSpace.getDefaultValue();
				} catch (ClassNotFoundException ex) {
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				}
			}

			if (defaultValue != null && !annotation.hasMember(attributeName)) {
				if (defaultValue instanceof java.lang.annotation.Annotation) {
					defaultValue = AnnotationAttributes.fromMap(AnnotationUtils.getAnnotationAttributes(
							(java.lang.annotation.Annotation) defaultValue, false, true));
				} else if (defaultValue instanceof java.lang.annotation.Annotation[]) {
					java.lang.annotation.Annotation[] realAnnotations = (java.lang.annotation.Annotation[]) defaultValue;
					AnnotationAttributes[] mappedAnnotations = new AnnotationAttributes[realAnnotations.length];
					for (int i = 0; i < realAnnotations.length; i++) {
						mappedAnnotations[i] = AnnotationAttributes.fromMap(AnnotationUtils.getAnnotationAttributes(realAnnotations[i], false,
								true));
					}
					defaultValue = mappedAnnotations;
				}
				annotation.addMember(new AnnotationMemberValuePair(attributeName, defaultValue.toString(), defaultValue));
			}
		}
	}

	@Override
	public void visit(String name, Object value) {
		if ("value".equals(name)) {
			annotation.addMember(new AnnotationMemberValuePair(null, value.toString()));
		} else {
			annotation.addMember(new AnnotationMemberValuePair(name, value.toString()));
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
		return AnnotationMetadataReadingVisitor.EMPTY_ANNOTATION_VISITOR;
	}

	@Override
	public AnnotationVisitor visitArray(final String name) {
		final Set<Object> values = new LinkedHashSet<Object>();
		return new EmptyAnnotationVisitor() {

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

				if (typeOfArray != null && name.equals("value") && !advancedValueProcessing) {
					annotation.addMember(new AnnotationMemberValuePair(null, value, (Object[]) values.toArray((Object[]) Array.newInstance(
							typeOfArray, values.size()))));
				} else if (typeOfArray != null && !annotation.hasMember(name)) {
					annotation.addMember(new AnnotationMemberValuePair(name, value, (Object[]) values.toArray((Object[]) Array.newInstance(
							typeOfArray, values.size()))));
				}
			}

			@Override
			public void visitEnum(String enumName, String type, String enumValue) {
				String className = Type.getType(type).getClassName();
				String value = ClassUtils.getShortName(className) + "." + enumValue;
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