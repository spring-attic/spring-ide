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
package org.springframework.ide.eclipse.wizard;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * @author Terry Denney
 */
public class WizardImages {

	private static final String WIZBAN = "wizban";

	private static final String OBJ = "obj16";

	private static ImageRegistry imageRegistry;

	public static final ImageDescriptor TEMPLATE_WIZARD_ICON = create(WIZBAN, "template_wizard.png");

	public static final ImageDescriptor TEMPLATE_ICON = create(OBJ, "template_project.png");

	public static final ImageDescriptor REFRESH_ICON = create(OBJ, "refresh.gif");

	public static final ImageDescriptor TEMPLATE_CATEGORY_ICON = create(OBJ, "category.png");

	private static URL baseURL = null;

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		}
		catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	public static Image getImage(ImageDescriptor imageDescriptor) {
		ImageRegistry imageRegistry = getImageRegistry();
		Image image = imageRegistry.get("" + imageDescriptor.hashCode());
		if (image == null) {
			image = imageDescriptor.createImage(true);
			imageRegistry.put("" + imageDescriptor.hashCode(), image);
		}
		return image;
	}

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}

	private static void initBaseURL() {
		if (baseURL == null) {
			baseURL = WizardPlugin.getDefault().getBundle().getEntry("/icons/full/");
		}
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		initBaseURL();
		if (baseURL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append("/");
		buffer.append(name);
		return new URL(baseURL, buffer.toString());
	}

}
