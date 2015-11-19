/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.		   - initial API and implementation
 *   Spring IDE Developers
 *****************************************************************************/

package org.springframework.ide.eclipse.osgi.blueprint.internal.util;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Standard attribute callback. Deals with ID, DEPENDS-ON and LAZY-INIT
 * attribute.
 * 
 * @author Costin Leau
 * @author Arnaud Mergey
 * 
 * @since 3.7.2
 */
public class StandardAttributeCallback implements AttributeCallback {

	public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
		String name = attribute.getLocalName();

		if (BeanDefinitionParserDelegate.ID_ATTRIBUTE.equals(name)) {
			return false;
		}

		if (BeanDefinitionParserDelegate.DEPENDS_ON_ATTRIBUTE.equals(name)) {
			builder.getRawBeanDefinition().setDependsOn((StringUtils.tokenizeToStringArray(attribute.getValue(),
					BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS)));
			return false;
		}
		if (BeanDefinitionParserDelegate.LAZY_INIT_ATTRIBUTE.equals(name)) {
			builder.setLazyInit(Boolean.valueOf(attribute.getValue()));
			return false;
		}
		return true;
	}
}
