/*******************************************************************************
 *  Copyright (c) 2012, 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure.ui;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class WizardUIInfoLoader {
	public WizardUIInfo load(InputStream jsonDescriptionInputStream) throws IOException {
		return load(new InputStreamReader(jsonDescriptionInputStream));
	}

	public WizardUIInfo load(Reader jsonDescriptionReader) throws IOException {
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.alias("info", WizardUIInfo.class);
		xstream.alias("element", WizardUIInfoElement.class);
		xstream.alias("page", WizardUIInfoPage.class);
		return (WizardUIInfo) xstream.fromXML(jsonDescriptionReader);
	}

	public WizardUIInfo load(String jsonDescriptionFile) throws IOException {
		return load(new FileReader(jsonDescriptionFile));
	}

	/**
	 * Retrieves the wizard UI info from a template. Note that the template date
	 * must first be downloaded before attempting to retrieve the wizard UI
	 * info, otherwise a CoreException is thrown. The download process is meant
	 * to be separate from the wizard UI info parsing, as they may occur at
	 * separate stages.
	 *
	 * @param template
	 * @return non-null Wizard ui info, or throws exception if an error occurred
	 * while resolving the info.
	 * @throws CoreException
	 */
	public WizardUIInfo getUIInfo(Template template) throws CoreException {
		if (template == null) {
			return null;
		}
		URL jsonWizardUIDescriptor = template.getTemplateLocation();

		if (jsonWizardUIDescriptor == null) {
			throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
					"Unable to find json descriptor for template: " + template.getName()
							+ ". Missing URL to template location."));
		}

		WizardUIInfo info;
		try {

			InputStream jsonDescriptionInputStream = jsonWizardUIDescriptor.openStream();
			info = load(jsonDescriptionInputStream);
		}
		catch (IOException ex) {
			throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
					"Failed to load json descriptor for wizard page due to: " + ex.getMessage(), ex));

		}
		catch (XStreamException ex) {
			throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
					"Failed to load json descriptor for wizard page due to: " + ex.getMessage(), ex));
		}

		if (info == null) {
			throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
					"Unable to find template project location"));
		}

		return info;
	}

}
