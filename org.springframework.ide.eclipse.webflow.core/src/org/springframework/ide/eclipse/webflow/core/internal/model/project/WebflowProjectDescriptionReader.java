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

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 */
public class WebflowProjectDescriptionReader {

	/**
	 * Reads project description for given project.
	 * 
	 * @param project
	 * 
	 * @return
	 */
	public static WebflowProjectDescription read(IWebflowProject project) {
		IFile file = project.getProject().getFile(
				new Path(IWebflowProject.DESCRIPTION_FILE));
		if (file.isAccessible()) {
			BufferedInputStream is = null;
			try {
				is = new BufferedInputStream(file.getContents());
				WebflowProjectDescriptionHandler handler = new WebflowProjectDescriptionHandler(
						project);
				try {
					SAXParserFactory factory = SAXParserFactory.newInstance();
					factory.setNamespaceAware(true);
					SAXParser parser = factory.newSAXParser();
					parser.parse(new InputSource(is), handler);
				}
				catch (ParserConfigurationException e) {
					handler.log(IStatus.ERROR, e);
				}
				catch (SAXException e) {
					handler.log(IStatus.WARNING, e);
				}
				catch (IOException e) {
					handler.log(IStatus.WARNING, e);
				}
				IStatus status = handler.getStatus();
				switch (status.getSeverity()) {
				case IStatus.ERROR:
					Activator.log(status);
					return null;

				case IStatus.WARNING:
				case IStatus.INFO:
					Activator.log(status);

				case IStatus.OK:
				default:
					return handler.getDescription();
				}
			}
			catch (CoreException e) {
				Activator.log(e.getStatus());
			}
			finally {
				if (is != null) {
					try {
						is.close();
					}
					catch (IOException e) {
					}
				}
			}
		}

		// Return empty project description
		return new WebflowProjectDescription(project);
	}
}