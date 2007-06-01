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
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.aspectj.lang.reflect.PerClauseKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.aop.core.model.builder.IWeavingClassLoaderSupport;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IAspectDefinitionBuilder} implementation that creates
 * {@link IAspectDefinition} from @AspectJ-style aspects.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AnnotationAspectDefinitionBuilder extends
		AbstractAspectDefinitionBuilder implements IAspectDefinitionBuilder {

	private static final String AJC_MAGIC = "ajc$";

	private final Map<String, ClassReader> classReaderCache = new ConcurrentHashMap<String, ClassReader>();

	private void addAspectDefinition(IAspectDefinition info,
			List<IAspectDefinition> aspectInfos) {
		AopLog.log(AopLog.BUILDER_MESSAGES, info.toString());
		aspectInfos.add(info);
	}

	@SuppressWarnings("unchecked")
	private void createAnnotationAspectDefinition(IBean bean, final String id,
			final String className, final List<IAspectDefinition> aspectInfos)
			throws Throwable {

		ClassReader classReader = getClassReader(className);
		if (classReader == null) {
			return;
		}

		AdviceAnnotationVisitor v = new AdviceAnnotationVisitor(id, className,
				bean.getElementStartLine());
		classReader.accept(v, false);

		List<IAspectDefinition> aspectDefinitions = v.getAspectDefinitions();
		for (IAspectDefinition def : aspectDefinitions) {
			def.setResource(bean.getElementResource());
			addAspectDefinition(def, aspectInfos);
		}
	}

	public void doBuildAspectDefinitions(IDOMDocument document, IFile file,
			List<IAspectDefinition> aspectInfos,
			IWeavingClassLoaderSupport classLoaderSupport) {
		if (BeansCoreUtils.isBeansConfig(file)) {
			IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig(
					file);
			parseAnnotationAspects(document, beansConfig, aspectInfos,
					classLoaderSupport);
		}
	}

	private ClassReader getClassReader(String className) {
		// check in cache first
		if (this.classReaderCache.containsKey(className)) {
			return this.classReaderCache.get(className);
		}

		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		InputStream inputStream = null;

		try {
			inputStream = classLoader.getResourceAsStream(ClassUtils
					.getClassFileName(className));

			// check if class exists on class path
			if (inputStream == null) {
				return null;
			}

			ClassReader reader = new ClassReader(inputStream);
			this.classReaderCache.put(className, reader);
			return reader;
		}
		catch (IOException e) {
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException e) {
				}
			}
		}
		return null;
	}

	/**
	 * If no &lt;aop:include&gt; elements were used then includePatterns will be
	 * null and all beans are included. If includePatterns is non-null, then one
	 * of the patterns must match.
	 */
	private boolean isIncluded(List<Pattern> includePatterns, String beanName) {
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

	private void parseAnnotationAspects(IDOMDocument document,
			IBeansConfig beansConfig, List<IAspectDefinition> aspectInfos,
			IWeavingClassLoaderSupport classLoaderSupport) {
		NodeList list = document.getDocumentElement().getElementsByTagNameNS(
				AOP_NAMESPACE_URI, ASPECTJ_AUTOPROXY_ELEMENT);
		if (list.getLength() > 0) {

			final List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();

			final Node item = list.item(0);
			List<Pattern> patternList = null;
			NodeList include = item.getChildNodes();
			for (int j = 0; j < include.getLength(); j++) {
				if (INCLUDE_ELEMENT.equals(include.item(j).getLocalName())) {
					patternList = new ArrayList<Pattern>();
					String pattern = getAttribute(include.item(j),
							NAME_ATTRIBUTE);
					if (StringUtils.hasText(pattern)) {
						patternList.add(Pattern.compile(pattern));
					}
				}
			}

			Set<IBean> beans = new HashSet<IBean>();
			beans.addAll(beansConfig.getBeans());

			Set<IBeansComponent> components = beansConfig.getComponents();
			for (IBeansComponent component : components) {
				beans.addAll(component.getBeans());
			}

			for (final IBean bean : beans) {
				parseAnnotationAspectFromSingleBean(beansConfig,
						classLoaderSupport, aspectDefinitions, patternList,
						bean);
			}

			if (item.getAttributes().getNamedItem(PROXY_TARGET_CLASS_ATTRIBUTE) != null) {
				boolean proxyTargetClass = Boolean.valueOf(item.getAttributes()
						.getNamedItem(PROXY_TARGET_CLASS_ATTRIBUTE)
						.getNodeValue());
				if (proxyTargetClass) {
					for (IAspectDefinition def : aspectDefinitions) {
						def.setProxyTargetClass(proxyTargetClass);
					}
				}
			}

			aspectInfos.addAll(aspectDefinitions);
		}
	}

	private void parseAnnotationAspectFromSingleBean(IBeansConfig beansConfig,
			IWeavingClassLoaderSupport classLoaderSupport,
			final List<IAspectDefinition> aspectDefinitions,
			List<Pattern> patternList, final IBean bean) {
		final String id = bean.getElementName();
		final String className = BeansModelUtils
				.getBeanClass(bean, beansConfig);
		if (className != null && isIncluded(patternList, id)) {

			try {
				classLoaderSupport
						.executeCallback(
								new IWeavingClassLoaderSupport.IWeavingClassLoaderAwareCallback() {

							public void doInActiveWeavingClassLoader()
									throws Throwable {
								if (validateAspect(className)) {
									createAnnotationAspectDefinition(bean, id,
											className, aspectDefinitions);
								}
							}
						});
			}
			catch (Throwable e) {
				AopLog.log(AopLog.BUILDER_MESSAGES,
						Activator.getFormattedMessage(
						"AspectDefinitionBuilder.exceptionOnNode",
						bean));
				Activator.log(e);
			}
		}
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
				if (v.getClassInfo().getAspectAnnotation().getValue()
						.toUpperCase()
						.equals(PerClauseKind.PERCFLOW.toString())) {
					return false;
				}
				if (v.getClassInfo().getAspectAnnotation().getValue()
						.toUpperCase().toString().equals(
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
}
