/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
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
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassHierachyContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * This {@link INamespaceContentAssistProcessor} is responsible to provide content assist support
 * for the <code>flow:*</code> namespace.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class WebflowConfigContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		BeanReferenceContentAssistCalculator beanRef = new BeanReferenceContentAssistCalculator(
				true);

		// web flow 1.x
		registerContentAssistCalculator("executor", "registry-ref", beanRef);
		registerContentAssistCalculator("repository", "conversation-manager-ref", beanRef);
		registerContentAssistCalculator("listener", "ref", beanRef);
		registerContentAssistCalculator("attribute", "type",
				new ClassContentAssistCalculator(false));

		// web flow 2.x
		registerContentAssistCalculator("flow-executor", "flow-registry", beanRef);
		registerContentAssistCalculator("listener", "ref", beanRef);
		registerContentAssistCalculator("flow-registry", "parent", beanRef);
		registerContentAssistCalculator("flow-registry", "flow-builder-services", beanRef);
		registerContentAssistCalculator("flow-builder", "class",
				new ClassHierachyContentAssistCalculator("org.springframework.webflow.engine.builder.FlowBuilder"));
		registerContentAssistCalculator("flow-builder-services", "view-factory-creator", beanRef);
		registerContentAssistCalculator("flow-builder-services", "expression-parser", beanRef);
		registerContentAssistCalculator("flow-builder-services", "conversion-service", beanRef);
//		registerContentAssistCalculator("flow-builder", "conversion-service",
//				new ClassHierachyContentAssistCalculator(
//						"org.springframework.binding.convert.ConversionService"));
//		registerContentAssistCalculator("flow-builder", "expression-parser",
//				new ClassHierachyContentAssistCalculator(
//						"org.springframework.binding.expression.ExpressionParser"));
//		registerContentAssistCalculator("flow-builder", "formatter-registry",
//				new ClassContentAssistCalculator(false));

	}

}
