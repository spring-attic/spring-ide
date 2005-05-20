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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;

public class WebFlowConfigValidator {

    public static final String DEBUG_OPTION = WebFlowCorePlugin.PLUGIN_ID
            + "/model/validator/debug";

    public static boolean DEBUG = WebFlowCorePlugin.isDebug(DEBUG_OPTION);

    private IProgressMonitor monitor;

    public void validate(IWebFlowConfig config, IProgressMonitor monitor) {
        this.monitor = monitor;

        // Validate the config file within all defined config sets
        boolean isValidated = false;
        Iterator configSets = ((IWebFlowProject) config.getElementParent())
                .getConfigSets().iterator();
        while (configSets.hasNext()) {
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
        }

        // If not already validated then validate config file now
        if (!isValidated) {

        }
        monitor.worked(1);
    }

}