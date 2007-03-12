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

package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * 
 */
class BottomAnchor extends AbstractConnectionAnchor {

    /**
     * 
     */
    private int offset;

    /**
     * 
     * 
     * @param source 
     * @param offset 
     */
    BottomAnchor(IFigure source, int offset) {
        super(source);
        this.offset = offset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.ConnectionAnchor#getLocation(org.eclipse.draw2d.geometry.Point)
     */
    public Point getLocation(Point reference) {
        Rectangle r = getOwner().getBounds().getCopy();
        getOwner().translateToAbsolute(r);
        int off = offset;
        if (off == -1)
            off = r.width / 2;
        if (r.contains(reference) || r.bottom() > reference.y)
            return r.getTop().translate(off, 0);
        else
            return r.getBottom().translate(off, -1);
    }

}