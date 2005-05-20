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

package org.springframework.ide.eclipse.web.flow.core.internal.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.web.flow.core.IWebFlowProjectMarker;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCoreUtils;
import org.springframework.ide.eclipse.web.flow.core.WebFlowDefinitionException;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelUtils;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;

public class WebFlowProjectValidator implements IProjectBuilder {

    public void build(IFile file, IProgressMonitor monitor) {
        if (WebFlowCoreUtils.isWebFlowConfig(file)) {
            monitor.beginTask(WebFlowCorePlugin.getFormattedMessage(
                    "WebFlowProjectValidator.validateFile", file.getFullPath()
                            .toString()), IProgressMonitor.UNKNOWN);
            // Delete all problem markers created by Spring IDE
            WebFlowCoreUtils.deleteProblemMarkers(file);
            monitor.worked(1);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            // Reset the corresponding config within the bean model to force
            // re-reading the config file and updating the model
            IWebFlowProject project = WebFlowCorePlugin.getModel().getProject(
                    file.getProject());
            WebFlowConfig config = (WebFlowConfig) project.getConfig(file);
            config.reset();

            // At first check if model was able to parse the config file
            WebFlowDefinitionException e = config.getException();
            if (e != null) {
                WebFlowModelUtils.createProblemMarker(config, e.getMessage(),
                        IMarker.SEVERITY_ERROR, e.getLineNumber(),
                        IWebFlowProjectMarker.ERROR_CODE_PARSING_FAILED);
            }
            else {
                monitor.worked(1);
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                // Now validate the modified config file
                //WebFlowConfigValidator validator = new WebFlowConfigValidator();
                //validator.validate(config, monitor);
            }
            monitor.done();
        }
    }
}