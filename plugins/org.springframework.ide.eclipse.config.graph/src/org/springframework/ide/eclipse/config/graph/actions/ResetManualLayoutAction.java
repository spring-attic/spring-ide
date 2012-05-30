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
package org.springframework.ide.eclipse.config.graph.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.config.core.preferences.SpringConfigPreferenceConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.ConfigGraphPlugin;


/**
 * @author Leo Dos Santos
 */
public class ResetManualLayoutAction extends SelectionAction implements IPropertyChangeListener {

	public static String RESET_LAYOUT_ID = "ResetLayout"; //$NON-NLS-1$

	private final IPreferenceStore prefStore;

	public ResetManualLayoutAction(IWorkbenchPart part) {
		super(part);
		setId(RESET_LAYOUT_ID);
		setText(Messages.ResetManualLayoutAction_RESET_LAYOUT_ACTION_LABEL);
		prefStore = ConfigGraphPlugin.getDefault().getPreferenceStore();
		prefStore.addPropertyChangeListener(this);
	}

	@Override
	protected boolean calculateEnabled() {
		return prefStore.getBoolean(SpringConfigPreferenceConstants.PREF_MANUAL_LAYOUT);
	}

	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (SpringConfigPreferenceConstants.PREF_MANUAL_LAYOUT.equals(property)) {
			setEnabled(calculateEnabled());
		}
	}

	@Override
	public void run() {
		IWorkbenchPart editor = getWorkbenchPart();
		if (editor instanceof AbstractConfigGraphicalEditor) {
			AbstractConfigGraphicalEditor graph = (AbstractConfigGraphicalEditor) editor;
			String uri = graph.getNamespaceUri();
			prefStore.firePropertyChangeEvent(SpringConfigPreferenceConstants.PROP_RESET_LAYOUT, null, uri);
		}
	}

}
