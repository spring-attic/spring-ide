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
import java.util.List;
import java.util.Set;

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
import org.springframework.ide.eclipse.javaconfig.core.model.BeanCreationMethod;
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

		IBeansConfig beansConfig = postProcessingContext
				.getBeansConfigRegistrySupport().getBeansConfig();

		ClassLoader classLoader = JdtUtils.getClassLoader(beansConfig
				.getElementResource());

		postProcessBeansConfig(postProcessingContext, beansConfig, classLoader);
	}

	/**
	 * Post process a {@link IBeansConfig} using the given classLoader
	 * @param postProcessingContext the post processing context
	 * @param beansConfig the {@link IBeansConfig} that should be post processed
	 * @param classLoader the {@link ClassLoader} to use
	 */
	private void postProcessBeansConfig(
			IBeansConfigPostProcessingContext postProcessingContext,
			IBeansConfig beansConfig, ClassLoader classLoader) {
		Set<IBean> beans = beansConfig.getBeans();
		for (IBean bean : beans) {
			postProcessBean(postProcessingContext, bean, beansConfig,
					classLoader);
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
			IBean bean, IBeansConfig beansConfig, ClassLoader classLoader) {

		String beanClassName = BeansModelUtils.getBeanClass(bean, beansConfig);
		if (beanClassName != null) {
			try {

				List<BeanCreationMethod> beanCreationMethods = new ArrayList<BeanCreationMethod>();

				String superClassName = beanClassName;
				do {
					ClassReader reader = new ClassReader(classLoader
							.getResourceAsStream(ClassUtils
									.getClassFileName(superClassName)));
					ConfigurationClassVisitor v = new ConfigurationClassVisitor();
					reader.accept(v, false);
					superClassName = v.getSuperClassName();
					beanCreationMethods.addAll(v.getBeanCreationMethods());
				} while (superClassName != null);

				if (beanCreationMethods != null) {
					for (BeanCreationMethod beanCreationMethod : beanCreationMethods) {
						processSingleBeanCreationMethod(postProcessingContext,
								bean, beanCreationMethod);
					}
				}
			}
			catch (IOException e) {
				// we can't find the class. that is ok here.
			}
		}
	}

	/**
	 * Creates a {@link RootBeanDefinition} for the given
	 * {@link BeanCreationMethod}.
	 * @param postProcessingContext the post processing context
	 * @param bean the {@link IBean} to post process
	 * @param beanCreationMethod the {@link BeanCreationMethod}
	 */
	private void processSingleBeanCreationMethod(
			IBeansConfigPostProcessingContext postProcessingContext,
			IBean bean, BeanCreationMethod beanCreationMethod) {

		RootBeanDefinition bd = new RootBeanDefinition();
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

		// create public or internal bean
		if (beanCreationMethod.isPublic()) {
			postProcessingContext.getBeansConfigRegistrySupport()
					.registerComponent(
							new BeanComponentDefinition(bd, beanCreationMethod
									.getName()));
		}
		else {
			postProcessingContext.getBeansConfigRegistrySupport()
					.registerComponent(
							new BeanComponentDefinition(bd,
									postProcessingContext
											.getBeanNameGenerator()
											.generateBeanName(bd, null)));
		}
	}
}
