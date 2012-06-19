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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.config.core.preferences.SpringConfigPreferenceConstants;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;


/**
 * @author Leo Dos Santos
 */
public class ToggleEndStateAction extends SelectionAction implements IPropertyChangeListener {

	public static String ID_ACTION = SpringConfigPreferenceConstants.PREF_DISPLAY_END_STATE;

	private final IPreferenceStore prefStore;

	public ToggleEndStateAction(IWorkbenchPart part) {
		super(part);
		setId(ID_ACTION);
		setText(Messages.ToggleEndStateAction_ACTION_LABEL);
		prefStore = ConfigUiPlugin.getDefault().getPreferenceStore();
		prefStore.addPropertyChangeListener(this);
		setState();
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}

	@Override
	public void dispose() {
		prefStore.removePropertyChangeListener(this);
		super.dispose();
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (ID_ACTION.equals(event.getProperty())) {
			setState();
		}
	}

	@Override
	public void run() {
		prefStore.setValue(ID_ACTION, isChecked());
	}

	private void setState() {
		setChecked(prefStore.getBoolean(ID_ACTION));
	}

}
