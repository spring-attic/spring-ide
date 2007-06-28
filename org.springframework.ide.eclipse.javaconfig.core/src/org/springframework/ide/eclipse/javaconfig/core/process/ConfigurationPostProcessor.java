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
package org.springframework.ide.eclipse.javaconfig.core.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessingContext;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessor;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.javaconfig.core.model.BeanAnnotationMetaData;
import org.springframework.ide.eclipse.javaconfig.core.model.ConfigurationASTVistor;
import org.springframework.ide.eclipse.javaconfig.core.model.ConfigurationClassVisitor;
import org.springframework.ide.eclipse.javaconfig.core.model.JdtModelSourceLocationFactory;

/**
 * A {@link IBeansConfigPostProcessor} implementation that process a
 * {@link IBeansConfig} for Spring JavaConfig configuration classes. Those
 * configuration classes carry the {@link Configuration} annotation.
 * <p>
 * This implementation relies on a ASM {@link ClassVisitor} to parse the
 * configuration information expressed by JavaConfig's {@link Bean} annotation
 * out of the configuration class.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ConfigurationPostProcessor implements IBeansConfigPostProcessor {

	/**
	 * Entry point for post processing Spring JavaConfig configuration
	 * @see IBeansConfigPostProcessor#postProcess(IBeansConfigPostProcessingContext)
	 */
	public void postProcess(
			IBeansConfigPostProcessingContext postProcessingContext) {

		Collection<IBean> beans = postProcessingContext
				.getBeansConfigRegistrySupport().getBeans();

		if (beans.iterator().hasNext()) {
			ClassLoader classLoader = JdtUtils.getClassLoader(beans.iterator()
					.next().getElementResource());
			postProcessBeans(postProcessingContext, beans, classLoader);
		}
	}

	/**
	 * Post process a {@link IBeansConfig} using the given classLoader
	 */
	private void postProcessBeans(
			IBeansConfigPostProcessingContext postProcessingContext,
			Collection<IBean> beans, ClassLoader classLoader) {
		for (IBean bean : beans) {
			postProcessBean(postProcessingContext, bean, classLoader);
		}
	}

	/**
	 * Post process a {@link IBean} using the given classLoader
	 * @param postProcessingContext the post processing context
	 * @param bean the {@link IBean} to post process
	 * @param beansConfig the {@link IBeansConfig} that should be post processed
	 * @param classLoader the {@link ClassLoader} to use
	 */
	private void postProcessBean(
			IBeansConfigPostProcessingContext postProcessingContext,
			IBean bean, ClassLoader classLoader) {

		String beanClassName = BeansModelUtils.getBeanClass(bean, null);
		IType beanType = JdtUtils.getJavaType(bean.getElementResource()
				.getProject(), beanClassName);
		if (beanClassName != null) {
			try {

				List<BeanAnnotationMetaData> beanAnnotationMetaData = new ArrayList<BeanAnnotationMetaData>();

				List<BeanAnnotationMetaData> externalBeanAnnotationMetaData = new ArrayList<BeanAnnotationMetaData>();

				String superClassName = beanClassName;
				do {
					ClassReader reader = new ClassReader(classLoader
							.getResourceAsStream(ClassUtils
									.getClassFileName(superClassName)));
					ConfigurationClassVisitor v = new ConfigurationClassVisitor();
					reader.accept(v, false);
					superClassName = v.getSuperClassName();
					beanAnnotationMetaData
							.addAll(v.getBeanAnnotationMetaData());
					externalBeanAnnotationMetaData.addAll(v
							.getExternalBeanAnnotationMetaData());
				} while (superClassName != null);

				if (beanAnnotationMetaData != null
						&& beanAnnotationMetaData.size() > 0) {
					List<BeanComponentDefinition> beanComponentDefinitions = new ArrayList<BeanComponentDefinition>();
					for (BeanAnnotationMetaData beanCreationMethod : beanAnnotationMetaData) {
						beanComponentDefinitions
								.add(processSingleBeanCreationMethod(
										postProcessingContext, bean,
										beanCreationMethod));
					}

					createBeanPropertyValues(beanType,
							beanComponentDefinitions,
							externalBeanAnnotationMetaData);
				}
			}
			catch (IOException e) {
				// we can't find the class. that is ok here.
			}
		}
	}

	private void createBeanPropertyValues(IType beanType,
			List<BeanComponentDefinition> beanComponentDefinitions,
			List<BeanAnnotationMetaData> externalBeanAnnotationMetaData) {
		try {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(beanType.getCompilationUnit());
			parser.setResolveBindings(true);
			ASTNode node = parser.createAST(new NullProgressMonitor());
			node.accept(new ConfigurationASTVistor(beanComponentDefinitions,
					externalBeanAnnotationMetaData));
		}
		catch (Exception e) {
			// we don't care about any exception here
		}
	}

	/**
	 * Creates a {@link RootBeanDefinition} for the given
	 * {@link BeanAnnotationMetaData}.
	 * @param postProcessingContext the post processing context
	 * @param bean the {@link IBean} to post process
	 * @param beanCreationMethod the {@link BeanAnnotationMetaData}
	 */
	private BeanComponentDefinition processSingleBeanCreationMethod(
			IBeansConfigPostProcessingContext postProcessingContext,
			IBean bean, BeanAnnotationMetaData beanCreationMethod) {

		RootBeanDefinition bd = new RootBeanDefinition();
		bd.setFactoryBeanName(bean.getElementName());
		bd.setFactoryMethodName(beanCreationMethod.getName());
		bd.setBeanClassName(beanCreationMethod.getReturnTypeName());
		bd.setDestroyMethodName(beanCreationMethod.getDestoryMethodName());
		bd.setInitMethodName(beanCreationMethod.getInitMethodName());
		bd.setDependsOn(beanCreationMethod.getDependsOn()
				.toArray(new String[0]));
		if (beanCreationMethod.getScope() != null) {
			bd.setScope(beanCreationMethod.getScope());
		}

		bd.setSource(JdtModelSourceLocationFactory.getModelSourceLocation(bean,
				beanCreationMethod));

		BeanComponentDefinition beanComponentDefinition = null;
		// create public or internal bean
		if (beanCreationMethod.isPublic()) {
			beanComponentDefinition = new BeanComponentDefinition(bd,
					beanCreationMethod.getName());
		}
		else {
			beanComponentDefinition = new BeanComponentDefinition(bd,
					postProcessingContext.getBeanNameGenerator()
							.generateBeanName(bd, null));
		}

		postProcessingContext.getBeansConfigRegistrySupport()
				.registerComponent(beanComponentDefinition);
		return beanComponentDefinition;
	}
}
