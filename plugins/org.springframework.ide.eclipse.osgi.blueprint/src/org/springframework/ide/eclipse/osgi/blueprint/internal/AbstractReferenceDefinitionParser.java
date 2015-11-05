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
import java.util.Set;

import org.eclipse.gemini.blueprint.config.internal.OsgiDefaultsDefinition;
import org.eclipse.gemini.blueprint.config.internal.adapter.OsgiServiceLifecycleListenerAdapter;
import org.eclipse.gemini.blueprint.config.internal.util.AttributeCallback;
import org.eclipse.gemini.blueprint.config.internal.util.ParserUtils;
import org.eclipse.gemini.blueprint.config.internal.util.ReferenceParsingUtil;
import org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.eclipse.gemini.blueprint.util.BeanReferenceFactoryBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class for parsing reference declarations. Contains common functionality such as adding listeners (and their
 * custom methods), interfaces, cardinality and so on.
 * 
 * <p/>
 * 
 * <strong>Note:</strong> This parser also handles the cyclic injection between an importer and its listeners by
 * breaking the chain by creating an adapter instead of the listener. The adapter will then do dependency lookup for the
 * listener.
 * 
 * @author Costin Leau
 * @author Arnaud Mergey
 * 
 * @since 3.7.2
 * 
 */
public abstract class AbstractReferenceDefinitionParser extends AbstractBeanDefinitionParser {

	/**
	 * Attribute callback dealing with 'cardinality' attribute.
	 * 
	 * @author Costin Leau
	 */
	class ReferenceAttributesCallback implements AttributeCallback {

		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
			String name = attribute.getLocalName();
			String value = attribute.getValue().trim();

			if (CARDINALITY.equals(name)) {
				builder.addPropertyValue(AVAILABILITY_PROP, ReferenceParsingUtil
						.determineAvailabilityFromCardinality(value));
				return false;
			}

			if (AVAILABILITY.equals(name)) {
				Availability avail = ReferenceParsingUtil.determineAvailability(value);
				builder.addPropertyValue(AVAILABILITY_PROP, avail);
				return false;
			}

			else if (SERVICE_BEAN_NAME.equals(name)) {
				builder.addPropertyValue(SERVICE_BEAN_NAME_PROP, value);
				return false;
			}

			else if (INTERFACE.equals(name)) {
				builder.addPropertyValue(INTERFACES_PROP, value);
				return false;
			}

