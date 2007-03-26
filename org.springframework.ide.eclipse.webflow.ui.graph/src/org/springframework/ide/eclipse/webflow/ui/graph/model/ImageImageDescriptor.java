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
