/*
 * Copyright 2002-2006 the original author or authors.
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
