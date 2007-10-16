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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.tx;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;

/**
 * {@link INamespaceHyperlinkDetector} responsible for handling hyperlink
 * detection on elements of the <code>tx:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0
 */
public class TxHyperlinkDetector extends NamespaceHyperlinkDetectorSupport implements
		IHyperlinkDetector {

	@Override
	public void init() {
		registerHyperlinkCalculator("transaction-manager", new BeanHyperlinkCalculator());
	}
}
