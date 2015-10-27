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

package org.springframework.ide.eclipse.osgi.blueprint.internal;

import java.util.Set;

import org.eclipse.gemini.blueprint.blueprint.config.internal.BlueprintDefaultsDefinition;
import org.eclipse.gemini.blueprint.blueprint.config.internal.BlueprintParser;
import org.eclipse.gemini.blueprint.blueprint.config.internal.BlueprintReferenceAttributeCallback;
import org.eclipse.gemini.blueprint.blueprint.config.internal.ParsingUtils;
import org.eclipse.gemini.blueprint.config.internal.CollectionBeanDefinitionParser;
import org.eclipse.gemini.blueprint.config.internal.OsgiDefaultsDefinition;
import org.eclipse.gemini.blueprint.config.internal.util.AttributeCallback;
import org.eclipse.gemini.blueprint.config.internal.util.ParserUtils;
import org.eclipse.gemini.blueprint.service.importer.support.CollectionType;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Costin Leau
 * 
 */
public abstract class BlueprintCollectionBeanDefinitionParser extends CollectionBeanDefinitionParser {

	private static final String REFERENCE_LISTENER = "reference-listener";

	@Override
	protected OsgiDefaultsDefinition resolveDefaults(Document document, ParserContext parserContext) {
		return new BlueprintDefaultsDefinition(document, parserContext);
	}

	@Override
	protected void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks,
			OsgiDefaultsDefinition defaults) {

		// add BlueprintAttr Callback
		AttributeCallback blueprintCallback = new BlueprintReferenceAttributeCallback();
		super.parseAttributes(element, builder, ParserUtils.mergeCallbacks(
				new AttributeCallback[] { blueprintCallback }, callbacks), defaults);
	}

	@Override
	protected Set parsePropertySetElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return BlueprintParser.parsePropertySetElement(context, beanDef, beanDefinition);
	}

	@Override
	protected Object parsePropertySubElement(ParserContext context, Element beanDef, BeanDefinition beanDefinition) {
		return BlueprintParser.parsePropertySubElement(context, beanDef, beanDefinition);
	}

	@Override
	protected void doParse(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		super.doParse(element, context, builder);
		builder.addPropertyValue("useBlueprintExceptions", true);
		builder.addPropertyValue("blueprintCompliant", true);
	}

	@Override
	protected String getListenerElementName() {
		return REFERENCE_LISTENER;
	}

	@Override
	protected CollectionType collectionType() {
		return null;
	}

	@Override
	protected String generateBeanName(String id, BeanDefinition def, ParserContext parserContext) {
		return super.generateBeanName(ParsingUtils.BLUEPRINT_GENERATED_NAME_PREFIX + id, def, parserContext);
	}

	@Override
	protected void postProcessListenerDefinition(BeanDefinition wrapperDef) {
		wrapperDef.getPropertyValues().addPropertyValue("blueprintCompliant", true);
	}

	@Override
	protected void applyDefaults(ParserContext parserContext, OsgiDefaultsDefinition defaults,
			BeanDefinitionBuilder builder) {
		super.applyDefaults(parserContext, defaults, builder);
		if (defaults instanceof BlueprintDefaultsDefinition) {
			BlueprintDefaultsDefinition defs = (BlueprintDefaultsDefinition) defaults;
			if (defs.getDefaultInitialization()) {
				builder.setLazyInit(defs.getDefaultInitialization());
			}
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
}