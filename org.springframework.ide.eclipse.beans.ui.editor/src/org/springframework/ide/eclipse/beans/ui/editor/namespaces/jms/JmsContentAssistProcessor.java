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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.jms;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * {@link INamespaceContentAssistProcessor} responsible for handling content assist 
 * request on elements of the <code>jms:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class JmsContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		IContentAssistCalculator beanRef = new BeanReferenceContentAssistCalculator(true);
		registerContentAssistCalculator("listener-container", "connection-factory", beanRef);
		registerContentAssistCalculator("listener-container", "task-executor", beanRef);
		registerContentAssistCalculator("listener-container", "destination-resolver", beanRef);
		registerContentAssistCalculator("listener-container", "message-converter", beanRef);
		registerContentAssistCalculator("listener-container", "transaction-manager", beanRef);
		registerContentAssistCalculator("listener", "ref", beanRef);
		registerContentAssistCalculator("jca-listener-container", "resource-adapter", beanRef);
		registerContentAssistCalculator("jca-listener-container", "activation-spec-factory", beanRef);
		registerContentAssistCalculator("jca-listener-container", "message-converter", beanRef);
		
		registerContentAssistCalculator("listener", "method", new ListenerMethodContentAssistCalculator());
	}
}
