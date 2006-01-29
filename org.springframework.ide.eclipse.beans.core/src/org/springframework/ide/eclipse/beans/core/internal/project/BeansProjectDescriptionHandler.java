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

package org.springframework.ide.eclipse.beans.core.internal.project;

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class provides a SAX handler for a Spring project's description file.
 *
 * @author Torsten Juergeleit
 */
public class BeansProjectDescriptionHandler extends DefaultHandler
								  implements IBeansProjectDescriptionConstants {
	protected static final int S_INITIAL = 0;
	protected static final int S_PROJECT_DESC = 1;
	protected static final int S_CONFIG_EXTENSIONS = 2;
	protected static final int S_CONFIG_EXTENSION = 3;
	protected static final int S_CONFIGS = 4;
	protected static final int S_CONFIG = 5;
	protected static final int S_CONFIG_SETS = 6;
	protected static final int S_CONFIG_SET = 7;
	protected static final int S_CONFIG_SET_NAME = 8;
	protected static final int S_CONFIG_SET_OVERRIDING = 9;
	protected static final int S_CONFIG_SET_INCOMPLETE = 10;
	protected static final int S_CONFIG_SET_CONFIGS = 11;
	protected static final int S_CONFIG_SET_CONFIG = 12;

	protected IBeansProject project;
	protected MultiStatus problems;
	protected BeansProjectDescription description;
	protected int state;
	protected BeansConfigSet configSet;

	protected final StringBuffer charBuffer = new StringBuffer();
	protected Locator locator;

	public BeansProjectDescriptionHandler(IBeansProject project) {
		this.project = project;
		problems = new MultiStatus(BeansCorePlugin.PLUGIN_ID,
							  IResourceStatus.FAILED_READ_METADATA,
							  "Error reading Spring project description", null);
		description = new BeansProjectDescription(project);
		state = S_INITIAL;
	}

	public IStatus getStatus() {
		return problems;
	}

	public BeansProjectDescription getDescription() {
		return description;
	}

	public void startElement(String uri, String elementName, String qname,
							 Attributes attributes) throws SAXException {
		//clear the character buffer at the start of every element
		charBuffer.setLength(0);
		switch (state) {
			case S_INITIAL :
				if (elementName.equals(PROJECT_DESCRIPTION)) {
					state = S_PROJECT_DESC;
				} else {
					throw new SAXParseException("No Spring project description",
												locator);
				}
				break;

			case S_PROJECT_DESC :
				if (elementName.equals(CONFIG_EXTENSIONS)) {
					state = S_CONFIG_EXTENSIONS;
				} else if (elementName.equals(CONFIGS)) {
					state = S_CONFIGS;
				} else if (elementName.equals(CONFIG_SETS)) {
					state = S_CONFIG_SETS;
				}
				break;

			case S_CONFIG_EXTENSIONS :
				if (elementName.equals(CONFIG_EXTENSION)) {
					state = S_CONFIG_EXTENSION;
				}
				break;

			case S_CONFIGS :
				if (elementName.equals(CONFIG)) {
					state = S_CONFIG;
				}
				break;

			case S_CONFIG_SETS :
				if (elementName.equals(CONFIG_SET)) {
					state = S_CONFIG_SET;
				}
				break;

			case S_CONFIG_SET :
				if (elementName.equals(NAME)) {
					state = S_CONFIG_SET_NAME;
				} else if (elementName.equals(OVERRIDING)) {
					state = S_CONFIG_SET_OVERRIDING;
				} else if (elementName.equals(INCOMPLETE)) {
					state = S_CONFIG_SET_INCOMPLETE;
				} else if (elementName.equals(CONFIGS)) {
					state = S_CONFIG_SET_CONFIGS;
				}
				break;

			case S_CONFIG_SET_CONFIGS :
				if (elementName.equals(CONFIG)) {
					state = S_CONFIG_SET_CONFIG;
				}
				break;
		}
	}

	public void endElement(String uri, String elementName, String qname)
														   throws SAXException {
		switch (state) {
			case S_PROJECT_DESC :

				// make sure that at least the default config extension is in
				// the list of config extensions
				if (description.getConfigExtensions().isEmpty()) {
					description.addConfigExtension(
									   IBeansProject.DEFAULT_CONFIG_EXTENSION);
				}
				break;

			case S_CONFIG_EXTENSIONS :
				if (elementName.equals(CONFIG_EXTENSIONS)) {
					state = S_PROJECT_DESC;
				}
				break;

			case S_CONFIG_EXTENSION :
				if (elementName.equals(CONFIG_EXTENSION)) {
					String extension = charBuffer.toString().trim();
					description.addConfigExtension(extension);
					state = S_CONFIG_EXTENSIONS;
				}
				break;

			case S_CONFIGS :
				if (elementName.equals(CONFIGS)) {
					state = S_PROJECT_DESC;
				}
				break;

			case S_CONFIG :
				if (elementName.equals(CONFIG)) {
					String config = charBuffer.toString().trim();

					// If given config is a full path within this Spring
					// project then convert it to a project relative path
					if (config.length() > 0 && config.charAt(0) == '/') {
						String projectPath = '/' + project.getElementName() + '/';
						if (config.startsWith(projectPath)) {
							config = config.substring(projectPath.length());
						}
					}
					description.addConfig(config);
					state = S_CONFIGS;
				}
				break;

			case S_CONFIG_SETS :
				if (elementName.equals(CONFIG_SETS)) {
					state = S_PROJECT_DESC;
				}
				break;

			case S_CONFIG_SET :
				if (elementName.equals(CONFIG_SET)) {
					description.addConfigSet(configSet);
					state = S_CONFIG_SETS;
				}
				break;

			case S_CONFIG_SET_NAME :
				if (elementName.equals(NAME)) {
					String name = charBuffer.toString().trim();
					configSet = new BeansConfigSet(project, name);
					state = S_CONFIG_SET;
				}
				break;

			case S_CONFIG_SET_OVERRIDING :
				if (elementName.equals(OVERRIDING)) {
					boolean override = Boolean.valueOf(
								   charBuffer.toString().trim()).booleanValue();
					configSet.setAllowBeanDefinitionOverriding(override);
					state = S_CONFIG_SET;
				}
				break;

			case S_CONFIG_SET_INCOMPLETE :
				if (elementName.equals(INCOMPLETE)) {
					boolean incomplete = Boolean.valueOf(
								   charBuffer.toString().trim()).booleanValue();
					configSet.setIncomplete(incomplete);
					state = S_CONFIG_SET;
				}
				break;

			case S_CONFIG_SET_CONFIGS :
				if (elementName.equals(CONFIGS)) {
					state = S_CONFIG_SET;
				}
				break;

			case S_CONFIG_SET_CONFIG :
				if (elementName.equals(CONFIG)) {
					String config = charBuffer.toString().trim();

					// If given config is a full path within this Spring
					// project then convert it to a project relative path
					if (config.length() > 0 && config.charAt(0) == '/') {
						String projectPath = '/' + project.getElementName() + '/';
						if (config.startsWith(projectPath)) {
							config = config.substring(projectPath.length());
						}
					}
					configSet.addConfig(config);
					state = S_CONFIG_SET_CONFIGS;
				}
				break;
		}
		charBuffer.setLength(0);
	}

	public void characters(char[] chars, int offset, int length)
														   throws SAXException {
		//accumulate characters and process them when endElement is reached
		charBuffer.append(chars, offset, length);
	}

	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
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
		problems.add(new Status(code, BeansCorePlugin.PLUGIN_ID,
					IResourceStatus.FAILED_READ_METADATA, errorMessage, error));
	}
}
