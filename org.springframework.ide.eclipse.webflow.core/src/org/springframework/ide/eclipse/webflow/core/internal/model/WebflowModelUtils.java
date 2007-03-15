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

package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowModelUtils {

	public static boolean isWebflowConfig(IFile file) {
		return getWebflowConfig(file) != null;
	}

	public static IWebflowConfig getWebflowConfig(IFile file) {
		IWebflowModel model = Activator.getModel();
		return (file != null && file.getProject() != null
				&& model.getProject(file.getProject()) != null
				&& model.getProject(file.getProject()).getConfig(file) != null ? model
				.getProject(file.getProject()).getConfig(file)
				: null);
	}

	public static List<IFile> getFiles(IProject project) {
		IWebflowProject webflowProject = Activator.getModel().getProject(
				project);
		List<IFile> files = new ArrayList<IFile>();
		if (webflowProject != null) {
			for (IWebflowConfig config : webflowProject.getConfigs()) {
				files.add(config.getResource());
			}
		}
		return files;
	}
}
