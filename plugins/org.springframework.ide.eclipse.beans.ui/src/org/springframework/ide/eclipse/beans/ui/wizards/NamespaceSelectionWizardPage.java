/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;

/**
 * {@link WizardPage} that displays a list of {@link INamespaceDefinition}s to the user in order to allow for selecting
 * the desired XSD namespace declarations.
 * @author Christian Dupuis
 * @since 2.0
 */
public class NamespaceSelectionWizardPage extends WizardPage {

	public class XsdLabelProvider extends LabelProvider {

		public Image getImage(Object element) {
			if (element instanceof INamespaceDefinition) {
				INamespaceDefinition xsdDef = (INamespaceDefinition) element;
				return xsdDef.getNamespaceImage();
			}
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}

		public String getText(Object element) {
			if (element instanceof INamespaceDefinition) {
				INamespaceDefinition xsdDef = (INamespaceDefinition) element;
				return xsdDef.getDefaultNamespacePrefix() + " - " + xsdDef.getNamespaceURI();
			}
			return "";
		}
	}

	public class VersionLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}
	}

	private class XsdConfigContentProvider implements IStructuredContentProvider {

		private List<INamespaceDefinition> namespaceDefinitionList = new ArrayList<INamespaceDefinition>();

		private boolean loading = false;

		private IProject activeProject = null;

		public Object[] getElements(Object obj) {
			IProject project = null;
			if (filePath != null && filePath.segmentCount() > 0) {
				String projectName = filePath.segment(0);
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}

			if ((activeProject == null && project != null) || (activeProject != null && !activeProject.equals(project))) {
				namespaceDefinitionList = new ArrayList<INamespaceDefinition>();
				loading = false;
			}

			if (namespaceDefinitionList.size() == 0 && !loading) {
				activeProject = project;
				NamespaceUtils.getNamespaceDefinitions(project, new NamespaceUtils.INamespaceDefinitionTemplate() {

					public void doWithNamespaceDefinitions(INamespaceDefinition[] namespaceDefinitions,
							final IProject project) {
						if (project == null || project.equals(activeProject)) {
							for (INamespaceDefinition namespaceDefinition : namespaceDefinitions) {
								synchronized (namespaceDefinitionList) {
									if (!namespaceDefinitionList.contains(namespaceDefinition)) {
										namespaceDefinitionList.add(namespaceDefinition);
									}
								}
							}

							Display.getDefault().asyncExec(new Runnable() {

								public void run() {
									if (!xsdViewer.getControl().isDisposed()) {
										xsdViewer.setInput(this);
									}
								}
							});
						}
						loading = false;
					}
				});
			}

			return namespaceDefinitionList.toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}
	}

	private class VersionContentProvider implements IStructuredContentProvider {

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

		public void dispose() {
		}
	}

	private static final int XSD_LIST_VIEWER_HEIGHT = 150;

	private static final int LIST_VIEWER_WIDTH = 340;

	private CheckboxTableViewer xsdViewer;

	private CheckboxTableViewer versionViewer;

	private Map<INamespaceDefinition, String> selectedVersion = new HashMap<INamespaceDefinition, String>();

	private INamespaceDefinition selectedNamespaceDefinition;

	private IPath filePath;

	protected NamespaceSelectionWizardPage(String pageName) {
		super(pageName);
		setTitle(BeansWizardsMessages.NewConfig_title);
		setDescription(BeansWizardsMessages.NewConfig_xsdDescription);
	}

	public void createControl(Composite parent) {
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
		namespaceLabel.setText("Select desired XSD namespace declarations:");

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = LIST_VIEWER_WIDTH;
		gd.heightHint = XSD_LIST_VIEWER_HEIGHT;

		// config set list viewer
		xsdViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		xsdViewer.getTable().setLayoutData(gd);
		xsdViewer.setContentProvider(new XsdConfigContentProvider());
		xsdViewer.setLabelProvider(new XsdLabelProvider());
		xsdViewer.setInput(this); // activate content provider
		INamespaceDefinition defaultDefinition = NamespaceUtils.getDefaultNamespaceDefinition();
		if (defaultDefinition != null) {
			xsdViewer.setGrayedElements(new Object[] { defaultDefinition });
			xsdViewer.setCheckedElements(new Object[] { defaultDefinition });
		}

		xsdViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
					selectedNamespaceDefinition = (INamespaceDefinition) obj;
					versionViewer.setInput(obj);
					if (selectedVersion.containsKey(selectedNamespaceDefinition)) {
						versionViewer.setCheckedElements(new Object[] { selectedVersion
								.get(selectedNamespaceDefinition) });
					}
					if (xsdViewer.getChecked(obj) && selectedNamespaceDefinition.getSchemaLocations().size() > 0) {
						versionViewer.getControl().setEnabled(true);
					}
					else {
						versionViewer.getControl().setEnabled(false);
					}

				}
			}
		});

		xsdViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(final CheckStateChangedEvent event) {
				Object obj = event.getElement();
				selectedNamespaceDefinition = (INamespaceDefinition) obj;
				versionViewer.setInput(obj);
				if (selectedVersion.containsKey(selectedNamespaceDefinition)) {
					versionViewer.setCheckedElements(new Object[] { selectedVersion.get(selectedNamespaceDefinition) });
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
		versionLabel.setText("Select desired XSD (if none is selected the default will be used):");

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
						selectedVersion.put(selectedNamespaceDefinition, (String) event.getElement());
					}
				}
				else {
					versionViewer.setCheckedElements(new Object[0]);
					selectedVersion.remove(selectedNamespaceDefinition);
				}
			}
		});
	}

	public List<INamespaceDefinition> getXmlSchemaDefinitions() {
		List<INamespaceDefinition> defs = new ArrayList<INamespaceDefinition>();
		Object[] checkedElements = xsdViewer.getCheckedElements();
		if (checkedElements != null) {
			for (int i = 0; i < checkedElements.length; i++) {
				defs.add((INamespaceDefinition) checkedElements[i]);
			}
		}
		return defs;
	}

	public Map<INamespaceDefinition, String> getSchemaVersions() {
		return selectedVersion;
	}

	public void setFilePath(IPath filePath) {
		this.filePath = filePath;
		if (xsdViewer != null) {
			xsdViewer.setInput(this);
		}
	}
}
