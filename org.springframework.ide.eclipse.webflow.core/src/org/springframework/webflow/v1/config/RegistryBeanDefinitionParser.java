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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.webflow.engine.builder.xml.XmlFlowRegistryFactoryBean;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} for the <code>&lt;registry&gt;</code> tag.
 * @author Christian Dupuis
 * @author Ben Hale
 */
class RegistryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
	
	// elements and attributes

	private static final String LOCATION_ELEMENT = "location";

	// properties

	private static final String FLOW_LOCATIONS_PROPERTY = "flowLocations";

	private static final String PATH_ATTRIBUTE = "path";

	protected Class getBeanClass(Element element) {
		return XmlFlowRegistryFactoryBean.class;
	}

	@SuppressWarnings("unchecked")
	protected void doParse(Element element, BeanDefinitionBuilder definitionBuilder) {
		List locationElements = DomUtils.getChildElementsByTagName(element, LOCATION_ELEMENT);
		List locations = getLocations(locationElements);
		definitionBuilder.addPropertyValue(FLOW_LOCATIONS_PROPERTY, locations.toArray(new String[locations.size()]));
	}

	/**
	 * Parse location definitions from given list of location elements.
	 */
	@SuppressWarnings("unchecked")
	private List getLocations(List locationElements) {
		List locations = new ArrayList(locationElements.size());
		for (Iterator i = locationElements.iterator(); i.hasNext();) {
			Element locationElement = (Element)i.next();
			String path = locationElement.getAttribute(PATH_ATTRIBUTE);
			if (StringUtils.hasText(path)) {
				locations.add(path);
			}
		}
		return locations;
	}
}