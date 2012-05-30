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
package org.springframework.ide.eclipse.config.ui.editors.batch.graph;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;


/**
 * @author Leo Dos Santos
 */
public class BatchImages {

	private static final URL baseURL = ConfigUiPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	private static final String BATCH = "batch"; //$NON-NLS-1$

	public static final ImageDescriptor DECISION = create(BATCH, "decision.png"); //$NON-NLS-1$

	public static final ImageDescriptor DECISION_SMALL = create(BATCH, "decision-small.png"); //$NON-NLS-1$

	public static final ImageDescriptor END = create(BATCH, "end.png"); //$NON-NLS-1$

	public static final ImageDescriptor END_SMALL = create(BATCH, "end-small.png"); //$NON-NLS-1$

	public static final ImageDescriptor FAIL = create(BATCH, "fail.png"); //$NON-NLS-1$

	public static final ImageDescriptor FAIL_SMALL = create(BATCH, "fail-small.png"); //$NON-NLS-1$

	public static final ImageDescriptor NEXT = create(BATCH, "next.png"); //$NON-NLS-1$

	public static final ImageDescriptor NEXT_SMALL = create(BATCH, "next-small.png"); //$NON-NLS-1$

	public static final ImageDescriptor SPLIT = create(BATCH, "split.png"); //$NON-NLS-1$

	public static final ImageDescriptor SPLIT_SMALL = create(BATCH, "split-small.png"); //$NON-NLS-1$

	public static final ImageDescriptor STOP = create(BATCH, "stop.png"); //$NON-NLS-1$

	public static final ImageDescriptor STOP_SMALL = create(BATCH, "stop-small.png"); //$NON-NLS-1$

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
