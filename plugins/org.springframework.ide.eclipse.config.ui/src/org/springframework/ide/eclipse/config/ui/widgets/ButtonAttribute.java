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
package org.springframework.ide.eclipse.config.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * An {@link AbstractAttributeWidget} that displays an attribute as a
 * hyperlinked label, followed by a single line text field and then followed by
 * a flat button.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @see HyperlinkedTextAttribute
 * @since 2.0.0
 */
public abstract class ButtonAttribute extends HyperlinkedTextAttribute {

	private final String buttonLabel;

	/**
	 * A button situated at the end of the widget set.
	 */
	protected Button button;

	/**
	 * Constructs a row of widgets to display and perform actions upon an XML
	 * attribute. The primary control of the widget set is a single line text
	 * field. This constructor will create a button with the label "Browse..."
	 * 
	 * @param client the parent composite
	 * @param toolkit the toolkit from the parent form
	 * @param attrName the name of the attribute displayed by the widget set
	 */
	public ButtonAttribute(Composite client, FormToolkit toolkit, String attrName) {
		this(client, toolkit, attrName, false);
	}

	/**
	 * Constructs a row of widgets to display and perform actions upon an XML
	 * attribute. The primary control of the widget set is a single line text
	 * field. This constructor will create a button with the label "Browse..."
	 * 
	 * @param client the parent composite
	 * @param toolkit the toolkit from the parent form
	 * @param attrName the name of the attribute displayed by the widget set
	 * @param required denotes whether this is a required field
	 */
	public ButtonAttribute(Composite client, FormToolkit toolkit, String attrName, boolean required) {
		this(client, toolkit, attrName, Messages.getString("ButtonAttribute.BROWSE_BUTTON"), required); //$NON-NLS-1$
	}

	/**
	 * Constructs a row of widgets to display and perform actions upon an XML
	 * attribute. The primary control of the widget set is a single line text
	 * field.
	 * 
	 * @param client the parent composite
	 * @param toolkit the toolkit of the parent form
	 * @param attrName the name of the attribute displayed by the widget set
	 * @param buttonLabel the button label
	 */
	public ButtonAttribute(Composite client, FormToolkit toolkit, String attrName, String buttonLabel) {
		this(client, toolkit, attrName, buttonLabel, false);
	}

	/**
	 * Constructs a row of widgets to display and perform actions upon an XML
	 * attribute. The primary control of the widget set is a single line text
	 * field.
	 * 
	 * @param client the parent composite
	 * @param toolkit the toolkit of the parent form
	 * @param attrName the name of the attribute displayed by the widget set
	 * @param buttonLabel the button label
	 * @param required denotes whether this is a required field
	 */
	public ButtonAttribute(Composite client, FormToolkit toolkit, String attrName, String buttonLabel, boolean required) {
		super(client, toolkit, attrName, required);
		this.buttonLabel = buttonLabel;
	}

	/**
	 * Clients must implement this method to perform an appropriate action based
	 * on the content of the attribute. This method is called automatically when
	 * the button is clicked.
	 */
	public abstract void browse();

	@Override
	public void createAttribute(int span) {
		super.createAttribute(span);

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.widthHint = 100;

		button = toolkit.createButton(client, buttonLabel, SWT.FLAT);
		button.setLayoutData(data);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browse();
			}
		});
	}

	/**
	 * Returns the button situated at the end of the widget set.
	 * 
	 * @return button situated at the end of the widget set
	 */
	public Button getButtonControl() {
		return button;
	}

}
