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
package org.springframework.ide.eclipse.security.ui.editor.hyperlink.security;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;

/**
 * {@link NamespaceHyperlinkDetectorSupport} hyperlink calculator for the security namespace.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class SecurityHyperlinkDetector extends NamespaceHyperlinkDetectorSupport implements
		IHyperlinkDetector {

	@Override
	public void init() {
		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();
		registerHyperlinkCalculator("http", "access-decision-manager-ref", beanRef);
		registerHyperlinkCalculator("http", "entry-point-ref", beanRef);
		registerHyperlinkCalculator("openid-login", "user-service-ref", beanRef);
		registerHyperlinkCalculator("remember-me", "user-service-ref", beanRef);
		registerHyperlinkCalculator("remember-me", "token-repository-ref", beanRef);
		registerHyperlinkCalculator("remember-me", "data-source-ref", beanRef);
		registerHyperlinkCalculator("x509", "user-service-ref", beanRef);
		registerHyperlinkCalculator("authentication-provider", "user-service-ref", beanRef);
		registerHyperlinkCalculator("ldap-user-service", "server-ref", beanRef);
		registerHyperlinkCalculator("ldap-authentication-provider", "server-ref", beanRef);
		registerHyperlinkCalculator("ldap-user-service", "cache-ref", beanRef);
		registerHyperlinkCalculator("jdbc-user-service", "cache-ref", beanRef);
		registerHyperlinkCalculator("password-encoder", "ref", beanRef);
		registerHyperlinkCalculator("jdbc-user-service", "data-source-ref", beanRef);
	}
}
