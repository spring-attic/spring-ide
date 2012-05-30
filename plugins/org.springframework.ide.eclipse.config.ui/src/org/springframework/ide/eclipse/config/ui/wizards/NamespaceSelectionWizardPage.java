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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springsource.ide.eclipse.commons.ui.StsUiImages;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * {@link WizardPage} that displays a list of {@link INamespaceDefinition}s to
 * the user in order to allow for selecting the desired XSD namespace
 * declarations.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class NamespaceSelectionWizardPage extends WizardPage {

	private class VersionContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public Object[] getElements(Object obj) {
			if (obj instanceof INamespaceDefinition) {
				return ((INamespaceDefinition) obj).getSchemaLocations().toArray();
			}
			else {
				return new Object[0];
			}
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public class VersionLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof String) {
				String label = (String) element;
				if (selectedNamespaceDefinition != null
						&& label.equals(selectedNamespaceDefinition.getDefaultSchemaLocation(file))) {
					label += " " + ConfigWizardsMessages.NamespaceConfig_default;
				}
				return label;
			}
			return super.getText(element);
		}
	}

	/**
	 * Content provider that supplies the list of available namespaces that are
	 * declared to the tooling via an extension point or that already appear in
	 * the XML configuration.
	 */
	private class XsdConfigContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public Object[] getElements(Object obj) {
			return getNamespaceDefinitionList().toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public class XsdLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			if (element instanceof INamespaceDefinition) {
				INamespaceDefinition xsdDef = (INamespaceDefinition) element;
				return xsdDef.getNamespaceImage();
			}
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof INamespaceDefinition) {
				INamespaceDefinition xsdDef = (INamespaceDefinition) element;
				return xsdDef.getNamespacePrefix(file) + " - " + xsdDef.getNamespaceURI();
			}
			return "";
		}
	}

	public static final String PAGE_NAME = "xsdPage";

	private static final int XSD_LIST_VIEWER_HEIGHT = 150;

	private static final int LIST_VIEWER_WIDTH = 340;

	private CheckboxTableViewer xsdViewer;

	private CheckboxTableViewer versionViewer;

	private final Map<INamespaceDefinition, String> selectedNamespaceVersionMap = new HashMap<INamespaceDefinition, String>();

	private INamespaceDefinition selectedNamespaceDefinition;

	private final Element beansElement;

	private List<INamespaceDefinition> namespaceDefinitionList = null;

	private INamespaceDefinition defaultNamespace = null;

	private final IFile file;

	protected NamespaceSelectionWizardPage(String pageName, Element beansElement, IFile file) {
		super(pageName);
		this.beansElement = beansElement;
		this.file = file;
		setTitle(ConfigWizardsMessages.NamespaceConfig_title);
		setDescription(ConfigWizardsMessages.NamespaceConfig_xsdDescription);
	}

	private String attributeNameToPrefix(String attributeName, String uri) {
		int colonIndex = attributeName.indexOf(":");
		if (attributeName.length() > 6 && attributeName.indexOf(":") > 0) {
			return attributeName.substring(colonIndex + 1, attributeName.length());
		}
		else if (attributeName.equals("xmlns")) {
			return uri.substring(uri.lastIndexOf("/") + 1);
		}
		else {
			return null;
		}
	}

	private void checkForDefaultNamespace() {

		if (defaultNamespace == null) {
			return;
		}

		Object[] checkedElements = xsdViewer.getCheckedElements();
		if (checkedElements != null) {

			for (Object currElement : checkedElements) {
				if (currElement.equals(defaultNamespace)) {
					this.setErrorMessage(null);
					setDescription(ConfigWizardsMessages.NamespaceConfig_xsdDescription);
					this.setPageComplete(true);
					return;
				}
			}
			this.setErrorMessage(ConfigWizardsMessages.NamespaceConfig_mustIncludeDefault);
			this.setPageComplete(false);
		}

	}

	public void createControl(Composite parent) {
		namespaceDefinitionList = getNamespaceDefinitionList();
		defaultNamespace = getDefaultNamespaceDefinition(namespaceDefinitionList);

		initializeDialogUnits(parent);
		// top level group
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginTop = 5;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());
		setControl(composite);

		Label namespaceLabel = new Label(composite, SWT.NONE);
		namespaceLabel.setText(ConfigWizardsMessages.NamespaceConfig_selectNamespace);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = LIST_VIEWER_WIDTH;
		gd.heightHint = XSD_LIST_VIEWER_HEIGHT;

		// config set list viewer
		xsdViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		xsdViewer.getTable().setLayoutData(gd);
		xsdViewer.setContentProvider(new XsdConfigContentProvider());
		xsdViewer.setLabelProvider(new XsdLabelProvider());
		xsdViewer.setInput(this); // activate content provider
		xsdViewer.setCheckedElements(getPreselectedElements());

		xsdViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
					selectedNamespaceDefinition = (INamespaceDefinition) obj;
					versionViewer.setInput(obj);
					if (selectedNamespaceVersionMap.containsKey(selectedNamespaceDefinition)) {
						versionViewer.setCheckedElements(new Object[] { selectedNamespaceVersionMap.get(
								selectedNamespaceDefinition).trim() });
					}
					if (xsdViewer.getChecked(obj) && selectedNamespaceDefinition.getSchemaLocations().size() > 0) {
						versionViewer.getControl().setEnabled(true);
					}
					else {
						versionViewer.getControl().setEnabled(false);
					}

					checkForDefaultNamespace();
				}
			}
		});

		xsdViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(final CheckStateChangedEvent event) {
				Object obj = event.getElement();
				selectedNamespaceDefinition = (INamespaceDefinition) obj;
				versionViewer.setInput(obj);
				if (selectedNamespaceVersionMap.containsKey(selectedNamespaceDefinition)) {
					versionViewer.setCheckedElements(new Object[] { selectedNamespaceVersionMap.get(
							selectedNamespaceDefinition).trim() });
				}

				if (event.getChecked() && selectedNamespaceDefinition != null
						&& selectedNamespaceDefinition.getSchemaLocations().size() > 0) {
					versionViewer.getControl().setEnabled(true);
				}
				else {
					versionViewer.getControl().setEnabled(false);
				}

			}
		});

		Label versionLabel = new Label(composite, SWT.NONE);
		versionLabel.setText(ConfigWizardsMessages.NamespaceConfig_selectSpecificXsd);

		versionViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		versionViewer.getTable().setLayoutData(gd);
		versionViewer.setContentProvider(new VersionContentProvider());
		versionViewer.setLabelProvider(new VersionLabelProvider());
		versionViewer.setSorter(new ViewerSorter());

		versionViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(final CheckStateChangedEvent event) {
				if (event.getChecked()) {
					versionViewer.setCheckedElements(new Object[] { event.getElement() });
					if (selectedNamespaceDefinition != null) {
						selectedNamespaceVersionMap.put(selectedNamespaceDefinition, (String) event.getElement());
					}
				}
				else {
					versionViewer.setCheckedElements(new Object[0]);
					selectedNamespaceVersionMap.remove(selectedNamespaceDefinition);
				}
			}
		});
	}

	/**
	 * Returns true if the given namespaceDefinition exists as an attribute in
	 * the XML configuration file.
	 */
	private boolean existsInConfiguration(INamespaceDefinition namespaceDefinition) {
		NamedNodeMap attributeMap = beansElement.getAttributes();
		for (int i = 0; i < attributeMap.getLength(); i++) {
			Node currItem = attributeMap.item(i);
			// Regular namespace case
			if (currItem.getNodeName().equals("xmlns:" + namespaceDefinition.getNamespacePrefix(file))) {
				return true;
			}
			// Default namespace case
			else if (currItem.getNodeName().equals("xmlns")
					&& currItem.getNodeValue().equals(namespaceDefinition.getNamespaceURI())) {
				return true;
			}
		}
		return false;
	}

	private INamespaceDefinition getDefaultNamespaceDefinition(List<INamespaceDefinition> namespaceDefinitions) {
		NamedNodeMap beanAttributes = beansElement.getAttributes();
		for (int i = 0; i < beanAttributes.getLength(); i++) {
			Node currAttributeNode = beanAttributes.item(i);
			String currAttributeName = currAttributeNode.getNodeName();
			String uri = currAttributeNode.getNodeValue();
			if (currAttributeName.equalsIgnoreCase("xmlns")) {
				for (INamespaceDefinition currNamespaceDefinition : namespaceDefinitions) {
					if (currNamespaceDefinition.getNamespaceURI().equals(uri)) {
						return currNamespaceDefinition;
					}
				}
			}
		}
		return null;
	}

	private List<INamespaceDefinition> getNamespaceDefinitionList() {
		if (namespaceDefinitionList == null) {
			namespaceDefinitionList = new ArrayList<INamespaceDefinition>();

			// Add namespaces declared using the extension point
			namespaceDefinitionList.addAll(NamespaceUtils.getNamespaceDefinitions());

			// Add any namespaces that exist in the XML document but aren't
			// already known to the tooling via the extension point
			NamedNodeMap beanAttributes = beansElement.getAttributes();
			for (int i = 0; i < beanAttributes.getLength(); i++) {
				Node currAttributeNode = beanAttributes.item(i);
				String currAttributeName = currAttributeNode.getNodeName();
				String uri = currAttributeNode.getNodeValue();
				if (isUnknownNamespace(currAttributeName, uri, namespaceDefinitionList)) {
					String schemaVersion = getSchemaVersionFromXml(uri);
					INamespaceDefinition namespaceDefinition = new DefaultNamespaceDefinition(attributeNameToPrefix(
							currAttributeName, uri), uri, schemaVersion, StsUiImages.XML_FILE.createImage());
					if (!"".equals(schemaVersion)) {
						this.selectedNamespaceVersionMap.put(namespaceDefinition, schemaVersion);
					}
					namespaceDefinitionList.add(namespaceDefinition);
				}
			}
		}
		return namespaceDefinitionList;
	}

	/**
	 * Return the namespace definitions that are already declared in the
	 * configuration file.
	 */
	private Object[] getPreselectedElements() {
		Set<INamespaceDefinition> checkedElements = new HashSet<INamespaceDefinition>();
		Object[] availableNamespaces = ((IStructuredContentProvider) xsdViewer.getContentProvider()).getElements(null);

		for (Object currAvailableNamespace : availableNamespaces) {
			INamespaceDefinition currNamespaceDefinition = (INamespaceDefinition) currAvailableNamespace;
			if (existsInConfiguration(currNamespaceDefinition)) {
				checkedElements.add(currNamespaceDefinition);
				String existingVersion = getSchemaVersionFromXml(currNamespaceDefinition.getNamespaceURI());
				if (!"".equals(existingVersion)) {
					selectedNamespaceVersionMap.put(currNamespaceDefinition, existingVersion);
				}
			}
		}

		return checkedElements.toArray();
	}

	private String getSchemaVersionFromXml(String namespaceUri) {
		String schemaLocationValue = beansElement.getAttribute(NamespaceConfigWizard.ATTR_SCHEMA_LOCATION);

		// Remove all line breaks and tabs
		schemaLocationValue = schemaLocationValue.replaceAll("\\n|\\t", " ");
		// Remove any extra spaces
		schemaLocationValue = schemaLocationValue.replaceAll(" +", " ");
		// Split along spaces into just the schema content
		String[] schemaVersions = schemaLocationValue.split(" ");

		for (int i = 0; i < schemaVersions.length; i++) {
			String currSchema = schemaVersions[i];
			if (currSchema.equals(namespaceUri) && (i + 1) < schemaVersions.length) {
				// String after the schema is the version
				return schemaVersions[i + 1];
			}
		}

		return "";
	}

	public Map<INamespaceDefinition, String> getSchemaVersions() {
		return selectedNamespaceVersionMap;
	}

	public List<INamespaceDefinition> getXmlSchemaDefinitions() {
		List<INamespaceDefinition> defs = new ArrayList<INamespaceDefinition>();
		Object[] checkedElements = xsdViewer.getCheckedElements();
		if (checkedElements != null) {
			for (Object element : checkedElements) {
				defs.add((INamespaceDefinition) element);
			}
		}
		return defs;
	}

	private boolean isUnknownNamespace(String attributeName, String namespaceUri,
			List<INamespaceDefinition> namespaceDefinitionList) {

		// Ignore xsi
		if (attributeName.toLowerCase().startsWith("xmlns:xsi")) {
			return false;
		}

		// Check non-default namespace case
		if (attributeName.toLowerCase().startsWith("xmlns:")
				&& !namespaceAttributeExistsInList(attributeName, namespaceUri, namespaceDefinitionList)
				&& attributeNameToPrefix(attributeName, namespaceUri) != null) {
			return true;
		}

		// Check default namespace case
		if (attributeName.equalsIgnoreCase("xmlns")
				&& !namespaceAttributeExistsInList(attributeName, namespaceUri, namespaceDefinitionList)) {
			return true;
		}

		return false;
	}

	private boolean namespaceAttributeExistsInList(String attributeName, String namespaceUri,
			List<INamespaceDefinition> namespaces) {
		String namespacePrefix = attributeNameToPrefix(attributeName, namespaceUri);
		for (INamespaceDefinition namespaceDefinition : namespaces) {
			INamespaceDefinition currNamespaceDefinition = namespaceDefinition;
			if (currNamespaceDefinition.getNamespacePrefix(file).equals(namespacePrefix)
					|| namespaceUri.equalsIgnoreCase(currNamespaceDefinition.getNamespaceURI())) {
				return true;
			}
		}
		return false;
	}
}
