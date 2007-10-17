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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.tx;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * {@link INamespaceContentAssistProcessor} responsible for handling content assist 
 * request on elements of the <code>tx:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class TxContentAssistProcessor extends
		NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		BeanReferenceContentAssistCalculator calculator = 
			new BeanReferenceContentAssistCalculator(true);
		registerContentAssistCalculator("advice", "transaction-manager",
				calculator);
		registerContentAssistCalculator("annotation-driven",
				"transaction-manager", calculator);
	}
}
