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
package org.springframework.ide.eclipse.core.ui.dialogs.input;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.springframework.ide.eclipse.core.ui.fields.StringDialogField;

/**
 * A simple input dialog with one field.
 * @author Pierre-Antoine Grégoire
 *
 */
public class SimpleInputDialog extends AbstractInputDialog {
	private String result;
	public String getResult(){
		return result;
	}
	private StringDialogField stringDialogField;
	
	public SimpleInputDialog(Shell parentShell, String dialogTitle, String dialogSubTitle, Image dialogTitleImage, String dialogMessage) {
		super(parentShell, dialogTitle, dialogSubTitle, dialogTitleImage, dialogMessage);
	}

	protected void createInputPart(Composite composite) {
		Composite container=getFormToolkit().createComposite(composite,SWT.WRAP);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		data.grabExcessHorizontalSpace=true;
		container.setLayoutData(data);
		container.setLayout(new TableWrapLayout());
		stringDialogField=new StringDialogField(getFormToolkit());
		stringDialogField.setLabelText(this.message);
		stringDialogField.doFillIntoTable(container,2);
		stringDialogField.getTextControl(null).addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				result=stringDialogField.getText();
			}
		
		});
	}

}
