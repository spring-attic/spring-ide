/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.webflow.v1.config.WebFlowConfigNamespaceHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @since 2.1.0
 */
public class CombiningWebFlowConfigNamespaceHandler implements NamespaceHandler {

	private WebFlowConfigNamespaceHandler v1NamespaceHandler;

	private org.springframework.webflow.config.WebFlowConfigNamespaceHandler v2NamespaceHandler;

	private Map<String, NamespaceHandler> namespaceHandlerMapping;

	public CombiningWebFlowConfigNamespaceHandler() {
		v1NamespaceHandler = new WebFlowConfigNamespaceHandler();
		v2NamespaceHandler = new org.springframework.webflow.config.WebFlowConfigNamespaceHandler();
		namespaceHandlerMapping = new HashMap<String, NamespaceHandler>();
	}

	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition,
			ParserContext parserContext) {
		if (namespaceHandlerMapping.containsKey(node.getLocalName())) {
			return namespaceHandlerMapping.get(node.getLocalName()).decorate(node, definition,
					parserContext);
		}
		else {
			parserContext.getReaderContext().fatal(
					"Cannot locate BeanDefinitionDecorator for "
							+ (node instanceof Element ? "element" : "attribute") + " ["
							+ node.getLocalName() + "]", node);
		}
		return null;
	}

	public void init() {
		v1NamespaceHandler.init();
		v2NamespaceHandler.init();

		namespaceHandlerMapping.put("execution-attributes", v1NamespaceHandler);
		namespaceHandlerMapping.put("execution-listeners", v1NamespaceHandler);
		namespaceHandlerMapping.put("executor", v1NamespaceHandler);
		namespaceHandlerMapping.put("registry", v1NamespaceHandler);

		namespaceHandlerMapping.put("flow-executor", v2NamespaceHandler);
		namespaceHandlerMapping.put("flow-execution-listeners", v2NamespaceHandler);
		namespaceHandlerMapping.put("flow-registry", v2NamespaceHandler);
		namespaceHandlerMapping.put("flow-builder-services", v2NamespaceHandler);
	}

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		if (namespaceHandlerMapping.containsKey(element.getLocalName())) {
			return namespaceHandlerMapping.get(element.getLocalName())
					.parse(element, parserContext);
		}
		else {
			parserContext.getReaderContext().fatal(
					"Cannot locate BeanDefinitionParser for element [" + element.getLocalName()
							+ "]", element);
		}
		return null;
	}

}
