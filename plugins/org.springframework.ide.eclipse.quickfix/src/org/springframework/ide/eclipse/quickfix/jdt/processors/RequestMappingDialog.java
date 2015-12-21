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
package org.springframework.ide.eclipse.quickfix.jdt.processors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Terry Denney
 */
public class RequestMappingDialog extends Dialog {

	private Method methodType;

	public RequestMappingDialog(Shell parentShell) {
		super(parentShell);
	}

	public Method getMethodType() {
		return methodType;
	}

	@Override
	protected Control createContents(Composite parent) {
		getShell().setText("Add RequestMapping Annotation");
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		Composite methodComposite = new Composite(composite, SWT.NONE);
		methodComposite.setLayout(new GridLayout(2, false));
		methodComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label methodLabel = new Label(methodComposite, SWT.NONE);
		methodLabel.setText("Method");

		GridData methodLabelData = new GridData(SWT.FILL, SWT.FILL, true, false);
		methodLabelData.horizontalSpan = 2;
		methodLabel.setLayoutData(methodLabelData);

		Button getButton = new Button(methodComposite, SWT.RADIO);
		getButton.setText("GET");
		getButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		getButton.setSelection(true);
		getButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				methodType = Method.GET;
			}
		});

		Button postButton = new Button(methodComposite, SWT.RADIO);
		postButton.setText("POST");
		postButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		postButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				methodType = Method.POST;
			}
		});

		methodType = Method.GET;

		super.createContents(composite);

		return composite;
	}

	public static enum Method {
		GET, POST
	}

}
