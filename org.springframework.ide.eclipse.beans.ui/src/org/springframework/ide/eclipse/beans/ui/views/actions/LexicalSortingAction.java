/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.views.actions;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.IPreferencesConstants;
import org.springframework.ide.eclipse.beans.ui.views.model.ModelSorter;

public class LexicalSortingAction extends Action {

	private static final String PREFIX = "View.SortAction.";

	private TreeViewer viewer;

	public LexicalSortingAction(TreeViewer viewer) {
		this.viewer = viewer;
		setText(BeansUIPlugin.getResourceString(PREFIX + "label"));
		BeansUIImages.setLocalImageDescriptors(this, "alphab_sort_co.gif");
		Preferences prefs = BeansUIPlugin.getDefault().getPluginPreferences();
		boolean checked = prefs.getBoolean(IPreferencesConstants.VIEW_SORT);
		valueChanged(checked, false);
	}

    public void run() {
        valueChanged(isChecked(), true);
    }

    private void valueChanged(boolean value, boolean doStore) {
        setChecked(value);
        viewer.setSorter(value ? new ModelSorter(true) :
        						 new ModelSorter(false));
        setToolTipText(value ?
			BeansUIPlugin.getResourceString(PREFIX + "tooltip.checked") :
			BeansUIPlugin.getResourceString(PREFIX + "tooltip.unchecked"));
        setDescription(value ?
			BeansUIPlugin.getResourceString(PREFIX + "description.checked") :
			BeansUIPlugin.getResourceString(PREFIX + "description.unchecked"));
        if (doStore) {
	        Preferences prefs = BeansUIPlugin.getDefault().getPluginPreferences();
	        prefs.setValue(IPreferencesConstants.VIEW_SORT, value);
	        BeansUIPlugin.getDefault().savePluginPreferences();
        }
    }
}
