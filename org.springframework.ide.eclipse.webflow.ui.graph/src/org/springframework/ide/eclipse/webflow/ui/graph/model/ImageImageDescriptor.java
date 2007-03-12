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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * 
 */
public class ImageImageDescriptor extends ImageDescriptor {

    /**
     * 
     */
    private Image image;

    /**
     * 
     * 
     * @param image 
     */
    public ImageImageDescriptor(Image image) {
        this.image = image;
    }

    /* (non-Javadoc)
     * @see ImageDescriptor#getImageData()
     */
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
     */
    public ImageData getImageData() {
        return image.getImageData();
    }

    /* (non-Javadoc)
     * @see Object#equals(Object)
     */
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return (obj != null) && getClass().equals(obj.getClass())
                && image.equals(((ImageImageDescriptor) obj).image);
    }

    /* (non-Javadoc)
     * @see Object#hashCode()
     */
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return image.hashCode();
    }
}
