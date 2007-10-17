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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.lang;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * {@link INamespaceContentAssistProcessor} responsible for handling content assist 
 * request on elements of the <code>lang:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0
 */
public class LangContentAssistProcessor extends
		NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		IContentAssistCalculator calculator = new ClassContentAssistCalculator(
				true);
		registerContentAssistCalculator("jruby", "script-interfaces",
				calculator);
		registerContentAssistCalculator("bsh", "script-interfaces", calculator);
		registerContentAssistCalculator("groovy", "customizer-ref",
				new BeanReferenceContentAssistCalculator(true));
	}
}
