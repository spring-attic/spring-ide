/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.actions;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.IPreferencesConstants;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;
import org.springframework.ide.eclipse.beans.ui.editor.outline.OutlineSorter;

public class LexicalSortingAction extends Action {

	private static final String PREFIX = "Outline.SortAction.";

	private TreeViewer viewer;

	public LexicalSortingAction(TreeViewer viewer) {
		this.viewer = viewer;
		setText(Activator.getResourceString(PREFIX + "label"));
		BeansUIImages.setLocalImageDescriptors(this, "alphab_sort_co.gif");
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		boolean checked = prefs.getBoolean(IPreferencesConstants.OUTLINE_SORT);
		update(checked, false);
	}

	@Override
	public void run() {
		update(isChecked(), true);
	}

	public void update(boolean value, boolean doStore) {
		setChecked(value);
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		boolean spring = prefs.getBoolean(IPreferencesConstants.OUTLINE_SPRING);
		if (spring) {
			viewer.setSorter(value ? new OutlineSorter() : null);
		}
		setToolTipText(value ? Activator.getResourceString(PREFIX
				+ "tooltip.checked") : Activator.getResourceString(PREFIX
				+ "tooltip.unchecked"));
		setDescription(value ? Activator.getResourceString(PREFIX
				+ "description.checked") : Activator.getResourceString(PREFIX
				+ "description.unchecked"));
		if (doStore) {
			prefs.setValue(IPreferencesConstants.OUTLINE_SORT, value);
			Activator.getDefault().savePluginPreferences();
			viewer.refresh(true);
		}
	}
}
