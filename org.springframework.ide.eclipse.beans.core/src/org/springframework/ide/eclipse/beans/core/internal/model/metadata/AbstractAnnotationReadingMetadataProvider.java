/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.metadata;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.objectweb.asm.ClassReader;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadataProvider;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;

/**
 * Abstract base {@link IBeanMetadataProvider} that uses a
 * {@link AnnotationMetadataReadingVisitor} to load annotation meta data from
 * the {@link IBean}'s bean class.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public abstract class AbstractAnnotationReadingMetadataProvider implements IBeanMetadataProvider {

	public final Set<IBeanMetadata> provideBeanMetadata(IBean bean, IBeansConfig beansConfig,
			IProgressMonitor progressMonitor, ClassReaderFactory classReaderFactory) {

		long start = System.currentTimeMillis();

		Set<IBeanMetadata> beanMetaDataSet = new HashSet<IBeanMetadata>();

		IType type = JdtUtils.getJavaType(bean.getElementResource().getProject(), bean
				.getClassName());
		IType originalType = type;
		if (type != null && !type.isBinary()) {
			AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor();

			String className = type.getFullyQualifiedName();
			try {
				while (className != null && !Object.class.getName().equals(className)
						&& type != null && !type.isBinary()) {
					ClassReader classReader = classReaderFactory.getClassReader(className);
					visitor.setType(type);
					classReader.accept(visitor, false);
					className = visitor.getSuperClassName();
					type = JdtUtils.getJavaType(bean.getElementResource().getProject(), className);
				}
			}
			catch (IOException e) {
				// Don't report exception here
			}

			// Call the actual processing of the found annotations
			processFoundAnnotations(bean, beanMetaDataSet, originalType, visitor);

		}
		if (BeanMetadataModel.DEBUG) {
			System.out.println("Processing bean [" + bean + "] took "
					+ (System.currentTimeMillis() - start) + "ms");
		}
		return beanMetaDataSet;
	}

	/**
	 * Method to be implemented by sub classes to process found annotation on
	 * {@link IBean} classes.
	 * @param bean the current {@link IBean}
	 * @param beanMetaDataSet the {@link Set} of {@link IBeanMetadata} to add the new meta data to
	 * @param type the current {@link IType} that was loaded from the bean's class name
	 * @param visitor the annotation visitor
	 */
	protected abstract void processFoundAnnotations(IBean bean, Set<IBeanMetadata> beanMetaDataSet,
			IType type, AnnotationMetadataReadingVisitor visitor);
}
