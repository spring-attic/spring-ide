/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * @author Kris De Volder
 */
public class RunStateImages {

	private Map<Object, Image[]> animations = new HashMap<>();

	public synchronized Image[] getAnimation(RunState state) throws Exception {
		Image[] anim = animations.get(state);
		if (anim==null) {
			String url = state.getImageUrl();
			animations.put(state, anim = createAnimation(url));
		}
		return anim;
	}

	private Image[] createAnimation(String urlString) throws Exception {
		// For a png there might be animation frames to load (ImageLoader cannot
		// pull out frames for an animated png)
		// Given an input url of the form "foo.png" this will search
		// for "foo_1.png", "foo_2.png" etc, until it can find no more frames
		int dot = urlString.lastIndexOf('.');
		String prefix = urlString.substring(0, dot);
		String suffix = urlString.substring(dot+1);

		List<Image> images = new ArrayList<>();
		int count = 1;
		ImageDescriptor descriptor = null;
		while ((descriptor = BootDashActivator.getImageDescriptor(prefix+"_"+Integer.toString(count++)+"."+suffix)) != null) {
			images.add(descriptor.createImage());
		}

		if (images.size() != 0) {
			// Animation frames were found, return them
			return images.toArray(new Image[images.size()]);
		}
		else {
			ImageDescriptor imageDescriptor = BootDashActivator.getImageDescriptor(urlString);
			Image image = imageDescriptor.createImage();
			return new Image[] {image};
		}


//		input = cl.getResourceAsStream(urlString);
//		try {
//			ImageData[] data = loader.load(input);
//			Image[] imgs = new Image[data.length];
//			for (int i = 0; i < imgs.length; i++) {
//				imgs[i] = new Image(Display.getDefault(), data[i]);
//			}
//			return imgs;
//		} finally {
//			input.close();
//		}
	}

	public synchronized Image[] getDecoratedImages(final RunState state, final ImageDescriptor descriptor, final int position) throws Exception {
		Image[] images = getAnimation(state);
		if (descriptor == null) {
			return images;
		} else {
			Object key = Arrays.<Object>asList(state, descriptor, position);
			Image[] decoratedImages = animations.get(key);
			if (decoratedImages == null) {
				decoratedImages = Arrays.copyOf(images, images.length);
				for (int i = 0; i < decoratedImages.length; i++) {
					decoratedImages[i] = new DecorationOverlayIcon(decoratedImages[i], descriptor, IDecoration.BOTTOM_RIGHT).createImage(decoratedImages[i].getDevice());
				}
				animations.put(key, decoratedImages);
			}
			return decoratedImages;
		}
	}

	void dispose() {
		if (animations!=null) {
			for (Image[] anim : animations.values()) {
				if (anim!=null) {
					for (Image image : anim) {
						image.dispose();
					}
				}
			}
			animations = null;
		}
	}

}
