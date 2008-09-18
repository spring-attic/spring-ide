/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.faces;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * This {@link INamespaceContentAssistProcessor} is responsible to provide content assist support
 * for the <code>faces:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class FacesConfigContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		BeanReferenceContentAssistCalculator beanRef = new BeanReferenceContentAssistCalculator(
				true);

		registerContentAssistCalculator("flow-builder-services", "expression-parser", beanRef);
		registerContentAssistCalculator("flow-builder-services", "formatter-registry", beanRef);
		registerContentAssistCalculator("flow-builder-services", "view-factory-creator", beanRef);
		registerContentAssistCalculator("flow-builder-services", "conversion-service", beanRef);

	}

}
