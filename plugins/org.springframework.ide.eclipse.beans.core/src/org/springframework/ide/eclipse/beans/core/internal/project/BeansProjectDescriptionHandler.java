/*******************************************************************************
 * Copyright (c) 2004, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.project;

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class provides a SAX handler for a Spring project's description file.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public class BeansProjectDescriptionHandler extends DefaultHandler implements
		IBeansProjectDescriptionConstants {
	protected enum State { INITIAL, PROJECT_DESC, CONFIG_EXTENSIONS,
		CONFIG_EXTENSION, CONFIG_SUFFIXES, CONFIG_SUFFIX, CONFIGS, AUTOCONFIGS,
		CONFIG, AUTOCONFIG, CONFIG_SETS, CONFIG_SET, CONFIG_SET_NAME, CONFIG_SET_OVERRIDING, 
		CONFIG_SET_INCOMPLETE, CONFIG_SET_CONFIGS,CONFIG_SET_CONFIG, VERSION, 
		PLUGIN_VERSION, ENABLE_IMPORTS, PROFILES, PROFILE
	}
	protected BeansProject project;
	protected MultiStatus problems;
	protected State state;
	protected BeansConfigSet configSet;

	protected final StringBuffer charBuffer = new StringBuffer();
	protected Locator locator;

	public BeansProjectDescriptionHandler(BeansProject project) {
		this.project = project;
		problems = new MultiStatus(BeansCorePlugin.PLUGIN_ID,
				IResourceStatus.FAILED_READ_METADATA,
				"Error reading Spring project description", null);
		state = State.INITIAL;
	}

	public IStatus getStatus() {
		return problems;
	}

	@Override
	public void startElement(String uri, String elementName, String qname,
			Attributes attributes) throws SAXException {
		// clear the character buffer at the start of every element
		charBuffer.setLength(0);
		if (state == State.INITIAL) {
			if (elementName.equals(PROJECT_DESCRIPTION)) {
				state = State.PROJECT_DESC;
			} else {
				throw new SAXParseException("No Spring project description",
						locator);
			}
		} else if (state == State.PROJECT_DESC) {
			if (elementName.equals(CONFIG_EXTENSIONS)) {
				state = State.CONFIG_EXTENSIONS;
			} else if (elementName.equals(CONFIG_SUFFIXES)) {
				state = State.CONFIG_SUFFIXES;
			} else if (elementName.equals(CONFIGS)) {
				state = State.CONFIGS;
			} else if (elementName.equals(AUTOCONFIGS)) {
				state = State.AUTOCONFIGS;
				project.setAutoConfigStatePersisted(true);
			} else if (elementName.equals(CONFIG_SETS)) {
				state = State.CONFIG_SETS;
			} else if (elementName.equals(ENABLE_IMPORTS)) {
				state = State.ENABLE_IMPORTS;
			} else if (elementName.equals(PLUGIN_VERSION)) {
				state = State.PLUGIN_VERSION;
			}
		} else if (state == State.CONFIG_EXTENSIONS) {
			if (elementName.equals(CONFIG_EXTENSION)) {
				state = State.CONFIG_EXTENSION;
			}
		} else if (state == State.CONFIG_SUFFIXES) {
			if (elementName.equals(CONFIG_SUFFIX)) {
				state = State.CONFIG_SUFFIX;
			}
		} else if (state == State.ENABLE_IMPORTS) {
			if (elementName.equals(ENABLE_IMPORTS)) {
				state = State.ENABLE_IMPORTS;
			}
		} else if (state == State.CONFIGS) {
			if (elementName.equals(CONFIG)) {
				state = State.CONFIG;
			}
		} else if (state == State.AUTOCONFIGS) {
			if (elementName.equals(AUTOCONFIG)) {
				state = State.AUTOCONFIG;
			}
		} else if (state == State.CONFIG_SETS) {
			if (elementName.equals(CONFIG_SET)) {
				state = State.CONFIG_SET;
			}
		} else if (state == State.CONFIG_SET) {
			if (elementName.equals(NAME)) {
				state = State.CONFIG_SET_NAME;
			} else if (elementName.equals(OVERRIDING)) {
				state = State.CONFIG_SET_OVERRIDING;
			} else if (elementName.equals(INCOMPLETE)) {
				state = State.CONFIG_SET_INCOMPLETE;
			} else if (elementName.equals(CONFIGS)) {
				state = State.CONFIG_SET_CONFIGS;
			} else if (elementName.equals(PROFILES)) {
				state = State.PROFILES;
			}
		} else if (state == State.CONFIG_SET_CONFIGS) {
			if (elementName.equals(CONFIG)) {
				state = State.CONFIG_SET_CONFIG;
			}
		} else if (state == State.PROFILES) {
			if (elementName.equals(PROFILE)) {
			state = State.PROFILE;
			}
		}
	}

	@Override
	public void endElement(String uri, String elementName, String qname)
			throws SAXException {
		if (state == State.PROJECT_DESC) {

			// make sure that at least the default config suffix is in
			// the list of config suffix
			if (project.getConfigSuffixes().isEmpty()) {
				project.addConfigSuffix(IBeansProject
						.DEFAULT_CONFIG_SUFFIX);
			}
		} else if (state == State.CONFIG_EXTENSIONS) {
			if (elementName.equals(CONFIG_EXTENSIONS)) {
				state = State.PROJECT_DESC;
			}
		} else if (state == State.ENABLE_IMPORTS) {
			if (elementName.equals(ENABLE_IMPORTS)) {
				boolean isImportEnabled = Boolean.valueOf(charBuffer.toString().trim());
				project.setImportsEnabled(isImportEnabled);
				state = State.PROJECT_DESC;
			}
		} else if (state == State.PLUGIN_VERSION) {
			if (elementName.equals(PLUGIN_VERSION)) {
				String version = charBuffer.toString().trim();
				project.setVersion(version);
				state = State.PROJECT_DESC;
			}
		} else if (state == State.CONFIG_SUFFIXES) {
			if (elementName.equals(CONFIG_SUFFIXES)) {
				state = State.PROJECT_DESC;
			}
		} else if (state == State.CONFIG_EXTENSION) {
			if (elementName.equals(CONFIG_EXTENSION)) {
				String extension = charBuffer.toString().trim();
				project.addConfigSuffix(extension);
				state = State.CONFIG_EXTENSIONS;
			}
		} else if (state == State.CONFIG_SUFFIX) {
			if (elementName.equals(CONFIG_SUFFIX)) {
				String extension = charBuffer.toString().trim();
				project.addConfigSuffix(extension);
				state = State.CONFIG_SUFFIXES;
			}
		} else if (state == State.CONFIGS) {
			if (elementName.equals(CONFIGS)) {
				state = State.PROJECT_DESC;
			}
		} else if (state == State.AUTOCONFIGS) {
			if (elementName.equals(AUTOCONFIGS)) {
				state = State.PROJECT_DESC;
			}
		} else if (state == State.CONFIG) {
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
				project.addConfig(config, IBeansConfig.Type.MANUAL);
				state = State.CONFIGS;
			}
		} else if (state == State.AUTOCONFIG) {
			if (elementName.equals(AUTOCONFIG)) {
				// TODO: set auto configs correctly
				state = State.AUTOCONFIGS;
			}
		} else if (state == State.CONFIG_SETS) {
			if (elementName.equals(CONFIG_SETS)) {
				state = State.PROJECT_DESC;
			}
		} else if (state == State.CONFIG_SET) {
			if (elementName.equals(CONFIG_SET)) {
				project.addConfigSet(configSet);
				state = State.CONFIG_SETS;
			}
		} else if (state == State.PROFILES) {
			if (elementName.equals(PROFILES)) {
				state = State.CONFIG_SET;
			}
		} else if (state == State.PROFILE) {
			if (elementName.equals(PROFILE)) {
				String profile = charBuffer.toString().trim();
				configSet.addProfile(profile);
				state = State.PROFILES;
			}
		} else if (state == State.CONFIG_SET_NAME) {
			if (elementName.equals(NAME)) {
				String name = charBuffer.toString().trim();
				configSet = new BeansConfigSet(project, name, IBeansConfigSet.Type.MANUAL);
				state = State.CONFIG_SET;
			}
		} else if (state == State.CONFIG_SET_OVERRIDING) {
			if (elementName.equals(OVERRIDING)) {
				boolean override = Boolean
						.valueOf(charBuffer.toString().trim()).booleanValue();
				configSet.setAllowBeanDefinitionOverriding(override);
				state = State.CONFIG_SET;
			}
		} else if (state == State.CONFIG_SET_INCOMPLETE) {
			if (elementName.equals(INCOMPLETE)) {
				boolean incomplete = Boolean.valueOf(
						charBuffer.toString().trim()).booleanValue();
				configSet.setIncomplete(incomplete);
				state = State.CONFIG_SET;
			}
		} else if (state == State.CONFIG_SET_CONFIGS) {
			if (elementName.equals(CONFIGS)) {
				state = State.CONFIG_SET;
			}
		} else if (state == State.CONFIG_SET_CONFIG) {
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
				state = State.CONFIG_SET_CONFIGS;
			}
		}
		charBuffer.setLength(0);
	}

	@Override
	public void characters(char[] chars, int offset, int length)
			throws SAXException {
		// accumulate characters and process them when endElement is reached
		charBuffer.append(chars, offset, length);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	@Override
	public void error(SAXParseException error) throws SAXException {
		log(IStatus.WARNING, error);
	}

	@Override
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
