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
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;

/**
 * Refreshes the list of template descriptors. Two types of descriptors exist:
 * <p/>
 * 1. Those stored in the preference store, that ONLY get refreshed if the
 * preference is marked as dirty
 * <p/>
 * 2. Those descriptors pointing to locations inside the wizard plugin bundle.
 * These are not refreshed, and are only read when the content manager is
 * initiliased by other components.
 */
public class DownloadDescriptorJob implements IRunnableWithProgress {

	public ContentManager getContentManager() {
		return ContentPlugin.getDefault().getManager();
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			MultiStatus results = new MultiStatus(WizardPlugin.PLUGIN_ID, 0, "Results of template project refresh:",
					null);
			// First refresh descriptor locations that are stored in the
			// preference store. If the content manager is dirty, it means
			// descriptor locations have been changed in the preference store
			if (getContentManager().isDirty()) {
				IStatus result = getContentManager().refresh(monitor, true);
				if (!result.isOK()) {
					results.add(result);
				}
			}

			if (!results.isOK()) {
				throw new InvocationTargetException(new CoreException(getStatus(
						"Error refreshing template project descriptors"
								+ (results.getMessage() != null ? " due to: " + results.getMessage() : ""), null)));
			}

		}
		catch (OperationCanceledException e) {
			final String message = ("Download of descriptor files cancelled.");

			throw new InvocationTargetException(new CoreException(getStatus(message, e)));
		}

	}

	protected IStatus getStatus(String message, Throwable t) {
		return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, message, t);
	}

}
