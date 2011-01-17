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
package org.springframework.ide.eclipse.beans.ui.refactoring.ltk;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Martin Lippert
 * @since 2.0
 */
public class RenameIdRefactoringInputPage extends UserInputWizardPage {

	private Text nameField;

	public RenameIdRefactoringInputPage(String name) {
		super(name);
	}

	public void createControl(Composite parent) {
		final RenameIdRefactoring refactoring = getRenameIdRefactoring();

		Composite result = new Composite(parent, SWT.NONE);
		setControl(result);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		result.setLayout(layout);

		Label label = new Label(result, SWT.NONE);
		label.setText("&" + refactoring.getType().getType() + " id:");

		nameField = createNameField(result);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;

		final Button referenceButton = new Button(result, SWT.CHECK);
		referenceButton.setText("&Update references");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.verticalIndent = 2;
		referenceButton.setLayoutData(data);

		nameField.setText(refactoring.getBeanId());

		nameField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent event) {
				handleInputChanged();
			}
		});

		referenceButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				((RefactoringWizard) getWizard())
						.setForcePreviewReview(referenceButton.getSelection());
				refactoring.setUpdateReferences(referenceButton.getSelection());
			}
		});

		referenceButton.setSelection(true);
		((RefactoringWizard) getWizard()).setForcePreviewReview(true);
		nameField.setFocus();
		nameField.selectAll();
		handleInputChanged();
	}

	private Text createNameField(Composite result) {
		Text field = new Text(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return field;
	}

	private RenameIdRefactoring getRenameIdRefactoring() {
		return (RenameIdRefactoring) getRefactoring();
	}

	void handleInputChanged() {
		RefactoringStatus status = new RefactoringStatus();
		RenameIdRefactoring refactoring = getRenameIdRefactoring();
		status.merge(refactoring.setBeanId(nameField.getText()));

		setPageComplete(!status.hasError());
		int severity = status.getSeverity();
		String message = status.getMessageMatchingSeverity(severity);
		if (severity >= RefactoringStatus.INFO) {
			setMessage(message, severity);
		}
		else {
			setMessage("", NONE); //$NON-NLS-1$
		}
	}
}
