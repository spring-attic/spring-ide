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
package org.springframework.ide.eclipse.core.io.xml;

import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

/**
 * A {@link DocumentLoader} implementation which loads
 * {@link Document documents} using Apache's Xerces XML parser.
 * @author Torsten Juergeleit
 */
public class XercesDocumentLoader implements DocumentLoader {

	public Document loadDocument(InputSource inputSource,
			EntityResolver entityResolver, ErrorHandler errorHandler,
			int validationMode, boolean namespaceAware) throws Exception {
		try {
			LineNumberPreservingDOMParser parser = new LineNumberPreservingDOMParser();
			parser.setEntityResolver(entityResolver);
			parser.setErrorHandler(errorHandler);
			if (validationMode != XmlBeanDefinitionReader.VALIDATION_NONE) {
				parser.setFeature("http://xml.org/sax/features/validation",
						true);
				parser.setFeature(
						"http://apache.org/xml/features/validation/dynamic",
						true);
				if (validationMode == XmlBeanDefinitionReader.VALIDATION_XSD) {
					parser.setFeature(
							"http://apache.org/xml/features/validation/schema",
							true);
				}
			}
			parser.parse(inputSource);
			return parser.getDocument();
		}
		catch (LinkageError e) {
			// log the Xerces location to the Error log in order to debug the location
			SpringCore.log(SpringCore.getFormattedMessage(
					"Plugin.xerces_location", SpringCoreUtils
					.getClassVersion(org.apache.xerces.impl.Version.class), SpringCoreUtils
					.getClassLocation(org.apache.xerces.impl.Version.class), SpringCoreUtils
					.getClassLoaderHierachy(org.apache.xerces.impl.Version.class)), e);
			throw new SAXException(SpringCore
					.getResourceString("Plugin.wrong_xerces_message"));
		}
		catch (SAXNotSupportedException e) {
			throw new SAXException(SpringCore
					.getResourceString("Plugin.wrong_xerces_message"));
		}
	}
}
