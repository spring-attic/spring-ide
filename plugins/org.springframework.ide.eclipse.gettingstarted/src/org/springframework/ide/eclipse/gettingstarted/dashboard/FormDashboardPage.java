/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.dashboard;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * An abstract class to help implement DashboardPage the contents of
 * which is a form.
 * 
 * @author Kris De Volder
 */
public abstract class FormDashboardPage extends ADashboardPage {

	private FormToolkit toolkit;
	private Form form;
	
	/**
	 * This method is final. Implement createFormContent instead.
	 */
	@Override
	final protected void createControl(Composite parent) {
		parent.setLayout(new FillLayout());
		toolkit = new FormToolkit(parent.getDisplay());
		form = createForm(parent);
		createFormContents(form);
	}

	protected Form createForm(Composite parent) {
		return toolkit.createForm(parent);
	}
	
	protected abstract void createFormContents(Form parent);

	@Override
	public void dispose() {
		super.dispose();
		if (toolkit!=null) {
			toolkit.dispose();
		}
	}
	
	/**
	 * @return The form with the contents of this page. May return null if 
	 * the contents has not yet been created.
	 */
	public Form getForm() {
		return form;
	}
	
	/**
	 * @return The FormToolkit that should be used to create the widgets on this 
	 * page. May return null if called before the toolkit has been created
	 * or after the page has been disposed.
	 */
	public FormToolkit getToolkit() {
		return toolkit;
	}
	
}
