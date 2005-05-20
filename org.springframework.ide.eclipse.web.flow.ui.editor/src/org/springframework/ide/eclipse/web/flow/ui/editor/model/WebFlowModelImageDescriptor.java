/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.ui.editor.model;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;

/**
 * An image descriptor consisting of a main icon and several adornments.
 * The adornments are computed according to flags set on creation of the
 * descriptor.
 */
public class WebFlowModelImageDescriptor extends CompositeImageDescriptor {

    private ImageDescriptor baseImage;

    private IState state;

    private Point size;

    /**
     * Create a new BeansUIImageDescriptor.
     * 
     * @param baseImage  an image descriptor used as the base image
     * @param node  a node which adornments are to be rendered
     * 
     */
    public WebFlowModelImageDescriptor(ImageDescriptor baseImage, IState state) {
        this.baseImage = baseImage;
        this.state = state;
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

    /**
     * Add any overlays to the image as specified in the flags.
     */
    protected void drawOverlays() {
        int x = 0;
        int y = 0;
        ImageData data = null;
        if (this.state.isStartState()) {
            data = WebFlowImages.DESC_OBJS_START_STATE.getImageData();
            drawImage(data, x, y);
        }
    }

    protected void setSize(Point size) {
        this.size = size;
    }
}
