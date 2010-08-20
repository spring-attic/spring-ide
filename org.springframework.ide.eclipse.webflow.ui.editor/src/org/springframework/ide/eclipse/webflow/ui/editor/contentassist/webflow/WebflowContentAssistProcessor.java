/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
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
 * {@link AbstractContentAssistProcessor} implementation that is used within the Spring Web Flow XML
 * Editor extensions.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		ClassContentAssistCalculator clazz = new ClassContentAssistCalculator(false);
		WebflowBeanReferenceContentAssistCalculator beanRef = new WebflowBeanReferenceContentAssistCalculator();
		StateReferenceContentAssistCalculator stateRef = new StateReferenceContentAssistCalculator();

		// web flow 1.x
		registerContentAssistCalculator("argument", "parameter-type", clazz);
		registerContentAssistCalculator("mapping", "to", clazz);
		registerContentAssistCalculator("mapping", "from", clazz);
		registerContentAssistCalculator("var", "class", clazz);
		registerContentAssistCalculator("attribute", "type", clazz);

		registerContentAssistCalculator("action", "bean", beanRef);
		registerContentAssistCalculator("bean-action", "bean", beanRef);
		registerContentAssistCalculator("var", "bean", beanRef);
		registerContentAssistCalculator("var", "name", beanRef);
		registerContentAssistCalculator("exception-handler", "bean", beanRef);

		registerContentAssistCalculator("transition", "to", stateRef);
		registerContentAssistCalculator("start-state", "idref", stateRef);
		registerContentAssistCalculator("if", "then", stateRef);
		registerContentAssistCalculator("if", "else", stateRef);

		registerContentAssistCalculator("transition", "on-exception",
				new ClassHierachyContentAssistCalculator(Throwable.class.getName()));

		registerContentAssistCalculator("subflow-state", "flow",
				new SubflowReferenceContentAssistCalculator());
		registerContentAssistCalculator("flow", "parent",
				new SubflowReferenceContentAssistCalculator());

		registerContentAssistCalculator("action", "method", new BeanMethodContentAssistCalculator());

		registerContentAssistCalculator("bean-action", "method",
				new BeanActionMethodContentAssistCalculator());
		
		// web flow 2.x
		registerContentAssistCalculator("attribute", "type", clazz);
		registerContentAssistCalculator("var", "class", clazz);
		registerContentAssistCalculator("input", "type", clazz);
		registerContentAssistCalculator("output", "type", clazz);
		registerContentAssistCalculator("evaluate", "result-type", clazz);

		registerContentAssistCalculator("exception-handler", "bean", beanRef);
		registerContentAssistCalculator("subflow-state", "subflow-attribute-mapper", beanRef);

		registerContentAssistCalculator("transition", "to", stateRef);
		registerContentAssistCalculator("flow", "start-state", stateRef);
		registerContentAssistCalculator("if", "then", stateRef);
		registerContentAssistCalculator("if", "else", stateRef);
		
		registerContentAssistCalculator("transition", "on-exception",
				new ClassHierachyContentAssistCalculator(Throwable.class.getName()));

		registerContentAssistCalculator("subflow-state", "subflow",
				new SubflowReferenceContentAssistCalculator());
	}
}
