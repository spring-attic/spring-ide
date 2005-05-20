/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.parts;

import org.eclipse.draw2d.IFigure;

/**
 * Experimental API.
 * @since 3.1
 */
public interface LayoutListener {

    /**
     * A stub implementation which implements all of the declared methods. 
     * @since 3.1
     */
    class Stub implements LayoutListener {

        /**
         * Stub which does nothing.
         * @see LayoutListener#invalidate(IFigure)
         */
        public void invalidate(IFigure container) {
        }

        /**
         * Stub which does nothing.
         * @see LayoutListener#layout(IFigure)
         */
        public boolean layout(IFigure container) {
            return false;
        }

        /**
         * Stub which does nothing.
         * @see LayoutListener#postLayout(IFigure)
         */
        public void postLayout(IFigure container) {
        }

        /**
         * Stub which does nothing.
         * @see LayoutListener#remove(IFigure)
         */
        public void remove(IFigure child) {
        }

        /**
         * Stub which does nothing.
         * @see LayoutListener#setConstraint(IFigure, java.lang.Object)
         */
        public void setConstraint(IFigure child, Object constraint) {
        }

    }

    /**
     * Called when a container has been invalidated.
     * @param container the invalidated Figure
     * @since 3.1
     */
    void invalidate(IFigure container);

    /**
     * Called prior to layout occurring.  A listener may intercept a layout by
     * returning <code>true</code>.  If the layout is intercepted, the container
     * <code>LayoutManager</code> will not receive a layout call.
     * @param container the figure incurring a layout
     * @return <code>true</code> if the layout has been intercepted by the listener
     * @since 3.1
     */
    boolean layout(IFigure container);

    /**
     * Called after layout has occurred.
     * @since 3.1
     * @param container the figure incurring a layout 
     */
    void postLayout(IFigure container);

    /**
     * Called when a child is about to be removed from its parent.
     * @since 3.1
     * @param child the child being removed
     */
    void remove(IFigure child);

    /**
     * Called when a child's constraint has been changed
     * @param child the child being updated
     * @param constraint the child's new constraint
     * @since 3.1
     */
    void setConstraint(IFigure child, Object constraint);

}
