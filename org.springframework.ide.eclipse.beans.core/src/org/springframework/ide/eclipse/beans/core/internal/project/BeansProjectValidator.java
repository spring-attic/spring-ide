/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.core.internal.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.beans.core.BeanDefinitionException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.validator.BeansConfigValidator;
import org.springframework.ide.eclipse.beans.core.internal.model.validator.BeansValidatorUtil;
import org.springframework.ide.eclipse.beans.core.internal.model.validator.IBeansConfigValidator;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

public class BeansProjectValidator implements IProjectBuilder {

	public void build(IFile file, IProgressMonitor monitor) {
		if (BeansCoreUtils.isBeansConfig(file)) {
			monitor.beginTask(BeansCorePlugin.getFormattedMessage(
					  "BeansProjectValidator.validateFile",
					  file.getFullPath().toString()), IProgressMonitor.UNKNOWN);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			// Delete all problem markers created by Spring IDE
			BeansCoreUtils.deleteProblemMarkers(file);

			// Reset the corresponding config within the bean model to force
			// re-reading the config file and updating the model
			IBeansProject project = BeansCorePlugin.getModel().getProject(
															 file.getProject());
			BeansConfig config = (BeansConfig) project.getConfig(file);
			config.reset();

			// At first check if model was able to parse the config file 
			BeanDefinitionException e = config.getException();
			if (e != null) {
				BeansValidatorUtil.createProblemMarker(config, e.getMessage(),
								 IMarker.SEVERITY_ERROR, e.getLineNumber(),
								 IBeansProjectMarker.ERROR_CODE_PARSING_FAILED);
			} else {
				// TODO implement extension point and maintain list of validators
				
				// Now validate the modified config file
				IBeansConfigValidator validator = new BeansConfigValidator();
				validator.validate(config, monitor);
			}
			monitor.done();
		}
	}
}
