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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.PropertySheet;
import org.springframework.ide.eclipse.web.flow.ui.WebFlowUIImages;
import org.springframework.ide.eclipse.web.flow.ui.WebFlowUIPlugin;
import org.springframework.ide.eclipse.web.flow.ui.views.WebFlowView;

public class PropertySheetAction extends Action {

    private static final String PREFIX = "View.OpenPropertySheetAction.";

    private WebFlowView view;

    public PropertySheetAction(WebFlowView view) {
        this.view = view;
        setText(WebFlowUIPlugin.getResourceString(PREFIX + "label"));
        setToolTipText(WebFlowUIPlugin.getResourceString(PREFIX + "tooltip"));
        WebFlowUIImages.setLocalImageDescriptors(this, "properties.gif");
    }

    public void run() {
        try {
            IWorkbenchPage page = WebFlowUIPlugin.getActiveWorkbenchPage();
            PropertySheet propertySheet = (PropertySheet) page
                    .showView(IPageLayout.ID_PROP_SHEET);
            page.activate(view);
            Viewer viewer = view.getViewer();
            viewer.getControl().setFocus();
            propertySheet.selectionChanged(view, viewer.getSelection());
        }
        catch (PartInitException e) {
            WebFlowUIPlugin.log(e);
        }
    }
}
