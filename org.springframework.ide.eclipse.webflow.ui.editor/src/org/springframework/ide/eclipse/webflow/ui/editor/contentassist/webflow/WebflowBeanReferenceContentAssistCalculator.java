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
package org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeanReferenceSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * {@link IContentAssistCalculator} that proposes bean references.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class WebflowBeanReferenceContentAssistCalculator implements
		IContentAssistCalculator {

	public void computeProposals(ContentAssistRequest request,
			String matchString, String attributeName, String namespace,
			String namepacePrefix) {
		if (matchString == null) {
			matchString = "";
		}

		IFile file = BeansEditorUtils.getFile(request);
		BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(
				request);

		IWebflowConfig config = Activator.getModel().getProject(
				file.getProject()).getConfig(file);
		if (config != null) {
			Set<IBean> beans = WebflowModelUtils.getBeans(config);
			for (IBean bean : beans) {
				requestor.acceptSearchMatch(bean, file, matchString);
			}
		}
	}
}