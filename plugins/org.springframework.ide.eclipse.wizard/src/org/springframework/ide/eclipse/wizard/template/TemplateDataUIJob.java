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
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ITemplateProjectData;
import org.springframework.ide.eclipse.wizard.template.infrastructure.RuntimeTemplateProjectData;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;

/**
 * Fetches the contents of a template, and set the data in the given template.
 * If the data is already local, it will just set it in the template. Otherwise
 * it will download the template data. Even if the data is already local, if
 * there is a newer version of the template, then a download will be performed.
 * Special exceptions like Simple Projects that do not require download as they
 * are bundled in the wizard plugin are also handled. Regardless of whether
 * template data needs to be downloaded or unzipped, any time a new template is
 * created, this method should be invoked at least once as to add the latest
 * template data to the template.
 * @throws CoreException if failure occurred while either downloading or
 * unzipping the template data
 */
public class TemplateDataUIJob implements IRunnableWithProgress {

	private final Template template;

	private final Shell shell;

	public TemplateDataUIJob(Template template, Shell shell) {
		this.template = template;
		this.shell = shell;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		ContentItem selectedItem = template.getItem();
		if (!selectedItem.isLocal() && selectedItem.getRemoteDescriptor().getUrl() == null) {
			String message = NLS.bind("In the descriptor file for ''{0}'', the URL to the project ZIP is missing.",
					selectedItem.getName());
			throw new InvocationTargetException(new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
					message)));
		}

		try {
			monitor.beginTask("Download template " + template.getName(), 100);
			ITemplateProjectData data;
			if (template.getItem().isRuntimeDefined()) {
				data = new RuntimeTemplateProjectData(template.getItem().getRuntimeProject());
			}
			else {
				data = TemplateUtils.importTemplate(template, shell, new SubProgressMonitor(monitor, 1));
			}
			template.setTemplateData(data);
			if (data == null) {
				throw new InvocationTargetException(new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
						NLS.bind("Template data missing. Please check the template " + template.getName()
								+ " to verify it has content.", null))));
			}
		}
		catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
		catch (OperationCanceledException e) {
			throw new InterruptedException();
		}
		finally {
			monitor.done();
		}

	}
}
