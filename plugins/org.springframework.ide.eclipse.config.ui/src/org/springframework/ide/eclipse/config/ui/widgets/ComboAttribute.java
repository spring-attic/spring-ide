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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * An {@link AbstractAttributeWidget} that displays an attribute as a plain text
 * label, followed by a combo box.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
public abstract class ComboAttribute extends AbstractAttributeWidget {

	private final String[] values;

	/**
	 * A combo box displaying the possible values for the attribute.
	 */
	protected Combo combo;

	/**
	 * Constructs a row of widgets to display and perform actions upon an XML
	 * attribute. The primary control of the widget set is a combo box.
	 * 
	 * @param client the parent composite
	 * @param toolkit the toolkit of the parent form
	 * @param attrName the name of the attribute displayed by the widget set
	 * @param values the set of values displayed by the combo box
	 */
	public ComboAttribute(Composite client, FormToolkit toolkit, String attrName, String[] values) {
		this(client, toolkit, attrName, values, false);
	}

	/**
	 * Constructs a row of widgets to display and perform actions upon an XML
	 * attribute. The primary control of the widget set is a combo box.
	 * 
	 * @param client the parent composite
	 * @param toolkit the toolkit of the parent form
	 * @param attrName the name of the attribute displayed by the widget set
	 * @param values the set of values displayed by the combo box
	 * @param required denotes whether this is a required field
	 */
	public ComboAttribute(Composite client, FormToolkit toolkit, String attrName, String[] values, boolean required) {
		super(client, toolkit, attrName, required);
		this.values = values;
	}

	@Override
	public void createAttribute(int span) {
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = span;

		combo = new Combo(client, SWT.READ_ONLY);
		if (values != null) {
			combo.setItems(values);
		}
		combo.setLayoutData(data);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modifyAttribute();
			}
		});
	}

	/**
	 * Returns the primary control of the widget set, a combo box displaying the
	 * possible values for the attribute.
	 * 
	 * @return combo box displaying the possible values for the attribute
	 */
	public Combo getComboControl() {
		return combo;
	}

}
