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
package org.springframework.ide.eclipse.config.ui.editors;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditor;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class SpringConfigGraphPage extends GraphEditor {

	private final AbstractConfigEditor editor;

	private ManagedForm mform;

	private ScrolledForm sform;

	public SpringConfigGraphPage(AbstractConfigEditor editor) {
		super();
		this.editor = editor;
	}

	@Override
	public void createPartControl(Composite parent) {
		if (mform == null) {
			mform = new ManagedForm(parent);
		}

		sform = mform.getForm();
		sform.getForm().setSeparatorVisible(true);
		sform.setText(Messages.getString("SpringConfigGraphPage.FORM_TITLE")); //$NON-NLS-1$
		updateHeader();

		FormToolkit toolkit = mform.getToolkit();
		toolkit.decorateFormHeading(sform.getForm());
		Composite body = sform.getBody();
		body.setLayout(new FillLayout());

		super.createPartControl(body);
	}

	@Override
	public void dispose() {
		if (mform != null) {
			mform.dispose();
		}
		super.dispose();
	}

	public AbstractConfigEditor getConfigEditor() {
		return editor;
	}

	protected void updateHeader() {
		if (sform != null && !sform.isDisposed() && editor != null) {
			mform.reflow(true);
			if (editor.isDirty()) {
				sform.setMessage(Messages.getString("SpringConfigGraphPage.DIRTY_INFO"), IMessageProvider.INFORMATION); //$NON-NLS-1$
			}
			else {
				sform.setMessage(null, IMessageProvider.NONE);
			}
		}
	}

}
