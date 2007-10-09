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
package org.springframework.ide.eclipse.aop.ui.matcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.aop.ui.matcher.internal.PointcutMatchQuery;
import org.springframework.ide.eclipse.aop.ui.matcher.internal.PointcutMatcherScope;
import org.springframework.ide.eclipse.aop.ui.matcher.internal.PointcutMatcherMessages;

/**
 * {@link ISearchPage} implementation that is used to enter pointcut match
 * requests in Eclipse' search UI.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class PointcutMatcherSearchPage extends DialogPage implements
		ISearchPage {

	private static class PointcutMatchData {

		public static PointcutMatchData create(IDialogSettings settings) {
			String pattern = settings.get("pattern");
			if (pattern.length() == 0) {
				return null;
			}

			String[] wsIds = settings.getArray("workingSets");
			IWorkingSet[] workingSets = null;
			if (wsIds != null && wsIds.length > 0) {
				IWorkingSetManager workingSetManager = PlatformUI
						.getWorkbench().getWorkingSetManager();
				workingSets = new IWorkingSet[wsIds.length];
				for (int i = 0; workingSets != null && i < wsIds.length; i++) {
					workingSets[i] = workingSetManager.getWorkingSet(wsIds[i]);
					if (workingSets[i] == null) {
						workingSets = null;
					}
				}
			}

			try {
				int scope = settings.getInt("scope");
				boolean isProxyTargetClass = settings
						.getBoolean("isProxyTargetClass");
				return new PointcutMatchData(pattern, isProxyTargetClass,
						scope, workingSets);
			}
			catch (NumberFormatException e) {
				return null;
			}
		}

		private String expression;

		private boolean isProxyTargetClass;

		private int scope;

		private IWorkingSet[] workingSets;

		public PointcutMatchData(String pattern, boolean isProxyTargetClass) {
			this(pattern, isProxyTargetClass,
					ISearchPageContainer.WORKSPACE_SCOPE, null);
		}

		public PointcutMatchData(String pattern, boolean isProxyTargetClass,
				int scope, IWorkingSet[] workingSets) {
			this.expression = pattern;
			this.isProxyTargetClass = isProxyTargetClass;
			this.scope = scope;
			this.workingSets = workingSets;
		}

		public String getExpression() {
			return expression;
		}

		public int getScope() {
			return scope;
		}

		public IWorkingSet[] getWorkingSets() {
			return workingSets;
		}

		public boolean isProxyTargetClass() {
			return isProxyTargetClass;
		}

		public void store(IDialogSettings settings) {
			settings.put("pattern", expression);
			settings.put("isProxyTargetClass", isProxyTargetClass);
			settings.put("scope", scope);
			if (workingSets != null) {
				String[] wsIds = new String[workingSets.length];
				for (int i = 0; i < workingSets.length; i++) {
					wsIds[i] = workingSets[i].getId();
				}
				settings.put("workingSets", wsIds);
			}
			else {
				settings.put("workingSets", new String[0]);
			}
		}
	}

	private static final int HISTORY_SIZE = 12;

	// Dialog store id constants
	private final static String PAGE_NAME = "PointcutMatcherPage";

	private static final String STORE_PROXY_TARGET_CLASS = "PROXY_TARGET_CLASS";

	private final static String STORE_HISTORY = "HISTORY";

	private final static String STORE_HISTORY_SIZE = "HISTORY_SIZE";

	private final List<PointcutMatchData> previousPointcutMatchData = 
		new ArrayList<PointcutMatchData>();

	private boolean firstTime = true;

	private IDialogSettings dialogSettings;

	private boolean isProxyTargetClass;

	private Combo expressionCombo;

	private ISearchPageContainer searchContainer;

	private CLabel statusLabel;

	private Button proxyTargetClassCheckbox;

	private PointcutMatcherScope scope;

	public PointcutMatcherSearchPage() {
		// required
	}

	public PointcutMatcherSearchPage(String title) {
		super(title);
	}

	public PointcutMatcherSearchPage(String title, ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * Creates the page's content.
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		readConfiguration();

		Composite result = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		result.setLayout(layout);

		Control expressionComposite = createExpression(result);
		expressionComposite.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false, 2, 1));
		Label separator = new Label(result, SWT.NONE);
		separator.setVisible(false);
		GridData data = new GridData(GridData.FILL, GridData.FILL, false,
				false, 2, 1);
		data.heightHint = convertHeightInCharsToPixels(1) / 3;
		separator.setLayoutData(data);

		setControl(result);
		Dialog.applyDialogFont(result);
	}

	private Control createExpression(Composite parent) {
		// Group with grid layout with 2 columns
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		group.setLayout(layout);

		// Expression text + info
		Label label = new Label(group, SWT.LEFT);
		label.setText(PointcutMatcherMessages.MatcherPage_expression);
		label.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false,
				false, 2, 1));

		// Expression combo
		expressionCombo = new Combo(group, SWT.SINGLE | SWT.BORDER);
		expressionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handlePatternSelected();
				updateOKStatus();
			}
		});
		expressionCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateOKStatus();
			}
		});
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, false,
				1, 1);
		data.widthHint = convertWidthInCharsToPixels(50);
		expressionCombo.setLayoutData(data);

		// proxy target class checkbox
		proxyTargetClassCheckbox = new Button(group, SWT.CHECK);
		proxyTargetClassCheckbox
				.setText(PointcutMatcherMessages.MatcherPage_proxyTargetClass);
		proxyTargetClassCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isProxyTargetClass = proxyTargetClassCheckbox.getSelection();
			}
		});
		proxyTargetClassCheckbox.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, false, false, 1, 1));
		// Text line which explains the special characters
		statusLabel = new CLabel(group, SWT.LEAD);
		statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		statusLabel.setFont(group.getFont());
		statusLabel.setAlignment(SWT.LEFT);
		statusLabel.setText(PointcutMatcherMessages.MatcherPage_expressionHint);

		return group;
	}

	@Override
	public void dispose() {
		writeConfiguration();
		super.dispose();
	}

	private PointcutMatchData findInPrevious(String pattern) {
		for (PointcutMatchData element : previousPointcutMatchData) {
			if (pattern.equals(element.getExpression())) {
				return element;
			}
		}
		return null;
	}

	private PointcutMatchData getDefaultInitValues() {
		if (!previousPointcutMatchData.isEmpty()) {
			return previousPointcutMatchData.get(0);
		}
		return new PointcutMatchData("", false);
	}

	/**
	 * Returns the page settings for this search page.
	 * @return the page settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = PointcutMatcherPlugin.getDefault()
				.getDialogSettings();
		dialogSettings = settings.getSection(PAGE_NAME);
		if (dialogSettings == null) {
			dialogSettings = settings.addNewSection(PAGE_NAME);
		}
		return dialogSettings;
	}

	private String getExpression() {
		return expressionCombo.getText();
	}

	/**
	 * Returns search pattern data and update previous searches. An existing
	 * entry will be updated.
	 */
	private PointcutMatchData getPatternData() {
		String pattern = getExpression();
		PointcutMatchData match = findInPrevious(pattern);
		if (match != null) {
			previousPointcutMatchData.remove(match);
		}
		match = new PointcutMatchData(pattern, isProxyTargetClass(),
				searchContainer.getSelectedScope(), searchContainer
						.getSelectedWorkingSets());

		previousPointcutMatchData.add(0, match); // insert on top
		return match;
	}

	private String[] getPreviousExpressions() {

		// Search results are not persistent
		int patternCount = previousPointcutMatchData.size();
		String[] patterns = new String[patternCount];
		for (int i = 0; i < patternCount; i++) {
			patterns[i] = (previousPointcutMatchData.get(i)).getExpression();
		}
		return patterns;
	}

	private void handlePatternSelected() {
		int selectionIndex = expressionCombo.getSelectionIndex();
		if (selectionIndex < 0
				|| selectionIndex >= previousPointcutMatchData.size()) {
			return;
		}
		PointcutMatchData data = previousPointcutMatchData
				.get(selectionIndex);

		expressionCombo.setText(data.getExpression());
		isProxyTargetClass = data.isProxyTargetClass();
		proxyTargetClassCheckbox.setSelection(data.isProxyTargetClass());
		if (data.getWorkingSets() != null) {
			searchContainer.setSelectedWorkingSets(data.getWorkingSets());
		}
		else {
			searchContainer.setSelectedScope(data.getScope());
		}
	}

	private void initSelections() {
		PointcutMatchData initData = null;

		// TODO handle seclection
		// ISelection sel = searchContainer.getSelection();
		// if (sel instanceof IStructuredSelection) {
		// initData = tryStructuredSelection((IStructuredSelection) sel);
		// } else if (sel instanceof ITextSelection) {
		// initData = trySimpleTextSelection((ITextSelection) sel);
		// }

		if (initData == null) {
			initData = getDefaultInitValues();
		}

		expressionCombo.setText(initData.getExpression());
		proxyTargetClassCheckbox.setSelection(initData.isProxyTargetClass());
	}

	private boolean isProxyTargetClass() {
		return proxyTargetClassCheckbox.getSelection();
	}

	private boolean isValidExpression() {
		if (getExpression().length() == 0) {
			return false;
		}

		// TODO CD move validation
		/*
		 * try { AspectJExpressionPointcut pointcut = new
		 * AspectJExpressionPointcut();
		 * pointcut.setExpression(getPointcutExpression());
		 * pointcut.matches(getClass()); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */
		return true;
	}

	public boolean performAction() {

		switch (searchContainer.getSelectedScope()) {
		case ISearchPageContainer.SELECTION_SCOPE:
			scope = PointcutMatcherScope.newSearchScope(searchContainer
					.getSelection(), false);
			break;
		case ISearchPageContainer.WORKING_SET_SCOPE:
			scope = PointcutMatcherScope.newSearchScope(searchContainer
					.getSelectedWorkingSets());
			break;
		case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
			scope = PointcutMatcherScope.newSearchScope(searchContainer
					.getSelection(), true);
			break;
		default:
			scope = PointcutMatcherScope.newSearchScope();
		}

		PointcutMatchData data = getPatternData();
		ISearchQuery query = new PointcutMatchQuery(scope,
				data.getExpression(), data.isProxyTargetClass());

		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInBackground(query);
		return true;
	}

	/**
	 * Initializes itself from the stored page settings.
	 */
	private void readConfiguration() {
		IDialogSettings s = getDialogSettings();
		isProxyTargetClass = s.getBoolean(STORE_PROXY_TARGET_CLASS);

		try {
			int historySize = s.getInt(STORE_HISTORY_SIZE);
			for (int i = 0; i < historySize; i++) {
				IDialogSettings histSettings = s.getSection(STORE_HISTORY + i);
				if (histSettings != null) {
					PointcutMatchData data = PointcutMatchData
							.create(histSettings);
					if (data != null) {
						previousPointcutMatchData.add(data);
					}
				}
			}
		}
		catch (NumberFormatException e) {
			// ignore
		}
	}

	public void setContainer(ISearchPageContainer container) {
		searchContainer = container;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible && expressionCombo != null) {
			if (firstTime) {
				firstTime = false;
				// Set item and text here to prevent page from resizing
				expressionCombo.setItems(getPreviousExpressions());
				initSelections();
			}
			expressionCombo.setFocus();
		}
		updateOKStatus();
		super.setVisible(visible);
	}

	// private SearchData tryStructuredSelection(IStructuredSelection selection)
	// {
	// if (selection == null || selection.size() > 1) {
	// return null;
	// }
	// Object o = selection.getFirstElement();
	// SearchData res = null;
	// if (res == null && o instanceof IAdaptable) {
	// IWorkbenchAdapter adapter = (IWorkbenchAdapter)
	// ((IAdaptable)o).getAdapter(IWorkbenchAdapter.class);
	// if (adapter != null) {
	// return new SearchData(SEARCH_FOR_BEAN_CLASS, adapter.getLabel(o),
	// isCaseSensitive, false);
	// }
	// }
	// return res;
	// }

	// private SearchData trySimpleTextSelection(ITextSelection selection) {
	// String selectedText= selection.getText();
	// if (selectedText != null && selectedText.length() > 0) {
	// int i = 0;
	// TODO while (i < selectedText.length() &&
	// !StringUtils.isLineDelimiterChar(selectedText.charAt(i))) {
	// while (i < selectedText.length()) {
	// i++;
	// }
	// if (i > 0) {
	// return new SearchData(SEARCH_FOR_BEAN_CLASS,
	// selectedText.substring(0, i), isCaseSensitive, false);
	// }
	// }
	// return null;
	// }

	final void updateOKStatus() {
		boolean isValid = isValidExpression();
		searchContainer.setPerformActionEnabled(isValid);
	}

	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s = getDialogSettings();
		s.put(STORE_PROXY_TARGET_CLASS, isProxyTargetClass);

		int historySize = Math.min(previousPointcutMatchData.size(),
				HISTORY_SIZE);
		s.put(STORE_HISTORY_SIZE, historySize);
		for (int i = 0; i < historySize; i++) {
			IDialogSettings histSettings = s.addNewSection(STORE_HISTORY + i);
			PointcutMatchData data = (previousPointcutMatchData.get(i));
			data.store(histSettings);
		}
	}
}
