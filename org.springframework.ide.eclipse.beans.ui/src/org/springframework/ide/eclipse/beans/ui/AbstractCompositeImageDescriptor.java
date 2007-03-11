/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

public abstract class AbstractCompositeImageDescriptor extends
		CompositeImageDescriptor {

	private Image baseImage;
	private int flags;
	private Point size;

	protected AbstractCompositeImageDescriptor(Image baseImage, int flags) {
		Assert.isNotNull(baseImage);
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