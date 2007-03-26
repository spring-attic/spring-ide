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
package org.springframework.ide.eclipse.aop.core.model.builder;

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
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.model.internal.AnnotationAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.AnnotationIntroductionDefinition;
import org.springframework.util.StringUtils;

/**
 * ASM-based Visitor that collects all @AspectJ-style annotations
 * 
 * @author Christian Dupuis
 * @since 2.0
 *  
 */
@SuppressWarnings("restriction")
public class AdviceAnnotationVisitor extends EmptyVisitor {

	private static final String BEFORE_ANNOTATION_DESC = "L"
			+ Before.class.getName().replace('.', '/') + ";";

	private static final String AFTER_ANNOTATION_DESC = "L"
			+ After.class.getName().replace('.', '/') + ";";

	private static final String AFTERRETURNING_ANNOTATION_DESC = "L"
			+ AfterReturning.class.getName().replace('.', '/') + ";";

	private static final String AFTERTHROWING_ANNOTATION_DESC = "L"
			+ AfterThrowing.class.getName().replace('.', '/') + ";";

	private static final String AROUND_ANNOTATION_DESC = "L"
			+ Around.class.getName().replace('.', '/') + ";";

	private static final String DECLARE_PARENTS_ANNOTATION_DESC = "L"
			+ DeclareParents.class.getName().replace('.', '/') + ";";

	private static Map<String, ADVICE_TYPES> ANNOTATION_TYPES = null;

	static {
		ANNOTATION_TYPES = new HashMap<String, ADVICE_TYPES>();
		ANNOTATION_TYPES.put(BEFORE_ANNOTATION_DESC, ADVICE_TYPES.BEFORE);
		ANNOTATION_TYPES.put(AFTER_ANNOTATION_DESC, ADVICE_TYPES.AFTER);
		ANNOTATION_TYPES.put(AFTERRETURNING_ANNOTATION_DESC,
				ADVICE_TYPES.AFTER_RETURNING);
		ANNOTATION_TYPES.put(AFTERTHROWING_ANNOTATION_DESC,
				ADVICE_TYPES.AFTER_THROWING);
		ANNOTATION_TYPES.put(AROUND_ANNOTATION_DESC, ADVICE_TYPES.AROUND);
		ANNOTATION_TYPES.put(DECLARE_PARENTS_ANNOTATION_DESC,
				ADVICE_TYPES.DECLARE_PARENTS);
	}

	private String visitedMethod = null;

	private String visitedField = null;

	private String visitedFieldType = null;

	private List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();

	private IAspectDefinition lastAspectDefinition = null;

	private IDOMNode node;

	private String aspectName;

	private String aspectClassName;

	public AdviceAnnotationVisitor(IDOMNode node, String aspectName,
			String aspectClassName) {
		this.node = node;
		this.aspectName = aspectName;
		this.aspectClassName = aspectClassName;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (visitedMethod != null && visible
				&& ANNOTATION_TYPES.containsKey(desc)) {

			AnnotationAspectDefinition def = new AnnotationAspectDefinition();
			aspectDefinitions.add(def);
			lastAspectDefinition = def;

			def.setAspectClassName(aspectClassName);
			def.setAdviceMethodName(visitedMethod);
			def.setNode(node);
			def.setAspectName(aspectName);
			def.setType(ANNOTATION_TYPES.get(desc));

			visitedMethod = null;
			visitedField = null;
			return this;
		}
		else if (visitedField != null && visible
				&& ANNOTATION_TYPES.containsKey(desc)) {
			AnnotationIntroductionDefinition def = new AnnotationIntroductionDefinition();
			aspectDefinitions.add(def);
			lastAspectDefinition = def;

			def.setDefiningField(visitedField);
			def.setIntroducedInterfaceName(visitedFieldType);

			def.setAspectClassName(aspectClassName);
			def.setAspectName(aspectName);
			def.setNode(node);

			visitedMethod = null;
			visitedField = null;
			return this;
		}
		return new EmptyVisitor();
	}

	@Override
	public void visit(String name, Object value) {
		if (lastAspectDefinition != null) {
			if (lastAspectDefinition instanceof AnnotationAspectDefinition) {
				if ("value".equals(name) || "pointcut".equals(name)) {
					((AnnotationAspectDefinition) lastAspectDefinition)
							.setPointcutExpression(value.toString());
				}
				else if ("argNames".equals(name)) {
					lastAspectDefinition.setArgNames(StringUtils
							.commaDelimitedListToStringArray(value.toString()));
				}
				else if ("returning".equals(name)) {
					lastAspectDefinition.setReturning(value.toString());
				}
				else if ("throwing".equals(name)) {
					lastAspectDefinition.setThrowing(value.toString());
				}
			}
			else if (lastAspectDefinition instanceof AnnotationIntroductionDefinition) {
				if ("defaultImpl".equals(name)) {
					((AnnotationIntroductionDefinition) lastAspectDefinition)
							.setDefaultImplName(value.toString());
				}
				else if ("value".equals(name)) {
					((AnnotationIntroductionDefinition) lastAspectDefinition)
							.setTypePattern(value.toString());
				}
			}
		}
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		visitedField = name;
		visitedFieldType = Type.getType(desc).getClassName();
		visitedMethod = null;
		return this;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
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
		visitedMethod = buf.toString();
		visitedField = null;
		lastAspectDefinition = null;
		return this;
	}

	public List<IAspectDefinition> getAspectDefinitions() {
		return aspectDefinitions;
	}

}
