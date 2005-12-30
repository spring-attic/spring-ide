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
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorPlugin;
import org.springframework.ide.eclipse.beans.ui.editor.IPreferencesConstants;

public class OutlineStyleAction extends Action {

	private static final String PREFIX = "Outline.SpringStyle.";

	private TreeViewer viewer;

	public OutlineStyleAction(TreeViewer viewer) {
		super(BeansEditorPlugin.getResourceString(PREFIX + "label"), AS_CHECK_BOX);
		this.viewer = viewer;
		Preferences prefs = BeansEditorPlugin.getDefault().getPluginPreferences();
		boolean checked = prefs.getBoolean(IPreferencesConstants.OUTLINE_SPRING);
		valueChanged(checked, false);
	}

	public void run() {
      valueChanged(isChecked(), true);
    }

    private void valueChanged(boolean value, boolean doStore) {
        setChecked(value);
        setToolTipText(value ?
			BeansEditorPlugin.getResourceString(PREFIX + "tooltip.checked") :
			BeansEditorPlugin.getResourceString(PREFIX + "tooltip.unchecked"));
        setDescription(value ?
			BeansEditorPlugin.getResourceString(PREFIX + "description.checked") :
			BeansEditorPlugin.getResourceString(PREFIX + "description.unchecked"));
        if (doStore) {
	        Preferences prefs = BeansEditorPlugin.getDefault().getPluginPreferences();
	        prefs.setValue(IPreferencesConstants.OUTLINE_SPRING, value);
	        BeansEditorPlugin.getDefault().savePluginPreferences();
	        viewer.refresh();
        }
    }
}
