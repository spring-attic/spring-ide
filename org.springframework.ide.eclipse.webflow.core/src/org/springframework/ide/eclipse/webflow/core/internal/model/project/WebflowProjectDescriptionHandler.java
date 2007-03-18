/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.core.internal.model.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 */
public class WebflowProjectDescriptionHandler extends DefaultHandler implements
		IWebflowProjectDescriptionConstants {

	/**
	 * 
	 */
	protected static final int S_INITIAL = 0;

	/**
	 * 
	 */
	protected static final int S_PROJECT_DESC = 1;

	/**
	 * 
	 */
	protected static final int S_CONFIGS = 2;

	/**
	 * 
	 */
	protected static final int S_CONFIG = 3;

	/**
	 * 
	 */
	protected static final int S_BEAN_CONFIG = 4;

	/**
	 * 
	 */
	protected static final int S_FILE = 5;

	/**
	 * 
	 */
	protected static final int S_NAME = 6;

	/**
	 * 
	 */
	protected IWebflowProject project;

	/**
	 * 
	 */
	protected IWebflowConfig webflowConfig;

	/**
	 * 
	 */
	protected MultiStatus problems;

	/**
	 * 
	 */
	protected WebflowProjectDescription description;

	/**
	 * 
	 */
	protected int state;

	/**
	 * 
	 */
	protected final StringBuffer charBuffer = new StringBuffer();

	/**
	 * 
	 */
	protected Locator locator;

	/**
	 * 
	 * 
	 * @param project
	 */
	public WebflowProjectDescriptionHandler(IWebflowProject project) {
		this.project = project;
		problems = new MultiStatus(Activator.PLUGIN_ID,
				IResourceStatus.FAILED_READ_METADATA,
				"Error reading Spring project description", null);
		description = new WebflowProjectDescription(project);
		state = S_INITIAL;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public IStatus getStatus() {
		return problems;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public WebflowProjectDescription getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String elementName, String qname,
			Attributes attributes) throws SAXException {
		// clear the character buffer at the start of every element
		charBuffer.setLength(0);
		switch (state) {
		case S_INITIAL:
			if (elementName.equals(PROJECT_DESCRIPTION)) {
				state = S_PROJECT_DESC;
			}
			else {
				throw new SAXParseException("No Spring project description",
						locator);
			}
			break;

		case S_PROJECT_DESC:
			if (elementName.equals(CONFIGS)) {
				state = S_CONFIGS;
			}
			else {
				throw new SAXParseException("No Spring project description",
						locator);
			}
			break;

		case S_CONFIGS:
			if (elementName.equals(CONFIG)) {
				state = S_CONFIG;
			}
			break;

		case S_CONFIG:
			if (elementName.equals(FILE)) {
				state = S_FILE;
			}
			else if (elementName.equals(BEANS_CONFIG)) {
				state = S_BEAN_CONFIG;
			}
			else if (elementName.equals(NAME)) {
				state = S_NAME;
			}
			else {
				state = S_CONFIG;
			}
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String elementName, String qname)
			throws SAXException {
		switch (state) {
		case S_PROJECT_DESC:
			// Don't think we need to do anything here.
			break;

		case S_CONFIGS:
			if (elementName.equals(CONFIGS)) {
				state = S_PROJECT_DESC;
			}
			break;

		case S_NAME:
			if (elementName.equals(NAME)) {
				String config = charBuffer.toString().trim();
				if (webflowConfig != null) {
					webflowConfig.setName(config);
					state = S_BEAN_CONFIG;
				}
				else {
					state = S_BEAN_CONFIG;
				}
			}
			break;

		case S_FILE:
			if (elementName.equals(FILE)) {
				String config = charBuffer.toString().trim();
				IFile file = project.getProject().getFile(config);
				if (file.exists()) {
					webflowConfig = description.addConfig(file);
					state = S_NAME;
				}
				else {
					state = S_CONFIG;
				}
			}
			else {
				state = S_CONFIG;
			}
			break;

		case S_CONFIG:
			if (elementName.equals(CONFIG)) {
				state = S_CONFIG;
			}
			else {
				state = S_PROJECT_DESC;
			}
			break;

		case S_BEAN_CONFIG:
			if (elementName.equals(BEANS_CONFIG)) {
				String config = charBuffer.toString().trim();
				if (webflowConfig != null) {
					webflowConfig.addBeansConfigElementId(config);
				}
			}
			else {
				state = S_CONFIG;
			}
			break;
		}
		charBuffer.setLength(0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] chars, int offset, int length)
			throws SAXException {
		// accumulate characters and process them when endElement is reached
		charBuffer.append(chars, offset, length);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException error) throws SAXException {
		log(IStatus.ERROR, error);
		throw error;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
	 */
	public void error(SAXParseException error) throws SAXException {
		log(IStatus.WARNING, error);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException error) throws SAXException {
		log(IStatus.WARNING, error);
	}

	/**
	 * 
	 * 
	 * @param code
	 * @param error
	 */
	public void log(int code, Throwable error) {
		log(code, error.getMessage(), error);
	}

	/**
	 * 
	 * 
	 * @param code
	 * @param errorMessage
	 * @param error
	 */
	public void log(int code, String errorMessage, Throwable error) {
		problems.add(new Status(code, Activator.PLUGIN_ID,
				IResourceStatus.FAILED_READ_METADATA, errorMessage, error));
	}
}