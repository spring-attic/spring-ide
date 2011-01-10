/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import java.io.Serializable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.util.ClassUtils;

/**
 * {@link IContentAssistCalculator} that calculates bean id proposals based on the bean class.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public abstract class AbstractIdContentAssistCalculator implements IContentAssistCalculator {

	private static final String[] FILTERED_NAMES = new String[] { Serializable.class.getName(),
			InitializingBean.class.getName(), FactoryBean.class.getName(),
			DisposableBean.class.getName() };

	protected void createBeanIdProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder, String className) {
		String beanId = buildDefaultBeanName(className);
		if (beanId.startsWith(context.getMatchString()) && shouldNotFilter(className)) {
			recorder.recordProposal(BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN), 10, beanId
					+ " - " + className, beanId);
		}
	}

	private String buildDefaultBeanName(String className) {
		String shortClassName = className;
		int ix = className.lastIndexOf('$');
		if (ix >= 0) {
			shortClassName = className.substring(ix + 1);
		}
		else {
			shortClassName = ClassUtils.getShortName(className);
		}
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
