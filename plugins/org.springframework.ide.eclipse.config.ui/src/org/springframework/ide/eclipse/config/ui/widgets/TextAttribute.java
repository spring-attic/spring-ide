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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * An {@link AbstractAttributeWidget} that displays an attribute as a plain text
 * label, followed by a single line text field.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
public abstract class TextAttribute extends AbstractAttributeWidget {

	/**
	 * A text field displaying the current value of the attribute.
	 */
	protected Text text;

	/**
	 * Constructs a row of widgets to display and perform actions upon an XML
	 * attribute. The primary control of the widget set is a single line text
	 * field.
	 * 
	 * @param client the parent composite
	 * @param toolkit the toolkit from the parent form
	 * @param attrName the name of the attribute displayed by the widget set
	 */
	public TextAttribute(Composite client, FormToolkit toolkit, String attrName) {
		this(client, toolkit, attrName, false);
	}

	/**
	 * Constructs a row of widgets to display and perform actions upon an XML
	 * attribute. The primary control of the widget set is a single line text
	 * field.
	 * 
	 * @param client the parent composite
	 * @param toolkit the toolkit from the parent form
	 * @param attrName the name of the attribute displayed by the widget set
	 * @param required denotes whether this is a required field
	 */
	public TextAttribute(Composite client, FormToolkit toolkit, String attrName, boolean required) {
		super(client, toolkit, attrName, required);
	}

	@Override
	public void createAttribute(int span) {
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = span;

		text = toolkit.createText(client, ""); //$NON-NLS-1$
		text.setLayoutData(data);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				modifyAttribute();
			}
		});
	}

	/**
	 * Returns the primary control of the widget set, a text field displaying
	 * the current value of the attribute.
	 * 
	 * @return text field displaying the current value of the attribute
	 */
	public Text getTextControl() {
		return text;
	}

}
