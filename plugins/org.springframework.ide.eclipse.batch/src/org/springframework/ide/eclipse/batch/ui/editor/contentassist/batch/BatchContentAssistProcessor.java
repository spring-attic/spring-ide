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
package org.springframework.ide.eclipse.batch.ui.editor.contentassist.batch;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * {@link INamespaceContentAssistProcessor} implementation responsible for the <code>batch:*</code>
 * namespace.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.2.2
 */
public class BatchContentAssistProcessor extends NamespaceContentAssistProcessorSupport {
	
	public void init() {
		IContentAssistCalculator stepRef = new StepReferenceContentAssistCalculator();
		registerContentAssistCalculator("next", "to", stepRef);
		registerContentAssistCalculator("stop", "restart", stepRef);
		registerContentAssistCalculator("split", "next", stepRef);
		registerContentAssistCalculator("step", "next", stepRef);
		
		IContentAssistCalculator beanRef = new BeanReferenceContentAssistCalculator();
		registerContentAssistCalculator("job", "parent", beanRef);
		registerContentAssistCalculator("step", "parent", beanRef);
		registerContentAssistCalculator("chunk", "processor", beanRef);
		registerContentAssistCalculator("chunk", "reader", beanRef);
		registerContentAssistCalculator("chunk", "writer", beanRef);
		
		IContentAssistCalculator classRef = new ClassContentAssistCalculator();
		registerContentAssistCalculator("listener", "class", classRef);
		registerContentAssistCalculator("job-listener", "class", classRef);
		registerContentAssistCalculator("step-listener", "class", classRef);
	}
	
}
