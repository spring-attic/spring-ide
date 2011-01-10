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

import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;

/**
 * Parser for the <code>&lt;oxm:jibx-marshaller/&gt; element.
 * @author Christian Dupuis
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class JibxMarshallerBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final String JIBX_MARSHALLER_CLASS_NAME = "org.springframework.oxm.jibx.JibxMarshaller";

    protected String getBeanClassName(Element element) {
        return JIBX_MARSHALLER_CLASS_NAME;
    }

}