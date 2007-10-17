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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.aop;

import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassHierachyContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;

/**
 * {@link IContentAssistCalculator} implementation that calculates the content
 * assist proposals for <code>default-impl</code> attribute.
 * <p>
 * Either searches for implementations of the interface(s) specified by the
 * <code>implement-interface</code> attribute. If this attribute has not been
 * specified a full class search will be triggered.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class DefaultImplContentAssistCalculator implements
		IContentAssistCalculator {

	public void computeProposals(ContentAssistRequest request,
			String matchString, String attributeName, String namespace,
			String namepacePrefix) {
		String implementInterface = BeansEditorUtils.getAttribute(request
				.getNode(), "implement-interface");
		if (StringUtils.hasText(implementInterface)) {
			new ClassHierachyContentAssistCalculator(implementInterface)
					.computeProposals(request, matchString, attributeName,
							namespace, namepacePrefix);
		}
		else {
			new ClassContentAssistCalculator(false).computeProposals(request,
					matchString, attributeName, namespace, namepacePrefix);
		}
	}
}