			return true;
		}
	};

	// Class properties
	private static final String LISTENERS_PROP = "listeners";

	private static final String AVAILABILITY_PROP = "availability";

	private static final String SERVICE_BEAN_NAME_PROP = "serviceBeanName";

	private static final String INTERFACES_PROP = "interfaces";

	private static final String TARGET_BEAN_NAME_PROP = "targetBeanName";

	private static final String TARGET_PROP = "target";

	// XML attributes/elements

	private static final String REFERENCE_LISTENER = "reference-listener";

	private static final String REF = "ref";

	private static final String INTERFACE = "interface";

	private static final String INTERFACES = "interfaces";

	private static final String AVAILABILITY = "availability";

	private static final String CARDINALITY = "cardinality";

	private static final String SERVICE_BEAN_NAME = "bean-name";

	public static final String GENERATED_REF = "org.eclipse.gemini.blueprint.config.reference.generated";
	public static final String PROMOTED_REF = "org.eclipse.gemini.blueprint.config.reference.promoted";

	/**
	 * Get OSGi defaults (in case they haven't been resolved).
	 * 
	 * @param document
     * @param parserContext
	 * @return
	 */
	protected OsgiDefaultsDefinition resolveDefaults(Document document, ParserContext parserContext) {
		return new OsgiDefaultsDefinition(document, parserContext);
	}

	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

		Class<?> beanClass = getBeanClass(element);
		Assert.notNull(beanClass);

		if (beanClass != null) {
			builder.getRawBeanDefinition().setBeanClass(beanClass);
		}

		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

		OsgiDefaultsDefinition defaults = resolveDefaults(element.getOwnerDocument(), parserContext);
		applyDefaults(parserContext, defaults, builder);

		doParse(element, parserContext, builder);

		AbstractBeanDefinition def = builder.getBeanDefinition();

		// check whether the bean is mandatory (and if it is, make it top-level
		// bean)
		if (parserContext.isNested()) {
			String value = element.getAttribute(AbstractBeanDefinitionParser.ID_ATTRIBUTE);
			value = (StringUtils.hasText(value) ? value + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR : "");
			String generatedName = generateBeanName(value, def, parserContext);
			// make the bean lazy (since it is an inner bean initially)
			def.setLazyInit(true);
			// disable autowiring for promoted bean
			def.setAutowireCandidate(false);
			def.setAttribute(PROMOTED_REF, Boolean.TRUE);

			BeanDefinitionHolder holder = new BeanDefinitionHolder(def, generatedName);
			BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
			return createBeanReferenceDefinition(generatedName, def);
		}

		return def;
	}

	protected void applyDefaults(ParserContext parserContext, OsgiDefaultsDefinition defaults,
			BeanDefinitionBuilder builder) {
		if (parserContext.isNested()) {
			// Inner bean definition must receive same scope as containing bean.
			builder.setScope(parserContext.getContainingBeanDefinition().getScope());
		}
		if (parserContext.isDefaultLazyInit()) {
			// Default-lazy-init applies to custom bean definitions as well.
			builder.setLazyInit(true);
		}
	}

	private AbstractBeanDefinition createBeanReferenceDefinition(String beanName, BeanDefinition actualDef) {
		GenericBeanDefinition def = new GenericBeanDefinition();
		def.setBeanClass(BeanReferenceFactoryBean.class);
		def.setAttribute(GENERATED_REF, Boolean.TRUE);
		def.setOriginatingBeanDefinition(actualDef);
		def.setDependsOn(new String[] { beanName });
		def.setSynthetic(true);
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue(TARGET_BEAN_NAME_PROP, beanName);
		def.setPropertyValues(mpv);
		return def;
	}

	protected void doParse(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		ReferenceParsingUtil.checkAvailabilityAndCardinalityDuplication(element, AVAILABILITY, CARDINALITY, context);

		OsgiDefaultsDefinition defaults = resolveDefaults(element.getOwnerDocument(), context);

		AttributeCallback callback = new ReferenceAttributesCallback();

		parseAttributes(element, builder, new AttributeCallback[] { callback }, defaults);

		if (!isCardinalitySpecified(builder)) {
			applyDefaultCardinality(builder, defaults);
		}

		parseNestedElements(element, context, builder);
		handleNestedDefinition(element, context, builder);
	}

	private boolean isCardinalitySpecified(BeanDefinitionBuilder builder) {
		return (builder.getBeanDefinition().getPropertyValues().getPropertyValue(AVAILABILITY_PROP) != null);
	}

	/**
	 * If the reference is a nested bean, make it a top-level bean if it's a mandatory dependency. This is done so that
	 * the beans can be discovered at startup and the appCtx can start waiting.
	 * 
	 * @param element
	 * @param context
	 * @param builder
	 */
	protected void handleNestedDefinition(Element element, ParserContext context, BeanDefinitionBuilder builder) {

	}

	/**
	 * Allow subclasses to add their own callbacks.
	 * 
	 * @param element
	 * @param builder
	 * @param callbacks
	 */
	protected void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks,
			OsgiDefaultsDefinition defaults) {
		ParserUtils.parseCustomAttributes(element, builder, callbacks);
	}

	/**
	 * Indicate the bean definition class for this element.
	 * 
	 * @param element
	 * @return
	 */
	protected abstract Class getBeanClass(Element element);

	/**
	 * Apply default cardinality.
	 * 
	 * @param builder
	 * @param defaults
	 */
	protected void applyDefaultCardinality(BeanDefinitionBuilder builder, OsgiDefaultsDefinition defaults) {
		builder.addPropertyValue(AVAILABILITY_PROP, defaults.getAvailability());
	}

	/**
	 * Parse nested elements. In case of a reference definition, this means using the listeners.
	 * 
	 * 
	 * @param element
	 * @param context
	 * @param builder
	 */
	protected void parseNestedElements(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		parseInterfaces(element, context, builder);
		parseListeners(element, getListenerElementName(), context, builder);
	}

	protected String getListenerElementName() {
		return REFERENCE_LISTENER;
	}

	/**
	 * Parse interfaces.
	 * 
	 * @param element
	 * @param context
	 * @param builder
	 */
	protected void parseInterfaces(Element parent, ParserContext parserContext, BeanDefinitionBuilder builder) {

		Element element = DomUtils.getChildElementByTagName(parent, INTERFACES);
		if (element != null) {
			// check shortcut on the parent
			if (parent.hasAttribute(INTERFACE)) {
				parserContext.getReaderContext().error(
						"either 'interface' attribute or <intefaces> sub-element has be specified", parent);
			}
			Set interfaces = parsePropertySetElement(parserContext, element, builder.getBeanDefinition());
			builder.addPropertyValue(INTERFACES_PROP, interfaces);
		}
	}

	/**
	 * Parse listeners.
	 * 
	 * @param element
	 * @param context
	 * @param builder
	 */
	protected void parseListeners(Element element, String subElementName, ParserContext context,
			BeanDefinitionBuilder builder) {
		List listeners = DomUtils.getChildElementsByTagName(element, subElementName);

		ManagedList listenersRef = new ManagedList();
		// loop on listeners
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			Element listnr = (Element) iter.next();

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
						context.getReaderContext()
								.error("nested bean declaration is not allowed if 'ref' attribute has been specified",
										beanDef);

					target = parsePropertySubElement(context, beanDef, builder.getBeanDefinition());

					// if this is a bean reference (nested <ref>), extract the name
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
			RootBeanDefinition wrapperDef = new RootBeanDefinition(OsgiServiceLifecycleListenerAdapter.class);

			// set the target name (if we have one)
			if (targetName != null) {
				// do some validation
				BeanDefinitionRegistry registry = context.getRegistry();
				if (registry.containsBeanDefinition(targetName)) {
					BeanDefinition beanDefinition = registry.getBeanDefinition(targetName);
					if (beanDefinition.getBeanClassName().equals(OsgiServiceFactoryBean.class.getName())) {
						context.getReaderContext()
								.error("service exporter '" + targetName + "' cannot be used as a reference listener",
										element);
					}
				}
				// no validation could be performed, save the name for easier retrieval
				AbstractBeanDefinition bd = builder.getBeanDefinition();
				Collection<String> str = (Collection<String>) bd.getAttribute(ParserUtils.REFERENCE_LISTENER_REF_ATTR);
				if (str == null) {
					str = new LinkedHashSet<String>(2);
					bd.setAttribute(ParserUtils.REFERENCE_LISTENER_REF_ATTR, str);
				}
				str.add(targetName);
				vals.addPropertyValue(TARGET_BEAN_NAME_PROP, targetName);
			}
			// else set the actual target
			else
				vals.addPropertyValue(TARGET_PROP, target);

			wrapperDef.setPropertyValues(vals);
			wrapperDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

			postProcessListenerDefinition(wrapperDef);

			// add listener to list
			listenersRef.add(wrapperDef);
		}

		PropertyValue previousListener =
				builder.getRawBeanDefinition().getPropertyValues().getPropertyValue(LISTENERS_PROP);

		if (previousListener != null) {
			ManagedList ml = (ManagedList) previousListener.getValue();
			listenersRef.addAll(0, ml);
		}
		
		builder.addPropertyValue(LISTENERS_PROP, listenersRef);
	}

	protected void postProcessListenerDefinition(BeanDefinition wrapperDef) {
	}

	protected Object parsePropertySubElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return context.getDelegate().parsePropertySubElement(beanDef, beanDefinition);
	}

	protected Set parsePropertySetElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return context.getDelegate().parseSetElement(beanDef, beanDefinition);
	}

	protected String generateBeanName(String prefix, BeanDefinition def, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();
		String name = prefix + BeanDefinitionReaderUtils.generateBeanName(def, registry);
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
}