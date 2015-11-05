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

package org.eclipse.gemini.blueprint.config.internal;

import java.util.Comparator;

import org.eclipse.gemini.blueprint.config.internal.util.AttributeCallback;
import org.eclipse.gemini.blueprint.config.internal.util.ParserUtils;
import org.eclipse.gemini.blueprint.service.importer.support.CollectionType;
import org.eclipse.gemini.blueprint.service.importer.support.MemberType;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.internal.util.ServiceReferenceComparator;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ide.eclipse.osgi.blueprint.internal.AbstractReferenceDefinitionParser;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * &lt;osgi:list&gt;, &lt;osgi:set&gt;, element parser.
 * 
 * @author Costin Leau
 * 
 */
public abstract class CollectionBeanDefinitionParser extends AbstractReferenceDefinitionParser {

	/**
	 * Greedy proxy attribute callback.
	 * 
	 * @author Costin Leau
	 */
	static class CollectionAttributeCallback implements AttributeCallback {

		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
			String name = attribute.getLocalName();
			if (MEMBER_TYPE.equals(name)) {
				builder.addPropertyValue(MEMBER_TYPE_PROPERTY, MemberType.valueOf(attribute.getValue().toUpperCase()
						.replace('-', '_')));
				return false;
			}
			return true;
		}
	}

	private static final String NESTED_COMPARATOR = "comparator";

	private static final String INLINE_COMPARATOR_REF = "comparator-ref";

	private static final String COLLECTION_TYPE_PROP = "collectionType";

	private static final String COMPARATOR_PROPERTY = "comparator";

	private static final String SERVICE_ORDER = "service";

	private static final String SERVICE_REFERENCE_ORDER = "service-reference";

	private static final String MEMBER_TYPE = "member-type";

	private static final String MEMBER_TYPE_PROPERTY = "memberType";

	private static final Comparator SERVICE_REFERENCE_COMPARATOR = new ServiceReferenceComparator();

	private static final String NATURAL = "natural";

	private static final String BASIS = "basis";

	protected Class getBeanClass(Element element) {
		return OsgiServiceCollectionProxyFactoryBean.class;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Add support for 'greedy-proxying' attribute.
	 */
	protected void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks,
			OsgiDefaultsDefinition defaults) {
		// add timeout callback
		CollectionAttributeCallback greedyProxyingCallback = new CollectionAttributeCallback();
		super.parseAttributes(element, builder, ParserUtils.mergeCallbacks(callbacks,
				new AttributeCallback[] { greedyProxyingCallback }), defaults);
	}

	protected void parseNestedElements(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		super.parseNestedElements(element, context, builder);
		parseComparator(element, context, builder);
	}

	/**
	 * Parse &lt;comparator&gt; element.
	 * 
	 * @param element
	 * @param context
	 * @param builder
	 */
	protected void parseComparator(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		boolean hasComparatorRef = element.hasAttribute(INLINE_COMPARATOR_REF);

		// check nested comparator
		Element comparatorElement = DomUtils.getChildElementByTagName(element, NESTED_COMPARATOR);

		Object nestedComparator = null;

		// comparator definition present
		if (comparatorElement != null) {
			// check duplicate nested and inline bean definition
			if (hasComparatorRef)
				context.getReaderContext().error(
						"nested comparator declaration is not allowed if " + INLINE_COMPARATOR_REF
								+ " attribute has been specified", comparatorElement);

			NodeList nl = comparatorElement.getChildNodes();

			// take only elements
			for (int i = 0; i < nl.getLength(); i++) {
				Node nd = nl.item(i);
				if (nd instanceof Element) {
					Element beanDef = (Element) nd;
					String name = beanDef.getLocalName();
					// check if we have a 'natural' tag (known comparator
					// definitions)
					if (NATURAL.equals(name))
						nestedComparator = parseNaturalComparator(beanDef);
					else
						// we have a nested definition
						nestedComparator = parsePropertySubElement(context, beanDef, builder.getBeanDefinition());
				}
			}

			// set the reference to the nested comparator reference
			if (nestedComparator != null)
				builder.addPropertyValue(COMPARATOR_PROPERTY, nestedComparator);
		}

		// set collection type
		// based on the existence of the comparator
		// we treat the case where the comparator is natural which means the
		// comparator
		// instance is null however, we have to force a sorted collection to be
		// used
		// so that the object natural ordering is used.

		if (comparatorElement != null || hasComparatorRef) {
			if (CollectionType.LIST.equals(collectionType())) {
				builder.addPropertyValue(COLLECTION_TYPE_PROP, CollectionType.SORTED_LIST);
			}

			if (CollectionType.SET.equals(collectionType())) {
				builder.addPropertyValue(COLLECTION_TYPE_PROP, CollectionType.SORTED_SET);
			}
		} else {
			builder.addPropertyValue(COLLECTION_TYPE_PROP, collectionType());
		}
	}

	/**
	 * Parse &lt;osgi:natural&gt; element.
	 * 
	 * @param element
	 * @return
	 */
	protected Comparator parseNaturalComparator(Element element) {
		Comparator comparator = null;
		NamedNodeMap attributes = element.getAttributes();
		for (int x = 0; x < attributes.getLength(); x++) {
			Attr attribute = (Attr) attributes.item(x);
			String name = attribute.getLocalName();
			String value = attribute.getValue();

			if (BASIS.equals(name)) {

				if (SERVICE_REFERENCE_ORDER.equals(value))
					return SERVICE_REFERENCE_COMPARATOR;

				// no comparator means relying on Comparable interface of the
				// services
				else if (SERVICE_ORDER.equals(value))
					return null;
			}

		}

		return comparator;
	}

	/**
	 * Hook used for indicating the main collection type (set/list) on which this parser applies.
	 * 
	 * @return service collection type
	 */
	protected abstract CollectionType collectionType();
}