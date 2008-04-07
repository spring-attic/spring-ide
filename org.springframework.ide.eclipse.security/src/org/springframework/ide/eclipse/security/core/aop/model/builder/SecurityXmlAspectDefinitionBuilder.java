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
package org.springframework.ide.eclipse.security.core.aop.model.builder;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.aop.core.internal.model.BeanAspectDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.JavaAdvisorDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.builder.AbstractAspectDefinitionBuilder;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0.5
 */
@SuppressWarnings("restriction")
public class SecurityXmlAspectDefinitionBuilder extends AbstractAspectDefinitionBuilder implements
		IAspectDefinitionBuilder {

	private static final String GLOBAL_METHOD_SECURITY_ELEMENT = "global-method-security";

	private static final String JSR250_ANNOTATION_EXPRESSION = 
		"(@within(javax.annotation.security.DenyAll) || @annotation(javax.annotation.security.DenyAll) || @within(javax.annotation.security.PermitAll) || @annotation(javax.annotation.security.PermitAll) || @within(javax.annotation.security.RolesAllowed) || @annotation(javax.annotation.security.RolesAllowed)) && execution(* *..*(..))";

	private static final String METHOD_SECURITY_INTERCEPTOR_BEAN_ID = "_methodSecurityInterceptor";

	private static final String METHOD_SECURITY_INTERCEPTOR_CLASS = 
		"org.springframework.security.intercept.method.aopalliance.MethodSecurityInterceptor";
	
	private static final String PROTECT_POINTCUT_ELEMENT = "protect-pointcut";

	private static final String SECURED_ANNOTATION_EXPRESSION = 
		"(@within(org.springframework.security.annotation.Secured) || @annotation(org.springframework.security.annotation.Secured)) && execution(* *..*(..))";

	private static final String SECURITY_NAMESPACE_URI = 
		"http://www.springframework.org/schema/security";

	public void doBuildAspectDefinitions(IDOMDocument document, IFile file,
			List<IAspectDefinition> aspectInfos, IProjectClassLoaderSupport classLoaderSupport) {
		parseGlobalMethodSecurityElement(document, file, aspectInfos, classLoaderSupport);
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

	private void parseGlobalMethodSecurityElement(final IDOMDocument document, IFile file,
			final List<IAspectDefinition> aspectInfos, IProjectClassLoaderSupport classLoaderSupport) {
		NodeList list = document.getDocumentElement().getElementsByTagNameNS(
				SECURITY_NAMESPACE_URI, GLOBAL_METHOD_SECURITY_ELEMENT);

		for (int i = 0; i < list.getLength(); i++) {
			List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();
			Node node = list.item(i);
			String securedAnnotations = getAttribute(node, "secured-annotations");
			String jsr250Annoatations = getAttribute(node, "jsr250-annotations");

			if ("enabled".equals(securedAnnotations)) {
				createAnnotationInfo(file, aspectInfos, node, SECURED_ANNOTATION_EXPRESSION);
			}
			if ("enabled".equals(jsr250Annoatations)) {
				createAnnotationInfo(file, aspectInfos, node, JSR250_ANNOTATION_EXPRESSION);
			}

			NodeList children = node.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if (PROTECT_POINTCUT_ELEMENT.equals(child.getLocalName())) {
					parseProtectPointcutElement(file, child, aspectDefinitions);
				}
			}

			aspectInfos.addAll(aspectDefinitions);
		}
	}

	private void createAnnotationInfo(IFile file, final List<IAspectDefinition> aspectInfos,
			Node node, String expression) {
		JavaAdvisorDefinition info1 = prepareJavaAdvisorDefinition(file, node, expression);
		info1.setProxyTargetClass(true);
		addAspectDefinition(info1, aspectInfos);
		JavaAdvisorDefinition info2 = prepareJavaAdvisorDefinition(file, node, expression);
		info2.setProxyTargetClass(false);
		addAspectDefinition(info2, aspectInfos);
	} 

	private void parseProtectPointcutElement(IFile file, Node protectPointcutNode,
			List<IAspectDefinition> aspectInfos) {

		String pointcutExpression = getAttribute(protectPointcutNode, EXPRESSION_ATTRIBUTE);

		if (StringUtils.hasText(pointcutExpression)) {
			JavaAdvisorDefinition info = prepareJavaAdvisorDefinition(file, protectPointcutNode,
					pointcutExpression);
			addAspectDefinition(info, aspectInfos);
		}
	}

	private JavaAdvisorDefinition prepareJavaAdvisorDefinition(IFile file, Node aspectNode,
			String pointcutExpression) {
		JavaAdvisorDefinition info = new JavaAdvisorDefinition();
		extractLineNumbers(info, (IDOMNode) aspectNode);
		info.setPointcutExpression(pointcutExpression);
		info.setAspectClassName(METHOD_SECURITY_INTERCEPTOR_CLASS);
		info.setAspectName(METHOD_SECURITY_INTERCEPTOR_BEAN_ID);
		info.setType(ADVICE_TYPES.AROUND);
		info.setAdviceMethodName("invoke");
		info.setAdviceMethodParameterTypes(new String[] { MethodInvocation.class.getName() });
		info.setResource(file);
		return info;
	}
	
}
