/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.graph.model;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window.IExceptionHandler;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IEntryActions;
import org.springframework.ide.eclipse.webflow.core.model.IExitActions;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowUtils;

/**
 * An image descriptor consisting of a main icon and several adornments. The
 * adornments are computed according to flags set on creation of the descriptor.
 */
public class WebflowModelImageDescriptor extends CompositeImageDescriptor {

	/**
	 * 
	 */
	private ImageDescriptor baseImage;

	/**
	 * 
	 */
	private IWebflowModelElement state;

	/**
	 * 
	 */
	private Point size;

	/**
	 * Create a new BeansUIImageDescriptor.
	 * 
	 * @param baseImage an image descriptor used as the base image
	 * @param state
	 */
	public WebflowModelImageDescriptor(ImageDescriptor baseImage,
			IWebflowModelElement state) {
		this.baseImage = baseImage;
		this.state = state;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	protected Point getSize() {
		if (size == null) {
			ImageData data = baseImage.getImageData();
			setSize(new Point(data.width, data.height));
		}
		return size;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int,
	 * int)
	 */
	protected void drawCompositeImage(int width, int height) {
		ImageData background = baseImage.getImageData();
		if (background == null) {
			background = DEFAULT_IMAGE_DATA;
		}
		drawImage(background, 0, 0);
		drawOverlays();
	}

	/**
	 * Add any overlays to the image as specified in the flags.
	 */
	protected void drawOverlays() {
		int x = 0;
		int y = 0;
		ImageData data = null;
		if (this.state.getElementParent() != null
				&& this.state.getElementParent() instanceof IWebflowState
				&& this.state.equals(((IWebflowState) this.state
						.getElementParent()).getStartState())) {
			data = WebflowUIImages.DESC_OVR_START_STATE.getImageData();
			drawImage(data, x, y);
		}
		if (this.state != null && this.state instanceof IActionElement) {
			IWebflowModelElement parent = this.state.getElementParent();

			if (parent instanceof IEntryActions) {
				data = WebflowUIImages.DESC_OVR_INPUT.getImageData();
				drawImage(data, x, y);
			}
			else if (parent instanceof IExitActions) {
				data = WebflowUIImages.DESC_OVR_OUTPUT.getImageData();
				drawImage(data, x, y);
			}
			/*
			 * else if (parent instanceof IRenderActions) { data =
			 * WebflowUIImages.DESC_OVR_RENDER.getImageData(); drawImage(data,
			 * x, y); }
			 */
		}
		if (this.state instanceof IState && !WebflowUtils.isValid(this.state)) {
			data = WebflowUIImages.DESC_OVR_ERROR.getImageData();
			drawImage(data, x, 8);
		}
		else if ((this.state instanceof IActionElement
				|| this.state instanceof IExceptionHandler
				|| this.state instanceof IIf || this.state instanceof IAttributeMapper)
				&& !WebflowUtils.isValid(this.state)) {
			data = WebflowUIImages.DESC_OVR_ERROR.getImageData();
			drawImage(data, x, 8);
		}

	}

	/**
	 * 
	 * 
	 * @param size
	 */
	protected void setSize(Point size) {
		this.size = size;
	}
}
