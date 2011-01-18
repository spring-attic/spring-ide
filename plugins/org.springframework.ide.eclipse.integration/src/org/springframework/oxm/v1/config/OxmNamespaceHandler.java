/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/

package org.springframework.oxm.v1.config;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@link NamespaceHandler} for the '<code>oxm</code>' namespace.
 * @author Christian Dupuis
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class OxmNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("jaxb1-marshaller", new Jaxb1MarshallerBeanDefinitionParser());
        registerBeanDefinitionParser("jaxb2-marshaller", new Jaxb2MarshallerBeanDefinitionParser());
        registerBeanDefinitionParser("jibx-marshaller", new JibxMarshallerBeanDefinitionParser());
        registerBeanDefinitionParser("xmlbeans-marshaller", new XmlBeansMarshallerBeanDefinitionParser());
    }
}
