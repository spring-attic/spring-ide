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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.webflow;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ClassHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;

/**
 * This {@link INamespaceHyperlinkDetector} is responsible to for the
 * <code>flow:*</code> namespace.
 * @author Christian Dupuis
 */
public class WebflowConfigHyperLinkDetector extends
		NamespaceHyperlinkDetectorSupport implements IHyperlinkDetector {

	@Override
	public void init() {
		registerHyperlinkCalculator("type", new ClassHyperlinkCalculator());

		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();
		registerHyperlinkCalculator("registry-ref", beanRef);
		registerHyperlinkCalculator("conversation-manager-ref", beanRef);
		registerHyperlinkCalculator("ref", beanRef);
	}
}