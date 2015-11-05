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

package org.springframework.ide.eclipse.osgi.blueprint;

import org.eclipse.gemini.blueprint.blueprint.config.internal.BlueprintCollectionBeanDefinitionParser;
import org.eclipse.gemini.blueprint.blueprint.config.internal.BlueprintReferenceBeanDefinitionParser;
import org.eclipse.gemini.blueprint.blueprint.config.internal.BlueprintServiceDefinitionParser;
import org.eclipse.gemini.blueprint.blueprint.config.internal.ParsingUtils;
import org.eclipse.gemini.blueprint.service.importer.support.CollectionType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ide.eclipse.osgi.blueprint.internal.BlueprintParser;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Namespace parser handling the root &lt;components&gt; element from RFC124 (the equivalent of Spring's &lt;beans&gt;
 * element).
 * 
 * @author Costin Leau
 * 
 * @since 3.7.2
 */
class BlueprintBeanDefinitionParser implements BeanDefinitionParser {

	static final String BLUEPRINT = "blueprint";

	private static final String DESCRIPTION = "description";
	private static final String BEAN = "bean";
	static final String REFERENCE = "reference";
	static final String SERVICE = "service";
	static final String REFERENCE_LIST = "reference-list";
	static final String REFERENCE_SET = "reference-set";

	public BeanDefinition parse(Element componentsRootElement, ParserContext parserContext) {
		// re-initialize defaults
		BeanDefinitionParserDelegate delegate = parserContext.getDelegate();
		delegate.initDefaults(componentsRootElement);

		NodeList nl = componentsRootElement.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element ele = (Element) node;
				String namespaceUri = ele.getNamespaceURI();
				// check beans namespace
				if (delegate.isDefaultNamespace(namespaceUri)) {
					BeanDefinitionHolder holder = delegate.parseBeanDefinitionElement(ele);
					ParsingUtils.decorateAndRegister(ele, holder, parserContext);
				}
				// handle own components
				else if (BlueprintParser.NAMESPACE_URI.equals(namespaceUri)) {
					parseTopLevelElement(ele, parserContext);
				}
				// leave the delegate to find a parser for it
				else {
					delegate.parseCustomElement(ele);
				}
			}
		}

		return null;
	}

	/**
	 * Parses the top elements belonging to the RFC 124 namespace. Namely these are &lt;component&gt;,
	 * &lt;description&gt; and &lt;type-converters&gt;
	 * 
	 * @param ele
	 * @param parserContext
	 */
	protected void parseTopLevelElement(Element ele, ParserContext parserContext) {
		// description
		if (DomUtils.nodeNameEquals(ele, DESCRIPTION)) {
			// ignore description for now
		} else if (DomUtils.nodeNameEquals(ele, BEAN)) {
			parseComponentElement(ele, parserContext);
		} else if (DomUtils.nodeNameEquals(ele, REFERENCE)) {
			parseReferenceElement(ele, parserContext);
		} else if (DomUtils.nodeNameEquals(ele, SERVICE)) {
			parseServiceElement(ele, parserContext);
		} else if (DomUtils.nodeNameEquals(ele, REFERENCE_LIST)) {
			parseListElement(ele, parserContext);
		} else if (DomUtils.nodeNameEquals(ele, REFERENCE_SET)) {
			parseSetElement(ele, parserContext);
		} else if (DomUtils.nodeNameEquals(ele, TypeConverterBeanDefinitionParser.TYPE_CONVERTERS)) {
			parseConvertersElement(ele, parserContext);
		} else {
			throw new IllegalArgumentException("Unknown element " + ele);
		}
	}

	/**
	 * Parses a &lt;component&gt element.
	 * 
	 * @param ele
	 * @param parserContext
	 */
	protected void parseComponentElement(Element ele, ParserContext parserContext) {
		BeanDefinitionHolder holder = new BlueprintParser().parseAsHolder(ele, parserContext);
		ParsingUtils.decorateAndRegister(ele, holder, parserContext);
	}

	/**
	 * Parses a &lt;type-converters&gt;.
	 * 
	 * @param ele
	 * @param parserContext
	 */
	protected void parseConvertersElement(Element ele, ParserContext parserContext) {
		BeanDefinitionParser parser = new TypeConverterBeanDefinitionParser();
		parser.parse(ele, parserContext);
	}

	private void parseReferenceElement(Element ele, ParserContext parserContext) {
		BeanDefinitionParser parser = new BlueprintReferenceBeanDefinitionParser();
		parser.parse(ele, parserContext);
	}

	private void parseServiceElement(Element ele, ParserContext parserContext) {
		BeanDefinitionParser parser = new BlueprintServiceDefinitionParser();
		parser.parse(ele, parserContext);
	}

	private void parseListElement(Element ele, ParserContext parserContext) {
		BeanDefinitionParser parser = new BlueprintCollectionBeanDefinitionParser() {

			@Override
			protected CollectionType collectionType() {
				return CollectionType.LIST;
			}
		};
		parser.parse(ele, parserContext);
	}

	private void parseSetElement(Element ele, ParserContext parserContext) {
		BeanDefinitionParser parser = new BlueprintCollectionBeanDefinitionParser() {

			@Override
			protected CollectionType collectionType() {
				return CollectionType.SET;
			}
		};

		parser.parse(ele, parserContext);
	}
}