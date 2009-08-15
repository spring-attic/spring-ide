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
package org.springframework.ide.eclipse.beans.ui.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.ui.dialogs.ProjectAndPreferencePage;

/**
 * {@link ProjectAndPreferencePage} that allows to configure default namepace versions.
 * @author Christian Dupuis
 * @since 2.2.5
 */
@SuppressWarnings("deprecation")
public class NamespaceVersionPreferencePage extends ProjectAndPreferencePage {

	public static final String PREF_ID = "org.springframework.ide.eclipse.beans.ui.namespaces.preferencePage"; //$NON-NLS-1$

	public static final String PROP_ID = "org.springframework.ide.eclipse.beans.ui.namespaces.projectPropertyPage"; //$NON-NLS-1$

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
				return xsdDef.getNamespacePrefix() + " - " + xsdDef.getNamespaceURI();
			}
			return "";
		}
	}

	public class VersionLabelProvider extends LabelProvider {

		public Image getImage(Object element) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}

		public String getText(Object element) {
			if (element instanceof String) {
				String label = (String) element;
				return label;
			}
			return super.getText(element);
		}
	}

	private class XsdConfigContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object obj) {
			return NamespaceUtils.getNamespaceDefinitions().toArray();
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

	private TableViewer xsdViewer;

	private CheckboxTableViewer versionViewer;
	
	private Button classpathCheckbox;
	
	private Map<INamespaceDefinition, String> selectedVersion = new HashMap<INamespaceDefinition, String>();

	private INamespaceDefinition selectedNamespaceDefinition;

	public Composite createPreferenceContent(Composite parent) {
		
		List<INamespaceDefinition> namespaces = NamespaceUtils.getNamespaceDefinitions();
		boolean checkClasspath = true;
		if (isProjectPreferencePage()) {
			SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(getProject(),
					BeansCorePlugin.PLUGIN_ID);
			for (INamespaceDefinition namespace : namespaces) {
				String version = prefs.getString(BeansCorePlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID
						+ namespace.getNamespaceURI(), "");
				selectedVersion.put(namespace, version);
			}
			checkClasspath = prefs.getBoolean(BeansCorePlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, true);
		}
		else {
			Preferences prefs = BeansCorePlugin.getDefault().getPluginPreferences();
			for (INamespaceDefinition namespace : namespaces) {
				String version = prefs.getString(BeansCorePlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID
						+ namespace.getNamespaceURI());
				selectedVersion.put(namespace, version);
			}
			checkClasspath = prefs.getBoolean(BeansCorePlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID);
		}

		initializeDialogUnits(parent);
		// top level group
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());
		
		classpathCheckbox = new Button(composite, SWT.CHECK);
		classpathCheckbox.setText("Use highest XSD version that is available on the project's classpath");
		classpathCheckbox.setSelection(checkClasspath);
		
		Label namespaceLabel = new Label(composite, SWT.NONE);
		namespaceLabel.setText("Select XSD namespace to configure default version:");

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = LIST_VIEWER_WIDTH;
		gd.heightHint = XSD_LIST_VIEWER_HEIGHT;

		// config set list viewer
		xsdViewer = new TableViewer(composite, SWT.BORDER);
		xsdViewer.getTable().setLayoutData(gd);
		xsdViewer.setContentProvider(new XsdConfigContentProvider());
		xsdViewer.setLabelProvider(new XsdLabelProvider());
		xsdViewer.setInput(this); // activate content provider

		xsdViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
					selectedNamespaceDefinition = (INamespaceDefinition) obj;
					versionViewer.setInput(obj);
					if (selectedVersion.get(selectedNamespaceDefinition) != null) {
						versionViewer.setCheckedElements(new Object[] { selectedVersion
								.get(selectedNamespaceDefinition) });
					}
					if (selectedNamespaceDefinition.getSchemaLocations().size() > 0) {
						versionViewer.getControl().setEnabled(true);
					}
					else {
						versionViewer.getControl().setEnabled(false);
					}
				}
			}
		});

		Label versionLabel = new Label(composite, SWT.NONE);
		versionLabel.setText("Select default schema version (if none is selected the versionless schema will be used):");

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
					selectedVersion.put(selectedNamespaceDefinition, "");
				}
			}
		});

		return composite;
	}

	@Override
	protected String getPreferencePageID() {
		return PREF_ID;
	}

	@Override
	protected String getPropertyPageID() {
		return PROP_ID;
	}

	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		return SpringCorePreferences.getProjectPreferences(project, BeansCorePlugin.PLUGIN_ID).getBoolean(
				BeansCorePlugin.NAMESPACE_VERSION_PROJECT_PREFERENCE_ID, false);
	}

	public boolean performOk() {
		if (isProjectPreferencePage()) {
			if (useProjectSettings()) {
				SpringCorePreferences.getProjectPreferences(getProject(), BeansCorePlugin.PLUGIN_ID).putBoolean(
						BeansCorePlugin.NAMESPACE_VERSION_PROJECT_PREFERENCE_ID, true);
			}
			else {
				SpringCorePreferences.getProjectPreferences(getProject(), BeansCorePlugin.PLUGIN_ID).putBoolean(
						BeansCorePlugin.NAMESPACE_VERSION_PROJECT_PREFERENCE_ID, false);
			}
			for (Map.Entry<INamespaceDefinition, String> entry : selectedVersion.entrySet()) {
				SpringCorePreferences.getProjectPreferences(getProject(), BeansCorePlugin.PLUGIN_ID).putString(
						BeansCorePlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID + entry.getKey().getNamespaceURI(),
						entry.getValue());
			}
			SpringCorePreferences.getProjectPreferences(getProject(), BeansCorePlugin.PLUGIN_ID).putBoolean(
					BeansCorePlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, classpathCheckbox.getSelection());
		}
		else {
			for (Map.Entry<INamespaceDefinition, String> entry : selectedVersion.entrySet()) {
				BeansCorePlugin.getDefault().getPluginPreferences().setValue(
						BeansCorePlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID + entry.getKey().getNamespaceURI(),
						entry.getValue());
			}
			BeansCorePlugin.getDefault().getPluginPreferences().setValue(
					BeansCorePlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, classpathCheckbox.getSelection());
		}
		BeansCorePlugin.getDefault().savePluginPreferences();
		return true;
	};
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		for (Map.Entry<INamespaceDefinition, String> entry : selectedVersion.entrySet()) {
			entry.setValue("");
		}
	}
}
