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

package org.springframework.ide.eclipse.web.flow.ui.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.ui.IPropertyListener;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;
import org.springframework.ide.eclipse.web.flow.ui.views.WebFlowView;

/**
 * The root node of an Spring view node tree.
 */
public class RootNode extends AbstractNode {

    public static final int PROJECTS = 1;

    private WebFlowView view;

    private Map projects;

    private ListenerList listeners;

    public RootNode(WebFlowView view) {
        super(null);
        this.view = view;
        projects = new HashMap();
        listeners = new ListenerList();
    }

    public void addPropertyListener(IPropertyListener listener) {
        listeners.add(listener);
    }

    public void removePropertyListener(IPropertyListener listener) {
        listeners.remove(listener);
    }

    public void propertyChanged(INode node, int propertyId) {
        Object[] array = listeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            IPropertyListener listener = (IPropertyListener) array[i];
            listener.propertyChanged(node, propertyId);
        }
    }

    public void reloadConfigs() {
        this.projects.clear();
        Iterator projects = WebFlowCorePlugin.getModel().getProjects()
                .iterator();
        while (projects.hasNext()) {
            IWebFlowProject project = (IWebFlowProject) projects.next();
            addProject(project.getElementName(), project.getConfigNames(),
                    project.getConfigSets());
        }
    }

    public ProjectNode getProject(IFile file) {
        return getProject(file.getProject().getName());
    }

    public ProjectNode getProject(String name) {
        return (projects.containsKey(name) ? (ProjectNode) projects.get(name)
                : null);
    }

    /**
     * Returns the list of projects stored in this root node
     * 
     * @return ProjectNode[] the projects in this node
     */
    public ProjectNode[] getProjects() {
        return (ProjectNode[]) projects.values().toArray(
                new ProjectNode[projects.size()]);
    }

    /**
     * Returns whether this root node contains any projects.
     * 
     * @return whether there are any projects
     */
    public boolean hasProjects() {
        return !projects.isEmpty();
    }

    /**
     * Adds a new Spring project to this root node.
     */
    public void addProject(String project, Collection configs,
            Collection configSets) {
        ProjectNode node = getProject(project);
        if (node == null) {
            node = new ProjectNode(this, project);
            projects.put(project, node);
            propertyChanged(this, PROJECTS);
        }
        node.setConfigs(configs);
        node.setConfigSets(configSets);
    }

    public ConfigNode getConfig(IFile file) {
        ProjectNode project = getProject(file);
        if (project != null) {
            return project.getConfig(file);
        }
        return null;
    }

    /**
     * Removes given Spring bean config from this root node.
     */
    public void removeConfig(IFile config) {
        removeConfig(config.getProject().getName(), config
                .getProjectRelativePath().toString());
    }

    /**
     * Removes given Spring bean config from this root node.
     */
    public void removeConfig(String project, String config) {
        ProjectNode node = getProject(project);
        if (node != null) {
            node.removeConfig(config);
            if (!node.hasConfigs()) {
                projects.remove(project);
                propertyChanged(this, PROJECTS);
            }
        }
    }

    /**
     * Returns whether this root node contains given project.
     */
    public boolean containsProject(IProject project) {
        return projects.containsKey(project.getName());
    }

    /**
     * Removes given Spring Project from this root node.
     */
    public void removeProject(IProject project) {
        projects.remove(project.getName());
        propertyChanged(this, PROJECTS);
    }

    public void remove(INode node) {
        projects.remove(node);
    }

    public void refreshViewer() {
        view.refresh();
    }

    public String toString() {
        StringBuffer text = new StringBuffer();
        text.append(getName());
        text.append(": projects=");
        text.append(projects);
        return text.toString();
    }
}
