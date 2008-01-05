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

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassHierachyContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;

/**
 * {@link AbstractContentAssistProcessor} implementation that is used within the
 * Spring Web Flow XML Editor extensions.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowContentAssistProcessor extends
		NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		ClassContentAssistCalculator clazz = new ClassContentAssistCalculator(false);
		registerContentAssistCalculator("argument", "parameter-type", clazz);
		registerContentAssistCalculator("mapping", "to", clazz);
		registerContentAssistCalculator("mapping", "form", clazz);
		registerContentAssistCalculator("var", "class", clazz);
		registerContentAssistCalculator("attribute", "type", clazz);

		WebflowBeanReferenceContentAssistCalculator beanRef = new WebflowBeanReferenceContentAssistCalculator();
		registerContentAssistCalculator("action", "bean", beanRef);
		registerContentAssistCalculator("bean-action", "bean", beanRef);
		registerContentAssistCalculator("var", "bean", beanRef);
		registerContentAssistCalculator("var", "name", beanRef);
		registerContentAssistCalculator("exception-handler", "bean", beanRef);

		StateReferenceContentAssistCalculator stateRef = new StateReferenceContentAssistCalculator();
		registerContentAssistCalculator("transition", "to", stateRef);
		registerContentAssistCalculator("start-state", "idref", stateRef);
		registerContentAssistCalculator("if", "then", stateRef);
		registerContentAssistCalculator("if", "else", stateRef);

		registerContentAssistCalculator("transition", "on-exception",
				new ClassHierachyContentAssistCalculator(Throwable.class
						.getName()));

		registerContentAssistCalculator("subflow-state", "flow",
				new SubflowReferenceContentAssistCalculator());

		registerContentAssistCalculator("action", "method",
				new BeanMethodContentAssistCalculator());

		registerContentAssistCalculator("bean-action", "method",
				new BeanActionMethodContentAssistCalculator());

	}
}
