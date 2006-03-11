/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchMessages;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanChildQuery;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanClassQuery;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanNameQuery;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanPropertyQuery;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanReferenceQuery;

/**
 * @author Torsten Juergeleit
 */
public class BeansSearchPage extends DialogPage implements ISearchPage {

	public static final String EXTENSION_POINT_ID = "org.springframework." +
									  "ide.eclipse.beans.ui.search.searchPage";
	public static final int SEARCH_FOR_BEAN_NAME = 0;
	public static final int SEARCH_FOR_BEAN_REFERENCE = 1; 
	public static final int SEARCH_FOR_BEAN_CLASS = 2;
	public static final int SEARCH_FOR_BEAN_CHILD = 3;
	public static final int SEARCH_FOR_BEAN_PROPERTY = 4;

	private static final int HISTORY_SIZE = 12;
	
	// Dialog store id constants
	private final static String PAGE_NAME = "BeansSearchPage";
	private final static String STORE_CASE_SENSITIVE = "CASE_SENSITIVE";
	private static final String STORE_REG_EX_SEARCH = "REG_EX_SEARCH";
	private final static String STORE_HISTORY = "HISTORY";
	private final static String STORE_HISTORY_SIZE = "HISTORY_SIZE";
	
	private final List previousSearchPatterns = new ArrayList();
	
	private boolean firstTime = true;
	private IDialogSettings dialogSettings;
	private boolean isCaseSensitive;
	private boolean isRegExSearch;
	
	private Combo expressionCombo;
	private ISearchPageContainer searchContainer;
	private Button regExCheckbox;
	private CLabel statusLabel;
	private Button caseSensitiveCheckbox;
	
	private Button[] searchForButtons;
	private String[] searchForText = {
							BeansSearchMessages.SearchPage_searchFor_name,
							BeansSearchMessages.SearchPage_searchFor_reference, 
							BeansSearchMessages.SearchPage_searchFor_class, 
							BeansSearchMessages.SearchPage_searchFor_child, 
							BeansSearchMessages.SearchPage_searchFor_property };
	public BeansSearchPage() {
		// required
	}

	public BeansSearchPage(String title) {
		super(title);
	}

	public BeansSearchPage(String title, ImageDescriptor image) {
		super(title, image);
	}

	private String[] getPreviousSearchPatterns() {

		// Search results are not persistent
		int patternCount = previousSearchPatterns.size();
		String [] patterns = new String[patternCount];
		for (int i= 0; i < patternCount; i++) {
			patterns[i] = ((SearchData)
								   previousSearchPatterns.get(i)).getPattern();
		}
		return patterns;
	}

	private int getSearchFor() {
		for (int i = 0; i < searchForButtons.length; i++) {
			if (searchForButtons[i].getSelection()) {
				return i;
			}
		}
		return -1;
	}

	private String getPattern() {
		return expressionCombo.getText();
	}

