/*******************************************************************************
 * Copyright (c) 2006 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
@SuppressWarnings({ "deprecation", "restriction" })
public class NewSpringProjectCreationPage extends NewJavaProjectWizardPageOne {

	private static final String CONFIG_PROPERTY_PREFIX = "ConfigurationPropertyPage." + "tabConfigFiles.";

	private static final String ENABLE_IMPORT_LABEL = CONFIG_PROPERTY_PREFIX + "enableImports.label";

	private static final String IGNORE_MISSING_NAMESPACEHANDLER_LABEL = CONFIG_PROPERTY_PREFIX
			+ "ignoreMissingNamespaceHandler.label";

	private Button classpathCheckbox;

	private Button disableNamespaceCachingCheckbox;

	private Button enableImportButton;

	private Button enableProjectFacetsButton;

	private Button ignoreMissingNamespaceHandlerButton;

	private Text suffixesText;

	private SelectionButtonDialogField useProjectSettingsButton;

	private Button versionCheckbox;

	private boolean enableImports;

	private boolean enableProjectFacets;

	private String suffixes;

	private boolean ignoreMissingNamespaceHandlers;

	private boolean loadHandlerFromClasspath;

	private boolean useHighestXsdVersion;

	private boolean disableNamespaceCaching;

	private boolean useProjectSettings;

	public NewSpringProjectCreationPage() {

	}

	@Override
	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		setTitle("Spring Configuration");

		Composite mainArea = new Composite(parent, SWT.NULL);

		mainArea.setLayout(initGridLayout(new GridLayout(), true));
		mainArea.setLayoutData(new GridData(GridData.FILL_BOTH));

		Dialog.applyDialogFont(mainArea);

		createConfigFileGroup(mainArea);
		createNamespaceSettingsGroup(mainArea);
		createFacetsGroup(mainArea);
		createJRESection(mainArea);

		// Create it to avoid NPEs when the superclass attempts to read/write to
		// the widget,
		// but don't add it to the layout as its usually empty and takes up
		// space
		Control infoControl = createInfoControl(mainArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(infoControl);
		GridData data = (GridData) infoControl.getLayoutData();
		data.exclude = true;
		infoControl.setLayoutData(data);

		// Show description on opening
		setErrorMessage(null);
		setMessage(null);

		// Note that child controls may have different fonts, therefore to make
		// sure all controls have the same
		// font as the parent, apply to all children.
		applyParentFont(mainArea);

		setControl(mainArea);

		refreshProjectValues();

		setPageComplete(validatePage());
	}

	/**
	 * 
	 * @param parent whose font should be applied to all of its children.
	 */
	protected void applyParentFont(Composite parent) {
		applyFontToChildren(parent, parent.getFont());
	}

	protected void applyFontToChildren(Control control, Font font) {

		if (control instanceof Composite) {
			Control[] children = ((Composite) control).getChildren();
			if (children != null) {
				for (Control child : children) {
					child.setFont(font);
					applyFontToChildren(child, font);
				}
			}
		}
	}

	protected void createJRESection(Composite parent) {
		Control jreControl = createJRESelectionControl(parent);
		jreControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Info Control is needed as a dependency of JRE control
		Control infoControl = createInfoControl(parent);
		infoControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/**
	 * Since the Java pages require a project name and location to be set in
	 * order to properly validate other configuration values, ensure that the
	 * project name and location from the wizard main page are updated in the
	 * Java pages.
	 */
	protected void refreshProjectValues() {
		NewSpringProjectWizard wizard = (NewSpringProjectWizard) getWizard();
		NewSpringProjectWizardMainPage mainPage = wizard.getMainPage();

		setProjectName(mainPage.getProjectName());
		setProjectLocationURI(mainPage.getProjectLocationURI());
	}

	public void performFinish() {
		refreshProjectValues();
	}

	public boolean enableImports() {
		return enableImports;
	}

	public boolean enableProjectFacets() {
		return enableProjectFacets;
	}

	public Set<String> getConfigSuffixes() {
		return StringUtils.commaDelimitedListToSet(suffixes);
	}

	public boolean ignoreMissingNamespaceHandlers() {
		return ignoreMissingNamespaceHandlers;
	}

	public boolean loadHandlerFromClasspath() {
		return loadHandlerFromClasspath;
	}

	public boolean disableNamespaceCaching() {
		return disableNamespaceCaching;
	}

	public boolean useHighestXsdVersion() {
		return useHighestXsdVersion;
	}

	public boolean useProjectSettings() {
		return useProjectSettings;
	}

	private Button createButton(Composite container, int style, int span, int indent) {
		Button button = new Button(container, style);
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		gd.horizontalIndent = indent;
		button.setLayoutData(gd);
		return button;
	}

	private void createConfigFileGroup(Composite container) {
		Group springGroup = new Group(container, SWT.NONE);
		springGroup.setText(NewSpringProjectWizardMessages.NewProjectPage_configFileSettings);
		springGroup.setLayout(initGridLayout(new GridLayout(), true));
		springGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create enable import checkbox
		enableImportButton = SpringUIUtils.createCheckBox(springGroup,
				BeansUIPlugin.getResourceString(ENABLE_IMPORT_LABEL));
		enableImportButton.setSelection(enableImports = IBeansProject.DEFAULT_IMPORTS_ENABLED);
		enableImportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(validatePage());
				enableImports = enableImportButton.getSelection();
			}
		});

		// Create ignore missing namespace handler checkbox
		ignoreMissingNamespaceHandlerButton = SpringUIUtils.createCheckBox(springGroup,
				BeansUIPlugin.getResourceString(IGNORE_MISSING_NAMESPACEHANDLER_LABEL));
		ignoreMissingNamespaceHandlerButton
				.setSelection(ignoreMissingNamespaceHandlers = BeansCorePlugin.IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY_DEFAULT);
		ignoreMissingNamespaceHandlerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(validatePage());
				ignoreMissingNamespaceHandlers = ignoreMissingNamespaceHandlerButton.getSelection();
			}
		});

		// Create suffix text field
		suffixesText = SpringUIUtils.createTextField(springGroup,
				NewSpringProjectWizardMessages.NewProjectPage_suffixes);
		suffixesText.setText(suffixes = IBeansProject.DEFAULT_CONFIG_SUFFIX);
		suffixesText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
				suffixes = suffixesText.getText();
			}
		});
	}

	private void createFacetsGroup(Composite container) {
		Group facetsGroup = new Group(container, SWT.NONE);
		facetsGroup.setText(NewSpringProjectWizardMessages.NewProjectPage_facetsSettings);
		facetsGroup.setLayout(initGridLayout(new GridLayout(), true));
		facetsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label description = new Label(facetsGroup, SWT.NONE);
		description.setText(NewSpringProjectWizardMessages.NewProjectPage_projectFacetsDescription);

		enableProjectFacetsButton = createButton(facetsGroup, SWT.CHECK, 1, 0);
		enableProjectFacetsButton.setText(NewSpringProjectWizardMessages.NewProjectPage_enableProjectFacets);
		enableProjectFacetsButton.setSelection(enableProjectFacets = false);

		enableProjectFacetsButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableProjectFacets = enableProjectFacetsButton.getSelection();
			}
		});
	}

	private void createNamespaceSettingsGroup(Composite container) {
		Group namespacesGroup = new Group(container, SWT.NONE);
		namespacesGroup.setText(NewSpringProjectWizardMessages.NewProjectPage_namespacesSettings);
		namespacesGroup.setLayout(initGridLayout(new GridLayout(), true));
		namespacesGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		IDialogFieldListener listener = new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				enableProjectSpecificSettings(((SelectionButtonDialogField) field).isSelected());
			}
		};

		useProjectSettingsButton = new SelectionButtonDialogField(SWT.CHECK);
		useProjectSettingsButton.setDialogFieldListener(listener);
		useProjectSettingsButton.setLabelText(NewSpringProjectWizardMessages.NewProjectPage_enableProjectSettings);
		useProjectSettingsButton.doFillIntoGrid(namespacesGroup, 1);
		LayoutUtil.setHorizontalGrabbing(useProjectSettingsButton.getSelectionButton(null));

		LayoutUtil.setHorizontalSpan(useProjectSettingsButton.getSelectionButton(null), 2);

		Label horizontalLine = new Label(namespacesGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		horizontalLine.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		horizontalLine.setFont(namespacesGroup.getFont());

		Preferences prefs = BeansCorePlugin.getDefault().getPluginPreferences();
		useHighestXsdVersion = prefs.getBoolean(BeansCorePlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID);
		loadHandlerFromClasspath = prefs.getBoolean(BeansCorePlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID);
		disableNamespaceCaching = prefs.getBoolean(BeansCorePlugin.DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID);

		versionCheckbox = createButton(namespacesGroup, SWT.CHECK, 1, convertHorizontalDLUsToPixels(5));
		versionCheckbox.setText(NewSpringProjectWizardMessages.NewProjectPage_highestXsdVersion);
		versionCheckbox.setSelection(useHighestXsdVersion);

		versionCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				useHighestXsdVersion = versionCheckbox.getSelection();
			}
		});

		classpathCheckbox = createButton(namespacesGroup, SWT.CHECK, 1, convertHorizontalDLUsToPixels(5));
		classpathCheckbox.setText(NewSpringProjectWizardMessages.NewProjectPage_loadXsdsFromClasspath);
		classpathCheckbox.setSelection(loadHandlerFromClasspath);
		classpathCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadHandlerFromClasspath = classpathCheckbox.getSelection();
				disableNamespaceCachingCheckbox.setEnabled(classpathCheckbox.getSelection());
			}
		});

		disableNamespaceCachingCheckbox = createButton(namespacesGroup, SWT.CHECK, 1, convertHorizontalDLUsToPixels(15));
		disableNamespaceCachingCheckbox.setText(NewSpringProjectWizardMessages.NewProjectPage_disableNamespaceCaching);
		disableNamespaceCachingCheckbox.setSelection(disableNamespaceCaching);
		disableNamespaceCachingCheckbox.setEnabled(loadHandlerFromClasspath);

		disableNamespaceCachingCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				disableNamespaceCaching = disableNamespaceCachingCheckbox.getSelection();
			}
		});

		enableProjectSpecificSettings(false);
	}

	private void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		this.useProjectSettings = useProjectSpecificSettings;

		// Enable related controls
		versionCheckbox.setEnabled(useProjectSpecificSettings);
		classpathCheckbox.setEnabled(useProjectSpecificSettings);
		disableNamespaceCachingCheckbox.setEnabled(useProjectSpecificSettings && classpathCheckbox.getSelection());
	}

	// Copied from NewJavaProjectWizardPageOne so that our margins will match
	private GridLayout initGridLayout(GridLayout layout, boolean margins) {
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		if (margins) {
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		}
		else {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		return layout;
	}

	private boolean isValidSuffix(String suffix) {
		if (suffix.length() == 0) {
			return false;
		}
		return true;
	}

	protected boolean validatePage() {
		String suffixes = suffixesText.getText().trim();
		if (suffixes.length() == 0) {
			setErrorMessage(NewSpringProjectWizardMessages.NewProjectPage_noSuffixes);
			return false;
		}
		StringTokenizer tokenizer = new StringTokenizer(suffixes, ",");
		while (tokenizer.hasMoreTokens()) {
			String suffix = tokenizer.nextToken().trim();
			if (!isValidSuffix(suffix)) {
				setErrorMessage(NewSpringProjectWizardMessages.NewProjectPage_invalidSuffixes);
				return false;
			}
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

}
