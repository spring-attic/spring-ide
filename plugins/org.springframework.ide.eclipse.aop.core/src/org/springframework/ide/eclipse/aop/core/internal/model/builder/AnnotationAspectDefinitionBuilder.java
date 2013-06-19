/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.aspectj.lang.reflect.PerClauseKind;
import org.eclipse.core.resources.IFile;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.asm.ClassReader;
import org.springframework.asm.Opcodes;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.internal.model.BeanAspectDefinition;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.aop.core.model.builder.IDocumentFactory;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansList;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansTypedString;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;
import org.springframework.util.StringUtils;

/**
 * {@link IAspectDefinitionBuilder} implementation that creates {@link IAspectDefinition} from  @AspectJ-style aspects.
 * 
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0
 */
public class AnnotationAspectDefinitionBuilder extends AbstractAspectDefinitionBuilder implements
		IAspectDefinitionBuilder {

	private static final String AJC_MAGIC = "ajc$";

	private static final String PROXY_TARGET_CLASS = "proxyTargetClass";

	private static final String INCLUDE_PATTERNS = "includePatterns";

	private ClassReaderFactory classReaderFactory = null;

	public void buildAspectDefinitions(List<IAspectDefinition> aspectInfos, IFile file,
			IProjectClassLoaderSupport classLoaderSupport, IDocumentFactory factory) {
		if (BeansCoreUtils.isBeansConfig(file, true)) {
			IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(file), true);
			parseAnnotationAspects(beansConfig, aspectInfos, classLoaderSupport);
		}
	}

	private void addAspectDefinition(IAspectDefinition info, List<IAspectDefinition> aspectInfos) {
		AopLog.log(AopLog.BUILDER_MESSAGES, info.toString());
		aspectInfos.add(info);
	}

	private void createAnnotationAspectDefinition(IBean bean, final String id, final String className,
			final List<IAspectDefinition> aspectInfos) throws Throwable {

		ClassReader classReader = getClassReader(className);
		if (classReader == null) {
			return;
		}

		AdviceAnnotationVisitor v = new AdviceAnnotationVisitor(id, className, bean.getElementStartLine(), bean
				.getElementEndLine());
		classReader.accept(v, 0);

		List<IAspectDefinition> aspectDefinitions = v.getAspectDefinitions();
		for (IAspectDefinition def : aspectDefinitions) {
			def.setResource(bean.getElementResource());
			addAspectDefinition(def, aspectInfos);
		}
	}

	private ClassReader getClassReader(String className) {
		// lazy initialize classReaderFactory to make sure it uses the correct classLoader
		if (classReaderFactory == null) {
			classReaderFactory = new CachingClassReaderFactory();
		}
		try {
			return classReaderFactory.getClassReader(className);
		}
		catch (IOException e) {
		}
		return null;
	}

	private void parseAnnotationAspectFromSingleBean(IBeansConfig beansConfig,
			IProjectClassLoaderSupport classLoaderSupport, final List<IAspectDefinition> aspectDefinitions,
			AspectJAutoProxyConfiguration configuration, final IBean bean) {
		final String id = bean.getElementName();
		final String className = BeansModelUtils.getBeanClass(bean, beansConfig);
		if (className != null && configuration.isIncluded(id)) {

			try {
				classLoaderSupport.executeCallback(new IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback() {

					public void doWithActiveProjectClassLoader() throws Throwable {
						if (validateAspect(className)) {
							createAnnotationAspectDefinition(bean, id, className, aspectDefinitions);
						}
					}
				});
			}
			catch (Throwable e) {
				AopLog.log(AopLog.BUILDER_MESSAGES, Activator.getFormattedMessage(
						"AspectDefinitionBuilder.exceptionOnNode", bean));
				Activator.log(e);
			}
		}
	}

	private void parseAnnotationAspects(IBeansConfig beansConfig,
			List<IAspectDefinition> aspectInfos, IProjectClassLoaderSupport classLoaderSupport) {
		AspectJAutoProxyConfiguration configuration = getAspectJAutoProxyConfiguration(beansConfig);

		// not configured for auto proxing
		if (!configuration.isAutoProxy()) {
			return;
		}

		List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();
		
		// make sure to iterate into all the beans nested inside components etc.
		for (IBean bean : BeansModelUtils.getBeans(beansConfig)) {
			parseAnnotationAspectFromSingleBean(beansConfig, classLoaderSupport, aspectDefinitions, configuration, bean);
		}
		
		if (configuration.isProxyTargetClass()) {
			for (IAspectDefinition def : aspectDefinitions) {
				((BeanAspectDefinition) def).setProxyTargetClass(configuration.isProxyTargetClass());
			}
		}

		aspectInfos.addAll(aspectDefinitions);
	}

	private AspectJAutoProxyConfiguration getAspectJAutoProxyConfiguration(IBeansConfig beansConfig) {
		AspectJAutoProxyConfiguration configuration = new AspectJAutoProxyConfiguration();
		
		// Firstly check the current document for precedence of an <aop:aspectj-autoroxy> element
		getAspectJConfigurationForBeansConfig(beansConfig, configuration);

		// Secondly check any config sets that the given beans config is a member in
		for (IBeansConfigSet configSet : BeansModelUtils.getConfigSets(beansConfig)) {
			for (IBeansConfig config : configSet.getConfigs()) {
				if (!config.equals(beansConfig)) {
					getAspectJConfigurationForBeansConfig(beansConfig, configuration);
				}
			}
		}

		return configuration;
	}

	private void getAspectJConfigurationForBeansConfig(IBeansConfig beansConfig, AspectJAutoProxyConfiguration configuration) {
		IBean autoproxyBean = BeansModelUtils.getBean(AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME, beansConfig);
		
		if (autoproxyBean != null) {
			configuration.setAutoProxy(true);
			
			IBeanProperty targetProxyClassProperty = autoproxyBean.getProperty(PROXY_TARGET_CLASS);
			if (targetProxyClassProperty != null && targetProxyClassProperty.getValue() != null) {
				String value = ((BeansTypedString)targetProxyClassProperty.getValue()).getString();
				boolean proxyTargetClass = Boolean.valueOf(value).booleanValue();
				if (proxyTargetClass) {
					configuration.setProxyTargetClass(proxyTargetClass);
				}				
			}
			
			IBeanProperty includes = autoproxyBean.getProperty(INCLUDE_PATTERNS);
			if (includes != null && includes.getValue() != null) {
				List<Pattern> patterns = new ArrayList<Pattern>();
				BeansList includesList = (BeansList) includes.getValue();
				List<Object> includePatterns = includesList.getList();
				for (Object includePattern : includePatterns) {
					String pattern = ((BeansTypedString)includePattern).getString();
					if (StringUtils.hasText(pattern)) {
						patterns.add(Pattern.compile(pattern));
					}
				}
				configuration.setIncluldePatterns(patterns);
			}
		}
	}

	private boolean validateAspect(String className) throws Throwable {

		ClassReader classReader = getClassReader(className);
		if (classReader == null) {
			return false;
		}
		AspectAnnotationVisitor v = new AspectAnnotationVisitor();
		classReader.accept(v, 0);

		if (!v.getClassInfo().hasAspectAnnotation()) {
			return false;
		}
		else {
			// we know it's an aspect, but we don't know whether it is an
			// @AspectJ aspect or a code style aspect.
			// This is an *unclean* test whilst waiting for AspectJ to
			// provide us with something better
			for (String m : v.getClassInfo().getMethodNames()) {
				if (m.startsWith(AJC_MAGIC)) {
					// must be a code style aspect
					return false;
				}
			}
			// validate supported instantiation models
			if (v.getClassInfo().getAspectAnnotation().getValue() != null) {
				if (v.getClassInfo().getAspectAnnotation().getValue().toUpperCase().equals(
						PerClauseKind.PERCFLOW.toString())) {
					return false;
				}
				if (v.getClassInfo().getAspectAnnotation().getValue().toUpperCase().toString().equals(
						PerClauseKind.PERCFLOWBELOW.toString())) {
					return false;
				}
			}

			// check if super class is Aspect as well and abstract
			if (v.getClassInfo().getSuperType() != null) {
				classReader = getClassReader(v.getClassInfo().getSuperType());
				if (classReader == null) {
					return false;
				}

				AspectAnnotationVisitor sv = new AspectAnnotationVisitor();
				classReader.accept(sv, 0);

				if (sv.getClassInfo().getAspectAnnotation() != null
						&& !((sv.getClassInfo().getModifier() & Opcodes.ACC_ABSTRACT) != 0)) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Merged representation of all <aop:aspectj-autoproxy /> elements in all {@link IBeansConfigSet}s
	 */
	class AspectJAutoProxyConfiguration {

		private List<Pattern> includePatterns;

		private boolean proxyTargetClass;

		private boolean isAutoProxy = false;

		public void setProxyTargetClass(boolean proxyTargetClass) {
			this.proxyTargetClass = proxyTargetClass;
		}

		public boolean isProxyTargetClass() {
			return proxyTargetClass;
		}

		public void setAutoProxy(boolean autoProxy) {
			this.isAutoProxy = autoProxy;
		}

		public boolean isAutoProxy() {
			return isAutoProxy;
		}

		public void setIncluldePatterns(List<Pattern> includePatterns) {
			this.includePatterns = includePatterns;
		}

		/**
		 * If no &lt;aop:include&gt; elements were used then includePatterns will be null and all beans are included. If
		 * includePatterns is non-null, then one of the patterns must match.
		 */
		public boolean isIncluded(String beanName) {
			if (includePatterns == null) {
				return true;
			}
			else if (includePatterns != null && includePatterns.size() == 0) {
				return false;
			}
			else {
				for (Pattern pattern : includePatterns) {
					if (beanName == null) {
						return false;
					}
					if (pattern.matcher(beanName).matches()) {
						return true;
					}
				}
				return false;
			}
		}
	}

}