	private SearchData findInPrevious(String pattern) {
		for (Iterator iter = previousSearchPatterns.iterator();
															iter.hasNext(); ) {
			SearchData element= (SearchData) iter.next();
			if (pattern.equals(element.getPattern())) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Returns search pattern data and update previous searches.
	 * An existing entry will be updated.
	 */
	private SearchData getPatternData() {
		String pattern = getPattern();
		SearchData match = findInPrevious(pattern);
		if (match != null) {
			previousSearchPatterns.remove(match);
		}
		match = new SearchData(getSearchFor(), pattern,
							   caseSensitiveCheckbox.getSelection(),
							   regExCheckbox.getSelection(),
							   searchContainer.getSelectedScope(),
							   searchContainer.getSelectedWorkingSets());
			
		previousSearchPatterns.add(0, match); // insert on top
		return match;
	}

	public void setContainer(ISearchPageContainer container) {
		this.searchContainer = container;
	}

	public void setVisible(boolean visible) {
		if (visible && expressionCombo != null) {
			if (firstTime) {
				firstTime = false;
				// Set item and text here to prevent page from resizing
				expressionCombo.setItems(getPreviousSearchPatterns());
				initSelections();
			}
			expressionCombo.setFocus();
		}
		updateOKStatus();
		super.setVisible(visible);
	}

	public boolean performAction() {
		SearchData data = getPatternData();

		// Setup search scope
		BeansSearchScope scope;
		switch (searchContainer.getSelectedScope()) {
			case ISearchPageContainer.SELECTION_SCOPE :
				scope = BeansSearchScope.newSearchScope(
										searchContainer.getSelection(), false);
				break;

			case ISearchPageContainer.WORKING_SET_SCOPE :
				scope = BeansSearchScope.newSearchScope(
									 searchContainer.getSelectedWorkingSets());
				break;

			case ISearchPageContainer.SELECTED_PROJECTS_SCOPE :
				
				scope = BeansSearchScope.newSearchScope(
										 searchContainer.getSelection(), true);
				break;

			default:
				scope = BeansSearchScope.newSearchScope();
		}

		ISearchQuery query = null;
		switch (data.getSearchFor()) {
			case SEARCH_FOR_BEAN_NAME :
				query = new BeanNameQuery(scope, data.getPattern(),
									  data.isCaseSensitive(), data.isRegExp());
				break;

			case SEARCH_FOR_BEAN_REFERENCE :
				query = new BeanReferenceQuery(scope, data.getPattern(),
									  data.isCaseSensitive(), data.isRegExp());
				break;

			case SEARCH_FOR_BEAN_CLASS :
				query = new BeanClassQuery(scope, data.getPattern(),
										   isCaseSensitive, isRegExSearch);
				break;

			case SEARCH_FOR_BEAN_CHILD :
				query = new BeanChildQuery(scope, data.getPattern(),
										   isCaseSensitive, isRegExSearch);
				break;

			case SEARCH_FOR_BEAN_PROPERTY :
				query = new BeanPropertyQuery(scope, data.getPattern(),
											  isCaseSensitive, isRegExSearch);
				break;
		} 

		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInBackground(query);
		return true;
	}

	/**
	 * Returns the page settings for this Java search page.
	 * 
	 * @return the page settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings =
							BeansSearchPlugin.getDefault().getDialogSettings();
		dialogSettings = settings.getSection(PAGE_NAME);
		if (dialogSettings == null) {
			dialogSettings= settings.addNewSection(PAGE_NAME);
		}
		return dialogSettings;
	}

	/**
	 * Initializes itself from the stored page settings.
	 */
	private void readConfiguration() {
		IDialogSettings s = getDialogSettings();
		isCaseSensitive = s.getBoolean(STORE_CASE_SENSITIVE);
		isRegExSearch = s.getBoolean(STORE_REG_EX_SEARCH);

		try {
			int historySize = s.getInt(STORE_HISTORY_SIZE);
			for (int i= 0; i < historySize; i++) {
				IDialogSettings histSettings = s.getSection(STORE_HISTORY + i);
				if (histSettings != null) {
					SearchData data = SearchData.create(histSettings);
					if (data != null) {
						previousSearchPatterns.add(data);
					}
				}
			}
		} catch (NumberFormatException e) {
			// ignore
		}
	}

	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s = getDialogSettings();
		s.put(STORE_CASE_SENSITIVE, isCaseSensitive);
		s.put(STORE_REG_EX_SEARCH, isRegExSearch);

		int historySize = Math.min(previousSearchPatterns.size(), HISTORY_SIZE);
		s.put(STORE_HISTORY_SIZE, historySize);
		for (int i= 0; i < historySize; i++) {
			IDialogSettings histSettings = s.addNewSection(STORE_HISTORY + i);
			SearchData data = ((SearchData) previousSearchPatterns.get(i));
			data.store(histSettings);
		}
	}

	/**
	 * Creates the page's content.
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		readConfiguration();

		Composite result = new Composite(parent, SWT.NONE);

		GridLayout layout= new GridLayout(2, false);
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
		
		Control searchFor = createSearchFor(result);
		searchFor.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
											 false, false, 1, 1));
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
		label.setText(BeansSearchMessages.SearchPage_expression); 
		label.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false,
										 false, 2, 1));
		// Expression combo
		expressionCombo = new Combo(group, SWT.SINGLE | SWT.BORDER);
		expressionCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handlePatternSelected();
				updateOKStatus();
			}
		});
		expressionCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doPatternModified();
				updateOKStatus();
			}
		});
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, false,
									 1, 1);
		data.widthHint = convertWidthInCharsToPixels(50);
		expressionCombo.setLayoutData(data);

		// Ignore case checkbox		
		caseSensitiveCheckbox = new Button(group, SWT.CHECK);
		caseSensitiveCheckbox.setText(BeansSearchMessages.SearchPage_caseSensitive); 
		caseSensitiveCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isCaseSensitive = caseSensitiveCheckbox.getSelection();
			}
		});
		caseSensitiveCheckbox.setLayoutData(new GridData(GridData.FILL,
										   GridData.FILL, false, false, 1, 1));
		// Text line which explains the special characters
		statusLabel = new CLabel(group, SWT.LEAD);
		statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		statusLabel.setFont(group.getFont());
		statusLabel.setAlignment(SWT.LEFT);
		statusLabel.setText(BeansSearchMessages.SearchPage_expressionHint); 

		// RegEx checkbox
		regExCheckbox = new Button(group, SWT.CHECK);
		regExCheckbox.setText(BeansSearchMessages.SearchPage_regularExpression); 
		regExCheckbox.setSelection(isRegExSearch);
		regExCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isRegExSearch = regExCheckbox.getSelection();
				updateOKStatus();
				writeConfiguration();
			}
		});
		regExCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		regExCheckbox.setFont(group.getFont());
		return group;
	}

	final void updateOKStatus() {
		boolean isValid = isValidSearchPattern();
		searchContainer.setPerformActionEnabled(isValid);
	}

	private boolean isValidSearchPattern() {
		if (getPattern().length() == 0) {
			return false;
		}
// TODO		return SearchPattern.createPattern(getPattern(), getSearchFor(), getLimitTo(), SearchPattern.R_EXACT_MATCH) != null;		
		return true;
	}

	public void dispose() {
		writeConfiguration();
		super.dispose();
	}

	private void doPatternModified() {
		// TODO Is this method necessary?
	}

	private void handlePatternSelected() {
		int selectionIndex = expressionCombo.getSelectionIndex();
		if (selectionIndex < 0 ||
							 selectionIndex >= previousSearchPatterns.size()) {
			return;
		}
		SearchData data = (SearchData)
									previousSearchPatterns.get(selectionIndex);
		setSearchFor(data.getSearchFor());

		expressionCombo.setText(data.getPattern());
		isCaseSensitive = data.isCaseSensitive();
		caseSensitiveCheckbox.setSelection(data.isCaseSensitive());
		regExCheckbox.setSelection(data.isRegExp());
		if (data.getWorkingSets() != null) {
			searchContainer.setSelectedWorkingSets(data.getWorkingSets());
		} else {
			searchContainer.setSelectedScope(data.getScope());
		}
	}

	private void setSearchFor(int searchFor) {
		for (int i= 0; i < searchForButtons.length; i++) {
			searchForButtons[i].setSelection(searchFor == i);
		}
	}

	private Control createSearchFor(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(BeansSearchMessages.SearchPage_searchFor); 
		group.setLayout(new GridLayout(2, true));

		SelectionAdapter selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				doPatternModified();
			}
		};

		searchForButtons = new Button[searchForText.length];
		for (int i = 0; i < searchForText.length; i++) {
			Button button = new Button(group, SWT.RADIO);
			button.setText(searchForText[i]);
			button.setSelection(i == SEARCH_FOR_BEAN_CLASS);
			button.setLayoutData(new GridData());
			button.addSelectionListener(selectionListener);
			searchForButtons[i] = button;
		}
		return group;		
	}

	private void initSelections() {
		SearchData initData = null;

// TODO handle seclection
//		ISelection sel = searchContainer.getSelection();
//		if (sel instanceof IStructuredSelection) {
//			initData = tryStructuredSelection((IStructuredSelection) sel);
//		} else if (sel instanceof ITextSelection) {
//			initData = trySimpleTextSelection((ITextSelection) sel);
//		}

		if (initData == null) {
			initData = getDefaultInitValues();
		}

		expressionCombo.setText(initData.getPattern());
		caseSensitiveCheckbox.setSelection(initData.isCaseSensitive());
		regExCheckbox.setSelection(initData.isRegExp());
		
		setSearchFor(initData.getSearchFor());
	}

	private SearchData tryStructuredSelection(IStructuredSelection selection) {
		if (selection == null || selection.size() > 1) {
			return null;
		}
		Object o = selection.getFirstElement();
		SearchData res = null;
		if (res == null && o instanceof IAdaptable) {
			IWorkbenchAdapter adapter = (IWorkbenchAdapter)
						   ((IAdaptable)o).getAdapter(IWorkbenchAdapter.class);
			if (adapter != null) {
				return new SearchData(SEARCH_FOR_BEAN_CLASS, adapter.getLabel(o),
									  isCaseSensitive, false);
			}
		}
		return res;
	}

	private SearchData trySimpleTextSelection(ITextSelection selection) {
		String selectedText= selection.getText();
		if (selectedText != null && selectedText.length() > 0) {
			int i = 0;
// TODO			while (i < selectedText.length() && !StringUtils.isLineDelimiterChar(selectedText.charAt(i))) {
			while (i < selectedText.length()) {
				i++;
			}
			if (i > 0) {
				return new SearchData(SEARCH_FOR_BEAN_CLASS,
						 selectedText.substring(0, i), isCaseSensitive, false);
			}
		}
		return null;
	}
	
	private SearchData getDefaultInitValues() {
		if (!previousSearchPatterns.isEmpty()) {
			return (SearchData) previousSearchPatterns.get(0);
		}
		return new SearchData(SEARCH_FOR_BEAN_NAME, "", isCaseSensitive, false);
	}	

	private static class SearchData {

		private int searchFor;
		private String pattern;
		private boolean isCaseSensitive;
		private boolean isRegExp;
		private int scope;
		private IWorkingSet[] workingSets;

		public SearchData(int searchFor, String pattern,
						  boolean isCaseSensitive, boolean isRegExp) {
			this(searchFor, pattern, isCaseSensitive, isRegExp,
				 ISearchPageContainer.WORKSPACE_SCOPE, null);
		}

		public SearchData(int searchFor, String pattern,
						  boolean isCaseSensitive, boolean isRegExp,
						  int scope, IWorkingSet[] workingSets) {
			this.searchFor = searchFor;
			this.pattern = pattern;
			this.isCaseSensitive = isCaseSensitive;
			this.isRegExp = isRegExp;
			this.scope = scope;
			this.workingSets = workingSets;
		}
		
		public String getPattern() {
			return pattern;
		}

		public boolean isCaseSensitive() {
			return isCaseSensitive;
		}

		public boolean isRegExp() {
			return isRegExp;
		}
		
		public int getSearchFor() {
			return searchFor;
		}
		
		public IWorkingSet[] getWorkingSets() {
			return workingSets;
		}

		public int getScope() {
			return scope;
		}

		public void store(IDialogSettings settings) {
			settings.put("searchFor", searchFor);
			settings.put("pattern", pattern);
			settings.put("isCaseSensitive", isCaseSensitive);
			settings.put("isRegExp", isRegExp);
			settings.put("scope", scope);
			if (workingSets != null) {
				String[] wsIds = new String[workingSets.length];
				for (int i = 0; i < workingSets.length; i++) {
					wsIds[i] = workingSets[i].getId();
				}
				settings.put("workingSets", wsIds);
			} else {
				settings.put("workingSets", new String[0]);
			}
		}

		public static SearchData create(IDialogSettings settings) {
			String pattern = settings.get("pattern");
			if (pattern.length() == 0) {
				return null;
			}

			String[] wsIds = settings.getArray("workingSets");
			IWorkingSet[] workingSets = null;
			if (wsIds != null && wsIds.length > 0) {
				IWorkingSetManager workingSetManager =
							  PlatformUI.getWorkbench().getWorkingSetManager();
				workingSets = new IWorkingSet[wsIds.length];
				for (int i= 0; workingSets != null && i < wsIds.length; i++) {
					workingSets[i] = workingSetManager.getWorkingSet(wsIds[i]);
					if (workingSets[i] == null) {
						workingSets = null;
					}
				}
			}

			try {
				int searchFor = settings.getInt("searchFor");
				int scope = settings.getInt("scope");
				boolean isCaseSensitive = settings.getBoolean(
															"isCaseSensitive");
				boolean isRegExp = settings.getBoolean("isRegExp");
				return new SearchData(searchFor, pattern, isCaseSensitive,
									  isRegExp, scope, workingSets);
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}
}
