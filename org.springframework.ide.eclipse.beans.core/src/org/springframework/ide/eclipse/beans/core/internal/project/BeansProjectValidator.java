/*
 * Copyright 2002-2006 the original author or authors.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigValidator;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

/**
 * @author Torsten Juergeleit
 */
public class BeansProjectValidator implements IProjectBuilder {

	public void build(IFile file, IProgressMonitor monitor) {
		if (BeansCoreUtils.isBeansConfig(file)) {
			monitor.beginTask(BeansCorePlugin.getFormattedMessage(
					"BeansProjectValidator.validateFile", file.getFullPath()
							.toString()), IProgressMonitor.UNKNOWN);
			// Delete all problem markers created by Spring IDE
			SpringCoreUtils.deleteProblemMarkers(file);
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			// Validate the modified config file
			IBeansProject project = BeansCorePlugin.getModel().getProject(
					file.getProject());
			BeansConfig config = (BeansConfig) project.getConfig(file);

			BeansConfigValidator validator = new BeansConfigValidator();
			validator.validate(config, monitor);
			monitor.done();
		}
	}
}
