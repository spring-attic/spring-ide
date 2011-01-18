/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.webflow;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ClassHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;

/**
 * This {@link INamespaceHyperlinkDetector} is responsible to for the <code>flow:*</code> namespace.
 * @author Christian Dupuis
 */
public class WebflowConfigHyperLinkDetector extends NamespaceHyperlinkDetectorSupport implements
		IHyperlinkDetector {

	@Override
	public void init() {
		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();
		ClassHyperlinkCalculator classRef = new ClassHyperlinkCalculator();

		// web flow 1.x
		registerHyperlinkCalculator("attribute", "type", classRef);
		registerHyperlinkCalculator("executor", "registry-ref", beanRef);
		registerHyperlinkCalculator("repository", "conversation-manager-ref", beanRef);
		registerHyperlinkCalculator("listener", "ref", beanRef);

		// web flow 2.x
		registerHyperlinkCalculator("flow-executor", "flow-registry", beanRef);
		registerHyperlinkCalculator("listener", "ref", beanRef);
		registerHyperlinkCalculator("flow-registry", "parent", beanRef);
		registerHyperlinkCalculator("flow-registry", "flow-builder-services", beanRef);
		registerHyperlinkCalculator("flow-builder", "class", classRef);
		registerHyperlinkCalculator("flow-builder-services", "view-factory-creator", beanRef);
		registerHyperlinkCalculator("flow-builder-services", "conversion-service", beanRef);
		registerHyperlinkCalculator("flow-builder-services", "expression-parser", beanRef);
//		registerHyperlinkCalculator("flow-builder", "conversion-service", classRef);
//		registerHyperlinkCalculator("flow-builder", "expression-parser", classRef);
//		registerHyperlinkCalculator("flow-builder", "formatter-registry", classRef);
	}
	
}