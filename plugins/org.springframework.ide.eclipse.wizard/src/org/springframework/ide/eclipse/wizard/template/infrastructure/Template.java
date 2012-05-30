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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;


/**
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

	public Template(ContentItem item, ImageDescriptor icon) {
		this.item = item;
		this.name = item.getName();
		this.description = item.getDescription();
		this.icon = icon;
	}

	public String getDescription() {
		return description;
	}

	public ImageDescriptor getIcon() {
		return icon;
	}

	public ContentItem getItem() {
		return item;
	}

	public String getName() {
		return name;
	}

	public ITemplateProjectData getTemplateData() {
		return data;
	}

	public URL getTemplateLocation() throws CoreException {
		if (data != null) {
			try {
				return data.getJsonDescriptor().toURL();
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
				return data.getZippedProject().toURL();
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
