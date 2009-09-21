/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
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
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Builder implementation that creates {@link IAspectDefinition} from Spring xml definition files.
 * Understands aop:config tags.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class XmlAspectDefinitionBuilder extends AbstractAspectDefinitionBuilder implements
		IAspectDefinitionBuilder {

	private static final String ADVICE_REF_ATTRIBUTE = "advice-ref";

	private static final String ADVISOR_ELEMENT = "advisor";

	private static final String AFTER_ELEMENT = "after";

	private static final String AFTER_RETURNING_ELEMENT = "after-returning";

	private static final String AFTER_THROWING_ELEMENT = "after-throwing";

	private static final String AOP_NAMESPACE_URI = "http://www.springframework.org/schema/aop";

	private static final String ARG_NAMES_ATTRIBUTE = "arg-names";

	private static final String AROUND_ELEMENT = "around";

	private static final String ASPECT_ELEMENT = "aspect";

	private static final String BEFORE_ELEMENT = "before";

	private static final String CONFIG_ELEMENT = "config";

	private static final String DECLARE_PARENTS_ELEMENT = "declare-parents";

	private static final String DEFAULT_IMPL_ATTRIBUTE = "default-impl";

	private static final String DELEGATE_REF_ATTRIBUTE = "delegate-ref";

	private static final String EXPRESSION_ATTRIBUTE = "expression";

	private static final String ID_ATTRIBUTE = "id";

	private static final String IMPLEMENT_INTERFACE_ATTRIBUTE = "implement-interface";

	private static final String METHOD_ATTRIBUTE = "method";

	private static final String POINTCUT_ELEMENT = "pointcut";

	private static final String POINTCUT_REF_ATTRIBUTE = "pointcut-ref";

	private static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";

	private static final String RETURNING_ATTRIBUTE = "returning";

	private static final String THROWING_ATTRIBUTE = "throwing";

	private static final String TYPES_MATCHING_ATTRIBUTE = "types-matching";

	public void doBuildAspectDefinitions(IDOMDocument document, IFile file,
			List<IAspectDefinition> aspectInfos, IProjectClassLoaderSupport classLoaderSupport) {
		parseAopConfigElement(document, file, aspectInfos, classLoaderSupport);
	}

	private void addAspectDefinition(IAspectDefinition info, List<IAspectDefinition> aspectInfos) {
		AopLog.log(AopLog.BUILDER_MESSAGES, info.toString());
		aspectInfos.add(info);
	}

	private void extractLineNumbers(IAspectDefinition def, IDOMNode node) {
		if (def instanceof BeanAspectDefinition) {
			BeanAspectDefinition bDef = (BeanAspectDefinition) def;
			bDef.setAspectStartLineNumber(((IDOMDocument) node.getOwnerDocument())
					.getStructuredDocument().getLineOfOffset(node.getStartOffset()) + 1);
			bDef.setAspectEndLineNumber(((IDOMDocument) node.getOwnerDocument())
					.getStructuredDocument().getLineOfOffset(node.getEndOffset()) + 1);
		}
	}

	private String getPointcut(final Node aspectNode, Map<String, String> rootPointcuts,
			Map<String, String> pointcuts) {
		final String pointcut;
		String pointcutAttribute = getAttribute(aspectNode, POINTCUT_ELEMENT);
		String pointcutRef = getAttribute(aspectNode, POINTCUT_REF_ATTRIBUTE);
		if (!StringUtils.hasText(pointcutAttribute)) {
			if (pointcuts.containsKey(pointcutRef)) {
				pointcut = pointcuts.get(pointcutRef);
			}
			else {
				pointcut = rootPointcuts.get(pointcutRef);
			}
		}
		else {
			pointcut = pointcutAttribute;
		}
		return pointcut;
	}

	private void parseAdvisorElement(final IFile file, final Node aspectNode,
			Map<String, String> rootPointcuts, final List<IAspectDefinition> aspectInfos,
			IProjectClassLoaderSupport classLoaderSupport) {

		final String beanRef = getAttribute(aspectNode, ADVICE_REF_ATTRIBUTE);
		final String className = BeansEditorUtils.getClassNameForBean(file, aspectNode
				.getOwnerDocument(), beanRef);

		if (StringUtils.hasText(className)) {
			NodeList aspectChildren = aspectNode.getParentNode().getChildNodes();
			Map<String, String> pointcuts = new HashMap<String, String>();
			parsePointcuts(pointcuts, aspectChildren);

			final String pointcutExpression = getPointcut(aspectNode, rootPointcuts, pointcuts);

			try {

				classLoaderSupport
						.executeCallback(new IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback() {

							public void doWithActiveProjectClassLoader() throws Throwable {

								Class<?> advisorClass = ClassUtils.loadClass(className);

								if (ClassUtils.loadClass(MethodInterceptor.class).isAssignableFrom(
										advisorClass)) {
									JavaAdvisorDefinition info = prepareJavaAdvisorDefinition(file,
											aspectNode, beanRef, className, pointcutExpression);
									info.setType(ADVICE_TYPE.AROUND);
									info.setAdviceMethodName("invoke");
									info.setAdviceMethodParameterTypes(new String[] { MethodInvocation.class
													.getName() });
									addAspectDefinition(info, aspectInfos);
								}
								if (ClassUtils.loadClass(MethodBeforeAdvice.class)
										.isAssignableFrom(advisorClass)) {
									JavaAdvisorDefinition info = prepareJavaAdvisorDefinition(file,
											aspectNode, beanRef, className, pointcutExpression);
									info.setType(ADVICE_TYPE.BEFORE);
									info.setAdviceMethodName(BEFORE_ELEMENT);
									info.setAdviceMethodParameterTypes(new String[] {
											Method.class.getName(), Object[].class.getName(),
											Object.class.getName() });
									addAspectDefinition(info, aspectInfos);
								}
								if (ClassUtils.loadClass(ThrowsAdvice.class).isAssignableFrom(
										advisorClass)) {
									JavaAdvisorDefinition info = prepareJavaAdvisorDefinition(file,
											aspectNode, beanRef, className, pointcutExpression);
									info.setType(ADVICE_TYPE.AFTER_THROWING);
									info.setAdviceMethodName("afterThrowing");
									info.setAdviceMethodParameterTypes(new String[] {
											Method.class.getName(), Object[].class.getName(),
											Object.class.getName(), Exception.class.getName() });
									addAspectDefinition(info, aspectInfos);
								}
								if (ClassUtils.loadClass(AfterReturningAdvice.class)
										.isAssignableFrom(advisorClass)) {
									JavaAdvisorDefinition info = prepareJavaAdvisorDefinition(file,
											aspectNode, beanRef, className, pointcutExpression);
									info.setType(ADVICE_TYPE.AFTER_RETURNING);
									info.setAdviceMethodName("afterReturning");
									info.setAdviceMethodParameterTypes(new String[] {
											Object.class.getName(), Method.class.getName(),
											Object[].class.getName(), Object.class.getName() });
									addAspectDefinition(info, aspectInfos);
								}
							}
						});
			}
			catch (Throwable e) {
				AopLog.log(AopLog.BUILDER_MESSAGES, Activator.getFormattedMessage(
						"AspectDefinitionBuilder.exceptionOnAdvisorNode", aspectNode));
//				Activator.log(e);
			}
		}
	}

	private void parseAopConfigElement(final IDOMDocument document, IFile file,
			final List<IAspectDefinition> aspectInfos, IProjectClassLoaderSupport classLoaderSupport) {
		NodeList list = document.getDocumentElement().getElementsByTagNameNS(AOP_NAMESPACE_URI,
				CONFIG_ELEMENT);

		for (int i = 0; i < list.getLength(); i++) {
			List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();
			Map<String, String> rootPointcuts = new HashMap<String, String>();
			Node node = list.item(i);
			NodeList children = node.getChildNodes();

			parsePointcuts(rootPointcuts, children);

			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if (ASPECT_ELEMENT.equals(child.getLocalName())) {
					parseAspectElement(file, child, rootPointcuts, aspectDefinitions);
				}
				else if (ADVISOR_ELEMENT.equals(child.getLocalName())) {
					parseAdvisorElement(file, child, rootPointcuts, aspectDefinitions,
							classLoaderSupport);
				}
			}

			if (node.getAttributes().getNamedItem(PROXY_TARGET_CLASS_ATTRIBUTE) != null) {
				boolean proxyTargetClass = Boolean.valueOf(node.getAttributes().getNamedItem(
						PROXY_TARGET_CLASS_ATTRIBUTE).getNodeValue());
				if (proxyTargetClass) {
					for (IAspectDefinition def : aspectDefinitions) {
						((BeanAspectDefinition) def).setProxyTargetClass(proxyTargetClass);
					}
				}
			}

			aspectInfos.addAll(aspectDefinitions);
		}
	}

	private void parseAspectElement(IFile file, Node child, Map<String, String> rootPointcuts,
			List<IAspectDefinition> aspectInfos) {
		String beanRef = getAttribute(child, "ref");
		String className = BeansEditorUtils.getClassNameForBean(file, child.getOwnerDocument(),
				beanRef);
		NodeList aspectChildren = child.getChildNodes();
		Map<String, String> pointcuts = new HashMap<String, String>();
		parsePointcuts(pointcuts, aspectChildren);

		for (int g = 0; g < aspectChildren.getLength(); g++) {
			Node aspectNode = aspectChildren.item(g);
			BeanAspectDefinition info = null;
			if (DECLARE_PARENTS_ELEMENT.equals(aspectNode.getLocalName())) {
				String typesMatching = getAttribute(aspectNode, TYPES_MATCHING_ATTRIBUTE);
				String defaultImpl = getAttribute(aspectNode, DEFAULT_IMPL_ATTRIBUTE);
				String implementInterface = getAttribute(aspectNode, IMPLEMENT_INTERFACE_ATTRIBUTE);
				String delegateRef = getAttribute(aspectNode, DELEGATE_REF_ATTRIBUTE);
				if (StringUtils.hasText(typesMatching)
						&& (StringUtils.hasText(defaultImpl) || StringUtils.hasText(delegateRef))
						&& StringUtils.hasText(implementInterface)) {
					info = new BeanIntroductionDefinition();
					((BeanIntroductionDefinition) info)
							.setIntroducedInterfaceName(implementInterface);
					((BeanIntroductionDefinition) info).setTypePattern(typesMatching);
					if (StringUtils.hasText(delegateRef)) {
						Node delegateBean = BeansEditorUtils.getFirstReferenceableNodeById(
								aspectNode.getOwnerDocument(), delegateRef, file);
						if (delegateBean != null) {
							defaultImpl = BeansEditorUtils.getClassNameForBean(delegateBean);
						}
					}
					((BeanIntroductionDefinition) info).setDefaultImplName(defaultImpl);
					((BeanIntroductionDefinition) info).setAspectClassName(defaultImpl);
					((BeanIntroductionDefinition) info).setAspectName(beanRef);
					extractLineNumbers(info, (IDOMNode) aspectNode);
				}
			}
			else if (StringUtils.hasText(className)) {
				if (BEFORE_ELEMENT.equals(aspectNode.getLocalName())) {
					info = repareBeanAspectDefinition(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPE.BEFORE);
				}
				else if (AROUND_ELEMENT.equals(aspectNode.getLocalName())) {
					info = repareBeanAspectDefinition(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPE.AROUND);
				}
				else if (AFTER_ELEMENT.equals(aspectNode.getLocalName())) {
					info = repareBeanAspectDefinition(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPE.AFTER);
				}
				else if (AFTER_RETURNING_ELEMENT.equals(aspectNode.getLocalName())) {
					info = repareBeanAspectDefinition(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPE.AFTER_RETURNING);
					String returning = getAttribute(aspectNode, RETURNING_ATTRIBUTE);
					info.setReturning(returning);
				}
				else if (AFTER_THROWING_ELEMENT.equals(aspectNode.getLocalName())) {
					info = repareBeanAspectDefinition(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPE.AFTER_THROWING);
					String throwing = getAttribute(aspectNode, THROWING_ATTRIBUTE);
					info.setThrowing(throwing);
				}
				else if (AROUND_ELEMENT.equals(aspectNode.getLocalName())) {
					info = repareBeanAspectDefinition(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPE.AROUND);
				}
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

	private void parsePointcuts(Map<String, String> rootPointcuts, NodeList children) {
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

	private JavaAdvisorDefinition prepareJavaAdvisorDefinition(final IFile file,
			final Node aspectNode, final String beanRef, final String className,
			final String pointcutExpression) {
		JavaAdvisorDefinition info = new JavaAdvisorDefinition();
		extractLineNumbers(info, (IDOMNode) aspectNode);
		info.setPointcutExpression(pointcutExpression);
		info.setAspectClassName(className);
		info.setAspectName(beanRef);
		info.setResource(file);
		return info;
	}

	private BeanAspectDefinition repareBeanAspectDefinition(Map<String, String> pointcuts,
			Map<String, String> rootPointcuts, Node aspectNode, IAopReference.ADVICE_TYPE type) {
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
			argNamesArray = StringUtils.commaDelimitedListToStringArray(argNames);
		}
		info.setArgNames(argNamesArray);
		extractLineNumbers(info, (IDOMNode) aspectNode);
		info.setPointcutExpression(pointcut);
		info.setType(type);
		info.setAdviceMethodName(method);
		return info;
	}
}
