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
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.config.internal.util;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.Conventions;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Callback relying on 'Spring' conventions. Normally this is the last callback
 * in the stack trying to convert the property name and then setting it on the
 * builder.
 * 
 * @see Conventions#attributeNameToPropertyName(String)
 * @see BeanDefinitionBuilder#addPropertyValue(String, Object)
 * 
 * @author Costin Leau
 */
public class ConventionsCallback implements AttributeCallback {

	public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
		String name = attribute.getLocalName();
		String propertyName = Conventions.attributeNameToPropertyName(name);
		builder.addPropertyValue(propertyName, attribute.getValue());
		return true;
	}
}
