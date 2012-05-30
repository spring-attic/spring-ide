/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * This class is responsible for aggregating the error info from the source page
 * of an {@link AbstractConfigEditor} and passing that info to the form header
 * of the other pages in the editor.
 * @author Leo Dos Santos
 * @since 2.3.1
 */
public class SpringConfigHeaderMessage {

	private final AbstractConfigEditor cEditor;

	private String message;

	private int messageType;

	public SpringConfigHeaderMessage(AbstractConfigEditor cEditor) {
		this.cEditor = cEditor;
	}

	public void setMessage(AbstractConfigFormPage page) {
		ScrolledForm form = page.getScrolledForm();
		if (form != null && !form.isDisposed()) {
			form.setMessage(message, messageType);
		}
	}

	public void updateMessage() {
		if (cEditor != null && cEditor.getResourceFile() != null && cEditor.getResourceFile().exists()) {
			try {
				IResource resource = cEditor.getResourceFile();
				IMarker[] markers = resource.findMarkers(SpringCore.MARKER_ID, true, IResource.DEPTH_ONE);
				message = null;
				messageType = IMessageProvider.NONE;

				if (markers.length > 0) {
					int errorCount = 0;
					int warningCount = 0;
					int infoCount = 0;
					for (IMarker marker : markers) {
						Object attr = marker.getAttribute(IMarker.SEVERITY);
						if (attr instanceof Integer) {
							Integer severity = (Integer) attr;
							if (severity == IMarker.SEVERITY_ERROR) {
								errorCount++;
							}
							else if (severity == IMarker.SEVERITY_WARNING) {
								warningCount++;
							}
							else if (severity == IMarker.SEVERITY_INFO) {
								infoCount++;
							}
						}
					}

					String errorMessage = ""; //$NON-NLS-1$
					String warnMessage = ""; //$NON-NLS-1$
					String infoMessage = ""; //$NON-NLS-1$
					String errorSingular = Messages.getString("AbstractConfigFormPage.ERROR_SINGULAR"); //$NON-NLS-1$
					String errorPlural = Messages.getString("AbstractConfigFormPage.ERROR_PLURAL"); //$NON-NLS-1$
					String warningSingular = Messages.getString("AbstractConfigFormPage.WARNING_SINGULAR"); //$NON-NLS-1$
					String warningPlural = Messages.getString("AbstractConfigFormPage.WARNING_PLURAL"); //$NON-NLS-1$
					String infoSingular = Messages.getString("AbstractConfigFormPage.INFO_SINGULAR"); //$NON-NLS-1$
					String infoPlural = Messages.getString("AbstractConfigFormPage.INFO_PLURAL"); //$NON-NLS-1$

					if (infoCount > 0) {
						messageType = IMessageProvider.INFORMATION;
						if (infoCount > 1) {
							infoMessage = infoCount + " " + infoPlural; //$NON-NLS-1$
						}
						else {
							infoMessage = infoCount + " " + infoSingular; //$NON-NLS-1$
						}
					}
					if (warningCount > 0) {
						messageType = IMessageProvider.WARNING;
						if (warningCount > 1) {
							warnMessage = warningCount + " " + warningPlural; //$NON-NLS-1$
						}
						else {
							warnMessage = warningCount + " " + warningSingular; //$NON-NLS-1$
						}
						if (infoCount > 0) {
							warnMessage += ", "; //$NON-NLS-1$
						}
					}
					if (errorCount > 0) {
						messageType = IMessageProvider.ERROR;
						if (errorCount > 1) {
							errorMessage = errorCount + " " + errorPlural; //$NON-NLS-1$
						}
						else {
							errorMessage = errorCount + " " + errorSingular; //$NON-NLS-1$
						}
						if (warningCount > 0 || infoCount > 0) {
							errorMessage += ", "; //$NON-NLS-1$
						}
					}
					message = errorMessage + warnMessage + infoMessage;
				}
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
						.getString("AbstractConfigFormPage.ERROR_UPDATING_PAGE_HEADER"), e)); //$NON-NLS-1$
			}
		}
	}

}
