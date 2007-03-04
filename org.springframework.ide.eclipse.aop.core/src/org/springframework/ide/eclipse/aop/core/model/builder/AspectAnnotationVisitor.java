/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.aop.core.model.builder;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * ASM-based visitor that checks if a certain class has the
 * @Aspect annotation
 * 
 * @author Christian Dupuis
 * @since 2.0
 * 
 */
public class AspectAnnotationVisitor extends EmptyVisitor {

	private ClassInfo classInfo = new ClassInfo();

	private static final String ASPECT_ANNOTATION_DESC = "L"
			+ Aspect.class.getName().replace('.', '/') + ";";

	private static final String OBJECT_CLASS = "java/lang/Object";

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		classInfo.setModifier(access);
		if (!OBJECT_CLASS.equals(superName)) {
			classInfo.setSuperType(superName);
		}
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (visible && ASPECT_ANNOTATION_DESC.equals(desc)) {
			classInfo.setAspectAnnotation(new AspectAnnotation());
			return this;
		}
		return new EmptyVisitor();
	}

	public void visit(String name, Object value) {
		if ("value".equals(name) && classInfo.hasAspectAnnotation()) {
			classInfo.getAspectAnnotation().setValue((String) value);
		}
	}

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
