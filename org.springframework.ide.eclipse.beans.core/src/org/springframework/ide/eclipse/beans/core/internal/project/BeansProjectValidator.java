/*
 * Copyright 2002-2007 the original author or authors.
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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigValidator;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

/**
 * @author Torsten Juergeleit
 */
public class BeansProjectValidator implements IProjectBuilder {

	public void build(IFile file, IProgressMonitor monitor) {
		if (BeansCoreUtils.isBeansConfig(file)) {
			validate(file, monitor);
		}
	}

	public static void validate(IFile file, IProgressMonitor monitor) {
		IWorkspaceRunnable validator = new BeansConfigValidator(file);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		ISchedulingRule rule = workspace.getRuleFactory().markerRule(file);
		try {
			workspace.run(validator, rule, IWorkspace.AVOID_UPDATE, monitor);
		}
		catch (CoreException e) {
			BeansCorePlugin.log("Error while running the validator", e);
		}
	}

	public void cleanup(IResource resource, IProgressMonitor monitor) {
		BeansModelUtils.deleteProblemMarkers(BeansModelUtils
				.getResourceModelElement(resource));
	}
}
