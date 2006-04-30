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

package org.springframework.ide.eclipse.ui;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A registry that maps <code>ImageDescriptor</code>s to <code>Image</code>.
 * @see org.eclipse.jface.resource.ImageDescriptor
 * @see org.eclipse.swt.graphics.Image
 */
public class ImageDescriptorRegistry {

	private HashMap registry = new HashMap(10);
	private Display display;

	/**
	 * Creates a new image descriptor registry for the current or default
	 * display, respectively.
	 */
	public ImageDescriptorRegistry() {
		this(SpringUIUtils.getStandardDisplay());
	}

	/**
	 * Creates a new image descriptor registry for the given display. All images
	 * managed by this registry will be disposed when the display gets disposed.
	 * 
	 * @param display  the display the images managed by this registry are
	 *  					allocated for
	 */
	public ImageDescriptorRegistry(Display display) {
		Assert.isNotNull(display);
		this.display = display;
		hookDisplay();
	}

	/**
	 * Returns the image associated with the given image descriptor.
	 * 
	 * @param descriptor  the image descriptor for which the registry manages
	 * 						an image
	 * @return the image associated with the image descriptor or
	 *         <code>null</code> if the image descriptor can't create the
	 *         requested image.
	 */
	public Image get(ImageDescriptor descriptor) {
		if (descriptor == null) {
			descriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		Image result = (Image) registry.get(descriptor);
		if (result != null) {
			return result;
		}
		Assert.isTrue(display == SpringUIUtils.getStandardDisplay(),
						SpringUIMessages.ImageDescriptorRegistry_wrongDisplay);
		result = descriptor.createImage();
		if (result != null) {
			registry.put(descriptor, result);
		}
		return result;
	}

	/**
	 * Disposes all images managed by this registry.
	 */
	public void dispose() {
		for (Iterator iter = registry.values().iterator(); iter.hasNext(); ) {
			Image image = (Image) iter.next();
			image.dispose();
		}
		registry.clear();
	}

	private void hookDisplay() {
		display.disposeExec(new Runnable() {
			public void run() {
				dispose();
			}
		});
	}
}
