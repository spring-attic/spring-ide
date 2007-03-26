/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
