/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
 * @author Christian Dupuis
 */
public class WebflowProjectDescriptionHandler extends DefaultHandler implements
		IWebflowProjectDescriptionConstants {

	protected static final int S_INITIAL = 0;

	protected static final int S_PROJECT_DESC = 1;

	protected static final int S_CONFIGS = 2;

	protected static final int S_CONFIG = 3;

	protected static final int S_BEAN_CONFIG = 4;

	protected static final int S_FILE = 5;

	protected static final int S_NAME = 6;

	protected IWebflowProject project;

	protected IWebflowConfig webflowConfig;

	protected MultiStatus problems;

	protected WebflowProjectDescription description;

	protected int state;

	protected final StringBuffer charBuffer = new StringBuffer();

	protected Locator locator;

	public WebflowProjectDescriptionHandler(IWebflowProject project) {
		this.project = project;
		problems = new MultiStatus(Activator.PLUGIN_ID,
				IResourceStatus.FAILED_READ_METADATA,
				"Error reading Spring project description", null);
		description = new WebflowProjectDescription(project);
		state = S_INITIAL;
	}

	public IStatus getStatus() {
		return problems;
	}

	public WebflowProjectDescription getDescription() {
		return description;
	}

	public void startElement(String uri, String elementName, String qname,
			Attributes attributes) throws SAXException {
		// clear the character buffer at the start of every element
		charBuffer.setLength(0);
		switch (state) {
		case S_INITIAL:
			if (elementName.equals(PROJECT_DESCRIPTION)) {
				state = S_PROJECT_DESC;
			}
			break;

		case S_PROJECT_DESC:
			if (elementName.equals(CONFIGS)) {
				state = S_CONFIGS;
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

	public void characters(char[] chars, int offset, int length)
			throws SAXException {
		// accumulate characters and process them when endElement is reached
		charBuffer.append(chars, offset, length);
	}

	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	public void fatalError(SAXParseException error) throws SAXException {
		log(IStatus.ERROR, error);
		throw error;
	}

	public void error(SAXParseException error) throws SAXException {
		log(IStatus.WARNING, error);
	}

	public void warning(SAXParseException error) throws SAXException {
		log(IStatus.WARNING, error);
	}

	public void log(int code, Throwable error) {
		log(code, error.getMessage(), error);
	}

	public void log(int code, String errorMessage, Throwable error) {
		problems.add(new Status(code, Activator.PLUGIN_ID,
				IResourceStatus.FAILED_READ_METADATA, errorMessage, error));
	}
}
