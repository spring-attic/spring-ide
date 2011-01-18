/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
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
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.internal.model.BeanAspectDefinition;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.aop.core.model.builder.IDocumentFactory;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IAspectDefinitionBuilder} implementation that creates {@link IAspectDefinition} from
 * @author Christian Dupuis
 * @AspectJ-style aspects.
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AnnotationAspectDefinitionBuilder extends AbstractAspectDefinitionBuilder implements
		IAspectDefinitionBuilder {

	private static final String AJC_MAGIC = "ajc$";

	private static final String AOP_NAMESPACE_URI = "http://www.springframework.org/schema/aop";

	private static final String ASPECTJ_AUTOPROXY_ELEMENT = "aspectj-autoproxy";

	private static final String INCLUDE_ELEMENT = "include";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";

	private ClassReaderFactory classReaderFactory = null;

	private IDocumentFactory documentFactory = null;

	public void buildAspectDefinitions(List<IAspectDefinition> aspectInfos, IFile file,
			IProjectClassLoaderSupport classLoaderSupport, IDocumentFactory factory) {
		if (BeansCoreUtils.isBeansConfig(file, true)) {
			this.documentFactory = factory;
			IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig(file, true);
			parseAnnotationAspects(factory.createDocument(file), beansConfig, aspectInfos, classLoaderSupport);
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
		classReader.accept(v, false);

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

	private void parseAnnotationAspects(IDOMDocument document, IBeansConfig beansConfig,
			List<IAspectDefinition> aspectInfos, IProjectClassLoaderSupport classLoaderSupport) {
		if (document == null || document.getStructuredDocument() == null) {
			return;
		}
		
		AspectJAutoProxyConfiguration configuration = getAspectJAutoProxyConfiguration(beansConfig, document);

		// not configured for auto proxing
		if (!configuration.isAutoProxy()) {
			return;
		}

		List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();

		for (IBean bean : beansConfig.getBeans()) {
			parseAnnotationAspectFromSingleBean(beansConfig, classLoaderSupport, aspectDefinitions, configuration, bean);
		}

		if (configuration.isProxyTargetClass()) {
			for (IAspectDefinition def : aspectDefinitions) {
				((BeanAspectDefinition) def).setProxyTargetClass(configuration.isProxyTargetClass());
			}
		}

		aspectInfos.addAll(aspectDefinitions);
	}

	private AspectJAutoProxyConfiguration getAspectJAutoProxyConfiguration(IBeansConfig beansConfig,
			IDOMDocument document) {

		AspectJAutoProxyConfiguration configuration = new AspectJAutoProxyConfiguration();

		// Firstly check the current document for precedence of an <aop:aspectj-autoroxy> element
		getAspectJConfigurationForDocument(document, configuration);

		// Secondly check any config sets that the given beans config is a member in
		for (IBeansConfigSet configSet : BeansModelUtils.getConfigSets(beansConfig)) {
			for (IBeansConfig config : configSet.getConfigs()) {
				if (!config.equals(beansConfig)) {
					document = documentFactory.createDocument((IFile) config.getElementResource());
					if (document != null && document.getDocumentElement() != null) {
						getAspectJConfigurationForDocument(document, configuration);
					}
				}
			}
		}

		return configuration;
	}

	private void getAspectJConfigurationForDocument(IDOMDocument document, AspectJAutoProxyConfiguration configuration) {
		NodeList list = getAspectJAutoProxyNodes(document);

		if (list.getLength() > 0) {
			configuration.setAutoProxy(true);
		}

		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			configuration.addIncludePattern(node);
			if (node.getAttributes() != null && node.getAttributes().getNamedItem(PROXY_TARGET_CLASS_ATTRIBUTE) != null) {
				boolean proxyTargetClass = Boolean.valueOf(
						node.getAttributes().getNamedItem(PROXY_TARGET_CLASS_ATTRIBUTE).getNodeValue()).booleanValue();
				if (proxyTargetClass) {
					configuration.setProxyTargetClass(proxyTargetClass);
				}
			}
		}
	}

	private NodeList getAspectJAutoProxyNodes(IDOMDocument document) {
		return document.getDocumentElement().getElementsByTagNameNS(AOP_NAMESPACE_URI, ASPECTJ_AUTOPROXY_ELEMENT);
	}

	private boolean validateAspect(String className) throws Throwable {

		ClassReader classReader = getClassReader(className);
		if (classReader == null) {
			return false;
		}
		AspectAnnotationVisitor v = new AspectAnnotationVisitor();
		classReader.accept(v, false);

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
				classReader.accept(sv, false);

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

		public void addIncludePattern(Node autoproxyNode) {
			NodeList include = autoproxyNode.getChildNodes();
			for (int j = 0; j < include.getLength(); j++) {
				if (INCLUDE_ELEMENT.equals(include.item(j).getLocalName())) {
					if (includePatterns == null) {
						includePatterns = new ArrayList<Pattern>();
					}
					String pattern = getAttribute(include.item(j), NAME_ATTRIBUTE);
					if (StringUtils.hasText(pattern)) {
						includePatterns.add(Pattern.compile(pattern));
					}
				}
			}
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
