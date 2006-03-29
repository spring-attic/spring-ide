/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.ide.eclipse.core.ui.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.springframework.ide.eclipse.core.ui.dialogs.IListAdapter;
import org.springframework.ide.eclipse.core.ui.dialogs.IStringButtonAdapter;
import org.springframework.ide.eclipse.core.ui.dialogs.internal.IListDialogField;
import org.springframework.ide.eclipse.core.ui.fields.DialogField;
import org.springframework.ide.eclipse.core.ui.fields.IDialogField;
import org.springframework.ide.eclipse.core.ui.fields.IDialogFieldListener;
import org.springframework.ide.eclipse.core.ui.fields.ITreeListAdapter;
import org.springframework.ide.eclipse.core.ui.fields.ListDialogField;
import org.springframework.ide.eclipse.core.ui.fields.TreeListDialogField;

/**
 * Base class for wizard pages implementors.
 * 
 * @author Pierre-Antoine Grégoire
 */
public abstract class AbstractWizardCustomPage extends WizardPage {

	private IStatus wizardStatus;

	private AllInOneAdapter wizardAdapter;

	private FormToolkit wizardFormToolkit;

	private ScrolledForm wizardScrolledForm;

	private int columnsNumber = 3;

	private List pageCompleteListeners;

	private boolean alreadyInited = false;

	protected int getColumnsNumber() {
		return columnsNumber;
	}

	protected void setColumnsNumber(int columnsNumber) {
		this.columnsNumber = columnsNumber;
	}

	/**
	 * @return Returns the wizardAdapter.
	 */
	protected AllInOneAdapter getWizardAdapter() {
		return wizardAdapter;
	}

	/**
	 * @param wizardAdapter
	 *            The wizardAdapter to set.
	 */
	protected void setWizardAdapter(AllInOneAdapter wizardAdapter) {
		this.wizardAdapter = wizardAdapter;
	}

	/**
	 * @return Returns the wizardStatus.
	 */
	protected IStatus getWizardStatus() {
		return wizardStatus;
	}

	/**
	 * @param wizardStatus
	 *            The wizardStatus to set.
	 */
	protected void setWizardStatus(IStatus wizardStatus) {
		this.wizardStatus = wizardStatus;
	}

	protected ScrolledForm getWizardForm() {
		return wizardScrolledForm;
	}

	protected FormToolkit getWizardFormToolkit() {
		return wizardFormToolkit;
	}

	/**
	 * Constructor for ConfigXmlPage.
	 * 
	 * @param pageName
	 */
	public AbstractWizardCustomPage(String wizardPageName, String title, String description, int columnsNumber) {
		super(wizardPageName);
		setColumnsNumber(columnsNumber);
		setTitle(title);
		setDescription(description);
		this.pageCompleteListeners = new ArrayList();
	}

	/**
	 * Constructor for ConfigXmlPage.
	 * 
	 * @param pageName
	 */
	public AbstractWizardCustomPage(String wizardId, String title, String description) {
		this(wizardId, title, description, 1);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		wizardFormToolkit = new FormToolkit(Display.getDefault());
		wizardScrolledForm = wizardFormToolkit.createScrolledForm(parent);
		TableWrapLayout wizardLayout = new TableWrapLayout();
		wizardLayout.numColumns = getColumnsNumber();
		wizardScrolledForm.getBody().setLayout(wizardLayout);
		wizardScrolledForm.getBody().setLayoutData(new GridData(GridData.FILL_BOTH));
		wizardAdapter = new AllInOneAdapter();
		wizardScrolledForm.setVisible(true);
		describe();
		setControl(wizardScrolledForm);
		getShell().addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent e) {
			}

