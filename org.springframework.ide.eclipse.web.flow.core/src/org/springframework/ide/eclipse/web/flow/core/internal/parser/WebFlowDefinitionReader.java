/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.core.internal.parser;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionParser;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.core.io.xml.LineNumberPreservingDOMParser;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.WebFlowDefinitionException;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Bean definition reader for XML web flow definitions. 
 */
public class WebFlowDefinitionReader {

    public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID
            + "/reader/debug";

    public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

    public void loadWebFlowDefinitions(IWebFlowConfig config, Resource resource)
            throws BeansException {
        if (DEBUG) {
            System.out.println("Reading config from " + resource);
        }
        InputStream input = null;
        try {
            input = resource.getInputStream();
            InputSource inputSource = new InputSource(input);
            inputSource.setSystemId(resource.getDescription());
            LineNumberPreservingDOMParser parser = new LineNumberPreservingDOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature(
                    "http://apache.org/xml/features/validation/dynamic", true);
            parser.setEntityResolver(new WebFlowDtdResolver());
            parser.setErrorHandler(new WebFlowErrorHandler());
            parser.parse(inputSource);
            registerWebFlowDefinitions(config, parser.getDocument(), resource);
        }
        catch (SAXException e) {
            throw new WebFlowDefinitionException(e);
        }
        catch (DOMException e) {
            throw new WebFlowDefinitionException(e);
        }
        catch (IOException e) {
            throw new WebFlowDefinitionException(e);
        }
        catch (BeansException e) {
            throw new WebFlowDefinitionException(e);
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    WebFlowCorePlugin.log("Could not close InputStream", e);
                }
            }
        }
    }

    /**
     * Register the bean definitions contained in the given DOM document. Called
     * by <code>loadBeanDefinitions</code>.
     * <p>
     * Creates a new instance of the parser class and invokes
     * <code>registerBeanDefinitions</code> on it.
     * 
     * @param doc
     *            the DOM document
     * @param resource
     *            the resource descriptor (for context information)
     * @return the number of bean definitions found
     * @throws BeansException
     *             in case of parsing errors
     * @see #loadBeanDefinitions
     * @see #setParserClass
     * @see XmlBeanDefinitionParser#registerBeanDefinitions
     */
    public void registerWebFlowDefinitions(IWebFlowConfig config, Document doc,
            Resource resource) throws BeansException {
        XmlFlowParser parser = new XmlFlowParser();
        parser.buildStates(config, doc);
    }

    /**
     * Private implementation of SAX ErrorHandler used when validating XML.
     */
    private static class WebFlowErrorHandler implements ErrorHandler {

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
}