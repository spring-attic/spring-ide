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

package org.springframework.ide.eclipse.web.flow.ui.views.actions;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.springframework.ide.eclipse.web.flow.ui.WebFlowUIImages;
import org.springframework.ide.eclipse.web.flow.ui.WebFlowUIPlugin;
import org.springframework.ide.eclipse.web.flow.ui.model.ModelSorter;

public class LexicalSortingAction extends Action {

    private String SORT_PREFIX = WebFlowUIPlugin.PLUGIN_ID + ".";

    private String VIEW_SORT = PREFIX + "view.outline.sort";

    private static final String PREFIX = "View.SortAction.";

    private TreeViewer viewer;

    public LexicalSortingAction(TreeViewer viewer) {
        this.viewer = viewer;
        setText(WebFlowUIPlugin.getResourceString(PREFIX + "label"));
        WebFlowUIImages.setLocalImageDescriptors(this, "alphab_sort_co.gif");
        Preferences prefs = WebFlowUIPlugin.getDefault().getPluginPreferences();
        boolean checked = prefs.getBoolean(VIEW_SORT);
        valueChanged(checked, false);
    }

    public void run() {
        valueChanged(isChecked(), true);
    }

    private void valueChanged(boolean value, boolean doStore) {
        setChecked(value);
        viewer
                .setSorter(value ? new ModelSorter(true) : new ModelSorter(
                        false));
        setToolTipText(value ? WebFlowUIPlugin.getResourceString(PREFIX
                + "tooltip.checked") : WebFlowUIPlugin.getResourceString(PREFIX
                + "tooltip.unchecked"));
        setDescription(value ? WebFlowUIPlugin.getResourceString(PREFIX
                + "description.checked") : WebFlowUIPlugin
                .getResourceString(PREFIX + "description.unchecked"));
        if (doStore) {
            Preferences prefs = WebFlowUIPlugin.getDefault()
                    .getPluginPreferences();
            prefs.setValue(VIEW_SORT, value);
            WebFlowUIPlugin.getDefault().savePluginPreferences();
        }
    }
}
