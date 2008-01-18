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
package org.springframework.ide.eclipse.beans.core.internal.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.io.xml.XMLWriter;

/**
 * This class saves the description of a Spring Beans project to an XML file.
 * 
 * @author Torsten Juergeleit
 */
public class BeansProjectDescriptionWriter implements
		IBeansProjectDescriptionConstants {

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID
			+ "/project/description/debug";

	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	public static void write(BeansProject project) {
		IFile file = project.getProject().getFile(
				new Path(IBeansProject.DESCRIPTION_FILE));
		if (DEBUG) {
			System.out.println("Writing project description to "
					+ file.getLocation().toString());
		}
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				XMLWriter writer = new XMLWriter(os);
				write(project, writer);
				writer.flush();
				writer.close();
			} finally {
				os.close();
			}
			if (!file.exists()) {
				file.create(new ByteArrayInputStream(os.toByteArray()),
						IResource.FORCE, null);
			} else {
				file.setContents(new ByteArrayInputStream(os.toByteArray()),
						IResource.FORCE, null);
			}
		} catch (IOException e) {
			BeansCorePlugin.log("Error writing " + file.getFullPath(), e);
		} catch (CoreException e) {
			BeansCorePlugin.log(e.getStatus());
		}
	}

	protected static void write(BeansProject project, XMLWriter writer) {
		writer.startTag(PROJECT_DESCRIPTION, null);
		// add version number
		writer.printSimpleTag(VERSION, CURRENT_VERSION);
		// add plugin version number
		writer.printCDataTag(PLUGIN_VERSION, BeansCorePlugin.getPluginVersion());
		writeCData(CONFIG_SUFFIXES, CONFIG_SUFFIX, project
				.getConfigSuffixes(), writer);
		writer.printCDataTag(ENABLE_IMPORTS, project.isImportsEnabled());
		write(CONFIGS, CONFIG, project.getConfigNames(), writer);
		write(CONFIG_SETS, project.getConfigSets(), writer);
		writer.endTag(PROJECT_DESCRIPTION);
	}

	protected static void write(IBeansConfigSet configSet, XMLWriter writer) {
		writer.startTag(CONFIG_SET, null);
		writer.printCDataTag(NAME, configSet.getElementName());
		writer.printSimpleTag(OVERRIDING, new Boolean(configSet
				.isAllowBeanDefinitionOverriding()).toString());
		writer.printSimpleTag(INCOMPLETE, new Boolean(configSet.isIncomplete())
				.toString());
		write(CONFIGS, CONFIG, configSet.getConfigNames(), writer);
		writer.endTag(CONFIG_SET);
	}

	protected static void write(String name, Set<?> elements,
			XMLWriter writer) {
		writer.startTag(name, null);
		for (Object element : elements) {
			if (element instanceof IBeansConfigSet) {
				write((IBeansConfigSet) element, writer);
			}
		}
		writer.endTag(name);
	}

	protected static void write(String name, String elementTagName,
			String[] values, XMLWriter writer) {
		writer.startTag(name, null);
		for (String value : values) {
			writer.printSimpleTag(elementTagName, value);
		}
		writer.endTag(name);
	}

	protected static void write(String name, String elementTagName,
			Set<?> values, XMLWriter writer) {
		writer.startTag(name, null);
		for (Object value : values) {
			writer.printSimpleTag(elementTagName, value);
		}
		writer.endTag(name);
	}

	protected static void writeCData(String name, String elementTagName,
			Set<?> values, XMLWriter writer) {
		writer.startTag(name, null);
		for (Object value : values) {
			writer.printCDataTag(elementTagName, value);
		}
		writer.endTag(name);
	}
}
