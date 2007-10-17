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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.jee;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * {@link INamespaceContentAssistProcessor} responsible for handling content assist 
 * request on elements of the <code>jee:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0
 */
public class JeeContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		IContentAssistCalculator calculator = new ClassContentAssistCalculator(true);
		registerContentAssistCalculator("jndi-lookup", "expected-type", calculator);
		registerContentAssistCalculator("jndi-lookup", "proxy-interface", calculator);
		registerContentAssistCalculator("remote-slsb", "home-interface", calculator);
		registerContentAssistCalculator("remote-slsb", "business-interface", calculator);
		registerContentAssistCalculator("local-slsb", "home-interface", calculator);
		registerContentAssistCalculator("local-slsb", "business-interface", calculator);
	}
}
