/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;

/**
 * {@link CompositeImageDescriptor} that can be used to decorate
 * {@link IBeansModelElement} with error and warning icons.
 * @author Christian Dupuis
 * @since 2.0.2
 */
class BeansModelImageDescriptor extends CompositeImageDescriptor {

	public static final int FLAG_ERROR = 1 << 2;

	public static final int FLAG_WARNING = 1 << 3;

	private Image baseImage;

	private Point size;

	private int flags;

	public BeansModelImageDescriptor(Image baseImage, IBeansModelElement state,
			int flags) {
		this.baseImage = baseImage;
		this.size = getSize();
		this.flags = flags;
	}

	protected Point getSize() {
		if (size == null) {
			ImageData data = baseImage.getImageData();
			setSize(new Point(data.width, data.height));
		}
		return size;
	}

	protected void drawCompositeImage(int width, int height) {
		ImageData background = baseImage.getImageData();
		if (background == null) {
			background = DEFAULT_IMAGE_DATA;
		}
		drawImage(background, 0, 0);
		drawOverlays();
	}

	protected void drawOverlays() {
		int x = 0;
		int y = baseImage.getBounds().height / 2;
		ImageData data = null;
		if ((flags & FLAG_ERROR) != 0) {
			data = BeansUIImages.DESC_OVR_ERROR.getImageData();
			drawImage(data, x, y);
		}
		if ((flags & FLAG_WARNING) != 0) {
			data = BeansUIImages.DESC_OVR_WARNING.getImageData();
			drawImage(data, x, y);
		}
	}

	/**
	 * @param size
	 */
	protected void setSize(Point size) {
		this.size = size;
	}

	public int hashCode() {
		return baseImage.hashCode() | flags | size.hashCode();
	}

	public boolean equals(final Object object) {
		if (object == null
				|| !BeansModelImageDescriptor.class.equals(object.getClass()))
			return false;
		BeansModelImageDescriptor other = (BeansModelImageDescriptor) object;
		return (baseImage.equals(other.baseImage) && flags == other.flags
				| size.equals(other.size));
	}

	public String toString() {
		return baseImage.toString();
	}
}
