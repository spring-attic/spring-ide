/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.browser;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;

public class Images {

	public static final String IMG_NAV_HOME = "IMG_NAV_HOME";
	
	// the image registry
	private static ImageRegistry imageRegistry;

	public static Image getImage(String key) {
		init();
		return imageRegistry.get(key);
	}

	private synchronized static void init() {
		if (imageRegistry==null) {
			imageRegistry = new ImageRegistry();
			img(IMG_NAV_HOME, "resources/icons/home_16.png");
		}
	}

	private static void img(String key, String path) {
		try {
		URL url = Platform.getBundle(GettingStartedActivator.PLUGIN_ID).getEntry(path);
		ImageDescriptor image = ImageDescriptor.createFromURL(url);
		imageRegistry.put(key, image);
		} catch (Throwable e) {
			//What the @#$!??
			GettingStartedActivator.log(e);
		}
	}
	
}
