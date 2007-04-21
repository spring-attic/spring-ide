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
package org.springframework.ide.eclipse.webflow.ui.graph.model;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IEntryActions;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
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
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
class WebflowModelImageDescriptor extends CompositeImageDescriptor {

	public static final int FLAG_STARTSTATE = 1 << 2;

	public static final int FLAG_ERROR = 1 << 3;

	public static final int FLAG_INPUT = 1 << 4;

	public static final int FLAG_OUTPUT = 1 << 5;

	private Image baseImage;

	private Point size;

	private int flags;

	/**
	 * Create a new WebflowModelImageDescriptor.
	 * @param baseImage an image descriptor used as the base image
	 * @param state
	 */
	public WebflowModelImageDescriptor(Image baseImage,
			IWebflowModelElement state) {
		this.baseImage = baseImage;
		this.flags = getFlags(state);
		this.size = getSize();
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
		if ((flags & FLAG_INPUT) != 0) {
			data = WebflowUIImages.DESC_OVR_INPUT.getImageData();
			drawImage(data, x, y);
		}
		if ((flags & FLAG_OUTPUT) != 0) {
			data = WebflowUIImages.DESC_OVR_OUTPUT.getImageData();
			drawImage(data, x, y);
		}
		if ((flags & FLAG_STARTSTATE) != 0) {
			data = WebflowUIImages.DESC_OVR_START_STATE.getImageData();
			drawImage(data, x, y);
		}
		if ((flags & FLAG_ERROR) != 0) {
			data = WebflowUIImages.DESC_OVR_ERROR.getImageData();
			drawImage(data, x, y);
		}
	}

	/**
	 * @param size
	 */
	protected void setSize(Point size) {
		this.size = size;
	}

	private int getFlags(IWebflowModelElement element) {
		int flags = 0;
		if (element.getElementParent() != null
				&& element.getElementParent() instanceof IWebflowState
				&& element.equals(((IWebflowState) element.getElementParent())
						.getStartState())) {
			flags |= FLAG_STARTSTATE;

		}
		if (element != null && element instanceof IActionElement) {
			IWebflowModelElement parent = element.getElementParent();

			if (parent instanceof IEntryActions) {
				flags |= FLAG_INPUT;
			}
			else if (parent instanceof IExitActions) {
				flags |= FLAG_OUTPUT;
			}
		}
		if (element instanceof IState && !WebflowUtils.isValid(element)) {
			flags |= FLAG_ERROR;
		}
		else if ((element instanceof IActionElement
				|| element instanceof IExceptionHandler
				|| element instanceof IIf || element instanceof IAttributeMapper)
				&& !WebflowUtils.isValid(element)) {
			flags |= FLAG_ERROR;
		}
		return flags;
	}

	public int hashCode() {
		return baseImage.hashCode() | flags | size.hashCode();
	}

	public boolean equals(final Object object) {
		if (object == null
				|| !WebflowModelImageDescriptor.class.equals(object.getClass()))
			return false;
		WebflowModelImageDescriptor other = (WebflowModelImageDescriptor) object;
		return (baseImage.equals(other.baseImage) && flags == other.flags && size
				.equals(other.size));
	}
	
	public String toString() {
		return baseImage.toString();
	}
}
	