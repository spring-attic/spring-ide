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
package org.springframework.ide.eclipse.javaconfig.core.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;

/**
 * ASM based {@link ClassVisitor} implementation that reads the Spring
 * JavaConfig meta data annotations {@link Configuration} and {@link Bean} from
 * the given {@link InputStream} representing a class.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ConfigurationClassVisitor extends EmptyVisitor {

	private static class BeanCreationMethodVisitor extends EmptyVisitor {

		private final BeanCreationMethod beanCreationMethod;

		public BeanCreationMethodVisitor(BeanCreationMethod beanCreationMethod) {
			this.beanCreationMethod = beanCreationMethod;
		}

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (BEAN_ANNOTATION_DESC.equals(desc)) {

				// mark precedence of @Bean annotation
				beanCreationMethod.setBeanCreationMethod(true);

				return new AnnotationVisitor() {

					private String lastAnnotationName = null;

					public void visit(String name, Object value) {
						if (name != null) {
							lastAnnotationName = name;
						}
						else {
							name = lastAnnotationName;
						}
						if (value != null) {
							if ("aliases".equals(name)) {
								beanCreationMethod.addAlias((String) value);
							}
							else if ("scope".equals(name)) {
								beanCreationMethod.setScope((String) value);
							}
							else if ("destroyMethodName".equals(name)) {
								beanCreationMethod
										.setDestoryMethodName((String) value);
							}
							else if ("initMethodName".equals(name)) {
								beanCreationMethod
										.setInitMethodName((String) value);
							}
							else if ("dependsOn".equals(name)) {
								beanCreationMethod.addDependsOn((String) value);
							}
							else if ("allowOverriding".equals(name)) {
								beanCreationMethod.setAllowsOverriding(Boolean
										.valueOf((String) value));
							}
						}
					}

					public AnnotationVisitor visitAnnotation(String name,
							String value) {
						lastAnnotationName = name;
						return this;
					}

					public AnnotationVisitor visitArray(String name) {
						lastAnnotationName = name;
						return this;
					}

					public void visitEnd() {
					}

					public void visitEnum(String arg0, String arg1, String arg2) {
					}
				};
			}
			else {
				return new EmptyVisitor();
			}

		}
	}

	private static final String BEAN_ANNOTATION_DESC = Type
			.getDescriptor(Bean.class);

	private static final String CONFIGURATION_ANNOTATION_DESC = Type
			.getDescriptor(Configuration.class);

	private static final String OBJECT_CLASS = Type
			.getInternalName(Object.class);

	private Stack<BeanCreationMethod> beanCreationMethods = new Stack<BeanCreationMethod>();

	private boolean isConfigurationAnnotationPresent = false;

	private String superClassName = null;

	private String className = null;

	public List<BeanCreationMethod> getBeanCreationMethods() {
		List<BeanCreationMethod> validBeanCreationMethods = new ArrayList<BeanCreationMethod>();
		for (BeanCreationMethod beanCreationMethod : beanCreationMethods) {
			if (beanCreationMethod.isBeanCreationMethod) {
				validBeanCreationMethods.add(beanCreationMethod);
			}
		}
		return validBeanCreationMethods;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		if (!OBJECT_CLASS.equals(superName)) {
			this.superClassName = superName;
		}
		this.className = name.replace('/', '.');
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (CONFIGURATION_ANNOTATION_DESC.equals(desc) && visible) {
			isConfigurationAnnotationPresent = true;
		}
		return new EmptyVisitor();
	}

	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
		// TODO do I need to support that?
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if (isConfigurationAnnotationPresent) {

			BeanCreationMethod beanCreationMethod = new BeanCreationMethod(
					name, Type.getReturnType(desc).getClassName(),
					this.className);
			this.beanCreationMethods.push(beanCreationMethod);

			beanCreationMethod.setPublic(Opcodes.ACC_PUBLIC == access);
			Type[] parameterTypes = Type.getArgumentTypes(desc);
			if (parameterTypes != null && parameterTypes.length > 0) {
				for (Type parameterType : parameterTypes) {
					beanCreationMethod.addParameterTypes(parameterType
							.getClassName());
				}
			}
			return new BeanCreationMethodVisitor(beanCreationMethod);
		}
		else {
			return new EmptyVisitor();
		}
	}
}
