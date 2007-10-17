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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.webflow;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * This {@link INamespaceContentAssistProcessor} is responsible to provide
 * content assist support for the <code>flow:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowConfigContentAssistProcessor extends
		NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		BeanReferenceContentAssistCalculator beanRef = new BeanReferenceContentAssistCalculator(
				true);
		registerContentAssistCalculator("executor", "registry-ref", beanRef);
		registerContentAssistCalculator("repository",
				"conversation-manager-ref", beanRef);
		registerContentAssistCalculator("listener", "ref", beanRef);
		registerContentAssistCalculator("attribute", "type",
				new ClassContentAssistCalculator());
	}
}
