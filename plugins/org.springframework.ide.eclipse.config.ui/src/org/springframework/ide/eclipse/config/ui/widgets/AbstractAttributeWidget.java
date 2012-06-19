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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * The base class for representing an attribute as a row of 2 or more widgets,
 * starting with a label and followed by the primary control.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
public abstract class AbstractAttributeWidget {

	/**
	 * The composite parenting the widget set.
	 */
	protected Composite client;

	/**
	 * The toolkit from the parent form, used to create new widgets.
	 */
	protected FormToolkit toolkit;

	/**
	 * The name of the attribute displayed by the widget set.
	 */
	protected String attr;

	/**
	 * Whether this represents a required attribute.
	 */
	protected boolean required;

	/**
	 * Constructs a row of widgets to display and perform actions upon an XML
	 * attribute.
	 * 
	 * @param client the parent composite
	 * @param toolkit the toolkit from the parent form
	 * @param attrName the name of the attribute displayed by the widget set
	 */
	public AbstractAttributeWidget(Composite client, FormToolkit toolkit, String attrName) {
		this(client, toolkit, attrName, false);
	}

	/**
	 * Constructs a row of widgets to display and perform actions upon an XML
	 * attribute.
	 * 
	 * @param client the parent composite
	 * @param toolkit the toolkit from the parent form
	 * @param attrName the name of the attribute displayed by the widget set
	 * @param required denotes whether this is a required field
	 */
	public AbstractAttributeWidget(Composite client, FormToolkit toolkit, String attrName, boolean required) {
		this.client = client;
		this.toolkit = toolkit;
		this.attr = attrName;
		this.required = required;
		createLabel();
	}

	/**
	 * Creates and positions the widgets that will display and perform actions
	 * on the underlying XML attribute. Creates the primary control with a
	 * column span of 1.
	 */
	public void createAttribute() {
		createAttribute(1);
	}

	/**
	 * Creates and positions the widgets that will display and perform actions
	 * on the underlying XML attribute.
	 * 
	 * @param span the number of columns spanned by the primary control
	 */
	public abstract void createAttribute(int span);

	/**
	 * Creates a label prefixing the widget set. Typically the label will
	 * display the attribute name followed by a colon, but clients may override
	 * if necessary.
	 */
	protected void createLabel() {
		Label label = null;
		if (required) {
			label = toolkit.createLabel(client, attr + "*:"); //$NON-NLS-1$
		}
		else {
			label = toolkit.createLabel(client, attr + ":"); //$NON-NLS-1$
		}
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
	}

	/**
	 * Returns the name of the attribute displayed by the widget set.
	 * 
	 * @return name of the attribute displayed by the widget set
	 */
	public String getAttributeName() {
		return attr;
	}

	/**
	 * Updates the XML model with the content of the widget set. This method is
	 * called automatically when the contents of the primary control change.
	 */
	public abstract void modifyAttribute();

	/**
	 * Updates the widget set with the content from the XML model.
	 */
	public abstract void update();

}
