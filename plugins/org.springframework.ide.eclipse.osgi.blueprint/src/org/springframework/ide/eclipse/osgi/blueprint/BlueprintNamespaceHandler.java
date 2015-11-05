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
import org.eclipse.gemini.blueprint.service.importer.support.CollectionType;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.ide.eclipse.osgi.blueprint.internal.BlueprintParser;

/**
 * Spring-based namespace handler for the blueprint/RFC-124 core namespace.
 * 
 * @author Costin Leau
 * @author Arnaud Mergey
 * 
 * @since 3.7.2
 * 
 */
public class BlueprintNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser(BlueprintBeanDefinitionParser.BLUEPRINT, new BlueprintBeanDefinitionParser());
		registerBeanDefinitionParser(BlueprintParser.BEAN, new BlueprintBeanBeanDefinitionParser());
		registerBeanDefinitionParser(TypeConverterBeanDefinitionParser.TYPE_CONVERTERS,
				new TypeConverterBeanDefinitionParser());

		registerBeanDefinitionParser(BlueprintBeanDefinitionParser.REFERENCE,
				new BlueprintReferenceBeanDefinitionParser());

		registerBeanDefinitionParser(BlueprintBeanDefinitionParser.REFERENCE_LIST,
				new BlueprintCollectionBeanDefinitionParser() {

					protected CollectionType collectionType() {
						return CollectionType.LIST;
					}
				});
		registerBeanDefinitionParser(BlueprintBeanDefinitionParser.REFERENCE_SET,
				new BlueprintCollectionBeanDefinitionParser() {

					protected CollectionType collectionType() {
						return CollectionType.SET;
					}
				});

		registerBeanDefinitionParser(BlueprintBeanDefinitionParser.SERVICE, new BlueprintServiceDefinitionParser());
	}
}