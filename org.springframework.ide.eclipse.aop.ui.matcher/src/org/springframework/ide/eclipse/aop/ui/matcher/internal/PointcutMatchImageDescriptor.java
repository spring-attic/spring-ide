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
package org.springframework.ide.eclipse.aop.ui.matcher.internal;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelImages;
import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * An image descriptor consisting of a main icon and several adornments. The
 * adornments are computed according to flags set on creation of the descriptor.
 * @author Christian Dupuis
 * @since 2.0.2
 */
class PointcutMatchImageDescriptor extends CompositeImageDescriptor {
	
	private static final int FLAG_JAVA_ELEMENT = 1 << 2;
	
	private Image baseImage;

	private Point size;

	private int flags;

	public PointcutMatchImageDescriptor(Image baseImage,
			Object state) {
		this.baseImage = baseImage;
		this.flags = getFlags(state);
		this.size = getSize();
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
		int y = 0;
		ImageData data = null;
		if ((flags & FLAG_JAVA_ELEMENT) != 0) {
			data = AopReferenceModelImages.DESC_OVR_ADVICE.getImageData();
			drawImage(data, x, y);
		}
	}

	protected void setSize(Point size) {
		this.size = size;
	}

	private int getFlags(Object element) {
		int flags = 0;
		if (element instanceof IAopReference) {
			flags |= FLAG_JAVA_ELEMENT;

		}
		else if (element instanceof IBean) {
			flags |= FLAG_JAVA_ELEMENT;
		}
		return flags;
	}

	public int hashCode() {
		return baseImage.hashCode() | flags | size.hashCode();
	}

	public boolean equals(final Object object) {
		if (object == null
				|| !PointcutMatchImageDescriptor.class.equals(object.getClass()))
			return false;
		PointcutMatchImageDescriptor other = (PointcutMatchImageDescriptor) object;
		return (baseImage.equals(other.baseImage) && flags == other.flags && size
				.equals(other.size));
	}
	
	public String toString() {
		return baseImage.toString();
	}
}
	