			public void focusGained(FocusEvent e) {
				if (!isAlreadyInited()) {
					initialize();
					setAlreadyInited(true);
					touch();
				}
			}

		});
		getShell().addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				if (!isAlreadyInited()) {
					initialize();
					setAlreadyInited(true);
					touch();
				}

			}

		});
	}

	protected void updateStatus(IStatus status) {
		wizardStatus = status;
		setPageComplete(!wizardStatus.matches(IStatus.ERROR));
		AbstractWizardCustomPage.applyToStatusLine(this, wizardStatus);
	}

	protected IStatus createStatus(int severity, String message) {
		IStatus status = new StatusInfo(severity, message);
		return status;
	}

	public void dispose() {
		super.dispose();

	}

	abstract protected void describe();

	abstract protected void initialize();

	abstract protected void touch();

	abstract protected IStatus validate();

	abstract protected void handleCustomButtonPressed(IDialogField field, int buttonIndex);

	abstract protected void handleSelectionChanged(IDialogField field);

	abstract protected void handleDoubleClicked(IDialogField field);

	abstract protected void handleChangeControlPressed(IDialogField field);

	abstract protected void handleDialogFieldChanged(IDialogField field);

	abstract protected void handleKeyPressed(TreeListDialogField field, KeyEvent event);

	/**
	 * Applies the status to the status line of a dialog page.
	 */
	public static void applyToStatusLine(DialogPage page, IStatus status) {
		String message = status.getMessage();
		if (message != null) {
			switch (status.getSeverity()) {
			case IStatus.OK:
				page.setMessage(message, IMessageProvider.NONE);
				page.setErrorMessage(null);
				break;
			case IStatus.WARNING:
				page.setMessage(message, IMessageProvider.WARNING);
				page.setErrorMessage(null);
				break;
			case IStatus.INFO:
				page.setMessage(message, IMessageProvider.INFORMATION);
				page.setErrorMessage(null);
				break;
			default:
				if (message.length() == 0) {
					message = null;
				}
				page.setMessage(message, IMessageProvider.ERROR);
				page.setErrorMessage(null);
				break;
			}
		}
	}

	public void addPageCompleteListener(IPageCompleteListener pageCompleteListener) {
		pageCompleteListeners.add(pageCompleteListener);
	}

	public void removePageCompleteListener(IPageCompleteListener pageCompleteListener) {
		pageCompleteListeners.remove(pageCompleteListener);
	}

	public interface IPageCompleteListener {
		public void pageComplete(AbstractWizardCustomPage wizardPage);
	}

	protected class AllInOneAdapter implements ITreeListAdapter, IListAdapter, IStringButtonAdapter, IDialogFieldListener {

		//
		// ----------IListAdapter
		//
		public void customButtonPressed(IListDialogField field, int index) {
			handleCustomButtonPressed(field, index);
		}

		/**
		 * @see IListAdapter#selectionChanged(ListDialogField)
		 */
		public void selectionChanged(IListDialogField field) {
			handleSelectionChanged(field);
		}

		/**
		 * @see IListAdapter#doubleClicked(ListDialogField)
		 */
		public void doubleClicked(IListDialogField field) {
			handleDoubleClicked(field);
		}

		//
		// ----------IStringButtonAdapter
		//
		/**
		 * @see IStringButtonAdapter#changeControlPressed(DialogField)
		 */
		public void changeControlPressed(IDialogField field) {
			handleChangeControlPressed(field);
		}

		//
		// ----------IDialogFieldListener
		//
		/**
		 * @see IDialogFieldListener#dialogFieldChanged(DialogField)
		 */
		public void dialogFieldChanged(IDialogField field) {
			handleDialogFieldChanged(field);
		}

		//
		// ----------ITreeListAdapter
		//
		public void customButtonPressed(TreeListDialogField field, int index) {
			handleCustomButtonPressed(field, index);
		}

		public void selectionChanged(TreeListDialogField field) {
			handleSelectionChanged(field);
		}

		public void doubleClicked(TreeListDialogField field) {
			handleDoubleClicked(field);
		}

		public void keyPressed(TreeListDialogField field, KeyEvent event) {
			handleKeyPressed(field, event);
		}

		public Object[] getChildren(TreeListDialogField field, Object element) {
			return null;
		}

		public Object getParent(TreeListDialogField field, Object element) {
			return null;
		}

		public boolean hasChildren(TreeListDialogField field, Object element) {
			return false;
		}
	}

	public void setPageComplete(boolean complete) {
		super.setPageComplete(complete);
		if (complete) {
			for (Iterator it = pageCompleteListeners.iterator(); it.hasNext();) {
				((IPageCompleteListener) it.next()).pageComplete(this);
			}
		}
	}

	protected void setAlreadyInited(boolean alreadyInited) {
		this.alreadyInited = alreadyInited;
	}

	protected boolean isAlreadyInited() {
		return alreadyInited;
	}

}