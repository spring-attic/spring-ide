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
package org.springframework.ide.eclipse.config.ui.editors.namespaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionListener;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.springframework.ide.eclipse.config.core.preferences.SpringConfigPreferenceConstants;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigLabelProvider;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigContentProvider;
import org.springsource.ide.eclipse.commons.ui.StsUiImages;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class NamespacesMasterPart extends AbstractConfigMasterPart implements INamespaceDefinitionListener {

	private class CheckStateListener implements ICheckStateListener {

		public void checkStateChanged(CheckStateChangedEvent event) {
			if (event.getElement() != null && event.getElement() instanceof INamespaceDefinition) {
				INamespaceDefinition definition = (INamespaceDefinition) event.getElement();
				updateDefinitionFromCheckState(definition, event.getChecked());
			}
		}

	}

	private class XsdConfigContentProvider extends SpringConfigContentProvider {

		public XsdConfigContentProvider(AbstractConfigFormPage page) {
			super(page);
		}

		@Override
		protected List<String> getChildNames(IDOMElement element) {
			return new ArrayList<String>();
		}

		@Override
		public Object[] getElements(Object inputElement) {
			List<INamespaceDefinition> result = new ArrayList<INamespaceDefinition>();
			if (inputElement instanceof Document) {
				Document document = (Document) inputElement;
				rootElement = document.getDocumentElement();
				result = namespaceDefinitionList;
			}
			return result.toArray();
		}

	}

	private Element rootElement;

	private volatile List<INamespaceDefinition> namespaceDefinitionList = new CopyOnWriteArrayList<INamespaceDefinition>();

	private volatile boolean loading = false;

	private final Set<INamespaceDefinition> selectedNamespaces;

	private final Map<INamespaceDefinition, String> selectedVersions;

	private CheckboxTableViewer xsdViewer;

	private CheckStateListener checkListener;

	private final ShallowFormatProcessorXML formatProcessor;

	private final CountDownLatch lazyInitializationLatch;

	public NamespacesMasterPart(AbstractConfigFormPage page, Composite parent) {
		super(page, parent);
		selectedNamespaces = new HashSet<INamespaceDefinition>();
		selectedVersions = new HashMap<INamespaceDefinition, String>();
		formatProcessor = new ShallowFormatProcessorXML();
		lazyInitializationLatch = new CountDownLatch(1);
		BeansCorePlugin.registerNamespaceDefinitionListener(this);
		getNamespaceDefinitionList();
	}

	protected void addXsdDefinition(INamespaceDefinition definition) {
		if (!existsInConfiguration(definition)) {
			StructuredTextViewer textView = getConfigEditor().getTextViewer();
			IDOMDocument doc = getConfigEditor().getDomDocument();
			doc.getModel().beginRecording(textView);

			if (rootElement == null) {
				// Create a beans element and add the default namespace
				rootElement = doc.createElement(BeansSchemaConstants.ELEM_BEANS);
				doc.appendChild(rootElement);

				INamespaceDefinition defNamespace = NamespaceUtils.getDefaultNamespaceDefinition();
				if (defNamespace != null) {
					if (!xsdViewer.getChecked(defNamespace)) {
						xsdViewer.setChecked(defNamespace, true);
					}
					rootElement.setAttribute(ConfigCoreUtils.ATTR_DEFAULT_NAMESPACE, defNamespace.getNamespaceURI());
				}
				rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Check again so we don't add the default namespace twice
			if (!existsInConfiguration(definition)) {
				rootElement.setAttribute(
						ConfigCoreUtils.ATTR_NAMESPACE_PREFIX
								+ definition.getNamespacePrefix(getConfigEditor().getResourceFile()),
						definition.getNamespaceURI());
			}

			selectedNamespaces.add(definition);
			updateXsdVersion();
			doc.getModel().endRecording(textView);
		}
	}

	private String attributeNameToPrefix(String attributeName, String uri) {
		int colonIndex = attributeName.indexOf(":"); //$NON-NLS-1$
		if (attributeName.length() > 6 && attributeName.indexOf(":") > 0) { //$NON-NLS-1$
			return attributeName.substring(colonIndex + 1, attributeName.length());
		}
		else if (attributeName.equals(ConfigCoreUtils.ATTR_DEFAULT_NAMESPACE)) {
			return uri.substring(uri.lastIndexOf("/") + 1); //$NON-NLS-1$
		}
		else {
			return null;
		}
	}

	@Override
	protected void createButtons(Composite client) {
		// Ignore
	}

	@Override
	protected ColumnViewer createViewer(Composite client) {
		return CheckboxTableViewer.newCheckList(client, SWT.BORDER);
	}

	@Override
	protected SpringConfigContentProvider createViewerContentProvider() {
		return new XsdConfigContentProvider(getFormPage());
	}

	@Override
	protected AbstractConfigLabelProvider createViewerLabelProvider() {
		return new NamespacesLabelProvider(getConfigEditor().getResourceFile());
	}

	@Override
	public void dispose() {
		if (xsdViewer != null && checkListener != null) {
			xsdViewer.removeCheckStateListener(checkListener);
		}
		BeansCorePlugin.unregisterNamespaceDefinitionListener(this);
	}

	/**
	 * Returns true if the given namespaceDefinition exists as an attribute in
	 * the XML configuration file.
	 */
	private boolean existsInConfiguration(INamespaceDefinition namespaceDefinition) {
		if (rootElement != null) {
			NamedNodeMap attributeMap = rootElement.getAttributes();
			for (int i = 0; i < attributeMap.getLength(); i++) {
				Node currItem = attributeMap.item(i);
				// Regular namespace case
				if (currItem.getNodeName().equals(
						ConfigCoreUtils.ATTR_NAMESPACE_PREFIX
								+ namespaceDefinition.getNamespacePrefix(getConfigEditor().getResourceFile()))) {
					return true;
				}
				// Default namespace case
				else if (currItem.getNodeName().equals(ConfigCoreUtils.ATTR_DEFAULT_NAMESPACE)
						&& currItem.getNodeValue().equals(namespaceDefinition.getNamespaceURI())) {
					return true;
				}
			}
			// Special case
			List<String> schemaLocationAttrs = ConfigCoreUtils.parseSchemaLocationAttr(getConfigEditor()
					.getDomDocument());
			if (schemaLocationAttrs != null && schemaLocationAttrs.contains(namespaceDefinition.getNamespaceURI())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		// TODO Auto-generated method stub

	}

	public CountDownLatch getLazyInitializationLatch() {
		return lazyInitializationLatch;
	}

	private synchronized List<INamespaceDefinition> getNamespaceDefinitionList() {
		if ((namespaceDefinitionList == null || namespaceDefinitionList.size() == 0) && !loading) {
			loading = true;
			NamespaceUtils.getNamespaceDefinitions(getConfigEditor().getResourceFile().getProject(),
					new NamespaceUtils.INamespaceDefinitionTemplate() {
						public void doWithNamespaceDefinitions(INamespaceDefinition[] namespaceDefinitions,
								IProject project) {
							List<INamespaceDefinition> newNamespaceDefinitions = new ArrayList<INamespaceDefinition>(
									Arrays.asList(namespaceDefinitions));
							NamespacesMasterPart.this.namespaceDefinitionList = triggerLoadNamespaceDefinitionList(newNamespaceDefinitions);
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									if (getViewer().getControl() != null && !getViewer().getControl().isDisposed()) {
										getViewer().setInput(getConfigEditor().getDomDocument());
										refresh();
									}
								}
							});
							loading = false;
							lazyInitializationLatch.countDown();
						}
					});
		}
		return namespaceDefinitionList;
	}

	/**
	 * Return the namespace definitions that are already declared in the
	 * configuration file.
	 */
	private Object[] getPreselectedElements() {
		Set<INamespaceDefinition> checkedElements = new HashSet<INamespaceDefinition>();
		Object[] availableNamespaces = ((IStructuredContentProvider) getViewer().getContentProvider())
				.getElements(getConfigEditor().getDomDocument());

		for (Object currAvailableNamespace : availableNamespaces) {
			INamespaceDefinition currNamespaceDefinition = (INamespaceDefinition) currAvailableNamespace;
			if (existsInConfiguration(currNamespaceDefinition)) {
				checkedElements.add(currNamespaceDefinition);
				selectedNamespaces.add(currNamespaceDefinition);
				String existingVersion = ConfigCoreUtils.getSelectedSchemaLocation(getConfigEditor().getDomDocument(),
						currNamespaceDefinition.getNamespaceURI());
				if (!"".equals(existingVersion)) { //$NON-NLS-1$
					selectedVersions.put(currNamespaceDefinition, existingVersion);
				}
			}
		}
		return checkedElements.toArray();
	}

	private String getSchemaLocationValue(INamespaceDefinition namespaceDefinition,
			Map<INamespaceDefinition, String> schemaVersions) {
		String schemaVersion = schemaVersions.get(namespaceDefinition);
		if (schemaVersion == null) {
			schemaVersion = namespaceDefinition.getDefaultSchemaLocation(getConfigEditor().getResourceFile());
		}
		return namespaceDefinition.getNamespaceURI() + " " + schemaVersion; //$NON-NLS-1$
	}

	protected Map<INamespaceDefinition, String> getSchemaVersions() {
		return selectedVersions;
	}

	@Override
	protected String getSectionDescription() {
		return Messages.getString("NamespacesMasterPart.MASTER_SECTION_DESCRIPTION"); //$NON-NLS-1$
	}

	@Override
	protected String getSectionTitle() {
		return Messages.getString("NamespacesMasterPart.MASTER_SECTION_TITLE"); //$NON-NLS-1$
	}

	private boolean isUnknownNamespace(String attributeName, String namespaceUri,
			List<INamespaceDefinition> namespaceDefinitionList) {
		// Ignore xsi
		if (attributeName.toLowerCase().startsWith("xmlns:xsi")) { //$NON-NLS-1$
			return false;
		}

		// Check non-default namespace case
		if (attributeName.toLowerCase().startsWith(ConfigCoreUtils.ATTR_NAMESPACE_PREFIX)
				&& !namespaceAttributeExistsInList(attributeName, namespaceUri, namespaceDefinitionList)
				&& attributeNameToPrefix(attributeName, namespaceUri) != null) {
			return true;
		}

		// Check default namespace case
		if (attributeName.equalsIgnoreCase(ConfigCoreUtils.ATTR_DEFAULT_NAMESPACE)
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
			if ((namespacePrefix != null && namespacePrefix.equals(currNamespaceDefinition
					.getNamespacePrefix(getConfigEditor().getResourceFile())))
					|| namespaceUri.equalsIgnoreCase(currNamespaceDefinition.getNamespaceURI())) {
				return true;
			}
		}
		return false;
	}

	public void onNamespaceDefinitionRegistered(INamespaceDefinitionListener.NamespaceDefinitionChangeEvent event) {
		if (shouldRefresh(event)) {
			namespaceDefinitionList.clear();
			getNamespaceDefinitionList();
		}
	}

	public void onNamespaceDefinitionUnregistered(INamespaceDefinitionListener.NamespaceDefinitionChangeEvent event) {
		onNamespaceDefinitionRegistered(event);
	}

	private void openPontDialog() {
		IPreferenceStore prefStore = ConfigUiPlugin.getDefault().getPreferenceStore();
		if (prefStore.getString(SpringConfigPreferenceConstants.PREF_DISPLAY_TABS_DIALOG).equals(
				MessageDialogWithToggle.PROMPT)) {
			MessageDialogWithToggle
					.openInformation(
							getFormPage().getSite().getShell(),
							Messages.getString("NamespacesMasterPart.PONT_DIALOG_TITLE"), //$NON-NLS-1$
							Messages.getString("NamespacesMasterPart.PONT_DIALOG_MESSAGE"), //$NON-NLS-1$
							Messages.getString("NamespacesMasterPart.PONT_DIALOG_CHECKBOX"), false, prefStore, SpringConfigPreferenceConstants.PREF_DISPLAY_TABS_DIALOG); //$NON-NLS-1$
		}
	}

	@Override
	protected void postCreateContents() {
		ColumnViewer viewer = getViewer();
		if (viewer != null && viewer instanceof CheckboxTableViewer) {
			checkListener = new CheckStateListener();
			xsdViewer = (CheckboxTableViewer) viewer;
			xsdViewer.setCheckedElements(getPreselectedElements());
			xsdViewer.addCheckStateListener(checkListener);
		}
	}

	@Override
	public void refresh() {
		xsdViewer.setCheckedElements(getPreselectedElements());
		super.refresh();
	}

	protected void removeXsdDefinition(INamespaceDefinition definition) {
		if (existsInConfiguration(definition)) {
			StructuredTextViewer textView = getConfigEditor().getTextViewer();
			IDOMDocument doc = getConfigEditor().getDomDocument();
			doc.getModel().beginRecording(textView);

			rootElement.removeAttribute(ConfigCoreUtils.ATTR_NAMESPACE_PREFIX
					+ definition.getNamespacePrefix(getConfigEditor().getResourceFile()));
			Attr attr = rootElement.getAttributeNode(ConfigCoreUtils.ATTR_DEFAULT_NAMESPACE);
			if (attr != null && attr.getNodeValue().equals(definition.getNamespaceURI())) {
				rootElement.removeAttributeNode(attr);
			}
			selectedNamespaces.remove(definition);
			updateXsdVersion();
			doc.getModel().endRecording(textView);
		}
	}

	private boolean shouldRefresh(INamespaceDefinitionListener.NamespaceDefinitionChangeEvent event) {
		return event.getProject() == null
				|| event.getProject().equals(getConfigEditor().getResourceFile().getProject());
	}

	private List<INamespaceDefinition> triggerLoadNamespaceDefinitionList(
			List<INamespaceDefinition> namespaceDefinitionList) {
		// Add any namespaces that exist in the XML document but aren't
		// already known to the tooling via the extension point
		if (rootElement == null) {
			rootElement = getConfigEditor().getDomDocument().getDocumentElement();
		}
		if (rootElement != null) {
			NamedNodeMap beanAttributes = rootElement.getAttributes();
			for (int i = 0; i < beanAttributes.getLength(); i++) {
				Node currAttributeNode = beanAttributes.item(i);
				String currAttributeName = currAttributeNode.getNodeName();
				String uri = currAttributeNode.getNodeValue();
				if (isUnknownNamespace(currAttributeName, uri, namespaceDefinitionList)) {
					String schemaVersion = ConfigCoreUtils.getSelectedSchemaLocation(
							getConfigEditor().getDomDocument(), uri);
					INamespaceDefinition namespaceDefinition = new DefaultNamespaceDefinition(attributeNameToPrefix(
							currAttributeName, uri), uri, schemaVersion, StsUiImages.XML_FILE.createImage());
					if (!"".equals(schemaVersion)) { //$NON-NLS-1$
						this.selectedVersions.put(namespaceDefinition, schemaVersion);
					}
					namespaceDefinitionList.add(namespaceDefinition);
				}
			}
		}
		// Special case
		List<String> schemaInfo = ConfigCoreUtils.parseSchemaLocationAttr(getConfigEditor().getDomDocument());
		if (schemaInfo != null) {
			Iterator<String> iter = schemaInfo.iterator();
			while (iter.hasNext()) {
				String uri = iter.next();
				if (iter.hasNext()) {
					String schemaVersion = iter.next();
					if (!namespaceAttributeExistsInList(ConfigCoreUtils.ATTR_SCHEMA_LOCATION, uri,
							namespaceDefinitionList)) {
						INamespaceDefinition namespaceDefinition = new DefaultNamespaceDefinition(
								attributeNameToPrefix(ConfigCoreUtils.ATTR_SCHEMA_LOCATION, uri), uri, schemaVersion,
								StsUiImages.XML_FILE.createImage());
						if (!"".equals(schemaVersion)) { //$NON-NLS-1$
							this.selectedVersions.put(namespaceDefinition, schemaVersion);
						}
						namespaceDefinitionList.add(namespaceDefinition);
					}
				}
			}
		}
		return namespaceDefinitionList;
	}

	public void updateDefinitionFromCheckState(INamespaceDefinition definition, boolean checked) {
		if (checked) {
			addXsdDefinition(definition);
		}
		else {
			removeXsdDefinition(definition);
		}
		openPontDialog();
	}

	protected void updateXsdVersion() {
		rootElement.removeAttribute(ConfigCoreUtils.ATTR_SCHEMA_LOCATION);
		Set<INamespaceDefinition> namespaces = selectedNamespaces;
		Map<INamespaceDefinition, String> schemaVersions = getSchemaVersions();
		String schemaLocationAttrVal = null;
		for (INamespaceDefinition currNamespaceDefinition : namespaces) {
			if (currNamespaceDefinition.getDefaultSchemaLocation(getConfigEditor().getResourceFile()) != null) {
				String currNamespaceDefSchemaLocationVal = getSchemaLocationValue(currNamespaceDefinition,
						schemaVersions);
				// Append the new schema location
				if (schemaLocationAttrVal == null) {
					schemaLocationAttrVal = currNamespaceDefSchemaLocationVal;
				}
				else {
					schemaLocationAttrVal += "\n" + "\t\t" + currNamespaceDefSchemaLocationVal; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

		if (schemaLocationAttrVal != null) {
			rootElement.setAttribute(ConfigCoreUtils.ATTR_SCHEMA_LOCATION, schemaLocationAttrVal.trim());
		}
		formatProcessor.formatNode(rootElement);
	}

}
