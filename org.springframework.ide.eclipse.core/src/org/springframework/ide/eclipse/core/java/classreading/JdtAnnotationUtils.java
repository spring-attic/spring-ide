/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christian Dupuis
 * @since 2.2.5
 */
public abstract class JdtAnnotationUtils {

	public static void processAnnotation(IAnnotation annotation, IType type,
			Map<String, Map<String, Object>> annotationMap) {
		Map<String, Object> attributesMap = new HashMap<String, Object>();
		String annotationName = null;
		if (type.isBinary()) {
			annotationName = annotation.getElementName();
		}
		else {
			annotationName = JdtUtils.resolveClassName(annotation.getElementName(), type);
		}
		IType annotationType = JdtUtils.getJavaType(type.getJavaProject().getProject(), annotationName);
		try {
			for (IMemberValuePair member : annotation.getMemberValuePairs()) {
				processAnnotation(member, annotationType, attributesMap, type);
			}

			for (IMethod annotationMethod : Introspector.getAllMethods(annotationType)) {
				if (annotationMethod.getDefaultValue() != null) {
					IMemberValuePair member = annotationMethod.getDefaultValue();
					if (!attributesMap.containsKey(member.getMemberName())) {
						processAnnotation(member, annotationType, attributesMap, type);
					}
				}
			}

			annotationMap.put(annotationName, attributesMap);
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
	}

	public static void processAnnotation(IMemberValuePair member, IType annotationType,
			Map<String, Object> attributesMap, IType type) throws JavaModelException {
		IMethod test = annotationType.getMethod(member.getMemberName(), null);
		int kind = Signature.getTypeSignatureKind(test.getReturnType());
		String returnTypeName = Signature.getElementType(test.getReturnType());
		String returnType = JdtUtils.resolveClassName(Signature.getSignatureSimpleName(returnTypeName), annotationType);

		if (member.getValueKind() == IMemberValuePair.K_STRING && returnType.equals(String.class.getName())) {
			if (kind == Signature.ARRAY_TYPE_SIGNATURE) {
				if (member.getValue().getClass().isArray()) {
					attributesMap.put(member.getMemberName(), member.getValue());
				}
				else {
					attributesMap.put(member.getMemberName(), new String[] { member.getValue().toString() });
				}
			}
			else {
				attributesMap.put(member.getMemberName(), member.getValue().toString());
			}
		}
		// support for class
		else if (member.getValueKind() == IMemberValuePair.K_CLASS && returnType.equals(Class.class.getName())) {
			if (kind == Signature.ARRAY_TYPE_SIGNATURE) {
				if (member.getValue().getClass().isArray()) {
					String[] values = Arrays.asList((Object[]) member.getValue()).toArray(new String[0]);
					for (int i = 0; i < values.length; i++) {
						String className = values[i];
						values[i] = JdtUtils.resolveClassName(className, type);
					}
					attributesMap.put(member.getMemberName(), values);
				}
				else if (member.getValue() instanceof String) {
					String className = (String) member.getValue();
					attributesMap.put(member.getMemberName(),
							new String[] { JdtUtils.resolveClassName(className, type) });
				}
				else {
					attributesMap.put(member.getMemberName(), member.getValue().toString());
				}
			}
			else {
				attributesMap.put(member.getMemberName(), member.getValue().toString());
			}
		}
		// support for empty string[]s
		else if (member.getValueKind() == IMemberValuePair.K_UNKNOWN && String.class.getName().equals(returnType)
				&& kind == Signature.ARRAY_TYPE_SIGNATURE) {
			attributesMap.put(member.getMemberName(), new String[0]);
		}
		// support for enum values
		else if (member.getValueKind() == IMemberValuePair.K_QUALIFIED_NAME) {
			String enumValue = member.getValue().toString();
			int i = enumValue.lastIndexOf('.');
			if (i > 0) {
				enumValue = enumValue.substring(i + 1);
			}

			Object valueToUse = member.getValue();
			try {
				Class<?> enumType = JdtAnnotationUtils.class.getClassLoader().loadClass(returnType);
				Field enumConstant = ReflectionUtils.findField(enumType, enumValue);
				if (enumConstant != null) {
					valueToUse = enumConstant.get(null);
				}
			}
			catch (Exception ex) {
				// Class not found - can't resolve class reference in annotation attribute.
			}
			attributesMap.put(member.getMemberName(), valueToUse);
		}
		else {
			attributesMap.put(member.getMemberName(), member.getValue());
		}
	}

}
