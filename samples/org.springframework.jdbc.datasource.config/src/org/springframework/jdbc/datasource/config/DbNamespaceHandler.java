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
package org.springframework.jdbc.datasource.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Christian Dupuis
 */
public class DbNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("driver-manager-data-source", new DriverManagerDataSourceBeanDefinitionParser());
	}
}
