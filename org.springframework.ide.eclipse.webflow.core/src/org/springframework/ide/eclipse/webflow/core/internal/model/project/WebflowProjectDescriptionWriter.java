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
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.io.xml.XMLWriter;
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
				Set<IBeansConfig> beansConfigs = config.getBeansConfigs();
				if (beansConfigs != null) {
					for (IBeansConfig bc : beansConfigs) {
						writer.printSimpleTag(BEANS_CONFIG, bc.getElementID());
					}
				}
				writer.endTag(CONFIG);
			}
		}
		writer.endTag(name);
	}
}