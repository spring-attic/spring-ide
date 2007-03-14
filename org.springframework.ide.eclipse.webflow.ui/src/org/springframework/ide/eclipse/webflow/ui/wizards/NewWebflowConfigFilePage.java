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

package org.springframework.ide.eclipse.webflow.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.wst.sse.core.internal.encoding.CommonEncodingPreferenceNames;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class NewWebflowConfigFilePage extends WizardNewFileCreationPage {

	public NewWebflowConfigFilePage(String pageName,
			IStructuredSelection selection) {
		super(pageName, selection);
		setDescription("Select the location and name of the Spring Web Flow definition file");
	}

	protected InputStream getInitialContents() {
		try {
			return createXMLDocument();
		}
		catch (Exception e) {
		}
		return null;
	}

	protected InputStream createXMLDocument() throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String charSet = getUserPreferredCharset();

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				outputStream, charSet));
		writer.println("<?xml version=\"1.0\" encoding=\"" + charSet + "\"?>"); //$NON-NLS-1$ //$NON-NLS-2$
		writer
				.println("<flow xmlns=\"http://www.springframework.org/schema/webflow\"\r\n"
						+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
						+ "	xsi:schemaLocation=\"http://www.springframework.org/schema/webflow\r\n"
						+ "        http://www.springframework.org/schema/webflow/spring-webflow-1.0.xsd\">\r\n"
						+ "\r\n"
						+ "	<start-state idref=\"start\" />\r\n"
						+ "	\r\n"
						+ "	<view-state id=\"start\">\r\n"
						+ "	</view-state>\r\n" + "\r\n" + "</flow>");
		writer.flush();
		outputStream.close();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				outputStream.toByteArray());
		return inputStream;
	}

	private String getUserPreferredCharset() {
		Preferences preference = XMLCorePlugin.getDefault()
				.getPluginPreferences();
		String charSet = preference
				.getString(CommonEncodingPreferenceNames.OUTPUT_CODESET);
		return charSet;
	}
}
