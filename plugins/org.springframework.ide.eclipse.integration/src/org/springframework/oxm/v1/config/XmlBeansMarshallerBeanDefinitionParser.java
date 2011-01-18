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

import org.w3c.dom.Element;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;

/**
 * Parser for the <code>&lt;oxm:xmlbeans-marshaller/&gt; element.
 * @author Christian Dupuis
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class XmlBeansMarshallerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public static final String XML_BEANS_MARSHALLER_CLASS_NAME = "org.springframework.oxm.xmlbeans.XmlBeansMarshaller";

    protected String getBeanClassName(Element element) {
        return XML_BEANS_MARSHALLER_CLASS_NAME;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder beanDefinitionBuilder) {
        String optionsName = element.getAttribute("options");
        if (StringUtils.hasText(optionsName)) {
            beanDefinitionBuilder.addPropertyReference("xmlOptions", optionsName);
        }
    }
}