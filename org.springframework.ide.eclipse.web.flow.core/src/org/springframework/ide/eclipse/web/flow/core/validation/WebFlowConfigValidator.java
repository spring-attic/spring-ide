/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.core.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;

public class WebFlowConfigValidator {

    public static final String VALIDATORS_EXTENSION_POINT = WebFlowCorePlugin.PLUGIN_ID
            + ".validators";

    public static final String DEBUG_OPTION = WebFlowCorePlugin.PLUGIN_ID
            + "/model/validator/debug";

    public static boolean DEBUG = WebFlowCorePlugin.isDebug(DEBUG_OPTION);

    private IProgressMonitor monitor;

    private List validators;

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

            IWebFlowConfigSet configSet = (IWebFlowConfigSet) configSets.next();
            if (configSet.hasConfig(config.getElementName())) {
                this.validateInternal(config, configSet, monitor);
                isValidated = true;
            }
        }

        // If not already validated then validate config file now
        if (!isValidated) {
            this.validateInternal(config, null, monitor);
        }
        monitor.worked(1);
    }

    private void validateInternal(IWebFlowConfig config,
            IWebFlowConfigSet configSet, IProgressMonitor monitor) {
        // Fill the list of the members of the <i>validators</i> extension-point
        validators = new ArrayList();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtension[] extensions = registry.getExtensionPoint(
                VALIDATORS_EXTENSION_POINT).getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            IExtension extension = extensions[i];
            IConfigurationElement[] elements = extension
                    .getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                try {
                    IConfigurationElement element = elements[j];

                    Object validator = element
                            .createExecutableExtension("class");
                    if (validator instanceof IWebFlowConfigValidator) {
                        validators.add(validator);
                    }
                } catch (CoreException e) {
                    SpringCore.log(e);
                }
            }
        }

        for (int i = 0; i < this.validators.size(); i++) {
            ((IWebFlowConfigValidator) this.validators.get(i)).validate(config,
                    configSet, monitor);
        }
    }
}