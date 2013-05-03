/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.SpringAsmInfo;
import org.springframework.asm.Type;
import org.springframework.ide.eclipse.aop.core.internal.model.AnnotationAspectDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.AnnotationIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.core.type.asm.EmptyAnnotationVisitor;
import org.springframework.ide.eclipse.core.type.asm.EmptyFieldVisitor;
import org.springframework.ide.eclipse.core.type.asm.EmptyMethodVisitor;
import org.springframework.util.StringUtils;

/**
 * ASM-based Visitor that collects all <code>@AspectJ<code>-style annotations
 * 
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Martin Lippert
 * @since 2.0
 */
public class AdviceAnnotationVisitor extends ClassVisitor {

	private static final String BEFORE_ANNOTATION_DESC = Type.getDescriptor(Before.class);

	private static final String AFTER_ANNOTATION_DESC = Type.getDescriptor(After.class);

	private static final String AFTERRETURNING_ANNOTATION_DESC = Type.getDescriptor(AfterReturning.class);

	private static final String AFTERTHROWING_ANNOTATION_DESC = Type.getDescriptor(AfterThrowing.class);

	private static final String AROUND_ANNOTATION_DESC = Type.getDescriptor(Around.class);

	private static final String DECLARE_PARENTS_ANNOTATION_DESC = Type.getDescriptor(DeclareParents.class);

	private static Map<String, ADVICE_TYPE> ANNOTATION_TYPES = null;

	static {
		ANNOTATION_TYPES = new HashMap<String, ADVICE_TYPE>();
		ANNOTATION_TYPES.put(BEFORE_ANNOTATION_DESC, ADVICE_TYPE.BEFORE);
		ANNOTATION_TYPES.put(AFTER_ANNOTATION_DESC, ADVICE_TYPE.AFTER);
		ANNOTATION_TYPES.put(AFTERRETURNING_ANNOTATION_DESC, ADVICE_TYPE.AFTER_RETURNING);
		ANNOTATION_TYPES.put(AFTERTHROWING_ANNOTATION_DESC, ADVICE_TYPE.AFTER_THROWING);
		ANNOTATION_TYPES.put(AROUND_ANNOTATION_DESC, ADVICE_TYPE.AROUND);
		ANNOTATION_TYPES.put(DECLARE_PARENTS_ANNOTATION_DESC, ADVICE_TYPE.DECLARE_PARENTS);
	}

	private List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();

	private int aspectStartLineNumber;
	private int aspectEndLineNumber;

	private String aspectName;
	private String aspectClassName;

	public AdviceAnnotationVisitor(String aspectName, String aspectClassName, int aspectStartLineNumber, int aspectEndLineNumber) {
		super(SpringAsmInfo.ASM_VERSION);
		this.aspectStartLineNumber = aspectStartLineNumber;
		this.aspectEndLineNumber = aspectEndLineNumber;
		this.aspectName = aspectName;
		this.aspectClassName = aspectClassName;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		Type[] types = Type.getArgumentTypes(desc);
		StringBuffer buf = new StringBuffer(name);
		if (types != null && types.length > 0) {
			buf.append("(");
			for (int i = 0; i < types.length; i++) {
				Type type = types[i];
				buf.append(type.getClassName());
				if (i < (types.length - 1)) {
					buf.append(", ");
				}
			}
			buf.append(")");
		}

		final String visitedMethod = buf.toString();

		return new EmptyMethodVisitor() {
			@Override
			public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
				if (visible && ANNOTATION_TYPES.containsKey(desc)) {
					final AnnotationAspectDefinition def = new AnnotationAspectDefinition();
					aspectDefinitions.add(def);

					def.setAspectClassName(aspectClassName);
					def.setAdviceMethodName(visitedMethod);
					def.setAspectStartLineNumber(aspectStartLineNumber);
					def.setAspectEndLineNumber(aspectEndLineNumber);
					def.setAspectName(aspectName);
					def.setType(ANNOTATION_TYPES.get(desc));

					return new EmptyAnnotationVisitor() {
						@Override
						public void visit(String name, Object value) {
							if ("value".equals(name) || "pointcut".equals(name)) {
								def.setPointcutExpression(value.toString());
							} else if ("argNames".equals(name)) {
								def.setArgNames(StringUtils.commaDelimitedListToStringArray(value.toString()));
							} else if ("returning".equals(name)) {
								def.setReturning(value.toString());
							} else if ("throwing".equals(name)) {
								def.setThrowing(value.toString());
							}
						}
					};
				}
				else {
					return new EmptyAnnotationVisitor();
				}
			}
		};
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		final String visitedField = name;
		final String visitedFieldType = Type.getType(desc).getClassName();

		return new EmptyFieldVisitor() {
			@Override
			public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
				if (visible && ANNOTATION_TYPES.containsKey(desc)) {
					final AnnotationIntroductionDefinition def = new AnnotationIntroductionDefinition();
					aspectDefinitions.add(def);
	
					def.setDefiningField(visitedField);
					def.setIntroducedInterfaceName(visitedFieldType);
	
					def.setAspectClassName(aspectClassName);
					def.setAspectName(aspectName);
					def.setAspectStartLineNumber(aspectStartLineNumber);
					def.setAspectEndLineNumber(aspectEndLineNumber);
	
					return new EmptyAnnotationVisitor() {
						public void visit(String name, Object value) {
							if ("defaultImpl".equals(name)) {
								def.setDefaultImplName(value.toString());
							} else if ("value".equals(name)) {
								def.setTypePattern(value.toString());
							}
						};
					};
				}
				else {
					return new EmptyAnnotationVisitor();
				}
			}
		};
	}

	public List<IAspectDefinition> getAspectDefinitions() {
		return aspectDefinitions;
	}

}
