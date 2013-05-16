/*******************************************************************************
 * Copyright (c) 2008, 2013 Spring IDE Developers
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
import org.springframework.ide.eclipse.aop.core.internal.model.JavaAdvisorDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.builder.AbstractAspectDefinitionBuilder;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.aop.core.model.builder.IDocumentFactory;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IAspectDefinitionBuilder} for Spring Security's global-method-security element.
 * 
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0.5
 */
@SuppressWarnings("restriction")
public class SecurityXmlAspectDefinitionBuilder extends AbstractAspectDefinitionBuilder implements
		IAspectDefinitionBuilder {

	private static final String EXPRESSION_ATTRIBUTE = "expression";

	private static final String GLOBAL_METHOD_SECURITY_ELEMENT = "global-method-security";

	private static final String JSR250_ANNOTATION_EXPRESSION = "(@within(javax.annotation.security.DenyAll) || @annotation(javax.annotation.security.DenyAll) || @within(javax.annotation.security.PermitAll) || @annotation(javax.annotation.security.PermitAll) || @within(javax.annotation.security.RolesAllowed) || @annotation(javax.annotation.security.RolesAllowed)) && execution(* *..*(..))";

	private static final String METHOD_SECURITY_INTERCEPTOR_BEAN_ID = "_methodSecurityInterceptor";

	private static final String METHOD_SECURITY_INTERCEPTOR_V2_CLASS = "org.springframework.security.intercept.method.aopalliance.MethodSecurityInterceptor";

	private static final String METHOD_SECURITY_INTERCEPTOR_V3_CLASS = "org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor";

	private static final String PROTECT_POINTCUT_ELEMENT = "protect-pointcut";

	private static final String SECURED_ANNOTATION_EXPRESSION = "(@within($CLASS) || @annotation($CLASS)) && execution(* *..*(..))";

	private static final String SECURED_ANNOTATION_V2_CLASS = "org.springframework.security.annotation.Secured";

	private static final String SECURED_ANNOTATION_V3_CLASS = "org.springframework.security.access.annotation.Secured";

	private static final String SECURITY_NAMESPACE_URI = "http://www.springframework.org/schema/security";

	public void buildAspectDefinitions(List<IAspectDefinition> aspectInfos, IFile file,
			IProjectClassLoaderSupport classLoaderSupport, IDocumentFactory factory) {
		if (file.getFileExtension() != null && file.getFileExtension().equals("xml")) {
			parseGlobalMethodSecurityElement(factory.createDocument(file), file, aspectInfos, classLoaderSupport);
		}
	}

	private void addAspectDefinition(IAspectDefinition info, List<IAspectDefinition> aspectInfos) {
		AopLog.log(AopLog.BUILDER_MESSAGES, info.toString());
		aspectInfos.add(info);
	}

	private void parseGlobalMethodSecurityElement(final IDOMDocument document, IFile file,
			final List<IAspectDefinition> aspectInfos, IProjectClassLoaderSupport classLoaderSupport) {
		if (document == null || document.getStructuredDocument() == null) {
			return;
		}

		NodeList list = document.getDocumentElement().getElementsByTagNameNS(SECURITY_NAMESPACE_URI,
				GLOBAL_METHOD_SECURITY_ELEMENT);

		if (list.getLength() == 0) {
			return;
		}
		
		// Check if Spring Security 2.0 or 3.0 is being used?
		boolean present20 = ClassUtils.isPresent(METHOD_SECURITY_INTERCEPTOR_V2_CLASS, classLoaderSupport
				.getProjectClassLoader());
		boolean present30 = ClassUtils.isPresent(METHOD_SECURITY_INTERCEPTOR_V3_CLASS, classLoaderSupport
				.getProjectClassLoader());
		if (present30 && present20) {
			// both versions are on the classpath -> what to do?
			return;
		}
		else if (!present20 && !present30) {
			// no Spring Security on classpath -> return
			return;
		}

		for (int i = 0; i < list.getLength(); i++) {
			List<IAspectDefinition> aspectDefinitions = new ArrayList<IAspectDefinition>();

			Node node = list.item(i);
			// TODO CD Spring Security currently only correctly supports annotations on the interface
			// which the following can't support as per AspectJ semantics. Waiting to Spring
			// Security to fix the problem.
			String securedAnnotations = getAttribute(node, "secured-annotations");
			String jsr250Annoatations = getAttribute(node, "jsr250-annotations");
			if ("enabled".equals(securedAnnotations)) {
				createAnnotationInfo(file, aspectInfos, node, SECURED_ANNOTATION_EXPRESSION.replace("$CLASS",
						(present30 ? SECURED_ANNOTATION_V3_CLASS : SECURED_ANNOTATION_V2_CLASS)), present30);
			}
			if ("enabled".equals(jsr250Annoatations)) {
				// Check if the JSR250 annotation are available
				boolean presentDenyAll = ClassUtils.isPresent("javax.annotation.security.DenyAll", classLoaderSupport
						.getProjectClassLoader());
				boolean presentPermitAll = ClassUtils.isPresent("javax.annotation.security.PermitAll", classLoaderSupport
						.getProjectClassLoader());
				boolean presentRolesAllowed = ClassUtils.isPresent("javax.annotation.security.RolesAllowed", classLoaderSupport
						.getProjectClassLoader());
				
				if (presentDenyAll && presentPermitAll && presentRolesAllowed) {
					createAnnotationInfo(file, aspectInfos, node, JSR250_ANNOTATION_EXPRESSION, present30);
				}
			}

			NodeList children = node.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if (PROTECT_POINTCUT_ELEMENT.equals(child.getLocalName())) {
					parseProtectPointcutElement(file, child, aspectDefinitions, present30);
				}
			}

			aspectInfos.addAll(aspectDefinitions);
		}
	}

	private void createAnnotationInfo(IFile file, final List<IAspectDefinition> aspectInfos, Node node,
			String expression, boolean present30) {
		JavaAdvisorDefinition info1 = prepareJavaAdvisorDefinition(file, node, expression, present30);
		info1.setProxyTargetClass(true);
		addAspectDefinition(info1, aspectInfos);
		JavaAdvisorDefinition info2 = prepareJavaAdvisorDefinition(file, node, expression, present30);
		info2.setProxyTargetClass(false);
		addAspectDefinition(info2, aspectInfos);
	}

	private void parseProtectPointcutElement(IFile file, Node protectPointcutNode, List<IAspectDefinition> aspectInfos,
			boolean present30) {

		String pointcutExpression = getAttribute(protectPointcutNode, EXPRESSION_ATTRIBUTE);

		if (StringUtils.hasText(pointcutExpression)) {
			JavaAdvisorDefinition info = prepareJavaAdvisorDefinition(file, protectPointcutNode, pointcutExpression,
					present30);
			addAspectDefinition(info, aspectInfos);
		}
	}

	private JavaAdvisorDefinition prepareJavaAdvisorDefinition(IFile file, Node aspectNode, String pointcutExpression,
			boolean present30) {
		JavaAdvisorDefinition info = new JavaAdvisorDefinition();
		extractLineNumbers(info, (IDOMNode) aspectNode);
		info.setPointcutExpression(pointcutExpression);
		info.setAspectClassName((present30 ? METHOD_SECURITY_INTERCEPTOR_V3_CLASS
				: METHOD_SECURITY_INTERCEPTOR_V2_CLASS));
		info.setAspectName(METHOD_SECURITY_INTERCEPTOR_BEAN_ID);
		info.setType(ADVICE_TYPE.AROUND);
		info.setAdviceMethodName("invoke");
		info.setAdviceMethodParameterTypes(new String[] { MethodInvocation.class.getName() });
		info.setResource(file);
		return info;
	}

}
