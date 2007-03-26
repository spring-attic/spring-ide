/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.springframework.util.Assert;

public abstract class AbstractCompositeImageDescriptor extends
		CompositeImageDescriptor {

	private Image baseImage;
	private int flags;
	private Point size;

	protected AbstractCompositeImageDescriptor(Image baseImage, int flags) {
		Assert.notNull(baseImage);
		this.baseImage = baseImage;
		this.flags = flags;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof AbstractCompositeImageDescriptor)) {
			return false;
		}
		AbstractCompositeImageDescriptor other =
				(AbstractCompositeImageDescriptor) object;
		return (baseImage.equals(other.baseImage) && flags == other.flags);
	}

	@Override
	public int hashCode() {
		return baseImage.hashCode() | flags;
	}

	public int getFlags() {
		return flags;
	}

	@Override
	protected final Point getSize() {
		if (size == null) {
			ImageData data = baseImage.getImageData();
			size = new Point(data.width, data.height);
		}
		return size;
	}

	@Override
	protected final void drawCompositeImage(int width, int height) {
		ImageData background = baseImage.getImageData();
		if (background == null) {
			background = DEFAULT_IMAGE_DATA;
		}
		drawImage(background, 0, 0);
		drawOverlays();
	}

	protected abstract void drawOverlays();
}
