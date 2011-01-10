/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * ASM-based visitor that checks if a certain class has the
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @Aspect annotation.
 * @since 2.0
 */
public class AspectAnnotationVisitor extends EmptyVisitor {

	private ClassInfo classInfo = new ClassInfo();

	private static final String ASPECT_ANNOTATION_DESC = Type
			.getDescriptor(Aspect.class);

	private static final String OBJECT_CLASS = Type
			.getInternalName(Object.class);

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		classInfo.setModifier(access);
		if (!OBJECT_CLASS.equals(superName)) {
			classInfo.setSuperType(superName);
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (visible && ASPECT_ANNOTATION_DESC.equals(desc)) {
			classInfo.setAspectAnnotation(new AspectAnnotation());
			return this;
		}
		return new EmptyVisitor();
	}

	@Override
	public void visit(String name, Object value) {
		if ("value".equals(name) && classInfo.hasAspectAnnotation()) {
			classInfo.getAspectAnnotation().setValue((String) value);
		}
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		classInfo.getMethodNames().add(name);
		return new EmptyVisitor();
	}

	public static class ClassInfo {

		private int modifier;

		private String superType = null;

		private List<String> methodNames = new ArrayList<String>();

		private AspectAnnotation aspectAnnotation;

		public AspectAnnotation getAspectAnnotation() {
			return aspectAnnotation;
		}

		public void setAspectAnnotation(AspectAnnotation aspectAnnotation) {
			this.aspectAnnotation = aspectAnnotation;
		}

		public String getSuperType() {
			return superType;
		}

		public void setSuperType(String superType) {
			this.superType = superType;
		}

		public List<String> getMethodNames() {
			return methodNames;
		}

		public void setMethodNames(List<String> methodNames) {
			this.methodNames = methodNames;
		}

		public boolean hasAspectAnnotation() {
			return this.aspectAnnotation != null;
		}

		public int getModifier() {
			return modifier;
		}

		public void setModifier(int modifier) {
			this.modifier = modifier;
		}
	}

	public static class AspectAnnotation {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	public ClassInfo getClassInfo() {
		return classInfo;
	}
}
