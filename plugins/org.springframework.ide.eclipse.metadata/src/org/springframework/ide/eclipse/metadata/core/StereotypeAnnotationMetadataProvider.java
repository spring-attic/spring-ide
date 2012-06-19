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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.eclipse.beans.core.metadata.model.IAnnotationBeanMetadataProvider;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * {@link IAnnotationBeanMetadataProvider} for Spring's stereotype annotations.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class StereotypeAnnotationMetadataProvider implements IAnnotationBeanMetadataProvider {

	/** The stereotype annotation classes supported by this provider */
	private static final List<String> SUPPORTED_STEREOTYPE_ANNOTATIONS = Arrays
			.asList(new String[] { Repository.class.getName(), Service.class.getName(), Component.class.getName(),
					Controller.class.getName() });

	/**
	 * {@inheritDoc}
	 */
	public Set<IBeanMetadata> provideBeanMetadata(IBean bean, IType type, IAnnotationMetadata visitor) {
		Set<IBeanMetadata> beanMetaDataSet = new LinkedHashSet<IBeanMetadata>();
		for (String annotationTypeName : visitor.getTypeLevelAnnotationClasses()) {
			if (!annotationTypeName.equals(Configuration.class.getName())
					&& !processStereoTypeAnnotation(bean, type, type, visitor, beanMetaDataSet, annotationTypeName,
							annotationTypeName)) {
				try {
					IType annotationType = type.getJavaProject().findType(annotationTypeName);
					if (annotationType != null && Flags.isAnnotation(annotationType.getFlags())) {
						for (IAnnotation annotation : annotationType.getAnnotations()) {
							String className = JdtUtils.resolveClassName(annotation.getElementName(), annotationType);
							processStereoTypeAnnotation(bean, type, annotationType, visitor, beanMetaDataSet,
									annotationTypeName, className);
						}
					}
				}
				catch (JavaModelException e) {
					// Nothing to report here
				}
			}
		}
		return beanMetaDataSet;
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean processStereoTypeAnnotation(IBean bean, IType beanType, IType type, IAnnotationMetadata visitor,
			Set<IBeanMetadata> beanMetaDataSet, String annotationTypeName, String annotationTypeNameToCheck) {
		if (SUPPORTED_STEREOTYPE_ANNOTATIONS.contains(annotationTypeNameToCheck)) {
			try {
				beanMetaDataSet
						.add(new StereotypeAnnotationMetadata(bean, annotationTypeName, visitor.getTypeLevelAnnotation(
								annotationTypeName).getMembers(), new JavaModelSourceLocation(beanType)));
			}
			catch (JavaModelException e) {
				// Nothing to report here
			}
			return true;
		}
		return false;
	}
}
