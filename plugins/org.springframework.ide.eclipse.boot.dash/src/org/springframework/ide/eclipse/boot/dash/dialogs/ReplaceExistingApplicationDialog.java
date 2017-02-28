/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

public class ReplaceExistingApplicationDialog extends SelectionDialog {

	private final List<String> boundServices;
	private final String message;

	public ReplaceExistingApplicationDialog(Shell parent, String title, String message, List<String> boundServices) {
		super(parent);
		setTitle(title);
		this.message = message;
		this.boundServices = boundServices;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Label messageLabel = new Label(composite, SWT.WRAP);
		messageLabel.setText(message);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(messageLabel);

		if (boundServices != null && !boundServices.isEmpty()) {
			createViewer(composite, boundServices);
		}

		return composite;
	}

	protected void createViewer(Composite composite, List<String> boundServices) {

		Label viewerLabel = new Label(composite, SWT.WRAP);
		viewerLabel.setText("WARNING: Any existing service bindings will be deleted.");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(viewerLabel);

		viewerLabel = new Label(composite, SWT.WRAP);
		viewerLabel.setText("Existing service bindings:");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(viewerLabel);

		TableViewer viewer = new TableViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		viewer.getTable().setHeaderVisible(false);
		viewer.getTable().setLinesVisible(false);

		viewer.setContentProvider(new IStructuredContentProvider() {

			Object[] elements;

			public void dispose() {
				// ignore
			}

			public Object[] getElements(Object inputElement) {
				return elements;
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				this.elements = (Object[]) newInput;
			}
		});

		viewer.setInput(boundServices.toArray());

		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
	}
}
