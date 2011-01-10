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

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser for the <code>&lt;oxm:jaxb2-marshaller/&gt; element.
 * @author Christian Dupuis
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class Jaxb2MarshallerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String JAXB2_MARSHALLER_CLASS_NAME = "org.springframework.oxm.jaxb.Jaxb2Marshaller";

    protected String getBeanClassName(Element element) {
        return JAXB2_MARSHALLER_CLASS_NAME;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder beanDefinitionBuilder) {
        String contextPath = element.getAttribute("contextPath");
        if (StringUtils.hasText(contextPath)) {
            beanDefinitionBuilder.addPropertyValue("contextPath", contextPath);
        }
        List classes = DomUtils.getChildElementsByTagName(element, "class-to-be-bound");
        if (!classes.isEmpty()) {
            ManagedList classesToBeBound = new ManagedList(classes.size());
            for (Iterator iterator = classes.iterator(); iterator.hasNext();) {
                Element classToBeBound = (Element) iterator.next();
                String className = classToBeBound.getAttribute("name");
                classesToBeBound.add(className);
            }
            beanDefinitionBuilder.addPropertyValue("classesToBeBound", classesToBeBound);
        }
    }

}
