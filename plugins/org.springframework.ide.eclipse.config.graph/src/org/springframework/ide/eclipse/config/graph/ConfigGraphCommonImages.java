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
package org.springframework.ide.eclipse.config.graph;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class ConfigGraphCommonImages {

	private static final URL baseURL = ConfigGraphPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	private static final String OBJ = "obj"; //$NON-NLS-1$

	public static final ImageDescriptor ACTIVITY = create(OBJ, "activity.png"); //$NON-NLS-1$

	public static final ImageDescriptor ACTIVITY_SMALL = create(OBJ, "activity-small.png"); //$NON-NLS-1$

	public static final ImageDescriptor CONNECTION_DASHED = create(OBJ, "connection-dashed.gif"); //$NON-NLS-1$

	public static final ImageDescriptor CONNECTION_SOLID = create(OBJ, "connection-solid.gif"); //$NON-NLS-1$

	public static final ImageDescriptor SEQUENCE_BEGIN_HORIZONTAL = create(OBJ, "sequence-begin-horizontal.png"); //$NON-NLS-1$

	public static final ImageDescriptor SEQUENCE_BEGIN_HORIZONTAL_SMALL = create(OBJ,
			"sequence-begin-horizontal-small.png"); //$NON-NLS-1$

	public static final ImageDescriptor SEQUENCE_BEGIN_VERTICAL = create(OBJ, "sequence-begin-vertical.png"); //$NON-NLS-1$

	public static final ImageDescriptor SEQUENCE_BEGIN_VERTICAL_SMALL = create(OBJ, "sequence-begin-vertical-small.png"); //$NON-NLS-1$

	public static final ImageDescriptor SEQUENCE_END_HORIZONTAL = create(OBJ, "sequence-end-horizontal.png"); //$NON-NLS-1$

	public static final ImageDescriptor SEQUENCE_END_HORIZONTAL_SMALL = create(OBJ, "sequence-end-horizontal-small.png"); //$NON-NLS-1$

	public static final ImageDescriptor SEQUENCE_END_VERTICAL = create(OBJ, "sequence-end-vertical.png"); //$NON-NLS-1$

	public static final ImageDescriptor SEQUENCE_END_VERTICAL_SMALL = create(OBJ, "sequence-end-vertical-small.png"); //$NON-NLS-1$

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		}
		catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
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

}
