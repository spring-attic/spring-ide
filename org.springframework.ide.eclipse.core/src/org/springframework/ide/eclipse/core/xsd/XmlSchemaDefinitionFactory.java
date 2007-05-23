/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.xsd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Simple Factory that contributes {@link IXmlSchemaDefinition} instances.
 * <p>
 * In future this implementation could leverage a extension point that enables 
 * to contribute custom Spring namespace definitions.
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class XmlSchemaDefinitionFactory {

	private static final List<IXmlSchemaDefinition> XML_DEFINITIONS;

	private static final IXmlSchemaDefinition DEFAULT_XML_DEFINITION = new DefaultSchemaDefinition(
			"", "http://www.springframework.org/schema/beans",
			"http://www.springframework.org/schema/beans/spring-beans-2.0.xsd");

	static {
		XML_DEFINITIONS = new ArrayList<IXmlSchemaDefinition>();
		XML_DEFINITIONS.add(new DefaultSchemaDefinition("util",
				"http://www.springframework.org/schema/util",
				"http://www.springframework.org/schema/util/spring-util-2.0.xsd"));
		XML_DEFINITIONS.add(new DefaultSchemaDefinition("tool",
				"http://www.springframework.org/schema/tool",
				"http://www.springframework.org/schema/tool/spring-tool-2.0.xsd"));
		XML_DEFINITIONS.add(new DefaultSchemaDefinition("aop",
				"http://www.springframework.org/schema/aop",
				"http://www.springframework.org/schema/aop/spring-aop-2.0.xsd"));
		XML_DEFINITIONS.add(new DefaultSchemaDefinition("context",
				"http://www.springframework.org/schema/context",
				"http://www.springframework.org/schema/context/spring-context-2.1.xsd"));
		XML_DEFINITIONS.add(new DefaultSchemaDefinition("lang",
				"http://www.springframework.org/schema/lang",
				"http://www.springframework.org/schema/lang/spring-lang-2.0.xsd"));
		XML_DEFINITIONS.add(new DefaultSchemaDefinition("tx",
				"http://www.springframework.org/schema/tx",
				"http://www.springframework.org/schema/tx/spring-tx-2.0.xsd"));
		XML_DEFINITIONS.add(new DefaultSchemaDefinition("jee",
				"http://www.springframework.org/schema/jee",
				"http://www.springframework.org/schema/jee/spring-jee-2.0.xsd"));
		XML_DEFINITIONS.add(new DefaultSchemaDefinition("jms",
				"http://www.springframework.org/schema/jms",
				"http://www.springframework.org/schema/jms/spring-jms-2.1.xsd"));
		
		Collections.sort(XML_DEFINITIONS, new Comparator<IXmlSchemaDefinition>() {

			public int compare(IXmlSchemaDefinition o1, IXmlSchemaDefinition o2) {
				return o1.getNamespacePrefix().compareTo(o2.getNamespacePrefix());
			}});
	}

	public static List<IXmlSchemaDefinition> getXmlSchemaDefinitions() {
		return XML_DEFINITIONS;
	}

	public static IXmlSchemaDefinition getDefaultXmlSchemaDefinition() {
		return DEFAULT_XML_DEFINITION;
	}
}
