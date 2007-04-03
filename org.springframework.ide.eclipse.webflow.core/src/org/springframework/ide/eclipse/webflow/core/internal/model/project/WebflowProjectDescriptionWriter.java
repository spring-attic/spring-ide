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
package org.springframework.ide.eclipse.webflow.core.internal.model.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.core.io.xml.XMLWriter;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * 
 */
public class WebflowProjectDescriptionWriter implements
		IWebflowProjectDescriptionConstants {

	/**
	 * 
	 * 
	 * @param description
	 * @param project
	 */
	public static void write(IProject project,
			WebflowProjectDescription description) {
		IFile file = project
				.getFile(new Path(IWebflowProject.DESCRIPTION_FILE));
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				XMLWriter writer = new XMLWriter(os);
				write(description, writer);
				writer.flush();
				writer.close();
			}
			finally {
				os.close();
			}
			if (!file.exists()) {
				file.create(new ByteArrayInputStream(os.toByteArray()),
						IResource.NONE, null);
			}
			else {
				file.setContents(new ByteArrayInputStream(os.toByteArray()),
						IResource.FORCE, null);
			}
		}
		catch (IOException e) {
		}
		catch (CoreException e) {
		}
	}

	/**
	 * 
	 * 
	 * @param description
	 * @param writer
	 * 
	 * @throws IOException
	 */
	protected static void write(WebflowProjectDescription description,
			XMLWriter writer) throws IOException {
		writer.startTag(PROJECT_DESCRIPTION, null);
		write(CONFIGS, CONFIG, description.getConfigs(), writer);
		writer.endTag(PROJECT_DESCRIPTION);
	}

	/**
	 * 
	 * 
	 * @param elementTagName
	 * @param writer
	 * @param name
	 * @param configs
	 * 
	 * @throws IOException
	 */
	protected static void write(String name, String elementTagName,
			List<IWebflowConfig> configs, XMLWriter writer) throws IOException {
		writer.startTag(name, null);
		if (configs != null) {
			for (IWebflowConfig config : configs) {
				writer.startTag(CONFIG, null);
				writer.printSimpleTag(FILE, config.getResource()
						.getProjectRelativePath().toString());
				writer.printSimpleTag(NAME, config.getName());
				Set<IModelElement> beansConfigs = config.getBeansConfigs();
				if (beansConfigs != null) {
					for (IModelElement bc : beansConfigs) {
						writer.printSimpleTag(BEANS_CONFIG, bc.getElementID());
					}
				}
				writer.endTag(CONFIG);
			}
		}
		writer.endTag(name);
	}
}
