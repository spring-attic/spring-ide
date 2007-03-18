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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * {@link IWorkspaceRunnable} that triggers validation of a single
 * {@link IWebflowConfig}.
 *
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowValidator implements IWorkspaceRunnable {

	private IFile file = null;

	public WebflowValidator(IFile file) {
		this.file = file;
	}

	public void run(IProgressMonitor monitor) throws CoreException {
		validate(monitor);
	}

	public void validate(IProgressMonitor monitor) {

		WebflowModelUtils.deleteProblemMarkers(file);

		IWebflowState webflowState = WebflowModelUtils
				.getWebflowState(this.file);
		if (webflowState != null) {
			WebflowValidationVisitor validationVisitor = new WebflowValidationVisitor(
					WebflowModelUtils.getWebflowConfig(this.file));

			webflowState.accept(validationVisitor, monitor);

			if (!monitor.isCanceled()) {
				WebflowModelUtils.createMarkerFromProblemReporter(
						validationVisitor.getProblemReporter(), file);
			}
		}
	}
}
