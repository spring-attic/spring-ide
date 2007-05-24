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
package org.springframework.ide.eclipse.beans.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.wst.sse.core.internal.encoding.CommonEncodingPreferenceNames;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;

/**
 * {@link WizardNewFileCreationPage} that enables to select a folder and a name
 * for the new {@link IBeansConfig}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class NewBeansConfigFilePage extends WizardNewFileCreationPage {

	private List<INamespaceDefinition> xmlSchemaDefinitions;

	public NewBeansConfigFilePage(String pageName,
			IStructuredSelection selection) {
		super(pageName, selection);
		setTitle(BeansWizardsMessages.NewConfig_title);
		setDescription(BeansWizardsMessages.NewConfig_fileDescription);
	}

	protected InputStream createXMLDocument() throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String charSet = getUserPreferredCharset();
		INamespaceDefinition defaultXsd = NamespaceUtils
				.getDefaultNamespaceDefinition();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				outputStream, charSet));
		writer.println("<?xml version=\"1.0\" encoding=\"" + charSet + "\"?>"); //$NON-NLS-1$ //$NON-NLS-2$
		writer
				.println("<beans xmlns=\""
						+ defaultXsd.getNamespaceURI()
						+ "\"\r\n"
						+ "\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
						+ getNamespaceMappings() + "\txsi:schemaLocation=\""
						+ getSchemaLocations() + "\">\r\n" + "\r\n" + "\r\n"
						+ "</beans>");
		writer.flush();
		outputStream.close();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				outputStream.toByteArray());
		return inputStream;
	}

	private String getNamespaceMappings() {
		StringBuilder builder = new StringBuilder();
		for (INamespaceDefinition def : xmlSchemaDefinitions) {
			builder.append("\t");
			builder.append("xmlns:");
			builder.append(def.getNamespacePrefix());
			builder.append("=\"");
			builder.append(def.getNamespaceURI());
			builder.append("\"\r\n");
		}
		return builder.toString();
	}

	private String getSchemaLocations() {
		StringBuilder builder = new StringBuilder();

		INamespaceDefinition defaultXsd = NamespaceUtils
				.getDefaultNamespaceDefinition();
		builder.append("\t\t");
		builder.append(defaultXsd.getNamespaceURI());
		builder.append(" ");
		builder.append(defaultXsd.getSchemaLocation());
		builder.append("\r\n");

		for (INamespaceDefinition def : xmlSchemaDefinitions) {
			builder.append("\t\t");
			builder.append(def.getNamespaceURI());
			builder.append(" ");
			builder.append(def.getSchemaLocation());
			builder.append("\r\n");
		}
		return builder.toString().trim();
	}

	protected InputStream getInitialContents() {
		try {
			return createXMLDocument();
		}
		catch (Exception e) {
		}
		return null;
	}

	private String getUserPreferredCharset() {
		Preferences preference = XMLCorePlugin.getDefault()
				.getPluginPreferences();
		String charSet = preference
				.getString(CommonEncodingPreferenceNames.OUTPUT_CODESET);
		return charSet;
	}

	public void setXmlSchemaDefinitions(
			List<INamespaceDefinition> xmlSchemaDefinitions) {
		this.xmlSchemaDefinitions = xmlSchemaDefinitions;
		Collections.sort(this.xmlSchemaDefinitions,
				new Comparator<INamespaceDefinition>() {
					public int compare(INamespaceDefinition o1,
							INamespaceDefinition o2) {
						return o1.getNamespacePrefix().compareTo(
								o2.getNamespacePrefix());
					}
				});
	}
}
