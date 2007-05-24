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
package org.springframework.ide.eclipse.aop.core.model.builder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.reflect.PerClauseKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.model.internal.BeanAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.BeanIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.JavaAspectDefinition;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Builder implementation that creates {@link IAspectDefinition} from Spring xml
 * definition files. Understands aop:config tags and
 * @AspectJ-style aspects.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AspectDefinitionBuilder {

	private final Map<String, ClassReader> classReaderCache = new ConcurrentHashMap<String, ClassReader>();

	private static final String ADVICE_REF_ATTRIBUTE = "advice-ref";

	private static final String ADVISOR_ELEMENT = "advisor";

	private static final String AFTER_ELEMENT = "after";

	private static final String AFTER_RETURNING_ELEMENT = "after-returning";

	private static final String AFTER_THROWING_ELEMENT = "after-throwing";

	private static final String AOP_NAMESPACE_URI = "http://www.springframework.org/schema/aop";

	private static final String ARG_NAMES_ATTRIBUTE = "arg-names";

	private static final String AROUND_ELEMENT = "around";

	private static final String ASPECT_ELEMENT = "aspect";

	private static final String ASPECTJ_AUTOPROXY_ELEMENT = "aspectj-autoproxy";

	private static final String BEAN_ELEMENT = "bean";

	private static final String BEANS_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	private static final String BEFORE_ELEMENT = "before";

	private static final String CONFIG_ELEMENT = "config";

	private static final String DECLARE_PARENTS_ELEMENT = "declare-parents";

	private static final String DEFAULT_IMPL_ATTRIBUTE = "default-impl";

	private static final String EXPRESSION_ATTRIBUTE = "expression";

	private static final String ID_ATTRIBUTE = "id";

	private static final String IMPLEMENT_INTERFACE_ATTRIBUTE = "implement-interface";

	private static final String INCLUDE_ELEMENT = "include";

	private static final String METHOD_ATTRIBUTE = "method";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String POINTCUT_ELEMENT = "pointcut";

	private static final String POINTCUT_REF_ATTRIBUTE = "pointcut-ref";

	private static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";

	private static final String RETURNING_ATTRIBUTE = "returning";

	private static final String THROWING_ATTRIBUTE = "throwing";

	private static final String TYPES_MATCHING_ATTRIBUTE = "types-matching";

	private void addAspectDefinition(IAspectDefinition info,
			List<IAspectDefinition> aspectInfos) {
		AopLog.log(AopLog.BUILDER_MESSAGES, info.toString());
		aspectInfos.add(info);
	}

	public List<IAspectDefinition> buildAspectDefinitions(
			final IDOMDocument document, IFile file) {
		final List<IAspectDefinition> aspectInfos = new ArrayList<IAspectDefinition>();

		parseXmlAspects(document, file, aspectInfos);
		parseAnnotationAspects(document, file, aspectInfos);

		return aspectInfos;
	}

	@SuppressWarnings("unchecked")
	private void createAnnotationAspectDefinition(final IDOMDocument document,
			IFile file, final Node bean, final String id,
			final String className, final List<IAspectDefinition> aspectInfos)
			throws Throwable {

		ClassReader classReader = getClassReader(className);
		if (classReader == null) {
			return;
		}

		AdviceAnnotationVisitor v = new AdviceAnnotationVisitor(
				(IDOMNode) bean, id, className);
		classReader.accept(v, false);

		List<IAspectDefinition> aspectDefinitions = v.getAspectDefinitions();
		for (IAspectDefinition def : aspectDefinitions) {
			def.setResource(file);
			def.setDocument(document);
			addAspectDefinition(def, aspectInfos);
		}
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

	private void parseAdvisors(IFile file, Node aspectNode,
			Map<String, String> rootPointcuts,
			List<IAspectDefinition> aspectInfos) {
		String beanRef = getAttribute(aspectNode, ADVICE_REF_ATTRIBUTE);
		String className = BeansEditorUtils.getClassNameForBean(file,
				aspectNode.getOwnerDocument(), beanRef);
		if (StringUtils.hasText(className)) {
			NodeList aspectChildren = aspectNode.getParentNode()
					.getChildNodes();
			Map<String, String> pointcuts = new HashMap<String, String>();
			parsePointcuts(pointcuts, aspectChildren);

			String pointcut = getAttribute(aspectNode, POINTCUT_ELEMENT);
			String pointcutRef = getAttribute(aspectNode,
					POINTCUT_REF_ATTRIBUTE);
			if (!StringUtils.hasText(pointcut)) {
				pointcut = pointcuts.get(pointcutRef);
				if (!StringUtils.hasText(pointcut)) {
					pointcut = rootPointcuts.get(pointcutRef);
				}
			}

			try {
				ClassLoader classLoader = Thread.currentThread()
						.getContextClassLoader();
				Class<?> advisorClass = classLoader.loadClass(className);

				if (classLoader.loadClass(MethodInterceptor.class.getName())
						.isAssignableFrom(advisorClass)) {
					JavaAspectDefinition info = new JavaAspectDefinition();
					info.setNode((IDOMNode) aspectNode);
					info.setPointcutExpression(pointcut);
					info.setAspectClassName(className);
					info.setAspectName(beanRef);
					info.setType(ADVICE_TYPES.AROUND);
					info.setAspectClassName(className);
					info.setAdviceMethodName("invoke");
					info.setAdviceMethodParameterTypes(new String[] { 
							MethodInvocation.class.getName() });
					info.setResource(file);
					info.setDocument((IDOMDocument) aspectNode
							.getOwnerDocument());
					addAspectDefinition(info, aspectInfos);
				}
				if (classLoader.loadClass(MethodBeforeAdvice.class.getName())
						.isAssignableFrom(advisorClass)) {
					JavaAspectDefinition info = new JavaAspectDefinition();
					info.setNode((IDOMNode) aspectNode);
					info.setPointcutExpression(pointcut);
					info.setAspectClassName(className);
					info.setAspectName(beanRef);
					info.setType(ADVICE_TYPES.BEFORE);
					info.setAdviceMethodName(BEFORE_ELEMENT);
					info.setAspectClassName(className);
					info.setAdviceMethodParameterTypes(new String[] {
							Method.class.getName(), Object[].class.getName(),
							Object.class.getName() });
					info.setResource(file);
					info.setDocument((IDOMDocument) aspectNode
							.getOwnerDocument());
					addAspectDefinition(info, aspectInfos);
				}
				if (classLoader.loadClass(ThrowsAdvice.class.getName())
						.isAssignableFrom(advisorClass)) {
					JavaAspectDefinition info = new JavaAspectDefinition();
					info.setNode((IDOMNode) aspectNode);
					info.setPointcutExpression(pointcut);
					info.setAspectClassName(className);
					info.setAspectName(beanRef);
					info.setType(ADVICE_TYPES.AFTER_THROWING);
					info.setAdviceMethodName("afterThrowing");
					info.setAspectClassName(className);
					info.setAdviceMethodParameterTypes(new String[] {
									Method.class.getName(),
									Object[].class.getName(),
									Object.class.getName(),
									Exception.class.getName() });
					info.setResource(file);
					info.setDocument((IDOMDocument) aspectNode
							.getOwnerDocument());
					addAspectDefinition(info, aspectInfos);
				}
				if (classLoader.loadClass(AfterReturningAdvice.class.getName())
						.isAssignableFrom(advisorClass)) {
					JavaAspectDefinition info = new JavaAspectDefinition();
					info.setNode((IDOMNode) aspectNode);
					info.setPointcutExpression(pointcut);
					info.setAspectClassName(className);
					info.setAspectName(beanRef);
					info.setType(ADVICE_TYPES.AFTER_RETURNING);
					info.setAdviceMethodName("afterReturning");
					info.setAspectClassName(className);
					info.setAdviceMethodParameterTypes(new String[] {
							Object.class.getName(), Method.class.getName(),
							Object[].class.getName(), Object.class.getName() });
					info.setResource(file);
					info.setDocument((IDOMDocument) aspectNode
							.getOwnerDocument());
					addAspectDefinition(info, aspectInfos);
				}
			}
			catch (Throwable e) {
				AopLog.log(AopLog.BUILDER_MESSAGES,	Activator.getFormattedMessage(
					"AspectDefinitionBuilder.exceptionOnAdvisorNode", aspectNode));
				Activator.log(e);
			}
		}
	}

	private void parseAnnotationAspects(final IDOMDocument document,
			IFile file, final List<IAspectDefinition> aspectInfos) {
		NodeList list;
		list = document.getDocumentElement().getElementsByTagNameNS(
				AOP_NAMESPACE_URI, ASPECTJ_AUTOPROXY_ELEMENT);
		if (list.getLength() > 0) {

			List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();

			Node item = list.item(0);
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

			list = document.getDocumentElement().getElementsByTagNameNS(
					BEANS_NAMESPACE_URI, BEAN_ELEMENT);

			for (int j = 0; j < list.getLength(); j++) {
				final Node bean = list.item(j);
				final String id = getAttribute(bean, ID_ATTRIBUTE);
				final String className = BeansEditorUtils
						.getClassNameForBean(bean);
				if (className != null && isIncluded(patternList, id)) {
					try {
						if (validateAspect(className)) {
							createAnnotationAspectDefinition(document, file,
									bean, id, className, aspectDefinitions);
						}
					}
					catch (Throwable e) {
						AopLog.log(AopLog.BUILDER_MESSAGES,	Activator.getFormattedMessage(
								"AspectDefinitionBuilder.exceptionOnNode",
								item));
						Activator.log(e);
					}
				}
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

	private BeanAspectDefinition parseAspect(Map<String, String> pointcuts,
			Map<String, String> rootPointcuts, Node aspectNode,
			IAopReference.ADVICE_TYPES type) {
		BeanAspectDefinition info = new BeanAspectDefinition();
		String pointcut = getAttribute(aspectNode, POINTCUT_ELEMENT);
		String pointcutRef = getAttribute(aspectNode, POINTCUT_REF_ATTRIBUTE);
		if (!StringUtils.hasText(pointcut)) {
			pointcut = pointcuts.get(pointcutRef);
			if (!StringUtils.hasText(pointcut)) {
				pointcut = rootPointcuts.get(pointcutRef);
			}
		}
		String argNames = getAttribute(aspectNode, ARG_NAMES_ATTRIBUTE);
		String method = getAttribute(aspectNode, METHOD_ATTRIBUTE);
		String[] argNamesArray = null;
		if (argNames != null) {
			argNamesArray = StringUtils
					.commaDelimitedListToStringArray(argNames);
		}
		info.setArgNames(argNamesArray);
		info.setNode((IDOMNode) aspectNode);
		info.setPointcutExpression(pointcut);
		info.setType(type);
		info.setAdviceMethodName(method);
		return info;
	}

	private void parseAspects(IFile file, Node child,
			Map<String, String> rootPointcuts,
			List<IAspectDefinition> aspectInfos) {
		String beanRef = getAttribute(child, "ref");
		String className = BeansEditorUtils.getClassNameForBean(file, child
				.getOwnerDocument(), beanRef);
		if (StringUtils.hasText(className)) {
			NodeList aspectChildren = child.getChildNodes();
			Map<String, String> pointcuts = new HashMap<String, String>();
			parsePointcuts(pointcuts, aspectChildren);

			for (int g = 0; g < aspectChildren.getLength(); g++) {
				Node aspectNode = aspectChildren.item(g);
				IAspectDefinition info = null;
				if (DECLARE_PARENTS_ELEMENT.equals(aspectNode.getLocalName())) {
					String typesMatching = getAttribute(aspectNode,
							TYPES_MATCHING_ATTRIBUTE);
					String defaultImpl = getAttribute(aspectNode,
							DEFAULT_IMPL_ATTRIBUTE);
					String implementInterface = getAttribute(aspectNode,
							IMPLEMENT_INTERFACE_ATTRIBUTE);
					if (StringUtils.hasText(typesMatching)
							&& StringUtils.hasText(defaultImpl)
							&& StringUtils.hasText(implementInterface)) {
						info = new BeanIntroductionDefinition();
						((BeanIntroductionDefinition) info)
								.setIntroducedInterfaceName(implementInterface);
						((BeanIntroductionDefinition) info)
								.setTypePattern(typesMatching);
						((BeanIntroductionDefinition) info)
								.setDefaultImplName(defaultImpl);
						((BeanIntroductionDefinition) info)
								.setAspectClassName(defaultImpl);
						((BeanIntroductionDefinition) info)
								.setAspectName(beanRef);
						((BeanIntroductionDefinition) info)
								.setNode((IDOMNode) aspectNode);
					}
				}
				else if (BEFORE_ELEMENT.equals(aspectNode.getLocalName())) {
					info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.BEFORE);
				}
				else if (AROUND_ELEMENT.equals(aspectNode.getLocalName())) {
					info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.AROUND);
				}
				else if (AFTER_ELEMENT.equals(aspectNode.getLocalName())) {
					info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.AFTER);
				}
				else if (AFTER_RETURNING_ELEMENT.equals(aspectNode
						.getLocalName())) {
					info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.AFTER_RETURNING);
					String returning = getAttribute(aspectNode,
							RETURNING_ATTRIBUTE);
					info.setReturning(returning);
				}
				else if (AFTER_THROWING_ELEMENT.equals(aspectNode
						.getLocalName())) {
					info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.AFTER_THROWING);
					String throwing = getAttribute(aspectNode,
							THROWING_ATTRIBUTE);
					info.setThrowing(throwing);
				}
				else if (AROUND_ELEMENT.equals(aspectNode.getLocalName())) {
					info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.AROUND);
				}
				if (info != null) {
					if (info.getAspectClassName() == null) {
						info.setAspectClassName(className);
					}
					info.setAspectName(beanRef);
					info.setResource(file);
					info.setDocument((IDOMDocument) child.getOwnerDocument());
					addAspectDefinition(info, aspectInfos);
				}
			}
		}
	}

	private void parsePointcuts(Map<String, String> rootPointcuts,
			NodeList children) {
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (POINTCUT_ELEMENT.equals(child.getLocalName())) {
				String id = getAttribute(child, ID_ATTRIBUTE);
				String expression = getAttribute(child, EXPRESSION_ATTRIBUTE);
				if (StringUtils.hasText(id) && StringUtils.hasText(expression)) {
					rootPointcuts.put(id, expression);
				}
			}
		}
	}

	private void parseXmlAspects(final IDOMDocument document, IFile file,
			final List<IAspectDefinition> aspectInfos) {
		NodeList list = document.getDocumentElement().getElementsByTagNameNS(
				AOP_NAMESPACE_URI, CONFIG_ELEMENT);

		for (int i = 0; i < list.getLength(); i++) {
			List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();
			Map<String, String> rootPointcuts = new HashMap<String, String>();
			Node node = list.item(i);
			NodeList children = node.getChildNodes();

			parsePointcuts(rootPointcuts, children);

			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if (ASPECT_ELEMENT.equals(child.getLocalName())) {
					parseAspects(file, child, rootPointcuts, aspectDefinitions);
				}
				else if (ADVISOR_ELEMENT.equals(child.getLocalName())) {
					parseAdvisors(file, child, rootPointcuts, aspectDefinitions);
				}
			}

			if (node.getAttributes().getNamedItem(PROXY_TARGET_CLASS_ATTRIBUTE) != null) {
				boolean proxyTargetClass = Boolean.valueOf(node.getAttributes()
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

	private boolean hasAttribute(Node node, String attributeName) {
		return (node != null && node.hasAttributes() && node.getAttributes()
				.getNamedItem(attributeName) != null);
	}

	private String getAttribute(Node node, String attributeName) {
		if (hasAttribute(node, attributeName)) {
			String value = node.getAttributes().getNamedItem(attributeName)
					.getNodeValue();
			value = StringUtils.replace(value, "\n", " ");
			value = StringUtils.replace(value, "\t", " ");
			return StringUtils.replace(value, "\r", " ");
		}
		return null;
	}

	private static final String AJC_MAGIC = "ajc$";

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
}
