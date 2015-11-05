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

import java.beans.PropertyEditor;
import java.util.List;

import org.eclipse.gemini.blueprint.blueprint.config.internal.ParsingUtils;
import org.eclipse.gemini.blueprint.blueprint.container.BlueprintConverterConfigurer;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ide.eclipse.osgi.blueprint.internal.BlueprintParser;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser handling Blueprint &lt;type-converters&gt; elements.
 * 
 * Transforms the {@link Converter converters} into {@link PropertyEditor} through a dedicated
 * {@link PropertyEditorRegistrar registrar} that gets registers through a {@link CustomEditorConfigurer}.
 * 
 * Note that no beans are actually instantiated, the parser generating just the definitions.
 * 
 * @author Costin Leau
 * @author Arnaud Mergey
 * 
 * @since 3.7.2
 * 
 */
class TypeConverterBeanDefinitionParser extends AbstractBeanDefinitionParser {

	public static final String TYPE_CONVERTERS = "type-converters";

	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

		BeanDefinitionBuilder registrarDefinitionBuilder =
				BeanDefinitionBuilder.genericBeanDefinition(BlueprintConverterConfigurer.class);

		List<Element> components = DomUtils.getChildElementsByTagName(element, BlueprintParser.BEAN);
		List<Element> componentRefs =
				DomUtils.getChildElementsByTagName(element, BeanDefinitionParserDelegate.REF_ELEMENT);

		ManagedList<Object> converterList = new ManagedList<Object>(componentRefs.size() + components.size());

		// add components
		for (Element component : components) {
			converterList.add(BlueprintParser.parsePropertySubElement(parserContext, component,
					registrarDefinitionBuilder.getBeanDefinition()));
		}
		// followed by bean references
		for (Element componentRef : componentRefs) {
			converterList.add(BlueprintParser.parsePropertySubElement(parserContext, componentRef,
					registrarDefinitionBuilder.getBeanDefinition()));
		}
		// add the list to the registrar definition
		registrarDefinitionBuilder.addConstructorArgValue(converterList);
		registrarDefinitionBuilder.setRole(BeanDefinition.ROLE_SUPPORT);
		registrarDefinitionBuilder.getRawBeanDefinition().setSynthetic(true);

		// build the CustomEditorConfigurer
		return registrarDefinitionBuilder.getBeanDefinition();
	}

	@Override
	protected boolean shouldGenerateId() {
		return true;
	}

	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		return ParsingUtils.resolveId(element, definition, parserContext, shouldGenerateId(),
				shouldGenerateIdAsFallback());
	}
}