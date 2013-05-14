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
 * Downloads all the template descriptors. See the template descriptor content
 * manager API on how to access the actual templates
 */
public class DownloadDescriptorJob implements IRunnableWithProgress {

	public ContentManager getContentManager() {
		return ContentPlugin.getDefault().getManager();
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			IStatus results = getContentManager().refresh(monitor, true, WizardPlugin.getDefault().getBundle());
			if (results.isOK()) {
				return;
			}
			else {
				final String message = (results.getChildren()[0]).getMessage();
				if (!results.isOK()) {
					throw new InvocationTargetException(new CoreException(getStatus(message, null)));
				}

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
