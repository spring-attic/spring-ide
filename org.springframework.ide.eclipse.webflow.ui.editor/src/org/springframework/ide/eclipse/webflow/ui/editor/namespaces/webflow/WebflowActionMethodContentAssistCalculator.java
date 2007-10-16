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
package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.MethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.IMethodFilter;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * {@link MethodContentAssistCalculator} extension that is used to propose
 * action methods.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public abstract class WebflowActionMethodContentAssistCalculator extends
		MethodContentAssistCalculator {

	public WebflowActionMethodContentAssistCalculator(IMethodFilter filter) {
		super(filter);
	}

	@Override
	protected final IType calculateType(ContentAssistRequest request,
			String attributeName) {
		if (BeansEditorUtils.hasAttribute(request.getNode(), "bean")) {
			String className = null;
			IWebflowConfig config = Activator.getModel().getProject(
					BeansEditorUtils.getFile(request).getProject()).getConfig(
					BeansEditorUtils.getFile(request));

			if (config != null) {
				Set<IBean> beans = WebflowModelUtils.getBeans(config);
				for (IBean bean : beans) {
					if (bean.getElementName().equals(
							BeansEditorUtils.getAttribute(request.getNode(),
									"bean"))) {
						className = BeansModelUtils.getBeanClass(bean, null);
					}
				}
				return JdtUtils.getJavaType(BeansEditorUtils.getFile(request)
						.getProject(), className);
			}
		}
		return null;
	}
}
