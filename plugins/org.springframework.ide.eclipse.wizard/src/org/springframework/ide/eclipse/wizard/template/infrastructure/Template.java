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
package org.springframework.ide.eclipse.wizard.template.infrastructure;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.TemplateUtils;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;

/**
 * 
 * Template of a project, which requires a content item from a content manager
 * that handles template descriptors. In addition, when a template is created,
 * it has an explicit API that fetches the templata data. This should be called
 * at some point in time after the template is created. The reason it is not
 * called during Template creation is that fetching template data may require
 * downloading the data. Therefore it is the responsibility of the Template
 * creator to explicitly invoke the data-fetching API when appropriate.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class Template implements ITemplateElement {

	private final String name;

	private final String description;

	private final ImageDescriptor icon;

	private ITemplateProjectData data;

	private final ContentItem item;

	/**
	 * 
	 * @param item content item contains metadata about the template parsed from
	 * a template descriptor file. Cannot be null.
	 * @param icon optional: its the icon shown in the template viewer
	 */
	public Template(ContentItem item, ImageDescriptor icon) {
		this.item = item;
		this.name = item.getName();
		this.description = item.getDescription();
		this.icon = icon;
	}

	/**
	 * 
	 * @return description of what type of template project would be created
	 * using this template
	 */
	public String getDescription() {
		return description;
	}

	public ImageDescriptor getIcon() {
		return icon;
	}

	/**
	 * Content item that contains metadata about the template originally parsed
	 * from a template descriptor file.
	 * @return
	 */
	public ContentItem getItem() {
		return item;
	}

	public String getName() {
		return name;
	}

	/**
	 * Template data that points to the template descriptor and wizard json
	 * files, as well as the template project zip file. If null, it means that
	 * the template data needs to be fetched using the separate data fetching
	 * API in the template
	 * @return Template data for the template, or null, meaning that the data
	 * may still need to be fetched.
	 */
	public ITemplateProjectData getTemplateData() {
		return data;
	}

	/**
	 * Fetches the contents of a template, and set the data in the given
	 * template. If the data is already local, it will just set it in the
	 * template. Otherwise it will download the template data. Even if the data
	 * is already local, if there is a newer version of the template, then a
	 * download will be performed. Special exceptions like Simple Projects that
	 * do not require download as they are bundled in the wizard plugin are also
	 * handled. Regardless of whether template data needs to be downloaded or
	 * unzipped, any time a new template is created, this method should be
	 * invoked at least once as to add the latest template data to the template.
	 * @throws CoreException if failure occurred while either downloading or
	 * unzipping the template data
	 */
	public void fetchTemplateData(Shell shell, IProgressMonitor monitor) throws CoreException {

		final Shell dialogueShell = shell;

		ContentItem selectedItem = getItem();
		if (!selectedItem.isLocal() && selectedItem.getRemoteDescriptor().getUrl() == null) {
			String message = NLS.bind("In the descriptor file for ''{0}'', the URL to the project ZIP is missing.",
					selectedItem.getName());
			MessageDialog.openError(dialogueShell, NLS.bind("URL missing", null), message);
			return;
		}

		// Otherwise fetch the data
		try {
			IRunnableWithProgress dataJob = new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					try {
						monitor.beginTask("Download template " + Template.this.getName(), 100);
						ITemplateProjectData data;
						if (Template.this.getItem().isRuntimeDefined()) {
							data = new RuntimeTemplateProjectData(Template.this.getItem().getRuntimeProject());
						}
						else {
							data = TemplateUtils.importTemplate(Template.this, dialogueShell, new SubProgressMonitor(
									monitor, 1));
						}
						Template.this.setTemplateData(data);
						if (data == null) {
							throw new InvocationTargetException(new CoreException(new Status(IStatus.ERROR,
									WizardPlugin.PLUGIN_ID, NLS.bind(
											"Template data missing. Please check the template "
													+ Template.this.getName() + " to verify it has content.", null))));
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
			};
			if (monitor == null) {
				// If no monitor is provided, open in a separate dialogue
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(dialogueShell);
				dialog.run(true, true, dataJob);
			}
			else {
				dataJob.run(monitor);
			}

		}
		catch (InterruptedException e) {
			// do nothing
		}
		catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof CoreException) {
				throw (CoreException) target;
			}
			else {
				throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}

	}

	public URL getTemplateLocation() throws CoreException {
		if (data != null) {
			try {
				return data.getJsonDescriptor().toURI().toURL();
			}
			catch (MalformedURLException e) {
				throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
						"Unable to resolve template location", e));
			}
		}
		return null;
	}

	public URL getZippedLocation() throws CoreException {
		if (data != null) {
			try {
				return data.getZippedProject().toURI().toURL();
			}
			catch (MalformedURLException e) {
				throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
						"Unable to resolve template zipped location", e));
			}
		}
		return null;
	}

	public void setTemplateData(ITemplateProjectData data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return this.getClass().toString() + "-" + name.toString();
	}

}
