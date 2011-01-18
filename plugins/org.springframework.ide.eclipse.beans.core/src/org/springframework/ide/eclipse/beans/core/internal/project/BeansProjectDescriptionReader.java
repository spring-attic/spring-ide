/*******************************************************************************
 * Copyright (c) 2004, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.project;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.SAXParser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class reads the description for a Spring Beans project from an XML file.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansProjectDescriptionReader {

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID + "/project/description/debug";

	public static final boolean DEBUG = SpringCore.isDebug(DEBUG_OPTION);

	/**
	 * Reads project description for given Spring project.
	 */
	public static void read(BeansProject project) {
		IFile file = ((IProject) project.getElementResource()).getFile(new Path(IBeansProject.DESCRIPTION_FILE));
		File rawFile = null;
		if (file != null && file.getLocation() != null) {
			rawFile = file.getLocation().toFile();
		}
		if (!(rawFile != null && rawFile.exists() && rawFile.canRead())) {
			file = ((IProject) project.getElementResource()).getFile(new Path(IBeansProject.DESCRIPTION_FILE_OLD));
			if (file != null && file.getLocation() != null) {
				rawFile = file.getLocation().toFile();
			}
		}
		
		if (rawFile != null && rawFile.exists() && rawFile.canRead()) {
			readDescriptionFromFile(project, file, rawFile);
		}
		
		// Add default config extension to project
		project.addConfigSuffix(IBeansProject.DEFAULT_CONFIG_SUFFIX);
	}

	private static void readDescriptionFromFile(BeansProject project, IFile file, File rawFile) {

		if (DEBUG) {
			System.out.println("Reading project description from " + file.getLocation().toString());
		}

		BufferedInputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(rawFile));
			BeansProjectDescriptionHandler handler = new BeansProjectDescriptionHandler(project);

			try {
				SAXParser parser = SpringCoreUtils.getSaxParser();
				parser.parse(new InputSource(is), handler);
			}
			catch (SAXException e) {
				handler.log(IStatus.ERROR, e);
			}
			catch (IOException e) {
				handler.log(IStatus.ERROR, e);
			}

			IStatus status = handler.getStatus();
			switch (status.getSeverity()) {
			case IStatus.ERROR:
				BeansCorePlugin.log(status);
				break;

			case IStatus.WARNING:
			case IStatus.INFO:
				BeansCorePlugin.log(status);
			}
		}
		catch (FileNotFoundException e) {
			BeansCorePlugin.log(e);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException e) {
					// ignore
				}
			}
		}
	}

}
