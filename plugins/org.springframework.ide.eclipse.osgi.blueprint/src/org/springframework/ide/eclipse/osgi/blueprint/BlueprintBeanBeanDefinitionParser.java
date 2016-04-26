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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ide.eclipse.osgi.blueprint.internal.BlueprintParser;
import org.w3c.dom.Element;

/**
 * Parser handling Blueprint &lt;bean&gt; elements.
 * 
 * @author Costin Leau
 * 
 * @since 3.7.2
 * 
 */
class BlueprintBeanBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BlueprintParser parser = new BlueprintParser();
		return parser.parse(element, parserContext);
	}
}
