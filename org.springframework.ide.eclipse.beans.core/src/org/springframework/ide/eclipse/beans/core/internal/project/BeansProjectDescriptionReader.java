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

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class reads the description for a Spring Beans project from an XML
 * file.
 *
 * @author Torsten Juergeleit
 */
public class BeansProjectDescriptionReader {

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID +
												   "/project/description/debug";
	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	/**
	 * Reads project description for given project.
	 */
	public static BeansProjectDescription read(IBeansProject project)  {
		IFile file = ((IProject) project.getElementResource()).getFile(new Path(
											   IBeansProject.DESCRIPTION_FILE));
		if (file.isAccessible()) {
			if (DEBUG) {
				System.out.println("Reading project description from " +
								   file.getLocation().toString());
			}
			BufferedInputStream is = null;
			try {
				is = new BufferedInputStream(file.getContents());
				BeansProjectDescriptionHandler handler =
									new BeansProjectDescriptionHandler(project);
				try {
					SAXParserFactory factory = SAXParserFactory.newInstance();
					factory.setNamespaceAware(true);
					SAXParser parser = factory.newSAXParser();
					parser.parse(new InputSource(is), handler);
				} catch (ParserConfigurationException e) {
					handler.log(IStatus.ERROR, e);
				} catch (SAXException e) {
					handler.log(IStatus.ERROR, e);
				} catch (IOException e) {
					handler.log(IStatus.ERROR, e);
				}
				IStatus status = handler.getStatus(); 
				switch (status.getSeverity()) {
					case IStatus.ERROR :
						BeansCorePlugin.log(status);
						break;
		
					case IStatus.WARNING :
					case IStatus.INFO :
						BeansCorePlugin.log(status);
		
					case IStatus.OK :
					default :
						return handler.getDescription();
				}
			} catch (CoreException e) {
				BeansCorePlugin.log(e.getStatus());
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}

		// Return empty project description
		return new BeansProjectDescription(project);
	}
}
