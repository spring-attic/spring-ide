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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.bean;

import java.io.Serializable;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.ClassUtils;

/**
 * {@link IContentAssistCalculator} that calculates bean id proposals based on
 * the bean class.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class BeanIdContentAssistCalculator implements IContentAssistCalculator {

	private static final String[] FILTERED_NAMES = new String[] {
			Serializable.class.getName(), InitializingBean.class.getName(),
			FactoryBean.class.getName(), DisposableBean.class.getName() };

	public void computeProposals(ContentAssistRequest request,
			String matchString, String attributeName, String namespace,
			String namepacePrefix) {
		addBeanIdProposal(request, matchString);
	}

	private void addBeanIdProposal(ContentAssistRequest request,
			String matchString) {
		String className = BeansEditorUtils.getClassNameForBean(request
				.getNode());
		if (className != null) {
			createBeanIdProposals(request, matchString, className);

			// add interface proposals
			IType type = JdtUtils.getJavaType(BeansEditorUtils.getFile(request)
					.getProject(), className);
			Set<IType> allInterfaces = Introspector
					.getAllImplenentedInterfaces(type);
			for (IType interf : allInterfaces) {
				createBeanIdProposals(request, matchString, interf
						.getFullyQualifiedName());
			}
		}
	}

	private void createBeanIdProposals(ContentAssistRequest request,
			String matchString, String className) {
		String beanId = buildDefaultBeanName(className);
		if (beanId.startsWith(matchString) && shouldNotFilter(className)) {
			request.addProposal(new BeansJavaCompletionProposal(beanId, request
					.getReplacementBeginPosition(), request
					.getReplacementLength(), beanId.length(), BeansUIImages
					.getImage(BeansUIImages.IMG_OBJS_BEAN), beanId + " - "
					+ className, null, 10, null));
		}
	}

	private String buildDefaultBeanName(String className) {
		String shortClassName = ClassUtils.getShortName(className);
		return java.beans.Introspector.decapitalize(shortClassName);
	}

	private boolean shouldNotFilter(String className) {
		for (String filter : FILTERED_NAMES) {
			if (className.startsWith(filter)) {
				return false;
			}
		}
		return true;
	}

}
