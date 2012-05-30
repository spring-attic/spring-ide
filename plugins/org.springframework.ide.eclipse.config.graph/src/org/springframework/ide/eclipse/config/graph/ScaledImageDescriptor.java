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

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * @author Leo Dos Santos
 */
public class ScaledImageDescriptor extends CompositeImageDescriptor {

	private final ImageData base;

	private final double scale;

	public ScaledImageDescriptor(ImageDescriptor descriptor, double scale) {
		base = getImageData(descriptor);
		this.scale = scale;
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(base.scaledTo(width, height), 0, 0);
	}

	private ImageData getImageData(ImageDescriptor descriptor) {
		ImageData data = descriptor.getImageData();
		// see bug 51965: getImageData can return null
		if (data == null) {
			data = DEFAULT_IMAGE_DATA;
		}
		return data;
	}

	@Override
	protected Point getSize() {
		int x = (int) (base.width * scale);
		int y = (int) (base.height * scale);
		return new Point(x, y);
	}

}
