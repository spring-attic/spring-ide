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

import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.web.flow.core.WebFlowDefinitionException;
import org.springframework.ide.eclipse.web.flow.core.internal.parser.WebFlowDefinitionReader;
import org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

/**
 * This class defines a Spring web flow configuration.
 */
public class WebFlowConfig extends WebFlowModelElement implements
        IWebFlowConfig {

    /** Exception which occured during reading the web flow config file */
    private WebFlowDefinitionException exception;

    /** This bean's config file */
    private IFile file;

    /** List of bean names, in registration order */
    private IWebFlowState rootState;

    private IWebFlowConfigSet configSet;

    public WebFlowConfig(IWebFlowProject project, String name) {
        super(project, name);
        file = getFile(name);
        if (file == null) {
            exception = new WebFlowDefinitionException("File not found");
        }
    }

    public IFile getConfigFile() {
        return file;
    }

    public String getConfigPath() {
        return (file != null ? file.getFullPath().toString() : null);
    }

    public IResource getElementResource() {
        return file;
    }

    public int getElementType() {
        return CONFIG;
    }

    public WebFlowDefinitionException getException() {
        if (rootState == null) {

            // Lazily initialization of beans list
            readConfig();
        }
        return exception;
    }

    public IState getRootState(String name) {
        if (rootState == null) {

            // Lazily initialization of beans list
            readConfig();
        }
        return rootState;
    }

    public boolean isReset() {
        return (rootState == null);
    }

    /**
     * Sets internal list of <code>IBean</code> s to <code>null</code>. Any
     * further access to the data of this instance of <code>IBeansConfig</code>
     * leads to reloading of this beans config file.
     */
    public void reset() {
        this.rootState = null;
        this.exception = null;

        // Reset all config sets which contain this config
        IWebFlowProject project = (IWebFlowProject) getElementParent();
        Iterator configSets = project.getConfigSets().iterator();
        while (configSets.hasNext()) {
            WebFlowConfigSet configSet = (WebFlowConfigSet) configSets.next();
            if (configSet.hasConfig(getElementName())) {
                configSet.reset();
            }
        }
    }

    public String toString() {
        return getElementName();
    }

    /**
     * Returns the file for given name. If the given name defines an external
     * resource (leading '/' -> not part of the project this config belongs to)
     * get the file from the workspace else from the project.
     * 
     * @return the file for given name
     */
    private IFile getFile(String name) {
        IContainer container;
        if (name.charAt(0) == '/') {
            container = ResourcesPlugin.getWorkspace().getRoot();
        }
        else {
            container = (IProject) getElementParent().getElementResource();
        }
        return (IFile) container.findMember(name);
    }

    private void readConfig() {
        try {
            WebFlowDefinitionReader reader = new WebFlowDefinitionReader();
            reader.loadWebFlowDefinitions(this, new FileResource(file));
        }
        catch (WebFlowDefinitionException e) {
            exception = e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig#getState()
     */
    public IWebFlowState getState(boolean reload) {
        if (this.rootState == null || reload) {
            readConfig();
        }
        return this.rootState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig#getState()
     */
    public IWebFlowState getState() {
        return this.getState(false);
    }

    public void setState(IWebFlowState state) {
        this.rootState = state;
    }

    public void accept(IModelElementVisitor visitor) {

        // First visit this project
        if (visitor.visit(this)) {

            // Now ask this configs's beans
            IWebFlowModelElement element = (IWebFlowModelElement) this.rootState;
            element.accept(visitor);
        }
    }
}