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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.web.flow.ui.WebFlowUIPlugin;
import org.springframework.ide.eclipse.web.flow.ui.model.ConfigNode;
import org.springframework.ide.eclipse.web.flow.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.web.flow.ui.model.INode;
import org.springframework.ide.eclipse.web.flow.ui.model.ProjectNode;
import org.springframework.ide.eclipse.web.flow.ui.properties.ConfigurationPropertyPage;
import org.springframework.ide.eclipse.web.flow.ui.views.WebFlowView;

public class OpenPropertiesAction extends Action {

    private static final String PREFIX = "View.OpenPropertiesAction.";

    private static final String TITLE = "PropertiesPage.title";

    private WebFlowView view;

    public OpenPropertiesAction(WebFlowView view) {
        super(WebFlowUIPlugin.getResourceString(PREFIX + "label"));
        setToolTipText(WebFlowUIPlugin.getResourceString(PREFIX + "tooltip"));
        this.view = view;
    }

    public void run() {
        IProject project = null;
        int block = 0;
        INode node = (INode) ((IStructuredSelection) view.getViewer()
                .getSelection()).getFirstElement();
        if (node instanceof ProjectNode) {
            project = ((ProjectNode) node).getProject();
        }
        else if (node instanceof ConfigNode) {
            project = ((ConfigNode) node).getProjectNode().getProject();
        }
        else if (node instanceof ConfigSetNode) {
            project = ((ConfigSetNode) node).getProjectNode().getProject();
            block = 1;
        }

        // Show project's property page
        if (project != null) {
            String title = WebFlowUIPlugin.getResourceString(TITLE)
                    + project.getName();
            IPreferencePage page = new ConfigurationPropertyPage(project, block);
            SpringUIUtils.showPreferencePage(ConfigurationPropertyPage.ID,
                    page, title);
        }
    }
}
