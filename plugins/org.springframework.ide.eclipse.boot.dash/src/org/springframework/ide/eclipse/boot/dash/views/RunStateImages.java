/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * @author Kris De Volder
 */
public class RunStateImages {

	private Map<RunState, Image> images = new HashMap<RunState, Image>();

	public synchronized Image getImg(RunState state) {
		Image img = images.get(state);
		if (img==null) {
			String url = state.getImageUrl();
			images.put(state, img = createImage(url));
		}
		return img;
	}

	private Image createImage(String url) {
		ImageDescriptor dsc = BootDashActivator.getImageDescriptor(url);
		if (dsc!=null) {
			return dsc.createImage(true);
		}
		return null;
	}

	void dispose() {
		if (images!=null) {
			for (Image im : images.values()) {
				im.dispose();
			}
			images = null;
		}
	}

}
