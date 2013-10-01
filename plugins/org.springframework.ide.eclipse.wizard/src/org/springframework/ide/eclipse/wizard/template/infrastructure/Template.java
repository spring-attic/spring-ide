/*******************************************************************************
 *  Copyright (c) 2012, 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
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
