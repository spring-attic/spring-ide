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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.springframework.ide.eclipse.config.graph.ScaledImageDescriptor;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;


/**
 * @author Leo Dos Santos
 */
public class WebFlowImages {

	private static final URL baseURL = ConfigUiPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	private static ImageRegistry imageRegistry;

	private static final double SCALE = 0.5;

	private static final String FLOW = "webflow"; //$NON-NLS-1$

	public static final ImageDescriptor ACTION = create(FLOW, "action.png"); //$NON-NLS-1$

	public static final ImageDescriptor ACTION_SMALL = scale(ACTION, SCALE);

	public static final ImageDescriptor DECISION = create(FLOW, "decision.png"); //$NON-NLS-1$

	public static final ImageDescriptor DECISION_SMALL = scale(DECISION, SCALE);

	public static final ImageDescriptor END = create(FLOW, "end.png"); //$NON-NLS-1$

	public static final ImageDescriptor END_SMALL = scale(END, SCALE);

	public static final ImageDescriptor SUBFLOW = create(FLOW, "subflow.png"); //$NON-NLS-1$

	public static final ImageDescriptor SUBFLOW_SMALL = scale(SUBFLOW, SCALE);

	public static final ImageDescriptor VIEW = create(FLOW, "view.png"); //$NON-NLS-1$

	public static final ImageDescriptor VIEW_SMALL = scale(VIEW, SCALE);

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		}
		catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (baseURL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(baseURL, buffer.toString());
	}

	private static ImageDescriptor scale(ImageDescriptor descriptor, double scale) {
		String key = "" + descriptor.hashCode(); //$NON-NLS-1$
		key += new Double(scale).hashCode();
		ImageDescriptor cache = getImageRegistry().getDescriptor(key);
		if (cache == null) {
			cache = new ScaledImageDescriptor(descriptor, scale);
			getImageRegistry().put(key, cache);
		}
		return cache;
	}

}
