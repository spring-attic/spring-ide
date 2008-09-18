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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.faces;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;

/**
 * This {@link INamespaceHyperlinkDetector} is responsible to for the <code>faces:*</code>
 * namespace.
 * @author Christian Dupuis
 */
public class FacesConfigHyperLinkDetector extends NamespaceHyperlinkDetectorSupport implements
		IHyperlinkDetector {

	@Override
	public void init() {
		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();

		registerHyperlinkCalculator("flow-builder-services", "expression-parser", beanRef);
		registerHyperlinkCalculator("flow-builder-services", "formatter-registry", beanRef);
		registerHyperlinkCalculator("flow-builder-services", "view-factory-creator", beanRef);
		registerHyperlinkCalculator("flow-builder-services", "conversion-service", beanRef);
	}

}