/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.config.internal;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.gemini.blueprint.config.internal.adapter.OsgiServiceRegistrationListenerAdapter;
import org.eclipse.gemini.blueprint.config.internal.util.AttributeCallback;
import org.eclipse.gemini.blueprint.config.internal.util.ParserUtils;
import org.eclipse.gemini.blueprint.service.exporter.support.DefaultInterfaceDetector;
import org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean;
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
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * BeanDefinitionParser for service element found in the osgi namespace.
 * 
 * @author Costin Leau
 * @author Hal Hildebrand
 * @author Andy Piper
 */
public class ServiceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	class ServiceAttributeCallback implements AttributeCallback {

		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder bldr) {
			String name = attribute.getLocalName();

			if (INTERFACE.equals(name)) {
				bldr.addPropertyValue(INTERFACES_PROP, attribute.getValue());
				return false;
			} else if (REF.equals(name)) {
				return false;
			}

			else if (AUTOEXPORT.equals(name)) {
				// convert constant to upper case to let Spring do the
				// conversion
				String label = attribute.getValue().toUpperCase(Locale.ENGLISH).replace('-', '_');
				bldr.addPropertyValue(AUTOEXPORT_PROP, Enum.valueOf(DefaultInterfaceDetector.class, label));
				return false;
			}

			else if (CONTEXT_CLASSLOADER.equals(name)) {
				// convert constant to upper case to let Spring do the
				// conversion

				String value = attribute.getValue().toUpperCase(Locale.ENGLISH).replace('-', '_');
				bldr.addPropertyValue(CCL_PROP, value);
				return false;
			}

			return true;
		}
	}

	// bean properties
	private static final String TARGET_BEAN_NAME_PROP = "targetBeanName";

	private static final String TARGET_PROP = "target";

	private static final String LISTENERS_PROP = "listeners";

	private static final String INTERFACES_PROP = "interfaces";

	private static final String AUTOEXPORT_PROP = "interfaceDetector";

	private static final String CCL_PROP = "exportContextClassLoader";
	// XML elements
	private static final String INTERFACES_ID = "interfaces";

	private static final String INTERFACE = "interface";

	private static final String PROPS_ID = "service-properties";

	private static final String LISTENER = "registration-listener";

	private static final String REF = "ref";

	private static final String AUTOEXPORT = "auto-export";

	private static final String CONTEXT_CLASSLOADER = "context-class-loader";

	protected Class getBeanClass(Element element) {
		return OsgiServiceFactoryBean.class;
	}

	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

		OsgiDefaultsDefinition defaults = resolveDefaults(element.getOwnerDocument(), parserContext);

		applyDefaults(parserContext, defaults, builder);

		AttributeCallback callback = new ServiceAttributeCallback();
		parseAttributes(element, builder, new AttributeCallback[] { callback }, defaults);

		// determine nested/referred beans
		Object target = null;
		if (element.hasAttribute(REF))
			target = new RuntimeBeanReference(element.getAttribute(REF));

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
					postProcessListenerDefinition(listenerDef);
					listeners.add(listenerDef);
				} else if (BeanDefinitionParserDelegate.DESCRIPTION_ELEMENT.equals(name)) {
					builder.getRawBeanDefinition().setDescription(subElement.getTextContent());
				}

				// nested bean reference/declaration
				else
					target = parseBeanReference(element, subElement, parserContext, builder);
			}
		}

		// if we have a named bean use target_bean_name (so we postpone the service creation)
		if (target instanceof RuntimeBeanReference) {
			builder.addPropertyValue(TARGET_BEAN_NAME_PROP, ((RuntimeBeanReference) target).getBeanName());
		} else {
			// add target (can be either an object instance or a bean
			// definition)
			builder.addPropertyValue(TARGET_PROP, target);
		}

		// add listeners
		builder.addPropertyValue(LISTENERS_PROP, listeners);
	}

	protected void applyDefaults(ParserContext parserContext, OsgiDefaultsDefinition defaults,
			BeanDefinitionBuilder builder) {
		if (parserContext.isDefaultLazyInit()) {
			// Default-lazy-init applies to custom bean definitions as well.
			builder.setLazyInit(true);
		}
	}

	protected void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks,
			OsgiDefaultsDefinition defaults) {

		// parse attributes
		ParserUtils.parseCustomAttributes(element, builder, callbacks);
	}

	/**
	 * Get OSGi defaults (in case they haven't been resolved).
	 * 
	 * @param document
	 * @return
	 */
	protected OsgiDefaultsDefinition resolveDefaults(Document document, ParserContext parserContext) {
		return new OsgiDefaultsDefinition(document, parserContext);
	}

	protected void postProcessListenerDefinition(BeanDefinition wrapperDef) {
	}

	// osgi:interfaces
	private boolean parseInterfaces(Element parent, Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		String name = element.getLocalName();

		// osgi:interfaces
		if (INTERFACES_ID.equals(name)) {
			// check shortcut on the parent
			if (parent.hasAttribute(INTERFACE)) {
				parserContext.getReaderContext().error(
						"either 'interface' attribute or <intefaces> sub-element has be specified", parent);
			}
			Set interfaces = parsePropertySetElement(parserContext, element, builder.getBeanDefinition());
			builder.addPropertyValue(INTERFACES_PROP, interfaces);
			return true;
		}

		return false;
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

	// parse nested bean definition
	private Object parseBeanReference(Element parent, Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		// check shortcut on the parent
		if (parent.hasAttribute(REF))
			parserContext.getReaderContext().error(
					"nested bean definition/reference cannot be used when attribute 'ref' is specified", parent);
		return parsePropertySubElement(parserContext, element, builder.getBeanDefinition());
	}

	// osgi:listener
	protected BeanDefinition parseListener(ParserContext context, Element element, BeanDefinitionBuilder builder) {

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

				target = parsePropertySubElement(context, nestedDefinition, builder.getBeanDefinition());
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
		RootBeanDefinition wrapperDef = new RootBeanDefinition(OsgiServiceRegistrationListenerAdapter.class);

		// set the target name (if we have one)
		if (targetName != null) {
			vals.addPropertyValue(TARGET_BEAN_NAME_PROP, targetName);
		}
		// else set the actual target
		else {
			vals.addPropertyValue(TARGET_PROP, target);
		}

		wrapperDef.setPropertyValues(vals);

		return wrapperDef;

	}

	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	protected Object parsePropertySubElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return context.getDelegate().parsePropertySubElement(beanDef, beanDefinition);
	}

	protected Set parsePropertySetElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return context.getDelegate().parseSetElement(beanDef, beanDefinition);
	}

	protected Map parsePropertyMapElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return context.getDelegate().parseMapElement(beanDef, beanDefinition);
	}

	protected void validateServiceReferences(Element element, String serviceId, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();
		String[] names = registry.getBeanDefinitionNames();

		for (String name : names) {
			BeanDefinition definition = registry.getBeanDefinition(name);
			Collection<String> exporters =
					(Collection<String>) definition.getAttribute(ParserUtils.REFERENCE_LISTENER_REF_ATTR);

			if (exporters != null && exporters.contains(serviceId)) {
				parserContext.getReaderContext()
						.error(
								"Service exporter '" + serviceId + "' cannot be used as a reference listener by '"
										+ name + "'", element);
			}
		}
	}

	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {

		String id = super.resolveId(element, definition, parserContext);
		validateServiceReferences(element, id, parserContext);
		return id;
	}
}