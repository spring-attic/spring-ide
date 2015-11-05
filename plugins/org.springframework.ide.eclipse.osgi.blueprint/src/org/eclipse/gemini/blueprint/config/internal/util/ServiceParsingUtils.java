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

import java.util.Set;

import org.eclipse.gemini.blueprint.config.internal.adapter.OsgiServiceRegistrationListenerAdapter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utils class for parsing various elements of a service declaration.
 * 
 * @author Costin Leau
 */
public abstract class ServiceParsingUtils {

	private static final String REF = "ref";

	private static final String TARGET_BEAN_NAME_PROP = "targetBeanName";
	private static final String TARGET_PROP = "target";
	private static final String INTERFACE = "interface";
	private static final String INTERFACES_PROP = "interfaces";
	private static final String INTERFACES_ID = "interfaces";
	private static final String PROPS_ID = "service-properties";

	public static BeanDefinition parseListener(ParserContext context, Element element, BeanDefinitionBuilder builder) {

		// filter elements
		NodeList nl = element.getChildNodes();

		// wrapped object
		Object target = null;
		// target bean name (used for cycles)
		String targetName = null;

		// discover if we have listener with ref and nested bean declaration
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element nestedDefinition = (Element) node;
				// check shortcut on the parent
				if (element.hasAttribute(REF))
					context.getReaderContext().error(
						"nested bean declaration is not allowed if 'ref' attribute has been specified",
						nestedDefinition);

				target = context.getDelegate().parsePropertySubElement(nestedDefinition, builder.getBeanDefinition());
				// if this is a bean reference (nested <ref>), extract the name
				if (target instanceof RuntimeBeanReference) {
					targetName = ((RuntimeBeanReference) target).getBeanName();
				}
			}
		}

		// extract registration/unregistration attributes from
		// <osgi:registration-listener>
		BeanDefinitionBuilder localBuilder = BeanDefinitionBuilder.rootBeanDefinition(OsgiServiceRegistrationListenerAdapter.class);
		localBuilder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		
		NamedNodeMap attrs = element.getAttributes();
		for (int x = 0; x < attrs.getLength(); x++) {
			Attr attribute = (Attr) attrs.item(x);
			String name = attribute.getLocalName();

			if (REF.equals(name))
				targetName = attribute.getValue();
			else
				localBuilder.addPropertyValue(Conventions.attributeNameToPropertyName(name), attribute.getValue());
		}

		// set the target name (if we have one)
		if (targetName != null)
			localBuilder.addPropertyValue(TARGET_BEAN_NAME_PROP, targetName);
		// else set the actual target
		else
			localBuilder.addPropertyValue(TARGET_PROP, target);

		return localBuilder.getBeanDefinition();
	}

	public static boolean parseInterfaces(Element parent, Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		String name = element.getLocalName();

		// osgi:interfaces
		if (INTERFACES_ID.equals(name)) {
			// check shortcut on the parent
			if (parent.hasAttribute(INTERFACE)) {
				parserContext.getReaderContext().error(
					"either 'interface' attribute or <intefaces> sub-element has be specified", parent);
			}
			Set interfaces = parserContext.getDelegate().parseSetElement(element, builder.getBeanDefinition());
			builder.addPropertyValue(INTERFACES_PROP, interfaces);
			return true;
		}

		return false;
	}

	// osgi:service-properties
	public static boolean parseServiceProperties(Element parent, Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		String name = element.getLocalName();

		if (PROPS_ID.equals(name)) {
			if (DomUtils.getChildElementsByTagName(element, BeanDefinitionParserDelegate.ENTRY_ELEMENT).size() > 0) {
				Object props = parserContext.getDelegate().parseMapElement(element, builder.getRawBeanDefinition());
				builder.addPropertyValue(Conventions.attributeNameToPropertyName(PROPS_ID), props);
			}
			else {
				parserContext.getReaderContext().error("Invalid service property type", element);
			}
			return true;
		}
		return false;
	}
}
