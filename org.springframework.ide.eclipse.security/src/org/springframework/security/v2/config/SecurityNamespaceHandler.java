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
package org.springframework.security.v2.config;

import java.lang.reflect.Constructor;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.ide.eclipse.security.Activator;
import org.springframework.security.config.AuthenticationManagerBeanDefinitionParser;
import org.springframework.security.config.CustomAfterInvocationProviderBeanDefinitionDecorator;
import org.springframework.security.config.CustomAuthenticationProviderBeanDefinitionDecorator;
import org.springframework.security.config.FilterInvocationDefinitionSourceBeanDefinitionParser;
import org.springframework.security.config.HttpSecurityBeanDefinitionParser;
import org.springframework.security.config.InterceptMethodsBeanDefinitionDecorator;
import org.springframework.security.config.JdbcUserServiceBeanDefinitionParser;
import org.springframework.security.config.LdapProviderBeanDefinitionParser;
import org.springframework.security.config.LdapServerBeanDefinitionParser;
import org.springframework.security.config.LdapUserServiceBeanDefinitionParser;
import org.springframework.security.config.OrderedFilterBeanDefinitionDecorator;
import org.springframework.security.config.UserServiceBeanDefinitionParser;

/**
 * Registers the bean definition parsers for the "security" namespace (http://www.springframework.org/schema/security).
 * @author Luke Taylor
 * @author Ben Alex
 * @since 2.0
 */
public class SecurityNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		// Parsers
		registerBeanDefinitionParser(Elements.LDAP_PROVIDER, new LdapProviderBeanDefinitionParser());
		registerBeanDefinitionParser(Elements.LDAP_SERVER, new LdapServerBeanDefinitionParser());
		registerBeanDefinitionParser(Elements.LDAP_USER_SERVICE, new LdapUserServiceBeanDefinitionParser());
		registerBeanDefinitionParser(Elements.HTTP, new HttpSecurityBeanDefinitionParser());
		registerBeanDefinitionParser(Elements.USER_SERVICE, new UserServiceBeanDefinitionParser());
		registerBeanDefinitionParser(Elements.JDBC_USER_SERVICE, new JdbcUserServiceBeanDefinitionParser());

		registerBeanDefinitionParser(Elements.AUTHENTICATION_PROVIDER,
				(BeanDefinitionParser) loadParser("org.springframework.security.config.AuthenticationProviderBeanDefinitionParser"));
		registerBeanDefinitionParser(Elements.GLOBAL_METHOD_SECURITY,
				(BeanDefinitionParser) loadParser("org.springframework.security.config.GlobalMethodSecurityBeanDefinitionParser"));
		registerBeanDefinitionParser(Elements.AUTHENTICATION_MANAGER, new AuthenticationManagerBeanDefinitionParser());
		registerBeanDefinitionParser(Elements.FILTER_INVOCATION_DEFINITION_SOURCE,
				new FilterInvocationDefinitionSourceBeanDefinitionParser());

		// Decorators
		registerBeanDefinitionDecorator(Elements.INTERCEPT_METHODS, new InterceptMethodsBeanDefinitionDecorator());
		registerBeanDefinitionDecorator(Elements.FILTER_CHAIN_MAP,
				(BeanDefinitionDecorator) loadParser("org.springframework.security.config.FilterChainMapBeanDefinitionDecorator"));
		registerBeanDefinitionDecorator(Elements.CUSTOM_FILTER, new OrderedFilterBeanDefinitionDecorator());
		registerBeanDefinitionDecorator(Elements.CUSTOM_AUTH_PROVIDER,
				new CustomAuthenticationProviderBeanDefinitionDecorator());
		registerBeanDefinitionDecorator(Elements.CUSTOM_AFTER_INVOCATION_PROVIDER,
				new CustomAfterInvocationProviderBeanDefinitionDecorator());
	}

	private Object loadParser(String className) {
		try {
			Class<?> parserClass = Activator.getDefault().getBundle().loadClass(className);
			Constructor<?> ctor = parserClass.getDeclaredConstructor();
			return BeanUtils.instantiateClass(ctor);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
