/*
 * Copyright 2002-2007 the original author or authors.
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

public class RenameBeanIdRefactoringInputPage extends UserInputWizardPage {

	private Text nameField;

	public RenameBeanIdRefactoringInputPage(String name) {
		super(name);
	}

	public void createControl(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);

		setControl(result);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		result.setLayout(layout);

		Label label = new Label(result, SWT.NONE);
		label.setText("&Bean id:");

		nameField = createNameField(result);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;

		final Button referenceButton = new Button(result, SWT.CHECK);
		referenceButton.setText("&Update references");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.verticalIndent = 2;
		referenceButton.setLayoutData(data);

		final RenameBeanIdRefactoring refactoring = getRenameBeanIdRefactoring();
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

	private RenameBeanIdRefactoring getRenameBeanIdRefactoring() {
		return (RenameBeanIdRefactoring) getRefactoring();
	}

	void handleInputChanged() {
		RefactoringStatus status = new RefactoringStatus();
		RenameBeanIdRefactoring refactoring = getRenameBeanIdRefactoring();
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