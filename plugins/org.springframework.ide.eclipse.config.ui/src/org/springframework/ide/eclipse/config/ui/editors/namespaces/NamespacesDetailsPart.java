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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class NamespacesDetailsPart extends AbstractConfigDetailsPart {

	private class VersionContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public Object[] getElements(Object obj) {
			if (obj instanceof INamespaceDefinition) {
				Set<String> elements = new HashSet<String>();
				String defaultLocation = ((INamespaceDefinition) obj).getDefaultSchemaLocation(getConfigEditor()
						.getResourceFile());
				if (defaultLocation != null) {
					elements.add(defaultLocation);
				}
				elements.addAll(((INamespaceDefinition) obj).getSchemaLocations());
				return elements.toArray();
			}
			else {
				return new Object[0];
			}
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class VersionLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof String) {
				String label = (String) element;
				if (selectedNamespaceDefinition != null
						&& label.equals(selectedNamespaceDefinition.getDefaultSchemaLocation(getConfigEditor()
								.getResourceFile()))) {
					label += " " + Messages.getString("NamespacesDetailsPart.DEFAULT_VERSION_SUFFIX"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return label;
			}
			return super.getText(element);
		}
	}

	private INamespaceDefinition selectedNamespaceDefinition;

	private Map<INamespaceDefinition, String> selectedVersions;

	private CheckboxTableViewer versionViewer;

	public NamespacesDetailsPart(AbstractConfigMasterPart master) {
		super(master);
	}

	@Override
	protected void createDetailsContent(Composite client) {
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 50;
		data.widthHint = 50;

		versionViewer = CheckboxTableViewer.newCheckList(client, SWT.BORDER);
		versionViewer.getTable().setLayoutData(data);
		versionViewer.setContentProvider(new VersionContentProvider());
		versionViewer.setLabelProvider(new VersionLabelProvider());
		versionViewer.setSorter(new ViewerSorter());

		versionViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(final CheckStateChangedEvent event) {
				if (event.getChecked()) {
					versionViewer.setCheckedElements(new Object[] { event.getElement() });
					if (selectedNamespaceDefinition != null) {
						selectedVersions.put(selectedNamespaceDefinition, (String) event.getElement());
					}
				}
				else {
					versionViewer.setCheckedElements(new Object[0]);
					selectedVersions.remove(selectedNamespaceDefinition);
				}

				if (getMasterPart() instanceof NamespacesMasterPart) {
					StructuredTextViewer textView = getConfigEditor().getTextViewer();
					IDOMDocument doc = getConfigEditor().getDomDocument();
					doc.getModel().beginRecording(textView);

					NamespacesMasterPart namespaceMaster = (NamespacesMasterPart) getMasterPart();
					namespaceMaster.updateXsdVersion();
					doc.getModel().endRecording(textView);
				}
			}
		});
	}

	@Override
	protected String getDetailsSectionDescription() {
		return Messages.getString("NamespacesDetailsPart.DETAILS_SECTION_DESCRIPTION"); //$NON-NLS-1$
	}

	@Override
	protected String getDetailsSectionTitle() {
		return Messages.getString("NamespacesDetailsPart.DETAILS_SECTION_TITLE"); //$NON-NLS-1$
	}

	public ColumnViewer getVersionViewer() {
		return versionViewer;
	}

	@Override
	public void refresh() {
		if (getMasterPart() instanceof NamespacesMasterPart) {
			NamespacesMasterPart namespaceMaster = (NamespacesMasterPart) getMasterPart();
			selectedVersions = namespaceMaster.getSchemaVersions();
			if (selectedVersions.containsKey(selectedNamespaceDefinition)) {
				versionViewer.setCheckedElements(new Object[] { selectedVersions.get(selectedNamespaceDefinition)
						.trim() });
			}

			if (getMasterViewer() instanceof CheckboxTableViewer) {
				CheckboxTableViewer checkViewer = (CheckboxTableViewer) getMasterViewer();
				if (checkViewer.getChecked(selectedNamespaceDefinition)) {
					String defaultLocation = selectedNamespaceDefinition.getDefaultSchemaLocation(getConfigEditor()
							.getResourceFile());
					if (selectedNamespaceDefinition.getSchemaLocations().size() > 0 || defaultLocation != null) {
						versionViewer.getControl().setEnabled(true);
					}
					else {
						versionViewer.getControl().setEnabled(false);
					}
				}
				else {
					versionViewer.getControl().setEnabled(false);
				}
			}
		}
		versionViewer.refresh();
		super.refresh();
	}

	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		INamespaceDefinition oldDefinition = selectedNamespaceDefinition;
		selectedNamespaceDefinition = null;

		IStructuredSelection sel = (IStructuredSelection) selection;
		if (sel != null) {
			Object obj = sel.getFirstElement();
			if (obj != null && obj instanceof INamespaceDefinition) {
				if (obj != oldDefinition) {
					selectedNamespaceDefinition = (INamespaceDefinition) obj;
				}
				else {
					selectedNamespaceDefinition = oldDefinition;
				}
				versionViewer.setInput(selectedNamespaceDefinition);
			}
		}
		super.selectionChanged(part, selection);
	}
}
