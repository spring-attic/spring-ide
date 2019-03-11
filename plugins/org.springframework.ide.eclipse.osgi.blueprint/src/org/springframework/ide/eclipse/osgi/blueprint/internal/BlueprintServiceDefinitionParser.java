/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * https://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at https://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *   Spring IDE Developers
 *****************************************************************************/

package org.springframework.ide.eclipse.osgi.blueprint.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.ide.eclipse.osgi.blueprint.internal.jaxb.TautoExportModes;
import org.springframework.ide.eclipse.osgi.blueprint.internal.util.AttributeCallback;
import org.springframework.ide.eclipse.osgi.blueprint.internal.util.ParserUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Costin Leau
 * @author Arnaud Mergey
 * 
 * @since 3.7.2
 */
public class BlueprintServiceDefinitionParser extends AbstractSingleBeanDefinitionParser {
	class ServiceAttributeCallback implements AttributeCallback {

		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder bldr) {
			String name = attribute.getLocalName();

			if (INTERFACE.equals(name)) {
				bldr.addPropertyValue(INTERFACES, attribute.getValue());
				return false;
			} else if (REF.equals(name)) {
				return false;
			}

			else if (AUTOEXPORT.equals(name)) {

				bldr.addPropertyValue("autoExport", TautoExportModes.fromValue(attribute.getValue()));
				return false;
			}

			return true;
		}
	}

	// XML elements
	private static final String INTERFACES_ID = "interfaces";

	private static final String INTERFACE = "interface";

	private static final String PROPS_ID = "service-properties";

	private static final String LISTENER = "registration-listener";

	private static final String REF = "ref";

	private static final String AUTOEXPORT = "auto-export";

	private static final String INTERFACES = "interfaces";

	private static final String DISABLED = "disabled";

	@Override
	protected Class<?> getBeanClass(Element element) {
		return null;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		// first check the attributes
		if (element.hasAttribute(AUTOEXPORT) && !DISABLED.equals(element.getAttribute(AUTOEXPORT).trim())) {
			if (element.hasAttribute(INTERFACE)) {
				parserContext.getReaderContext()
						.error("either 'auto-export' or 'interface' attribute has be specified but not both", element);
			}
			if (DomUtils.getChildElementByTagName(element, INTERFACES) != null) {
				parserContext.getReaderContext().error(
						"either 'auto-export' attribute or <intefaces> sub-element has be specified but not both",
						element);

			}

		}

		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		builder.getRawBeanDefinition().setSynthetic(true);
		//fake factory to prevent validation error in ui
		builder.getRawBeanDefinition().setFactoryBeanName("internal");
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

		BlueprintDefaultsDefinition defaults = resolveDefaults(element.getOwnerDocument(), parserContext);

		AttributeCallback callback = new ServiceAttributeCallback();
		parseAttributes(element, builder, new AttributeCallback[] { callback }, defaults);

		// determine nested/referred beans
		Object target = null;
		if (element.hasAttribute(REF)) {
			target = new RuntimeBeanReference(element.getAttribute(REF));
		}

		// element is considered parent
		NodeList nl = element.getChildNodes();

		ManagedList listeners = new ManagedList();

		// parse all sub elements
		// we iterate through them since we have to 'catch' the possible nested
		// bean which has an unknown name local name

		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element subElement = (Element) node;
				String name = subElement.getLocalName();

				// osgi:interface
				if (parseInterfaces(element, subElement, parserContext, builder))
					;
				// osgi:service-properties
				else if (parseServiceProperties(element, subElement, parserContext, builder))
					;
				// osgi:registration-listener
				else if (LISTENER.equals(name)) {
					BeanDefinition listenerDef = parseListener(parserContext, subElement, builder);
					listeners.add(listenerDef);
				} else if (BeanDefinitionParserDelegate.DESCRIPTION_ELEMENT.equals(name)) {
					builder.getRawBeanDefinition().setDescription(subElement.getTextContent());
				}

				// nested bean reference/declaration
				else
					target = parseBeanReference(element, subElement, parserContext, builder);
			}
		}

		// if we have a named bean use target_bean_name (so we postpone the
		// service creation)
		if (target instanceof RuntimeBeanReference) {
			builder.addPropertyValue("ref", ((RuntimeBeanReference) target).getBeanName());
		} else {
			// add target (can be either an object instance or a bean
			// definition)
			builder.addPropertyValue("bean", target);
		}

		// add listeners
		builder.addPropertyValue("registrationListener", listeners);
	}

	// parse nested bean definition
	private Object parseBeanReference(Element parent, Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		// check shortcut on the parent
		if (parent.hasAttribute(REF))
			parserContext.getReaderContext()
					.error("nested bean definition/reference cannot be used when attribute 'ref' is specified", parent);
		return parsePropertySubElement(parserContext, element, builder.getRawBeanDefinition());
	}

	private void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks,
			BlueprintDefaultsDefinition defaults) {

		// parse attributes
		ParserUtils.parseCustomAttributes(element, builder, callbacks);
	}

	// osgi:service-properties
	private boolean parseServiceProperties(Element parent, Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		String name = element.getLocalName();

		if (PROPS_ID.equals(name)) {
			Object props = null;
			// check inlined ref
			String ref = element.getAttribute(REF).trim();

			boolean hasRef = StringUtils.hasText(ref);

			if (DomUtils.getChildElementsByTagName(element, BeanDefinitionParserDelegate.ENTRY_ELEMENT).size() > 0) {
				if (hasRef) {
					parserContext.getReaderContext().error(
							"Nested service properties definition cannot be used when attribute 'ref' is specified",
							element);
				} else {
					props = parsePropertyMapElement(parserContext, element, builder.getRawBeanDefinition());
				}
			}

			if (hasRef) {
				props = new RuntimeBeanReference(ref);
			}

			if (props != null) {
				builder.addPropertyValue(Conventions.attributeNameToPropertyName(PROPS_ID), props);
			} else {
				parserContext.getReaderContext().error("Invalid service property declaration", element);
			}
			return true;
		}
		return false;
	}

	// osgi:interfaces
	private boolean parseInterfaces(Element parent, Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		String name = element.getLocalName();

		// osgi:interfaces
		if (INTERFACES_ID.equals(name)) {
			// check shortcut on the parent
			if (parent.hasAttribute(INTERFACE)) {
				parserContext.getReaderContext()
						.error("either 'interface' attribute or <intefaces> sub-element has be specified", parent);
			}
			Set<?> interfaces = parsePropertySetElement(parserContext, element, builder.getRawBeanDefinition());
			builder.addPropertyValue(INTERFACES, interfaces);
			return true;
		}

		return false;
	}

	private Map<?, ?> parsePropertyMapElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return BlueprintParser.parsePropertyMapElement(context, beanDef, beanDefinition);
	}

	private Set<?> parsePropertySetElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return BlueprintParser.parsePropertySetElement(context, beanDef, beanDefinition);
	}

	private Object parsePropertySubElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return BlueprintParser.parsePropertySubElement(context, beanDef, beanDefinition);
	}

	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		String id = ParsingUtils.resolveId(element, definition, parserContext, shouldGenerateId(),
				shouldGenerateIdAsFallback());

		validateServiceReferences(element, id, parserContext);
		return id;
	}

	private BlueprintDefaultsDefinition resolveDefaults(Document document, ParserContext parserContext) {
		return new BlueprintDefaultsDefinition(document, parserContext);
	}

	// osgi:listener
	private BeanDefinition parseListener(ParserContext context, Element element, BeanDefinitionBuilder builder) {

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

				target = parsePropertySubElement(context, nestedDefinition, builder.getRawBeanDefinition());
				// if this is a bean reference (nested <ref>), extract the name
				if (target instanceof RuntimeBeanReference) {
					targetName = ((RuntimeBeanReference) target).getBeanName();
				}
			}
		}

		// extract registration/unregistration attributes from
		// <osgi:registration-listener>
		MutablePropertyValues vals = new MutablePropertyValues();

		NamedNodeMap attrs = element.getAttributes();
		for (int x = 0; x < attrs.getLength(); x++) {
			Attr attribute = (Attr) attrs.item(x);
			String name = attribute.getLocalName();

			if (REF.equals(name))
				targetName = attribute.getValue();
			else {
				vals.addPropertyValue(Conventions.attributeNameToPropertyName(name), attribute.getValue());
			}

		}

		// create serviceListener wrapper
		RootBeanDefinition wrapperDef = new RootBeanDefinition();

		// set the target name (if we have one)
		if (targetName != null) {
			vals.addPropertyValue("ref", targetName);
		}
		// else set the actual target
		else {
			vals.addPropertyValue("bean", target);
		}

		wrapperDef.setPropertyValues(vals);

		return wrapperDef;

	}

	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	private void validateServiceReferences(Element element, String serviceId, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();
		String[] names = registry.getBeanDefinitionNames();

		for (String name : names) {
			BeanDefinition definition = registry.getBeanDefinition(name);
			Collection<String> exporters = (Collection<String>) definition
					.getAttribute(ParserUtils.REFERENCE_LISTENER_REF_ATTR);

			if (exporters != null && exporters.contains(serviceId)) {
				parserContext.getReaderContext().error(
						"Service exporter '" + serviceId + "' cannot be used as a reference listener by '" + name + "'",
						element);
			}
		}
	}
}