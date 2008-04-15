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
package org.springframework.ide.eclipse.beans.core.model.metadata;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.objectweb.asm.ClassReader;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.metadata.BeanMetadataModel;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMetadataReadingVisitor;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;

/**
 * Abstract base {@link IBeanMetadataProvider} that uses a {@link AnnotationMetadataReadingVisitor}
 * to load annotation meta data from the {@link IBean}'s bean class.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public abstract class AbstractAnnotationReadingMetadataProvider extends BeanMetadataProviderAdapter
		implements IBeanMetadataProvider {

	/**
	 * Internal cache of {@link ClassReaderFactory} keyed by the corresponding {@link IProject}.
	 */
	private final Map<IProject, ClassReaderFactory> classReaderFactoryCache = 
		new ConcurrentHashMap<IProject, ClassReaderFactory>();

	/**
	 * Internal cache of {@link IAnnotationMetadata} keyed by the corresponding {@link IType}. It
	 * is important to key with {@link IType} and not just with FQCN as a class can exist multiple
	 * times with the same name in different projects.
	 */
	private final Map<IType, IAnnotationMetadata> metadataCache = 
		new ConcurrentHashMap<IType, IAnnotationMetadata>();

	@Override
	public final Set<IBeanMetadata> provideBeanMetadata(IBean bean, IBeansConfig beansConfig,
			IProgressMonitor progressMonitor) {
		long start = System.currentTimeMillis();

		Set<IBeanMetadata> beanMetadata = new HashSet<IBeanMetadata>();

		IType type = JdtUtils.getJavaType(bean.getElementResource().getProject(), bean
				.getClassName());

		// Get annotation meta data
		IAnnotationMetadata visitor = getAnnotationMetadata(bean, getClassReaderFactory(beansConfig
				.getElementResource().getProject()), type);

		if (visitor != null) {
			// Call the actual processing of the found annotations
			processFoundAnnotations(bean, beanMetadata, type, visitor, progressMonitor);
		}

		if (BeanMetadataModel.DEBUG) {
			System.out.println("Processing bean [" + bean + "] took "
					+ (System.currentTimeMillis() - start) + "ms");
		}
		return beanMetadata;
	}

	/**
	 * Returns an {@link IAnnotationMetadata} implementation. This method checks the
	 * {@link #metadataCache} before creating a new instance.
	 */
	protected IAnnotationMetadata getAnnotationMetadata(IBean bean,
			ClassReaderFactory classReaderFactory, IType type) {
		IType orginalType = type;

		// No support for binary types (class files)
		if (type == null || type.isBinary()) {
			return null;
		}

		// Check cache first
		if (metadataCache.containsKey(orginalType)) {
			return metadataCache.get(orginalType);
		}

		// Create new annotation meta data
		AnnotationMetadataReadingVisitor visitor = createAnnotationMetadataReadingVisitor();

		String className = type.getFullyQualifiedName();
		try {
			while (className != null && !Object.class.getName().equals(className) && type != null
					&& !type.isBinary()) {
				ClassReader classReader = classReaderFactory.getClassReader(className);
				visitor.setType(type);
				classReader.accept(visitor, false);
				className = visitor.getSuperClassName();
				type = JdtUtils.getJavaType(bean.getElementResource().getProject(), className);
			}
		}
		catch (IOException e) {
			BeansCorePlugin.log("Error during AST class visiting", e);
		}

		// cache here in case exception was thrown we don't want to retry over and over again
		if (visitor != null) {
			// make sure to cache with the original type
			metadataCache.put(orginalType, visitor);
		}
		return visitor;
	}

	/**
	 * Returns a {@link ClassReaderFactory} for the given <code>project</code>.
	 * <p>
	 * This method checks for an already created {@link ClassReaderFactory} in the internal cache
	 * {@link #classReaderFactoryCache} before creating a new instance.
	 */
	private ClassReaderFactory getClassReaderFactory(IProject project) {
		if (!classReaderFactoryCache.containsKey(project)) {
			classReaderFactoryCache.put(project, new CachingClassReaderFactory(JdtUtils
					.getClassLoader(project, false)));
		}
		return classReaderFactoryCache.get(project);
	}

	/**
	 * Creates a new {@link AnnotationMetadataReadingVisitor} instances.
	 * <p>
	 * Note: subclasses may override this method to provide another implementation.
	 */
	protected AnnotationMetadataReadingVisitor createAnnotationMetadataReadingVisitor() {
		return new AnnotationMetadataReadingVisitor();
	}

	/**
	 * Method to be implemented by sub classes to process found annotation on {@link IBean} classes.
	 * @param bean the current {@link IBean}
	 * @param beanMetaDataSet the {@link Set} of {@link IBeanMetadata} to add the new meta data to
	 * @param type the current {@link IType} that was loaded from the bean's class name
	 * @param progressMonitor the progress monitor to report status
	 * @param visitor the annotation visitor
	 */
	protected abstract void processFoundAnnotations(IBean bean, Set<IBeanMetadata> beanMetaDataSet,
			IType type, IAnnotationMetadata metadata, IProgressMonitor progressMonitor);
}
