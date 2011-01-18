/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.webflow.v1.config;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.webflow.engine.support.ApplicationViewSelector;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} for the <code>&lt;execution-attributes&gt;</code> tag.
 * @author Christian Dupuis
 * @author Ben Hale
 */
class ExecutionAttributesBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
	
	// elements and attributes

	private static final String ATTRIBUTE_ELEMENT = "attribute";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String TYPE_ATTRIBUTE = "type";

	private static final String VALUE_ATTRIBUTE = "value";

	// properties

	private static final String SOURCE_MAP_PROPERTY = "sourceMap";

	protected Class getBeanClass(Element element) {
		return MapFactoryBean.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder definitionBuilder) {
		List attributeElements = DomUtils.getChildElementsByTagName(element, ATTRIBUTE_ELEMENT);
		Map attributeMap = new ManagedMap(attributeElements.size());
		putAttributes(attributeMap, attributeElements);
		putSpecialAttributes(attributeMap, element);
		definitionBuilder.addPropertyValue(SOURCE_MAP_PROPERTY, attributeMap);
	}

	/**
	 * Add all attributes defined in given list of attribute elements to given map.
	 */
	@SuppressWarnings("unchecked")
	private void putAttributes(Map attributeMap, List attributeElements) {
		for (Iterator i = attributeElements.iterator(); i.hasNext();) {
			Element attributeElement = (Element)i.next();
			String type = attributeElement.getAttribute(TYPE_ATTRIBUTE);
			Object value;
			if (StringUtils.hasText(type)) {
				value = new TypedStringValue(attributeElement.getAttribute(VALUE_ATTRIBUTE), type);
			} else {
				value = attributeElement.getAttribute(VALUE_ATTRIBUTE);
			}
			attributeMap.put(attributeElement.getAttribute(NAME_ATTRIBUTE), value);
		}
	}

	/**
	 * Add all non-generic (special) attributes defined in given element
	 * to given map.
	 */
	private void putSpecialAttributes(Map attributeMap, Element element) {
		putAlwaysRedirectOnPauseAttribute(attributeMap,
				DomUtils.getChildElementByTagName(element, ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE));
	}

	/**
	 * Parse the "alwaysRedirectOnPause" attribute from given element and
	 * add it to given map.
	 */
	@SuppressWarnings("unchecked")
	private void putAlwaysRedirectOnPauseAttribute(Map attributeMap, Element element) {
		if (element != null) {
			Boolean value = Boolean.valueOf(element.getAttribute(VALUE_ATTRIBUTE));
			attributeMap.put(ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE, value);
		}
	}
}