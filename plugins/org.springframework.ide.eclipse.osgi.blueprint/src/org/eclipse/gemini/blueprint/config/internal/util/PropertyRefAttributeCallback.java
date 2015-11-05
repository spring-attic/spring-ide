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
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Convention callback that transforms "&lt;property-name&gt;-ref" attributes
 * into a bean definition that sets the give &lt;property-name&gt; to a bean
 * reference pointing to the attribute value.
 * 
 * <p/> Thus attribute "comparator-ref='bla'" will have property 'comparator'
 * pointing to bean named 'bla'.
 * 
 * @see BeanDefinitionBuilder#addPropertyReference(String, String)
 * 
 * @author Costin Leau
 */
public class PropertyRefAttributeCallback implements AttributeCallback {

	private static final String PROPERTY_REF = "-ref";


	public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
		String name = attribute.getLocalName();
		if (name.endsWith(PROPERTY_REF)) {
			String propertyName = name.substring(0, name.length() - PROPERTY_REF.length());
			builder.addPropertyReference(propertyName, attribute.getValue());
			return false;
		}
		return true;
	}
}
