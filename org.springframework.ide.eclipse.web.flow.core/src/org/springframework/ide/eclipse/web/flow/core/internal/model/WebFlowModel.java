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

package org.springframework.ide.eclipse.web.flow.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.internal.model.resources.WebFlowResourceChangeListener;
import org.springframework.ide.eclipse.web.flow.core.internal.model.resources.IWebFlowResourceChangeEvents;
import org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModel;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelChangedListener;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;
import org.springframework.ide.eclipse.web.flow.core.model.WebFlowModelChangedEvent;
import org.springframework.ide.eclipse.web.flow.core.validation.WebFlowConfigValidator;

public class WebFlowModel extends WebFlowModelElement implements IWebFlowModel {

    public static final String DEBUG_OPTION = WebFlowCorePlugin.PLUGIN_ID
            + "/model/debug";

    public static boolean DEBUG = WebFlowCorePlugin.isDebug(DEBUG_OPTION);

    private Map projects;

    private List modelListeners;

    private IResourceChangeListener workspaceListener;

    public WebFlowModel() {
        super(null, "" /* model has empty name */);
        this.projects = new HashMap();
        this.modelListeners = new ArrayList();
        this.workspaceListener = new WebFlowResourceChangeListener(
                new ResourceChangeEventHandler());
    }

    public int getElementType() {
        return MODEL;
    }

    public IResource getElementResource() {
        return null;
    }

    public void startup() {
        if (DEBUG) {
            System.out.println("Web Flow Model startup");
        }
        initialize();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.addResourceChangeListener(workspaceListener,
                WebFlowResourceChangeListener.LISTENER_FLAGS);
    }

    public void shutdown() {
        if (DEBUG) {
            System.out.println("Web Flow Model shutdown");
        }
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.removeResourceChangeListener(workspaceListener);
        workspaceListener = null;
        projects.clear();
    }

    public boolean hasProject(IProject project) {
        if (project != null && project.isAccessible()
                && projects.containsKey(project)) {
            return true;
        }
        return false;
    }

    public IWebFlowProject getProject(IProject project) {
        if (hasProject(project)) {
            return (IWebFlowProject) projects.get(project);
        }
        return null;
    }

