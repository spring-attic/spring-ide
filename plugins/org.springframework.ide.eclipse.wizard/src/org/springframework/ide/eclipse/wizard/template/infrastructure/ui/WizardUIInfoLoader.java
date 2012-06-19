/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure.ui;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.thoughtworks.xstream.XStream;
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

}
