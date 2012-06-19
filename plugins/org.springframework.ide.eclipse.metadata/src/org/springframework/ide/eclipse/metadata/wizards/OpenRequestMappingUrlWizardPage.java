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
package org.springframework.ide.eclipse.metadata.wizards;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.springframework.ide.eclipse.metadata.MetadataPlugin;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingMethodToClassMap;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingView;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingViewLabelProvider;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class OpenRequestMappingUrlWizardPage extends WizardPage {

	private static String PREFIX_VARIABLE_PREFS = "com.springsource.sts.metadata.url.wizard.variables"; //$NON-NLS-1$

	private static String KEY_ROOT = "root"; //$NON-NLS-1$

	private static String KEY_URL_DATA = "urlData"; //$NON-NLS-1$

	private static String KEY_NAME = "name"; //$NON-NLS-1$

	private static String KEY_VALUE = "value"; //$NON-NLS-1$

	private IPreferenceStore prefStore;

	private String prefStoreKey;

	private RequestMappingMethodToClassMap input;

	private RequestMappingViewLabelProvider labelProvider;

	private IProject project;

	private List<Combo> comboList;

	private Map<String, List<String>> cacheMap;

	private Combo urlText;

	protected OpenRequestMappingUrlWizardPage(RequestMappingMethodToClassMap input,
			RequestMappingViewLabelProvider labelProvider, IProject project) {
		super(Messages.OpenRequestMappingUrlWizardPage_PAGE_TITLE);
		setTitle(Messages.OpenRequestMappingUrlWizardPage_HEADER_TITLE);
		setDescription(Messages.OpenRequestMappingUrlWizardPage_DESCRIPTION);
		this.input = input;
		this.labelProvider = labelProvider;
		this.project = project;
		prefStore = MetadataPlugin.getDefault().getPreferenceStore();
		prefStoreKey = PREFIX_VARIABLE_PREFS.concat(input.getMethodMetadata().getHandleIdentifier());
		comboList = new ArrayList<Combo>();
		cacheMap = new HashMap<String, List<String>>();
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		
		Set<String> urlInput = new HashSet<String>();
		IModule[] modules = ServerUtil.getModules(project);
		if (modules != null) {
			for (IModule module : modules) {
				IServer[] servers = ServerUtil.getServersByModule(module, new NullProgressMonitor());
				for (IServer server : servers) {
					IURLProvider provider = (IURLProvider) server.loadAdapter(IURLProvider.class, new NullProgressMonitor());
					if (provider != null) {
						URL url = provider.getModuleRootURL(module);
						if (url != null) {
							urlInput.add(url.toString());
						}
					}
				}
			}
		}

		Label urlLabel = new Label(container, SWT.NONE);
		urlLabel.setText(Messages.OpenRequestMappingUrlWizardPage_LABEL_URL_PREFIX);
		urlText = new Combo(container, SWT.DROP_DOWN);
		urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		urlText.addModifyListener(new TextModifyListener());
		if (urlInput.size() > 0) {
			urlText.setItems(urlInput.toArray(new String[urlInput.size()]));
			urlText.select(0);
		}

		loadPreferenceCache();
		createVariableControls(container);
		setControl(container);
		setPageComplete(validatePage());
	}

	private void createVariableControls(Composite parent) {
		String url = labelProvider.getColumnText(input, RequestMappingView.COLUMN_URL);
		Pattern pattern = Pattern.compile("\\{\\w*\\}"); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(url);
		while (matcher.find()) {
			String textLabel = matcher.group();
			Label label = new Label(parent, SWT.NONE);
			label.setText(textLabel + ": "); //$NON-NLS-1$
			Combo combo = new Combo(parent, SWT.DROP_DOWN);
			combo.setData(KEY_URL_DATA, textLabel);
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			comboList.add(combo);
			if (cacheMap.containsKey(textLabel)) {
				List<String> strings = cacheMap.get(textLabel);
				combo.setItems(strings.toArray(new String[strings.size()]));
				combo.setText(combo.getItem(0));
			}
			combo.addModifyListener(new TextModifyListener());
		}
	}

	private void loadPreferenceCache() {
		String cache = prefStore.getString(prefStoreKey);
		if (cache != null && cache.length() > 0) {
			try {
				IMemento memento = XMLMemento.createReadRoot(new StringReader(cache));
				IMemento[] children = memento.getChildren(KEY_URL_DATA);
				for (IMemento child : children) {
					String name = child.getString(KEY_NAME);
					String value = child.getString(KEY_VALUE);
					if (name != null && value != null) {
						List<String> subList = cacheMap.get(name);
						if (subList == null) {
							subList = new LinkedList<String>();
						}
						subList.add(value);
						cacheMap.put(name, subList);
					}
				}
			}
			catch (WorkbenchException e) {
				StatusHandler.log(new Status(IStatus.ERROR, MetadataPlugin.PLUGIN_ID,
						Messages.OpenRequestMappingUrlWizardPage_ERROR_LOADING_CACHE, e));
			}
		}
	}

	private void savePreferenceCache() {
		String mementoString = null;
		XMLMemento rootMemento = XMLMemento.createWriteRoot(KEY_ROOT);
		for (Combo combo : comboList) {
			String textLabel = (String) combo.getData(KEY_URL_DATA);
			String value = combo.getText();
			List<String> subList = cacheMap.get(textLabel);
			if (subList == null) {
				subList = new LinkedList<String>();
			}
			subList.remove(value);
			subList.add(0, value);
			cacheMap.put(textLabel, subList);

			for (int i = 0; i < subList.size(); i++) {
				if (i >= 10) {
					break;
				}
				IMemento child = rootMemento.createChild(KEY_URL_DATA);
				child.putString(KEY_NAME, textLabel);
				child.putString(KEY_VALUE, subList.get(i));
			}
		}

		try {
			StringWriter writer = new StringWriter();
			rootMemento.save(writer);
			mementoString = writer.getBuffer().toString();
			prefStore.setValue(prefStoreKey, mementoString);
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, MetadataPlugin.PLUGIN_ID,
					Messages.OpenRequestMappingUrlWizardPage_ERROR_SAVING_CACHE, e));
		}
	}

	private String getConstructedUrl() {
		String url = ""; //$NON-NLS-1$
		if (urlText != null) {
			url = urlText.getText();
			if (url.endsWith("/")) { //$NON-NLS-1$
				url = url.substring(0, url.length()-1);
			}
		}
		url = url.concat(labelProvider.getColumnText(input, RequestMappingView.COLUMN_URL));

		for (Combo combo : comboList) {
			String textLabel = (String) combo.getData(KEY_URL_DATA);
			url = url.replace(textLabel, combo.getText());
		}
		
		return url;
	}

	protected void performPageFinish() {
		savePreferenceCache();
		TasksUiUtil.openUrl(getConstructedUrl());
	}

	private boolean validatePage() {
		if (!comboList.isEmpty()) {
			for (Combo combo : comboList) {
				if (combo.getText() == null || combo.getText().length() <= 0) {
					return false;
				}
			}
		}
		return (urlText.getText() != null && urlText.getText().length() > 0);
	}

	private class TextModifyListener implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			String errorMessage = null;
			boolean valid = validatePage();
			if (!valid) {
				errorMessage = "'" + getConstructedUrl() //$NON-NLS-1$
						+ Messages.OpenRequestMappingUrlWizardPage_WARNING_URL_CONSTRUCTION;
			}
			setErrorMessage(errorMessage);
			setPageComplete(valid);
		}

	}

}
