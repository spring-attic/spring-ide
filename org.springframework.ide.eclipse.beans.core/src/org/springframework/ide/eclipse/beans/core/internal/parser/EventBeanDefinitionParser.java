/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.core.internal.parser;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.BeanDefinitionException;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extension to Spring's <code>DefaultXmlBeanDefinitionParser</code> which
 * overwrites some methods to call methods of a specified handler for the
 * following events:
 * <ul>
 * <li>Start parsing a bean definition
 * <li>Registering a bean's constructor argument
 * <li>Registering a bean's property
 * <li>Registering a bean
 * </ul>
 * Nested beans are supported too.
 * @see org.springframework.ide.eclipse.beans.core.internal.parser.IBeanDefinitionEvents
 */
public class EventBeanDefinitionParser extends DefaultXmlBeanDefinitionParser {

	private static final String ROOT_ELEMENT = "beans";

	private IBeanDefinitionEvents eventHandler;

	/** Counter used to keep track of nested bean definitions (inner beans) */ 
	private int nestedBeanCount;

	public EventBeanDefinitionParser(IBeanDefinitionEvents eventHandler) {
		this.eventHandler = eventHandler;
	}

	/**
	 * Checks if imported BeanDefinition resource exists. The BeanDefinition is
	 * not validated yet. This is done if the imported BeanDefinition file is
	 * modified and the validator is run on it.
	 */
	protected void importBeanDefinitionResource(Element ele) {
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);

		// Skip locations which are specified as URL
		if (!ResourceUtils.isUrl(location)) {
			try {
				Resource relativeResource = getResource().createRelative(
																	 location);
				if (!relativeResource.exists()) {
					throw new FileNotFoundException("Invalid relative " +
									   "resource location '" + location + "'");
				}
			} catch (IOException e) {
				throw new BeanDefinitionException(ele, e);
			}
		}
	}

	/**
	 * Checks root node of given document for Spring config file root. 
	 */
	public int registerBeanDefinitions(BeanDefinitionReader reader,
									   Document doc, Resource resource) {
		Element root = doc.getDocumentElement();
		if (!ROOT_ELEMENT.equals(root.getNodeName())) {
			throw new BeanDefinitionException(root, "No Spring bean config " +
									   "found in " + resource.getDescription());
		}
		return super.registerBeanDefinitions(reader, doc, resource);
	}

	/**
	 * Starts parsing a bean and registers the bean definition.
	 * All relevant exceptions are catched and rethrown wrapped together with
	 * the corresponding XML element.
	 */
	protected BeanDefinitionHolder parseBeanDefinitionElement(Element ele) {
		try {
			nestedBeanCount++;
			if (eventHandler != null) {
				if (nestedBeanCount > 1) {
					eventHandler.startBean(ele, true);
				} else {
					eventHandler.startBean(ele, false);
				}
			}
			BeanDefinitionHolder bdHolder = super.parseBeanDefinitionElement(
																		  ele);
			// Replace Spring's BeanDefinitionHolder with our own version with
			// overwritten toString()
			if (bdHolder != null) {
				bdHolder = new ExtendedBeanDefinitionHolder(bdHolder);
			}
			if (eventHandler != null) {
				if (nestedBeanCount > 1) {
					eventHandler.registerBean(bdHolder, true);

					// Inner beans are not registered with the bean definition
					// registry - so we do this ourselves
					// This way our super method creates a unique name for
					// anonymous inner beans too - they are named
					// "<class name>[#<occurrence counter>]"
					BeanDefinitionRegistry registry =
									 getBeanDefinitionReader().getBeanFactory();
					registry.registerBeanDefinition(bdHolder.getBeanName(),
												  bdHolder.getBeanDefinition());
				} else {
					eventHandler.registerBean(bdHolder, false);
				}
			}
			nestedBeanCount--;
			return bdHolder;
		} catch (DOMException e) {
			throw new BeanDefinitionException(ele, e);
		} catch (BeansException e) {
			throw new BeanDefinitionException(ele, e);
		}
	}

	/**
	 * Registers a bean constructor argument.
	 * All relevant exceptions are catched and rethrown wrapped together with
	 * the corresponding XML element.
	 */
	protected void parseConstructorArgElement(Element ele, String beanName,
										  ConstructorArgumentValues cargs)
										  throws BeanDefinitionStoreException {
		try {
			if (eventHandler != null) {
				ConstructorArgumentValuesFilter filter =
								new ConstructorArgumentValuesFilter(cargs, ele);
				eventHandler.startConstructorArgument(ele);
				super.parseConstructorArgElement(ele, beanName, filter);
			} else {
				super.parseConstructorArgElement(ele, beanName, cargs);
			}
		} catch (DOMException e) {
			throw new BeanDefinitionException(ele, e);
		} catch (BeansException e) {
			throw new BeanDefinitionException(ele, e);
		}
	}

	/**
	 * Registers a bean property.
	 * All relevant exceptions are catched and rethrown wrapped together with
	 * the corresponding XML element.
	 */
	protected void parsePropertyElement(Element ele, String beanName,
								MutablePropertyValues pvs) throws DOMException {
		try {
			if (eventHandler != null) {
				eventHandler.startProperty(ele);
			}
			super.parsePropertyElement(ele, beanName, pvs);
			if (eventHandler != null) {
				String name = ele.getAttribute(NAME_ATTRIBUTE);
				eventHandler.registerProperty(name, pvs);
			}
		} catch (DOMException e) {
			throw new BeanDefinitionException(ele, e);
		} catch (BeansException e) {
			throw new BeanDefinitionException(ele, e);
		}
	}

	/**
	 * Converts an idref into a bean reference.
	 */
	protected Object parsePropertySubElement(Element ele, String beanName) {
		Object value = super.parsePropertySubElement(ele, beanName);
		if (ele.getTagName().equals(IDREF_ELEMENT)) {
			value = new RuntimeBeanReference((String) value);
		}
		return value;
	}

	private class ConstructorArgumentValuesFilter
											 extends ConstructorArgumentValues {
		private ConstructorArgumentValues cargs;

		public ConstructorArgumentValuesFilter(
							 ConstructorArgumentValues cargs, Element element) {
			this.cargs = cargs;
		}

		public void addIndexedArgumentValue(int index, Object value) {
			this.cargs.addIndexedArgumentValue(index, value);
			eventHandler.registerConstructorArgument(index, value, null);
		}

		public void addIndexedArgumentValue(int index, Object value, String type) {
			this.cargs.addIndexedArgumentValue(index, value, type);
			eventHandler.registerConstructorArgument(index, value, type);
		}

		public void addGenericArgumentValue(Object value) {
			this.cargs.addGenericArgumentValue(value);
			eventHandler.registerConstructorArgument(-1, value, null);
		}

		public void addGenericArgumentValue(Object value, String type) {
			this.cargs.addGenericArgumentValue(value, type);
			eventHandler.registerConstructorArgument(-1, value, type);
		}
	}
}
