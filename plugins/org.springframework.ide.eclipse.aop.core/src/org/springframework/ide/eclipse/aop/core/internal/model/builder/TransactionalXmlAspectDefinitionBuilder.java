/*******************************************************************************
 * Copyright (c) 2009, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import java.util.List;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.aop.core.internal.model.JavaAdvisorDefinition;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.aop.core.model.builder.IDocumentFactory;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IAspectDefinitionBuilder} that takes a
 * <pre>
 * tx:annotation-config
 * </pre>
 * element and converts this into AOP references.
 * @author Christian Dupuis
 * @since 2.2.7
 */
@SuppressWarnings("restriction")
public class TransactionalXmlAspectDefinitionBuilder extends AbstractAspectDefinitionBuilder implements
		IAspectDefinitionBuilder {

	private static final String TRANSACTION_INTERCEPTOR_CLASS = "org.springframework.transaction.interceptor.TransactionInterceptor";

	private static final String ANNOTATION_EXPRESSION = "(@within(org.springframework.transaction.annotation.Transactional) || @annotation(org.springframework.transaction.annotation.Transactional)) && execution(* *..*(..))";

	private static final String TX_NAMESPACE_URI = "http://www.springframework.org/schema/tx";

	public void buildAspectDefinitions(List<IAspectDefinition> aspectInfos, IFile file,
			IProjectClassLoaderSupport classLoaderSupport, IDocumentFactory factory) {
		parseAnnotationDrivenElement(factory.createDocument(file), file, aspectInfos, classLoaderSupport);
	}

	private void addAspectDefinition(IAspectDefinition info, List<IAspectDefinition> aspectInfos) {
		AopLog.log(AopLog.BUILDER_MESSAGES, info.toString());
		aspectInfos.add(info);
	}

	private void parseAnnotationDrivenElement(final IDOMDocument document, IFile file,
			final List<IAspectDefinition> aspectInfos, IProjectClassLoaderSupport classLoaderSupport) {
		if (document == null || document.getStructuredDocument() == null) {
			return;
		}
		
		NodeList list = document.getDocumentElement().getElementsByTagNameNS(TX_NAMESPACE_URI, "annotation-driven");

		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String mode = getAttribute(node, "mode");
			if (!StringUtils.hasText(mode) || "proxy".equals(mode)) {
				String isProxyTargetClass = getAttribute(node, "proxy-target-class");
				addAspectDefinition(createAnnotationInfo(file, node, isProxyTargetClass), aspectInfos);
			}
		}
	}

	private IAspectDefinition createAnnotationInfo(IFile file, Node node, String isProxyTargetClass) {
		JavaAdvisorDefinition info = prepareJavaAdvisorDefinition(file, node, ANNOTATION_EXPRESSION);
		info.setProxyTargetClass(("true".equalsIgnoreCase(isProxyTargetClass)));
		return info;
	}

	private JavaAdvisorDefinition prepareJavaAdvisorDefinition(IFile file, Node aspectNode, String pointcutExpression) {
		JavaAdvisorDefinition info = new JavaAdvisorDefinition();
		extractLineNumbers(info, (IDOMNode) aspectNode);
		info.setPointcutExpression(pointcutExpression);
		info.setAspectClassName(TRANSACTION_INTERCEPTOR_CLASS);
		
		IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
		if (config != null) {
			Set<IBean> beans = config.getBeans(TRANSACTION_INTERCEPTOR_CLASS);
			if (beans.size() > 0) {
				for (IBean bean : beans) {
					if (info.getAspectStartLineNumber() <= bean.getElementStartLine() && info.getAspectEndLineNumber() >= bean.getElementEndLine()) {
						info.setAspectName(bean.getElementName());
						break;
					}
				}
			}
		}

		info.setType(ADVICE_TYPE.AROUND);
		info.setAdviceMethodName("invoke");
		info.setAdviceMethodParameterTypes(new String[] { MethodInvocation.class.getName() });
		info.setResource(file);
		return info;
	}

}
