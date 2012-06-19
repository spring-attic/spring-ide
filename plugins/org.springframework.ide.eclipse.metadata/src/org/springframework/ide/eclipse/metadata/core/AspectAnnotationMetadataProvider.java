/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.metadata.core;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareAnnotation;
import org.aspectj.lang.annotation.DeclareError;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.DeclareWarning;
import org.aspectj.lang.annotation.Pointcut;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.metadata.model.IAnnotationBeanMetadataProvider;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.annotation.Annotation;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;

/**
 * {@link IAnnotationBeanMetadataProvider} that is responsible for creating metadata based on
 * AspectJ 5 annotations, like {@link Aspect}, {@link Pointcut}.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class AspectAnnotationMetadataProvider implements IAnnotationBeanMetadataProvider {
	
	/** FQCN of the {@link Aspect} annotation */
	private static final String ASPECT_ANNOTATION_CLASS = Aspect.class.getName();

	/** FQCN of the method level annotations from AspectJ 5 */
	private static final String[] METHOD_ANNOTATION_CLASSES = new String[] {
			Before.class.getName(), After.class.getName(), AfterReturning.class.getName(),
			AfterThrowing.class.getName(), Around.class.getName(), DeclareParents.class.getName(),
			DeclareAnnotation.class.getName(), DeclareWarning.class.getName(),
			DeclareError.class.getName(), Pointcut.class.getName() };
	
	/**
	 * Creates bean metadata based on AspectJ annotations.
	 */
	public Set<IBeanMetadata> provideBeanMetadata(IBean bean, IType type,
			IAnnotationMetadata visitor) {
		Set<IBeanMetadata> beanMetaDataSet = new LinkedHashSet<IBeanMetadata>();
		try {
			if (visitor.hasTypeLevelAnnotations(ASPECT_ANNOTATION_CLASS)) {
				Set<IMethodMetadata> methodMetaData = new HashSet<IMethodMetadata>();
				for (Map.Entry<IMethod, Annotation> entry : visitor.getMethodLevelAnnotations(
						METHOD_ANNOTATION_CLASSES).entrySet()) {
					methodMetaData.add(new AspectMethodAnnotationMetadata(entry.getValue()
							.getAnnotationClass(), entry.getKey().getHandleIdentifier(), entry
							.getValue().getMembers(), new JavaModelSourceLocation(entry.getKey())));
				}
				beanMetaDataSet.add(new AspectAnnotationMetadata(bean, ASPECT_ANNOTATION_CLASS,
						(visitor.hasTypeLevelAnnotations(ASPECT_ANNOTATION_CLASS) ? visitor
								.getTypeLevelAnnotation(ASPECT_ANNOTATION_CLASS).getMembers()
								: null), new JavaModelSourceLocation(type), methodMetaData));
			}
		}
		catch (JavaModelException e) {
			// just fail quietly
		}
		return beanMetaDataSet;
	}

}
