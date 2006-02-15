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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansClassQuery;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansReferenceQuery;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;

/**
 * @author David Watkins
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansSearchPage extends DialogPage implements ISearchPage {

	private static boolean _includeSubtypes;

	private static ArrayList _fqnHistory = new ArrayList();

	private static ArrayList _typeHistory = new ArrayList();

	private IType _type = null;

	private String _reference = "";

	private Combo _typeHistoryCombo;

	private Button _includeSubtypesCheckbox;

	private ISearchPageContainer _container;

	private Button _browseButton;

	private Combo _refHistoryCombo;

	private static ArrayList _refHistory = new ArrayList();

	private Button _refSearch;

	private Button _javaSearch;


    /**
     * Creates a new empty dialog page.
     */
    public BeansSearchPage() {
        //No initial behaviour
    }
	
	
	public BeansSearchPage(String title) {
		super(title);
	}

	public BeansSearchPage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public void createControl(final Composite parent) {
			Composite contents = new Composite(parent, SWT.NONE);

			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;

			setControl(contents);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			contents.setLayout(layout);
			contents.setLayoutData(gridData);

			_javaSearch = createTypeSearchElements(parent, contents);
			Label sep = new Label(contents, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(fullRow());
			sep.setText("or");

			_refSearch = createReferenceSearchElements(contents);
			createSearchToggle();
			Dialog.applyDialogFont(contents);

			initialiseSelection();
	}

	private void initialiseSelection() {
		ISelection rawSelection = BeansSearchPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService()
				.getSelection();
		if (rawSelection == null
				|| !(rawSelection instanceof IStructuredSelection)) {
			return;
		}
		IStructuredSelection selection = (IStructuredSelection) rawSelection;

		Object selectedObj = selection.getFirstElement();
		if (selectedObj != null) {
			IType selectedType = null;
			String selectedFqn = null;

			if (selectedObj instanceof IType) {
				selectedType = (IType) selectedObj;
			}

			if (selectedObj instanceof ICompilationUnit) {
				ICompilationUnit unit = (ICompilationUnit) selectedObj;
				try {
					selectedType = (IType) unit.getTypes()[0];
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}

			if (selectedType != null) {
				_type = selectedType;
				selectedFqn = selectedType.getFullyQualifiedName();
				_typeHistoryCombo.add(selectedFqn);
				_typeHistoryCombo.setText(selectedFqn);
			}
		}
	}

	private Button createReferenceSearchElements(Composite contents) {
		final Button refSearch = new Button(contents, SWT.RADIO);
		refSearch.setEnabled(true);
		refSearch.setSelection(false);
		refSearch.setText("Search by name/id");
		refSearch.setLayoutData(fullRow());

		_refHistoryCombo = createReferenceHistoryCombo(contents);
		return refSearch;
	}

	private Combo createReferenceHistoryCombo(Composite contents) {
		final Combo refCombo = new Combo(contents, SWT.DROP_DOWN);
		populateReferenceHistory(refCombo);
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				String txt = refCombo.getText();
				txt = txt.trim();
				if (txt.equals("")) {
					_container.setPerformActionEnabled(false);
				} else {
					_container.setPerformActionEnabled(true);
				}
				_reference = txt;
			}
		};

		refCombo.addListener(SWT.Modify, listener);
		refCombo.addListener(SWT.Selection, listener);
		refCombo.setEnabled(false);
		refCombo.setLayoutData(fullRow());
		return refCombo;
	}

	private void populateReferenceHistory(Combo refCombo) {
		for (int i = 0; i < _refHistory.size(); i++) {
			refCombo.add((String) _refHistory.get(i));
		}
	}

	private Button createTypeSearchElements(final Composite parent,
			Composite contents) {
		final Button javaSearch = new Button(contents, SWT.RADIO);
		javaSearch.setEnabled(true);
		javaSearch.setSelection(true);
		javaSearch.setText("Search by class");
		javaSearch.setLayoutData(fullRow());

		_typeHistoryCombo = createTypeHistoryCombo(contents);
		_typeHistoryCombo.setLayoutData(bigFirst());

		_browseButton = createBrowseButton(parent, contents);
		_includeSubtypesCheckbox = createIncludesSubtypesCheckbox(contents);
		return javaSearch;
	}

	private void createSearchToggle() {
		SelectionAdapter adapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button but = (Button) e.getSource();
				if (but == _refSearch) {
					enableRefSearch();
				}
				if (but == _javaSearch) {
					enableTypeSearch();
				}
			}
		};

		_refSearch.addSelectionListener(adapter);
		_javaSearch.addSelectionListener(adapter);
	}

	private void enableRefSearch() {
		_includeSubtypesCheckbox.setEnabled(false);
		_browseButton.setEnabled(false);
		_typeHistoryCombo.setEnabled(false);
		_refHistoryCombo.setEnabled(true);
		if (_reference.equals("")) {
			_container.setPerformActionEnabled(false);
		} else {
			_container.setPerformActionEnabled(true);
		}
	}

	private void enableTypeSearch() {
		_includeSubtypesCheckbox.setEnabled(true);
		_browseButton.setEnabled(true);
		_typeHistoryCombo.setEnabled(true);
		_refHistoryCombo.setEnabled(false);
		if (_type == null) {
			_container.setPerformActionEnabled(false);
		} else {
			_container.setPerformActionEnabled(true);
		}
	}

	private GridData bigFirst() {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 1;
		return gd;
	}

	private GridData fullRow() {
		GridData gd = new GridData(0, 0, true, false);
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		return gd;
	}

	private Button createBrowseButton(final Composite parent, Composite contents) {
		Button browseButton = new Button(contents, SWT.NONE);

		browseButton.setText("Browse");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				SelectionDialog dialog;
				try {
					dialog = JavaUI.createTypeDialog(parent.getShell(),
							new ProgressMonitorDialog(parent.getShell()),
							SearchEngine.createWorkspaceScope(),
							IJavaElementSearchConstants.CONSIDER_ALL_TYPES, false);
					dialog.setTitle("Select Bean Type");
					dialog
							.setMessage("Select bean type to search for in Spring configs:");
					if (dialog.open() == IDialogConstants.CANCEL_ID) {
						return;
					}
					Object[] types = dialog.getResult();
					if (types == null || types.length == 0)
						return;
					setType((IType) types[0]);
				} catch (JavaModelException e1) {
					e1.printStackTrace();
				}

			}
		});
		return browseButton;
	}

	private Button createIncludesSubtypesCheckbox(Composite contents) {
		Button includeSubtypesCheckbox = new Button(contents, SWT.CHECK);
		includeSubtypesCheckbox.setText("Include Subtypes");
		includeSubtypesCheckbox.setSelection(_includeSubtypes);
		return includeSubtypesCheckbox;
	}

	private Combo createTypeHistoryCombo(Composite contents) {
		final Combo historyCombo = new Combo(contents, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		historyCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = historyCombo.getSelectionIndex();
				historyCombo.getItem(selectionIndex);
				String fqn = historyCombo.getText();
				if (fqn != null && !fqn.trim().equals("")) {
					setType((IType) _typeHistory.get(selectionIndex));
				} else {
					_container.setPerformActionEnabled(false);
				}
			}
		});
		populateTypeHistory(historyCombo);
		return historyCombo;

	}

	private void populateTypeHistory(Combo historyCombo) {
		for (int i = 0; i < _fqnHistory.size(); i++) {
			historyCombo.add((String) _fqnHistory.get(i));
		}
	}

	public void setContainer(ISearchPageContainer container) {
		_container = container;

	}

	private IType getType() {
		return _type;
	}

	public boolean performAction() {
		ISearchQuery collator = null;
		BeansSearchScope scope = getSearchScope();
		if (_javaSearch.getSelection()) {
			collator = new BeansClassQuery(scope, getType().getFullyQualifiedName());
		}
		if (_refSearch.getSelection()) {
			collator = new BeansReferenceQuery(scope, _reference);
			if (!_refHistory.contains(_reference)) {
				_refHistory.add(_reference);
			}
		}
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInBackground(collator);
		return true;
	}

	private BeansSearchScope getSearchScope() {
		BeansSearchScope scope;
		switch (_container.getSelectedScope()) {
			case ISearchPageContainer.SELECTION_SCOPE :
				scope = BeansSearchScope.newSearchScope(_container.getSelection(), false);
				break;

			case ISearchPageContainer.WORKING_SET_SCOPE :
				scope = BeansSearchScope.newSearchScope(_container.getSelectedWorkingSets());
				break;

			case ISearchPageContainer.SELECTED_PROJECTS_SCOPE :
				
				scope = BeansSearchScope.newSearchScope(_container.getSelection(), true);
				break;

			default:
				scope = BeansSearchScope.newSearchScope();
		}
		return scope;
	}

	private void setType(IType type) {
		_type = type;
		String fqn = type.getFullyQualifiedName();
		if (!_fqnHistory.contains(fqn)) {
			_fqnHistory.add(fqn);
			_typeHistory.add(getType());
			_typeHistoryCombo.add(fqn);
		}
		_typeHistoryCombo.setText(fqn);
		_container.setPerformActionEnabled(true);
		try {
			if (type.isInterface()) {
				_includeSubtypesCheckbox.setSelection(true);
				_includeSubtypesCheckbox.setEnabled(false);
			} else {
				_includeSubtypesCheckbox.setEnabled(true);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		_includeSubtypes = _includeSubtypesCheckbox.getSelection();
	}
}
