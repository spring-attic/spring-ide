/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;

/**
 * Refreshes the list of template descriptors.
 */
public class DownloadDescriptorJob implements IRunnableWithProgress {

	public ContentManager getContentManager() {
		return ContentPlugin.getDefault().getManager();
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			IStatus result = Status.OK_STATUS;
			// First refresh descriptor locations that are stored in the
			// preference store. If the content manager is dirty, it means
			// descriptor locations have been changed in the preference store
			if (getContentManager().isDirty()) {
				result = getContentManager().refresh(monitor, true);
			}

			if (!result.isOK()) {
				String errorMessage = ErrorUtils.getErrorMessage(result);
				throw new InvocationTargetException(new CoreException(getStatus(errorMessage, null)));
			}

		}
		catch (OperationCanceledException e) {
			final String message = "Download of descriptor files cancelled.";

			throw new InvocationTargetException(new CoreException(getStatus(message, e)));
		}

	}

	protected IStatus getStatus(String message, Throwable t) {
		return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, message, t);
	}

}
