/*******************************************************************************
 * Copyright (c) 2005, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.project;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.SAXParser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Description reader for the .springWebflow file
 * @author Christian Dupuis
 */
public class WebflowProjectDescriptionReader {

	/**
	 * Reads project description for given project.
	 * @param project
	 * @return
	 */
	public static WebflowProjectDescription read(IWebflowProject project) {
		IFile file = project.getProject().getFile(new Path(IWebflowProject.DESCRIPTION_FILE));
		File rawFile = null;
		if (file != null && file.getLocation() != null) {
			rawFile = file.getLocation().toFile();
		}
		if (!(rawFile != null && rawFile.exists() && rawFile.canRead())) {
			file = project.getProject().getFile(new Path(IWebflowProject.DESCRIPTION_FILE_OLD));
			if (file != null && file.getLocation() != null) {
				rawFile = file.getLocation().toFile();
			}
		}

		if (rawFile != null && rawFile.exists() && rawFile.canRead()) {
			BufferedInputStream is = null;
			try {
				// Force resource refresh in case resource is not in sync
				is = new BufferedInputStream(new FileInputStream(rawFile));
				WebflowProjectDescriptionHandler handler = new WebflowProjectDescriptionHandler(project);
				try {
					SAXParser parser = SpringCoreUtils.getSaxParser();
					parser.parse(new InputSource(is), handler);
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
			catch (FileNotFoundException e) {
				Activator.log(e);
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
