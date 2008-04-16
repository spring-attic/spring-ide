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
package org.springframework.ide.eclipse.security.ui.editor.contentassist.security;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;

/**
 * {@link NamespaceContentAssistProcessorSupport} content assist processor for the security
 * namespace.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class SecurityContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		BeanReferenceContentAssistCalculator beanRef = new BeanReferenceContentAssistCalculator(
				true);
		registerContentAssistCalculator("http", "access-decision-manager-ref", beanRef);
		registerContentAssistCalculator("http", "entry-point-ref", beanRef);
		registerContentAssistCalculator("openid-login", "user-service-ref", beanRef);
		registerContentAssistCalculator("remember-me", "user-service-ref", beanRef);
		registerContentAssistCalculator("remember-me", "token-repository-ref", beanRef);
		registerContentAssistCalculator("remember-me", "data-source-ref", beanRef);
		registerContentAssistCalculator("x509", "user-service-ref", beanRef);
		registerContentAssistCalculator("authentication-provider", "user-service-ref", beanRef);
		registerContentAssistCalculator("ldap-user-service", "server-ref", beanRef);
		registerContentAssistCalculator("ldap-authentication-provider", "server-ref", beanRef);
		registerContentAssistCalculator("ldap-user-service", "cache-ref", beanRef);
		registerContentAssistCalculator("jdbc-user-service", "cache-ref", beanRef);
		registerContentAssistCalculator("password-encoder", "ref", beanRef);
		registerContentAssistCalculator("jdbc-user-service", "data-source-ref", beanRef);

	}
}
