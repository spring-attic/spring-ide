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

package org.springframework.ide.eclipse.osgi.blueprint.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.ide.eclipse.osgi.blueprint.internal.jaxb.Tavailability;
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
 * 
 */
public class BlueprintReferenceListBeanDefinitionParser extends AbstractBeanDefinitionParser {

	/**
	 * Attribute callback dealing with 'cardinality' attribute.
	 * 
	 * @author Costin Leau
	 */
	class ReferenceAttributesCallback implements AttributeCallback {

		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
			String name = attribute.getLocalName();
			String value = attribute.getValue().trim();

			if (AVAILABILITY.equals(name)) {
				Tavailability avail = Tavailability.fromValue(value);
				builder.addPropertyValue(AVAILABILITY, avail);
				return false;
			}

			else if (INTERFACE.equals(name)) {
				builder.addPropertyValue(INTERFACE, value);
				return false;
			}

			return true;
		}
	};

	/**
	 * Reference attribute callback extension that looks for 'singular'
	 * reference attributes (such as timeout).
	 * 
	 * @author Costin Leau
	 */
	static class MemberTypeAttributeCallback implements AttributeCallback {

		boolean isMemberTypeSpecified = false;

		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
			String name = attribute.getLocalName();

			if (MEMBER_TYPE.equals(name)) {
				isMemberTypeSpecified = true;
			}

			return true;
		}
	}

	// Class attributes
	private static final String LISTENERS_PROP = "referenceListener";

	// XML attributes/elements
	private static final String MEMBER_TYPE = "member-type";

	private static final String REFERENCE_LISTENER = "reference-listener";

	private static final String REF = "ref";

	private static final String INTERFACE = "interface";

	private static final String AVAILABILITY = "availability";

	private BlueprintDefaultsDefinition resolveDefaults(Document document, ParserContext parserContext) {
		return new BlueprintDefaultsDefinition(document, parserContext);
	}

	private void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks,
			BlueprintDefaultsDefinition defaults) {

		// add BlueprintAttr Callback
		AttributeCallback blueprintCallback = new BlueprintReferenceAttributeCallback();
		MemberTypeAttributeCallback memberTypeCallback = new MemberTypeAttributeCallback();
		ParserUtils.parseCustomAttributes(element, builder, ParserUtils.mergeCallbacks(callbacks,
				new AttributeCallback[] { memberTypeCallback, blueprintCallback }));
	}

	private Object parsePropertySubElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return BlueprintParser.parsePropertySubElement(context, beanDef, beanDefinition);
	}

	private void doParse(Element element, ParserContext context, BeanDefinitionBuilder builder) {

		BlueprintDefaultsDefinition defaults = resolveDefaults(element.getOwnerDocument(), context);

		AttributeCallback callback = new ReferenceAttributesCallback();

		parseAttributes(element, builder, new AttributeCallback[] { callback }, defaults);

		if (!isCardinalitySpecified(builder)) {
			applyDefaultCardinality(builder, defaults);
		}

		parseNestedElements(element, context, builder);
	}

	private boolean isCardinalitySpecified(BeanDefinitionBuilder builder) {
		return (builder.getRawBeanDefinition().getPropertyValues().getPropertyValue(AVAILABILITY) != null);
	}

	private String getListenerElementName() {
		return REFERENCE_LISTENER;
	}

	private String generateBeanName(String id, BeanDefinition def, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();
		String name = ParsingUtils.BLUEPRINT_GENERATED_NAME_PREFIX + id
				+ BeanDefinitionReaderUtils.generateBeanName(def, registry);
		String generated = name;
		int counter = 0;

		while (registry.containsBeanDefinition(generated)) {
			generated = name + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + counter;
			if (parserContext.isNested()) {
				generated = generated.concat("#generated");
			}
			counter++;
		}

		return generated;
	}

	private void applyDefaults(ParserContext parserContext, BlueprintDefaultsDefinition defaults,
			BeanDefinitionBuilder builder) {
		if (parserContext.isNested()) {
			// Inner bean definition must receive same scope as containing bean.
			builder.setScope(parserContext.getContainingBeanDefinition().getScope());
		}
		if (parserContext.isDefaultLazyInit()) {
			// Default-lazy-init applies to custom bean definitions as well.
			builder.setLazyInit(true);
		}

		if (defaults.getDefaultInitialization()) {
			builder.setLazyInit(defaults.getDefaultInitialization());
		}
	}

	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {

		String id = element.getAttribute(ID_ATTRIBUTE);
		if (!StringUtils.hasText(id)) {
			id = generateBeanName("", definition, parserContext);
		}
		return id;
	}

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		builder.getRawBeanDefinition().setSynthetic(true);
		//fake factory to prevent validation error in ui
		builder.getRawBeanDefinition().setFactoryBeanName("internal");
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

		BlueprintDefaultsDefinition defaults = resolveDefaults(element.getOwnerDocument(), parserContext);
		applyDefaults(parserContext, defaults, builder);

		doParse(element, parserContext, builder);

		AbstractBeanDefinition def = builder.getBeanDefinition();

		return def;
	}

	/**
	 * Apply default cardinality.
	 * 
	 * @param builder
	 * @param defaults
	 */
	private void applyDefaultCardinality(BeanDefinitionBuilder builder, BlueprintDefaultsDefinition defaults) {
		builder.addPropertyValue(AVAILABILITY, defaults.getAvailability());
	}

	/**
	 * Parse nested elements. In case of a reference definition, this means
	 * using the listeners.
	 * 
	 * 
	 * @param element
	 * @param context
	 * @param builder
	 */
	private void parseNestedElements(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		parseListeners(element, getListenerElementName(), context, builder);
	}

	/**
	 * Parse listeners.
	 * 
	 * @param element
	 * @param context
	 * @param builder
	 */
	private void parseListeners(Element element, String subElementName, ParserContext context,
			BeanDefinitionBuilder builder) {
		List<Element> listeners = DomUtils.getChildElementsByTagName(element, subElementName);

		ManagedList listenersRef = new ManagedList();
		// loop on listeners
		for (Iterator<Element> iter = listeners.iterator(); iter.hasNext();) {
			Element listnr = iter.next();

			// wrapper target object
			Object target = null;

			// target bean name (in case of a reference)
			String targetName = null;

			// filter elements
			NodeList nl = listnr.getChildNodes();

			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					Element beanDef = (Element) node;

					// check inline ref
					if (listnr.hasAttribute(REF))
						context.getReaderContext().error(
								"nested bean declaration is not allowed if 'ref' attribute has been specified",
								beanDef);

					target = parsePropertySubElement(context, beanDef, builder.getRawBeanDefinition());

					// if this is a bean reference (nested <ref>), extract the
					// name
					if (target instanceof RuntimeBeanReference) {
						targetName = ((RuntimeBeanReference) target).getBeanName();
					}
				}
			}

			// extract bind/unbind attributes from <osgi:listener>
			// Element
			MutablePropertyValues vals = new MutablePropertyValues();

			NamedNodeMap attrs = listnr.getAttributes();
			for (int x = 0; x < attrs.getLength(); x++) {
				Attr attribute = (Attr) attrs.item(x);
				String name = attribute.getLocalName();

				// extract ref value
				if (REF.equals(name))
					targetName = attribute.getValue();
				else
					vals.addPropertyValue(Conventions.attributeNameToPropertyName(name), attribute.getValue());
			}

			// create serviceListener adapter
			RootBeanDefinition wrapperDef = new RootBeanDefinition();

			// set the target name (if we have one)
			if (targetName != null) {
				// no validation could be performed, save the name for easier
				// retrieval
				AbstractBeanDefinition bd = builder.getRawBeanDefinition();
				Collection<String> str = (Collection<String>) bd.getAttribute(ParserUtils.REFERENCE_LISTENER_REF_ATTR);
				if (str == null) {
					str = new LinkedHashSet<String>(2);
					bd.setAttribute(ParserUtils.REFERENCE_LISTENER_REF_ATTR, str);
				}
				str.add(targetName);
				vals.addPropertyValue("ref", targetName);
			}
			// else set the actual target
			else
				vals.addPropertyValue("bean", target);

			wrapperDef.setPropertyValues(vals);
			wrapperDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

			// add listener to list
			listenersRef.add(wrapperDef);
		}

		PropertyValue previousListener = builder.getRawBeanDefinition().getPropertyValues()
				.getPropertyValue(LISTENERS_PROP);

		if (previousListener != null) {
			ManagedList ml = (ManagedList) previousListener.getValue();
			listenersRef.addAll(0, ml);
		}

		builder.addPropertyValue(LISTENERS_PROP, listenersRef);
	}
}