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

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.BeanDefinitionException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class EventBeanDefinitionReader implements BeanDefinitionReader {

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID +
																"/reader/debug";
	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	private IBeanDefinitionEvents eventHandler;
	private BeanDefinitionRegistry beanFactory;

	public EventBeanDefinitionReader(IBeanDefinitionEvents eventHandler) {
		this.eventHandler = eventHandler;
		beanFactory = new NoOpRegistry();
	}

	public BeanDefinitionRegistry getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Returns null to prevent class loading of bean classes. 
	 */
	public ClassLoader getBeanClassLoader() {
		return null;
	}

	/**
	 * Load bean definitions from the specified XML file.
	 * @param resource the resource descriptor for the XML file
	 * @throws BeansException in case of loading or parsing errors
	 */
	public int loadBeanDefinitions(Resource resource) throws BeansException {
		if (DEBUG) {
			System.out.println("Reading config from " + resource);
		}
		InputStream input = null;
		try {
			input = resource.getInputStream();
			InputSource inputSource = new InputSource(input);
			inputSource.setSystemId(resource.getDescription());
			LineNumberPreservingDOMParser parser =
											new LineNumberPreservingDOMParser();
			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setFeature(
					 "http://apache.org/xml/features/validation/dynamic", true);
			parser.setEntityResolver(new BeansDtdResolver());
			parser.setErrorHandler(new BeansErrorHandler());
			parser.parse(inputSource);
			return registerBeanDefinitions(parser.getDocument(), resource);
		} catch (SAXException e) {
			throw new BeanDefinitionException(e);
		} catch (DOMException e) {
			throw new BeanDefinitionException(e);
		} catch (IOException e) {
			throw new BeanDefinitionException(e);
		} catch (BeansException e) {
			throw new BeanDefinitionException(e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					BeansCorePlugin.log("Could not close InputStream", e);
				}
			}
		}
	}

	/**
	 * Register the bean definitions contained in the given DOM document.
	 * Called by <code>loadBeanDefinitions</code>.
	 * <p>Creates a new instance of the <code>EventBeanDefinitionParser</code>
	 * and invokes <code>registerBeanDefinitions</code> on it.
	 * @param doc the DOM document
	 * @param resource the resource descriptor (for context information)
	 * @throws BeansException in case of parsing errors
	 * @see #loadBeanDefinitions
	 * @see EventBeanDefinitionParser#registerBeanDefinitions
	 */
	protected int registerBeanDefinitions(Document doc, Resource resource)
														 throws BeansException {
		EventBeanDefinitionParser parser = new EventBeanDefinitionParser();
		parser.setEventHandler(this.eventHandler);
		return parser.registerBeanDefinitions(this, doc, resource);
	}

	/**
	 * Private implementation of SAX ErrorHandler used when validating XML.
	 */
	private static class BeansErrorHandler implements ErrorHandler {

		public void error(SAXParseException e) throws SAXException {
			throw e;
		}

		public void fatalError(SAXParseException e) throws SAXException {
			throw e;
		}

		public void warning(SAXParseException e) throws SAXException {
			// ignore XML parse warnings
		}
	}

	private static class NoOpRegistry implements BeanDefinitionRegistry {

		public int getBeanDefinitionCount() {
			return 0;
		}

		public String[] getBeanDefinitionNames() {
			return null;
		}

		public boolean containsBeanDefinition(String name) {
			return false;
		}

		public BeanDefinition getBeanDefinition(String name) throws BeansException {
			return null;
		}

		public void registerBeanDefinition(String name, BeanDefinition beanDefinition) throws BeansException {
		}

		public String[] getAliases(String name) throws NoSuchBeanDefinitionException {
			return null;
		}

		public void registerAlias(String name, String alias) throws BeansException {
		}
	}
}
