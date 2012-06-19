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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.eclipse.beans.core.metadata.model.IAnnotationBeanMetadataProvider;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.annotation.Annotation;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;

/**
 * {@link IAnnotationBeanMetadataProvider} that is responsible for creating metadata based on Spring 3.0 {@link Configuration}.
 * @author Christian Dupuis
 * @since 2.1.0
 */
public class ConfigurationClassAnnotationMetadataProvider implements IAnnotationBeanMetadataProvider {

	/** FQCN of the {@link Configuration} annotation */
	private static final String CONFIGURATION_ANNOTATION_CLASS = Configuration.class.getName();

	/** FQCN of the method level annotations */
	private static final String[] BEAN_ANNOTATION_CLASSES = new String[] { Bean.class.getName() };

	/**
	 * Creates bean metadata based on Spring 3.0 {@link Configuration} annotations.
	 */
	public Set<IBeanMetadata> provideBeanMetadata(IBean bean, IType type, IAnnotationMetadata visitor) {
		Set<IBeanMetadata> beanMetaDataSet = new LinkedHashSet<IBeanMetadata>();
		try {
			if (visitor.hasTypeLevelAnnotations(CONFIGURATION_ANNOTATION_CLASS)) {
				Set<IMethodMetadata> methodMetaData = new HashSet<IMethodMetadata>();
				for (Map.Entry<IMethod, Annotation> entry : visitor
						.getMethodLevelAnnotations(BEAN_ANNOTATION_CLASSES).entrySet()) {
					methodMetaData.add(new BeanMethodAnnotationMetadata(entry.getValue().getAnnotationClass(), entry
							.getKey().getHandleIdentifier(), entry.getValue().getMembers(),
							new JavaModelSourceLocation(entry.getKey())));
				}
				beanMetaDataSet.add(new ConfigurationAnnotationMetadata(bean, CONFIGURATION_ANNOTATION_CLASS, (visitor
						.hasTypeLevelAnnotations(CONFIGURATION_ANNOTATION_CLASS) ? visitor.getTypeLevelAnnotation(
						CONFIGURATION_ANNOTATION_CLASS).getMembers() : null), new JavaModelSourceLocation(type),
						methodMetaData));
			}
		}
		catch (JavaModelException e) {
			// just fail quietly
		}
		return beanMetaDataSet;
	}

}
