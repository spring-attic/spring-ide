/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
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
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.wst.sse.core.internal.encoding.CommonEncodingPreferenceNames;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * {@link WizardNewFileCreationPage} that enables to select a folder and a name for the new
 * {@link IBeansConfig}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class NewBeansConfigFilePage extends WizardNewFileCreationPage {

	private Map<INamespaceDefinition, String> schemaVersions;

	private List<INamespaceDefinition> xmlSchemaDefinitions;

	private Button addNature;

	public NewBeansConfigFilePage(String pageName, IStructuredSelection selection) {
		super(pageName, selection);
		setTitle(BeansWizardsMessages.NewConfig_title);
		setDescription(BeansWizardsMessages.NewConfig_fileDescription);
	}

	protected InputStream createXMLDocument() throws Exception {

		final IPath containerPath = getContainerFullPath();
		IPath newFilePath = containerPath.append(getFileName());
		final IFile newFileHandle = createFileHandle(newFilePath);

		// Get the line separator from the platform configuration
		String lineSeparator = SpringCoreUtils.getLineSeparator((String) null, newFileHandle
				.getProject());

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String charSet = getUserPreferredCharset();
		INamespaceDefinition defaultXsd = NamespaceUtils.getDefaultNamespaceDefinition();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, charSet));
		writer.println("<?xml version=\"1.0\" encoding=\"" + charSet + "\"?>"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.println("<beans xmlns=\"" + defaultXsd.getNamespaceURI() + "\"" + lineSeparator
				+ "\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator
				+ getNamespaceMappings(lineSeparator) + "\txsi:schemaLocation=\""
				+ getSchemaLocations(lineSeparator) + "\">" + lineSeparator + lineSeparator
				+ lineSeparator + "</beans>");
		writer.flush();
		outputStream.close();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		return inputStream;
	}

	protected InputStream getInitialContents() {
		try {
			return createXMLDocument();
		}
		catch (Exception e) {
		}
		return null;
	}

	private String getNamespaceMappings(String lineSeparator) {
		INamespaceDefinition defaultXsd = NamespaceUtils.getDefaultNamespaceDefinition();
		StringBuilder builder = new StringBuilder();
		for (INamespaceDefinition def : xmlSchemaDefinitions) {
			if (!def.equals(defaultXsd)) {
				builder.append("\t");
				builder.append("xmlns:");
				builder.append(def.getNamespacePrefix());
				builder.append("=\"");
				builder.append(def.getNamespaceURI());
				builder.append("\"");
				builder.append(lineSeparator);
			}
		}
		return builder.toString();
	}

	private String getSchemaLocations(String lineSeparator) {
		StringBuilder builder = new StringBuilder();

		INamespaceDefinition defaultXsd = NamespaceUtils.getDefaultNamespaceDefinition();
		builder.append("\t\t");
		builder.append(defaultXsd.getNamespaceURI());
		builder.append(" ");
		if (schemaVersions.containsKey(defaultXsd)) {
			builder.append(schemaVersions.get(defaultXsd));
		}
		else {
			builder.append(defaultXsd.getDefaultSchemaLocation());
		}
		builder.append(lineSeparator);

		for (INamespaceDefinition def : xmlSchemaDefinitions) {
			if (def.getDefaultSchemaLocation() != null && !def.equals(defaultXsd)) {
				builder.append("\t\t");
				builder.append(def.getNamespaceURI());
				builder.append(" ");
				if (schemaVersions.containsKey(def)) {
					builder.append(schemaVersions.get(def));
				}
				else {
					builder.append(def.getDefaultSchemaLocation());
				}
				builder.append(lineSeparator);
			}
		}
		return builder.toString().trim();
	}

	private String getUserPreferredCharset() {
		Preferences preference = XMLCorePlugin.getDefault().getPluginPreferences();
		String charSet = preference.getString(CommonEncodingPreferenceNames.OUTPUT_CODESET);
		return charSet;
	}

	public void setSchemaVersions(Map<INamespaceDefinition, String> schemaVersions) {
		this.schemaVersions = schemaVersions;
	}

	public void setXmlSchemaDefinitions(List<INamespaceDefinition> xmlSchemaDefinitions) {
		this.xmlSchemaDefinitions = xmlSchemaDefinitions;
		Collections.sort(this.xmlSchemaDefinitions, new Comparator<INamespaceDefinition>() {
			public int compare(INamespaceDefinition o1, INamespaceDefinition o2) {
				return o1.getNamespacePrefix().compareTo(o2.getNamespacePrefix());
			}
		});
	}

	@Override
	protected boolean validatePage() {
		if (super.validatePage()) {
			IPath path = getContainerFullPath();
			if (path != null && path.segment(0) != null) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
						path.segment(0));
				if (!SpringCoreUtils.isSpringProject(project) && !addNature.getSelection()) {
					setErrorMessage("Selected folder does not belong to a Spring project.");
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void createAdvancedControls(Composite parent) {
		super.createAdvancedControls(parent);
		addNature = new Button(parent, SWT.CHECK);
		addNature.setText("Add Spring project nature if required");
		addNature.setSelection(true);
		addNature.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}
		}); 
	}
}
