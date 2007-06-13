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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.internal.model.BeanAspectDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.BeanIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.JavaAdvisorDefinition;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.aop.core.model.builder.IWeavingClassLoaderSupport;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Builder implementation that creates {@link IAspectDefinition} from Spring xml
 * definition files. Understands aop:config tags.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class XmlAspectDefinitionBuilder extends AbstractAspectDefinitionBuilder
		implements IAspectDefinitionBuilder {

	private void addAspectDefinition(IAspectDefinition info,
			List<IAspectDefinition> aspectInfos) {
		AopLog.log(AopLog.BUILDER_MESSAGES, info.toString());
		aspectInfos.add(info);
	}

	public void doBuildAspectDefinitions(IDOMDocument document, IFile file,
			List<IAspectDefinition> aspectInfos, IWeavingClassLoaderSupport classLoaderSupport) {
		parseXmlAspects(document, file, aspectInfos);
	}

	private int getLineNumber(IDOMDocument document, IDOMNode node) {
		return document.getStructuredDocument().getLineOfOffset(
				node.getStartOffset()) + 1;
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
					JavaAdvisorDefinition info = new JavaAdvisorDefinition();
					info.setAspectLineNumber(getLineNumber(
							(IDOMDocument) aspectNode.getOwnerDocument(),
							(IDOMNode) aspectNode));
					info.setPointcutExpression(pointcut);
					info.setAspectClassName(className);
					info.setAspectName(beanRef);
					info.setType(ADVICE_TYPES.AROUND);
					info.setAspectClassName(className);
					info.setAdviceMethodName("invoke");
					info
							.setAdviceMethodParameterTypes(new String[] { MethodInvocation.class
									.getName() });
					info.setResource(file);
					addAspectDefinition(info, aspectInfos);
				}
				if (classLoader.loadClass(MethodBeforeAdvice.class.getName())
						.isAssignableFrom(advisorClass)) {
					JavaAdvisorDefinition info = new JavaAdvisorDefinition();
					info.setAspectLineNumber(getLineNumber(
							(IDOMDocument) aspectNode.getOwnerDocument(),
							(IDOMNode) aspectNode));
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
					addAspectDefinition(info, aspectInfos);
				}
				if (classLoader.loadClass(ThrowsAdvice.class.getName())
						.isAssignableFrom(advisorClass)) {
					JavaAdvisorDefinition info = new JavaAdvisorDefinition();
					info.setAspectLineNumber(getLineNumber(
							(IDOMDocument) aspectNode.getOwnerDocument(),
							(IDOMNode) aspectNode));
					info.setPointcutExpression(pointcut);
					info.setAspectClassName(className);
					info.setAspectName(beanRef);
					info.setType(ADVICE_TYPES.AFTER_THROWING);
					info.setAdviceMethodName("afterThrowing");
					info.setAspectClassName(className);
					info
							.setAdviceMethodParameterTypes(new String[] {
									Method.class.getName(),
									Object[].class.getName(),
									Object.class.getName(),
									Exception.class.getName() });
					info.setResource(file);
					addAspectDefinition(info, aspectInfos);
				}
				if (classLoader.loadClass(AfterReturningAdvice.class.getName())
						.isAssignableFrom(advisorClass)) {
					JavaAdvisorDefinition info = new JavaAdvisorDefinition();
					info.setAspectLineNumber(getLineNumber(
							(IDOMDocument) aspectNode.getOwnerDocument(),
							(IDOMNode) aspectNode));
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
					addAspectDefinition(info, aspectInfos);
				}
			}
			catch (Throwable e) {
				AopLog
						.log(
								AopLog.BUILDER_MESSAGES,
								Activator
										.getFormattedMessage(
												"AspectDefinitionBuilder.exceptionOnAdvisorNode",
												aspectNode));
				Activator.log(e);
			}
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
		info.setAspectLineNumber(getLineNumber((IDOMDocument) aspectNode
				.getOwnerDocument(), (IDOMNode) aspectNode));
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
								.setAspectLineNumber(getLineNumber(
										(IDOMDocument) aspectNode
												.getOwnerDocument(),
										(IDOMNode) aspectNode));

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
}
