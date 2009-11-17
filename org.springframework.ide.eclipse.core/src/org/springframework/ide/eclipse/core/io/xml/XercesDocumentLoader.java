/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.io.xml;

import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

/**
 * A {@link DocumentLoader} implementation which loads {@link Document documents} using Apache's Xerces XML parser.
 * @author Torsten Juergeleit
 * @auhtor Christian Dupuis
 */
public class XercesDocumentLoader implements DocumentLoader {

	public Document loadDocument(InputSource inputSource, EntityResolver entityResolver, ErrorHandler errorHandler,
			int validationMode, boolean namespaceAware) throws Exception {
		try {
			LineNumberPreservingDOMParser parser = new LineNumberPreservingDOMParser();
			parser.setEntityResolver(entityResolver);
			parser.setErrorHandler(errorHandler);
			
			// Setting this to true will trigger XSD downloads from the internet which will really slow down Spring in
			// case of flaky internet connection
			if (validationMode != XmlBeanDefinitionReader.VALIDATION_NONE) {
				parser.setFeature("http://xml.org/sax/features/validation", false);
				parser.setFeature("http://apache.org/xml/features/validation/dynamic", false);
				if (validationMode == XmlBeanDefinitionReader.VALIDATION_XSD) {
					parser.setFeature("http://apache.org/xml/features/validation/schema", true);
				}
			}
			parser.parse(inputSource);
			return parser.getDocument();
		}
		catch (LinkageError e) {
			logXercesLocation(e);
			throw new SAXException(SpringCore.getResourceString("Plugin.wrong_xerces_message"));
		}
		catch (ClassCastException e) {
			logXercesLocation(e);
			throw new SAXException(SpringCore.getResourceString("Plugin.wrong_xerces_message"));
		}
		catch (SAXNotSupportedException e) {
			throw new SAXException(SpringCore.getResourceString("Plugin.wrong_xerces_message"));
		}
	}

	/**
	 * Logs the location of the Xerces XML parser's class {@link org.apache.xerces.impl.Version} to the error log.
	 */
	protected void logXercesLocation(Throwable throwable) throws SAXException {
		Class xercesVersion = org.apache.xerces.impl.Version.class;
		SpringCore.log(SpringCore.getFormattedMessage("Plugin.xerces_location", ClassUtils
				.getClassVersion(xercesVersion), ClassUtils.getClassLocation(xercesVersion), ClassUtils
				.getClassLoaderHierachy(xercesVersion)), throwable);
	}
}
