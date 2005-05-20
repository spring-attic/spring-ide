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

package org.springframework.ide.eclipse.web.flow.core.model;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * This interface holds information for a Spring Web Flows project.
 */
public interface IWebFlowProject extends IWebFlowModelElement {

    /** File name of the Spring Beans project description */
    public static final String DESCRIPTION_FILE = ".springWebFlow";

    /**
     * Returns corresponding Eclipse project.
     */
    IProject getProject();

    /**
     * Retruns a list of config names.
     */
    Collection getConfigNames();

    /**
     * Returns true if given file belongs to the list of Spring Web Flow config
     * files which are stored in the project description.
     */
    boolean hasConfig(IFile file);

    /**
     * Returns true if given config name belongs to the list of Spring Web Flow
     * config files which are stored in the project description.
     */
    boolean hasConfig(String configName);

    /**
     * Returns <code>IWebFlowConfig</code> for given config file.
     */
    IWebFlowConfig getConfig(IFile configFile);

    /**
     * Returns <code>IWebFlowConfig</code> of given name.
     */
    IWebFlowConfig getConfig(String configName);

    /**
     * Returns a collection of all <code>IWebFlowConfig</code> s defined in this
     * project.
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig
     */
    Collection getConfigs();

    /**
     * Returns a list of <code>IWebFlowConfigSet</code> instances.
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet
     */
    Collection getConfigSets();

    /**
     * Returns true if a config set with the given name is defined within this
     * project.
     * 
     * @param name
     * @return
     */
    boolean hasConfigSet(String configSetName);
}