    public IWebFlowProject getProject(String name) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                name);
        return getProject(project);
    }

    /**
     * Returns a collection of all <code>IBeansProject</code> s defined in
     * this model.
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject
     */
    public Collection getProjects() {
        return projects.values();
    }

    public boolean hasConfig(IFile configFile) {
        if (configFile != null) {
            IWebFlowProject project = getProject(configFile.getProject());
            if (project != null) {
                return project.hasConfig(configFile);
            }
        }
        return false;
    }

    public IWebFlowConfig getConfig(IFile configFile) {
        if (configFile != null) {
            IWebFlowProject project = getProject(configFile.getProject());
            if (project != null) {
                return project.getConfig(configFile);
            }
        }
        return null;
    }

    public void addChangeListener(IWebFlowModelChangedListener listener) {
        modelListeners.add(listener);
    }

    public void removeChangeListener(IWebFlowModelChangedListener listener) {
        modelListeners.remove(listener);
    }

    private void initialize() {
        if (DEBUG) {
            System.out.println("Initializing model - loading all projects");
        }
        this.projects.clear();
        List projects = getBeansProjects();
        if (!projects.isEmpty()) {
            Iterator iter = projects.iterator();
            while (iter.hasNext()) {
                IProject project = (IProject) iter.next();
                this.projects.put(project, new WebFlowProject(project));
            }
        }
    }

    /**
     * Returns a list of all <code>IProject</code> s with the Beans project
     * nature.
     * 
     * @return list of all <code>IProject</code> s with the Beans project
     *         nature
     */
    private static List getBeansProjects() {
        List springProjects = new ArrayList();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
                .getProjects();
        for (int i = 0; i < projects.length; i++) {
            IProject project = projects[i];
            if (SpringCoreUtils.isSpringProject(project)) {
                springProjects.add(project);
            }
        }
        return springProjects;
    }

    private void notifyListeners(IWebFlowModelElement element, int type) {
        WebFlowModelChangedEvent event = new WebFlowModelChangedEvent(element,
                type);
        Iterator iter = modelListeners.iterator();
        while (iter.hasNext()) {
            IWebFlowModelChangedListener listener = (IWebFlowModelChangedListener) iter
                    .next();
            listener.elementChanged(event);
        }
    }

    public void accept(IModelElementVisitor visitor) {

        // Ask this model's projects
        Iterator iter = projects.values().iterator();
        while (iter.hasNext()) {
            IWebFlowModelElement element = (IWebFlowModelElement) iter.next();
            element.accept(visitor);
        }
    }

    /**
     * Internal resource change event handler.
     */
    private class ResourceChangeEventHandler implements
            IWebFlowResourceChangeEvents {

        public boolean isSpringProject(IProject project) {
            return hasProject(project);
        }

        public void springNatureAdded(IProject project) {
            if (DEBUG) {
                System.out.println("Spring beans nature added to project '"
                        + project.getName() + "'");
            }
            WebFlowProject proj = new WebFlowProject(project);
            projects.put(project, proj);
            notifyListeners(proj, WebFlowModelChangedEvent.ADDED);
        }

        public void springNatureRemoved(IProject project) {
            if (DEBUG) {
                System.out.println("Spring beans nature removed from "
                        + "project '" + project.getName() + "'");
            }
            IWebFlowProject proj = (IWebFlowProject) projects.remove(project);
            notifyListeners(proj, WebFlowModelChangedEvent.REMOVED);
        }

        public void projectAdded(IProject project) {
            if (DEBUG) {
                System.out.println("Project '" + project.getName() + "' added");
            }
            WebFlowProject proj = new WebFlowProject(project);
            projects.put(project, proj);
            notifyListeners(proj, WebFlowModelChangedEvent.ADDED);
        }

        public void projectOpened(IProject project) {
            if (DEBUG) {
                System.out
                        .println("Project '" + project.getName() + "' opened");
            }
            WebFlowProject proj = new WebFlowProject(project);
            projects.put(project, proj);
            notifyListeners(proj, WebFlowModelChangedEvent.ADDED);
        }

        public void projectClosed(IProject project) {
            if (DEBUG) {
                System.out
                        .println("Project '" + project.getName() + "' closed");
            }
            IWebFlowProject proj = (IWebFlowProject) projects.remove(project);
            notifyListeners(proj, WebFlowModelChangedEvent.REMOVED);
        }

        public void projectDeleted(IProject project) {
            if (DEBUG) {
                System.out.println("Project '" + project.getName()
                        + "' deleted");
            }
            IWebFlowProject proj = (IWebFlowProject) projects.remove(project);
            notifyListeners(proj, WebFlowModelChangedEvent.REMOVED);
        }

        public void projectDescriptionChanged(IFile file) {
            if (DEBUG) {
                System.out.println("Project description '" + file.getFullPath()
                        + "' changed");
            }
            WebFlowProject project = (WebFlowProject) projects.get(file
                    .getProject());
            project.reset();
            notifyListeners(project, WebFlowModelChangedEvent.CHANGED);
        }

        public void configAdded(IFile file) {
            if (DEBUG) {
                System.out.println("Config '" + file.getFullPath() + "' added");
            }
            WebFlowProject project = (WebFlowProject) projects.get(file
                    .getProject());
            project.addConfig(file);
            IWebFlowConfig config = project.getConfig(file);
            notifyListeners(config, WebFlowModelChangedEvent.ADDED);
        }

        public void configChanged(IFile file) {
            if (DEBUG) {
                System.out.println("Config '" + file.getFullPath()
                        + "' changed");
            }
            IWebFlowProject project = (IWebFlowProject) projects.get(file
                    .getProject());
            WebFlowConfig config = (WebFlowConfig) project.getConfig(file);

            // There is no need to reset this config again if it's already done
            // by the BeansConfigValidator
            if (!config.isReset()) {
                config.reset();
            }
            notifyListeners(config, WebFlowModelChangedEvent.CHANGED);
        }

        public void configRemoved(IFile file) {
            if (DEBUG) {
                System.out.println("Config '" + file.getFullPath()
                        + "' removed");
            }
            WebFlowProject project = (WebFlowProject) projects.get(file
                    .getProject());
            WebFlowConfig config = (WebFlowConfig) project.getConfig(file);
            project.removeConfig(file);
            notifyListeners(config, WebFlowModelChangedEvent.REMOVED);
        }

        public void beanClassChanged(String className, Collection configs) {
            if (DEBUG) {
                System.out.println("Bean class '" + className + "' changed");
            }
            WebFlowConfigValidator validator = new WebFlowConfigValidator();
            Iterator iter = configs.iterator();
            while (iter.hasNext()) {
                IWebFlowConfig config = (IWebFlowConfig) iter.next();
                validator.validate(config, new NullProgressMonitor());
            }
        }
    }
}