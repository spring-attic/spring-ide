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
import org.springframework.ide.eclipse.beans.core.metadata.model.IAnnotationBeanMetadataProvider;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.annotation.Annotation;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link IAnnotationBeanMetadataProvider} for the {@link Transactional} anntation.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class TransactionalAnnotationMetadataProvider implements IAnnotationBeanMetadataProvider {

	/** The {@link Transactional} annotation class */
	private static final String TRANSACTIONAL_CLASS = Transactional.class.getName();

	public Set<IBeanMetadata> provideBeanMetadata(IBean bean, IType type, IAnnotationMetadata visitor) {
		Set<IBeanMetadata> beanMetaDataSet = new LinkedHashSet<IBeanMetadata>();
		try {
			if (visitor.hasTypeLevelAnnotations(TRANSACTIONAL_CLASS)
					|| visitor.hasMethodLevelAnnotations(TRANSACTIONAL_CLASS)) {
				Set<IMethodMetadata> methodMetaData = new HashSet<IMethodMetadata>();
				for (Map.Entry<IMethod, Annotation> entry : visitor.getMethodLevelAnnotations(TRANSACTIONAL_CLASS)
						.entrySet()) {
					methodMetaData.add(new TransactionalMethodAnnotationMetadata(TRANSACTIONAL_CLASS, entry.getKey()
							.getHandleIdentifier(), entry.getValue().getMembers(), new JavaModelSourceLocation(entry
							.getKey())));
				}
				beanMetaDataSet.add(new TransactionalAnnotationMetadata(bean, TRANSACTIONAL_CLASS, (visitor
						.hasTypeLevelAnnotations(TRANSACTIONAL_CLASS) ? visitor.getTypeLevelAnnotation(
						TRANSACTIONAL_CLASS).getMembers() : null), new JavaModelSourceLocation(type), methodMetaData));
			}
		}
		catch (JavaModelException e) {
			// just fail quietly
		}
		return beanMetaDataSet;
	}

}
