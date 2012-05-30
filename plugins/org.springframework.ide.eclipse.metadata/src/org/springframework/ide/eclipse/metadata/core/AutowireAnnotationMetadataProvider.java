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

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ide.eclipse.beans.core.metadata.model.IAnnotationBeanMetadataProvider;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.annotation.Annotation;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;

/**
 * {@link IAnnotationBeanMetadataProvider} that is responsible for creating metadata based on Spring 3.0
 * {@link Autowired}.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class AutowireAnnotationMetadataProvider implements IAnnotationBeanMetadataProvider {

	/** FQCN of the method level annotations */
	private static final String[] ANNOTATION_CLASSES = new String[] { Value.class.getName(), Autowired.class.getName(),
			"javax.inject.Inject", "javax.ejb.EJB", "javax.annotation.Resource", "javax.xml.ws.WebServiceRef" };

	/**
	 * Creates bean metadata based on Spring 3.0 autowiring annotations.
	 */
	public Set<IBeanMetadata> provideBeanMetadata(IBean bean, IType type, IAnnotationMetadata visitor) {
		Set<IBeanMetadata> beanMetaDataSet = new LinkedHashSet<IBeanMetadata>();
		try {
			for (String annotationClass : ANNOTATION_CLASSES) {
				Set<IMethodMetadata> methodMetaData = new HashSet<IMethodMetadata>();
				if (visitor.hasMethodLevelAnnotations(annotationClass)) {
					for (Map.Entry<IMethod, Annotation> entry : visitor.getMethodLevelAnnotations(annotationClass)
							.entrySet()) {
						methodMetaData.add(new AutowireMethodAnnotationMetadata(entry.getValue().getAnnotationClass(),
								entry.getKey().getHandleIdentifier(), entry.getValue().getMembers(),
								new JavaModelSourceLocation(entry.getKey())));
					}
				}
				if (visitor.hasFieldLevelAnnotations(annotationClass)) {
					for (Map.Entry<IField, Annotation> entry : visitor.getFieldLevelAnnotations(annotationClass)
							.entrySet()) {
						methodMetaData.add(new AutowireMethodAnnotationMetadata(entry.getValue().getAnnotationClass(),
								entry.getKey().getHandleIdentifier(), entry.getValue().getMembers(),
								new JavaModelSourceLocation(entry.getKey())));
					}
				}
				if (methodMetaData.size() > 0) {
					beanMetaDataSet.add(new AutowireAnnotationMetadata(bean, annotationClass,
							new JavaModelSourceLocation(type), methodMetaData));
				}
			}
		}
		catch (JavaModelException e) {
			// just fail quietly
		}
		return beanMetaDataSet;
	}

}
