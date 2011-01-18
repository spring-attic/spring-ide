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
package org.springframework.ide.eclipse.osgi.ui.editor.hyperlink.osgi;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ClassHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.MethodHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;

/**
 * {@link INamespaceHyperlinkDetector} implementation responsible for the <code>osgi:*</code>
 * namespace.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.0.1
 */
public class OsgiHyperlinkDetector extends NamespaceHyperlinkDetectorSupport implements
		IHyperlinkDetector {

	@Override
	public void init() {
		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();
		registerHyperlinkCalculator("depends-on", beanRef);
		registerHyperlinkCalculator("ref", beanRef);
		registerHyperlinkCalculator("property-placeholder", "defaults-ref", beanRef);
		registerHyperlinkCalculator("interface", new ClassHyperlinkCalculator());

		MethodHyperlinkCalculator methodRef = new RegistrationMethodHyperlinkCalculator();
		registerHyperlinkCalculator("registration-method", methodRef);
		registerHyperlinkCalculator("unregistration-method", methodRef);

	}
}
