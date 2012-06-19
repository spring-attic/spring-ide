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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.springframework.ide.eclipse.config.core.preferences.SpringConfigPreferenceConstants;
import org.springframework.ide.eclipse.config.graph.ConfigGraphPlugin;


/**
 * @author Leo Dos Santos
 */
public class ToggleLayoutAction extends Action implements IPropertyChangeListener {

	private String message;

	private final IPreferenceStore prefStore;

	private final String LAYOUT_PATH = "icons/layout.gif"; //$NON-NLS-1$

	private final String LAYOUT_DISABLED_PATH = "icons/layout_disabled.gif"; //$NON-NLS-1$

	public ToggleLayoutAction() {
		super(Messages.ToggleLayoutAction_ACTION_NAME, Action.AS_CHECK_BOX);
		ImageDescriptor imageDesc = ConfigGraphPlugin.imageDescriptorFromPlugin(ConfigGraphPlugin.PLUGIN_ID, LAYOUT_PATH);
		ImageDescriptor disabledDesc = ConfigGraphPlugin.imageDescriptorFromPlugin(ConfigGraphPlugin.PLUGIN_ID,
				LAYOUT_DISABLED_PATH);
		setImageDescriptor(imageDesc);
		setDisabledImageDescriptor(disabledDesc);
		prefStore = ConfigGraphPlugin.getDefault().getPreferenceStore();
		prefStore.addPropertyChangeListener(this);
		setState();
	}

	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (SpringConfigPreferenceConstants.PREF_MANUAL_LAYOUT.equals(property)) {
			setState();
		}
	}

	@Override
	public void run() {
		prefStore.setValue(SpringConfigPreferenceConstants.PREF_MANUAL_LAYOUT, isChecked());
	}

	private void setState() {
		boolean state = prefStore.getBoolean(SpringConfigPreferenceConstants.PREF_MANUAL_LAYOUT);
		if (state) {
			message = Messages.ToggleLayoutAction_TOOLTIP_ENABLE_AUTO_LAYOUT;
		}
		else {
			message = Messages.ToggleLayoutAction_TOOLTIP_ENABLE_MANUAL_LAYOUT;
		}
		setToolTipText(message);
		setChecked(state);
	}

}
