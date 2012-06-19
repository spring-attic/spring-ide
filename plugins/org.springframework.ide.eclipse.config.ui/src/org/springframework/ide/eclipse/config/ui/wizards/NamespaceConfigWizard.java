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
package org.springframework.ide.eclipse.config.ui.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.format.IStructuredFormatProcessor;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springsource.ide.eclipse.commons.ui.StsUiImages;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Wizard that displays the namespace configuration page.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class NamespaceConfigWizard extends Wizard implements INewWizard {

	protected static final String ATTR_NAMESPACE_PREFIX = "xmlns:";

	protected static final String ATTR_SCHEMA_LOCATION = "xsi:schemaLocation";

	private NamespaceSelectionWizardPage namespacePage = null;

	private Element beansElement = null;

	private final IStructuredFormatProcessor formatProcessor;

	private IStructuredModel model;

	private EditorPart editor;

	private final IFile file;

	public NamespaceConfigWizard(IFile xmlConfigFile) throws CoreException {
		formatProcessor = new ShallowFormatProcessorXML();
		this.file = xmlConfigFile;
		this.beansElement = getBeansXmlElement(xmlConfigFile);
		setDialogSettings(BeansUIPlugin.getDefault().getDialogSettings());
		setDefaultPageImageDescriptor(StsUiImages.NAMESPACE_CONFIG_ICON);
		this.setWindowTitle(ConfigWizardsMessages.NamespaceConfig_windowTitle);
	}

	public NamespaceConfigWizard(IFile xmlConfigFile, EditorPart editor) throws CoreException {
		this(xmlConfigFile);
		this.editor = editor;
	}

	@Override
	public void addPages() {
		super.addPages();
		namespacePage = new NamespaceSelectionWizardPage(NamespaceSelectionWizardPage.PAGE_NAME, beansElement, file);
		addPage(namespacePage);
	}

	/**
	 * Ensures that each namespace selected in the dialog is declared in the
	 * configuration file
	 */
	private void addSelectedNamespaces() {
		List<INamespaceDefinition> namespaces = namespacePage.getXmlSchemaDefinitions();
		Map<INamespaceDefinition, String> schemaVersions = namespacePage.getSchemaVersions();

		String defaultNamespaceUri = beansElement.getAttribute("xmlns");

		for (INamespaceDefinition currNamespaceDefinition : namespaces) {

			// Set the namespace declaration XML attribute. Skip the default
			// namespace that doesn't change
			if (!currNamespaceDefinition.getNamespaceURI().equals(defaultNamespaceUri)) {
				beansElement.setAttribute(ATTR_NAMESPACE_PREFIX + currNamespaceDefinition.getNamespacePrefix(file),
						currNamespaceDefinition.getNamespaceURI());
			}

			if (currNamespaceDefinition.getDefaultSchemaLocation(file) != null) {
				String schemaLocationAttrVal = beansElement.getAttribute(ATTR_SCHEMA_LOCATION);

				String currNamespaceDefSchemaLocationVal = getSchemaLocationValue(currNamespaceDefinition,
						schemaVersions);

				// Append the new schema location
				if (schemaLocationAttrVal == null) {
					schemaLocationAttrVal = currNamespaceDefSchemaLocationVal;
				}
				else {
					schemaLocationAttrVal += "\n" + "\t\t" + currNamespaceDefSchemaLocationVal;
				}

				beansElement.setAttribute(ATTR_SCHEMA_LOCATION, schemaLocationAttrVal.trim());
			}

		}
	}

	@Override
	public void dispose() {
		if (model != null) {
			model.releaseFromEdit();
			model = null;
		}
		super.dispose();
	}

	/**
	 * Formats the attributes of the beans XML element. The beans element
	 * contains bean elements that may have special formatting that shouldn't be
	 * altered. However, the API doesn't support formatting just the attributes.
	 * So this method removes children, formats, and re-adds them.
	 */
	@SuppressWarnings("unused")
	private void formatNamespaces() {
		NodeList childNodes = beansElement.getChildNodes();
		List<Node> childrenToRemove = new ArrayList<Node>();
		List<Node> childNodeCopies = new ArrayList<Node>();
		for (int i = 0; i < childNodes.getLength(); i++) {
			childrenToRemove.add(childNodes.item(i));
			childNodeCopies.add(childNodes.item(i).cloneNode(true));
		}

		for (int i = 0; i < childrenToRemove.size(); i++) {
			beansElement.removeChild(childrenToRemove.get(i));
		}
		formatProcessor.formatNode(beansElement);

		for (int i = 0; i < childNodeCopies.size(); i++) {
			beansElement.appendChild(childNodeCopies.get(i));
		}
	}

	private Element getBeansXmlElement(IFile xmlConfigFile) throws CoreException {
		IModelManager modelManager = StructuredModelManager.getModelManager();
		try {
			model = modelManager.getModelForEdit(xmlConfigFile);
		}
		catch (CoreException e) {
			throw new CoreException(new Status(Status.ERROR, ConfigUiPlugin.PLUGIN_ID,
					"Could not open model for editing"));
		}
		catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, ConfigUiPlugin.PLUGIN_ID,
					"Could not open model for editing"));
		}

		if (model == null) {
			throw new CoreException(new Status(Status.ERROR, ConfigUiPlugin.PLUGIN_ID,
					"Could not open model for editing"));
		}

		IStructuredDocumentRegion beansRegion = null;

		for (IStructuredDocumentRegion currRegion : model.getStructuredDocument().getStructuredDocumentRegions()) {
			if (currRegion.getFullText().startsWith("<beans")) {
				beansRegion = currRegion;
				break;
			}
		}

		if (beansRegion == null) {
			throw new CoreException(new Status(Status.ERROR, ConfigUiPlugin.PLUGIN_ID,
					"Could not open model for editing"));
		}

		Element beansXmlElement = (Element) model.getIndexedRegion(beansRegion.getStartOffset());

		if (beansXmlElement == null || !beansXmlElement.getNodeName().startsWith("beans")) {
			throw new CoreException(new Status(Status.ERROR, ConfigUiPlugin.PLUGIN_ID,
					"Could not open model for editing"));
		}

		return beansXmlElement;

	}

	private String getSchemaLocationValue(INamespaceDefinition namespaceDefinition,
			Map<INamespaceDefinition, String> schemaVersions) {
		String schemaVersion = schemaVersions.get(namespaceDefinition);
		if (schemaVersion == null) {
			schemaVersion = namespaceDefinition.getDefaultSchemaLocation(file);
		}
		return namespaceDefinition.getNamespaceURI() + " " + schemaVersion;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setNeedsProgressMonitor(true);
		setWindowTitle(ConfigWizardsMessages.NamespaceConfig_windowTitle);
		setDefaultPageImageDescriptor(BeansUIImages.DESC_WIZ_CONFIG);
	}

	@Override
	public boolean performFinish() {
		if (editor != null && editor.getAdapter(StructuredTextEditor.class) != null) {
			StructuredTextEditor textEditor = (StructuredTextEditor) editor.getAdapter(StructuredTextEditor.class);
			textEditor.getTextViewer().setRedraw(false);
			performFinishHelper();
			textEditor.getTextViewer().setRedraw(true);
		}
		else {
			performFinishHelper();
		}
		return true;
	}

	private void performFinishHelper() {
		model.beginRecording(this);
		removeNamespaceInformation();
		addSelectedNamespaces();
		formatProcessor.formatNode(beansElement);
		model.endRecording(this);
	}

	/**
	 * Removes all namespaces except the default
	 */
	private void removeNamespaceInformation() {
		beansElement.removeAttribute(ATTR_SCHEMA_LOCATION);
		NamedNodeMap beanAttributes = beansElement.getAttributes();
		List<String> attributesToRemove = new ArrayList<String>();
		for (int i = 0; i < beanAttributes.getLength(); i++) {
			Node currAttributeNode = beanAttributes.item(i);
			String currAttributeName = currAttributeNode.getNodeName();
			if (currAttributeName.toLowerCase().startsWith(ATTR_NAMESPACE_PREFIX)
					&& !currAttributeName.toLowerCase().startsWith("xmlns:xsi")) {
				attributesToRemove.add(currAttributeName);
			}

		}
		for (String currAttributeName : attributesToRemove) {
			beansElement.removeAttribute(currAttributeName);
		}
	}

}
