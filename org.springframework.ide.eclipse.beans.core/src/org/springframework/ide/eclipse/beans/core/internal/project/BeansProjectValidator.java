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
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.BeansConfigValidator;

public class BeansProjectValidator extends BeansProjectBuilder {

	public static final String BUILDER_ID = BeansCorePlugin.PLUGIN_ID +
    														  ".beansvalidator";
	protected void buildFile(IFile file, IProgressMonitor monitor) {
		monitor.beginTask(BeansCorePlugin.getFormattedMessage(
											"BeansConfigValidator.validateFile",
											file.getFullPath().toString()), 2);
		BeansConfigValidator validator = new BeansConfigValidator(monitor);
		validator.validate(file);
		if (!monitor.isCanceled()) {
			monitor.done();
		}
	}
}
