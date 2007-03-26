/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.actions;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;
import org.springframework.ide.eclipse.beans.ui.editor.IPreferencesConstants;
import org.springframework.ide.eclipse.beans.ui.editor.outline.OutlineSorter;

public class OutlineStyleAction extends Action {

	private static final String PREFIX = "Outline.SpringStyle.";

	private TreeViewer viewer;

	public OutlineStyleAction(TreeViewer viewer) {
		super(Activator.getResourceString(PREFIX + "label"), AS_CHECK_BOX);
		this.viewer = viewer;
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		boolean checked = prefs
				.getBoolean(IPreferencesConstants.OUTLINE_SPRING);
		update(checked, false);
		setImageDescriptor(BeansUIImages.DESC_OBJS_SPRING);
	}

	@Override
	public void run() {
		update(isChecked(), true);
	}

	public void update(boolean value, boolean doStore) {
		setChecked(value);
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		boolean sort = prefs.getBoolean(IPreferencesConstants.OUTLINE_SORT);
		if (sort) {
			viewer.setSorter(value ? new OutlineSorter() : null);
		}
		setToolTipText(value ? Activator.getResourceString(PREFIX
				+ "tooltip.checked") : Activator.getResourceString(PREFIX
				+ "tooltip.unchecked"));
		setDescription(value ? Activator.getResourceString(PREFIX
				+ "description.checked") : Activator.getResourceString(PREFIX
				+ "description.unchecked"));
		if (doStore) {
			prefs.setValue(IPreferencesConstants.OUTLINE_SPRING, value);
			Activator.getDefault().savePluginPreferences();
			viewer.refresh();
		}
	}
}
