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

package org.springframework.ide.eclipse.webflow.ui.graph.policies;

import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.StateLabel;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.IfPart;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.StatePart;

/**
 * 
 */
public class StateSelectionEditPolicy extends NonResizableEditPolicy {

    /**
     * 
     * 
     * @return 
     */
    private StateLabel getLabel() {
        if (getHost() instanceof StatePart) {
            StatePart part = (StatePart) getHost();
            return ((StateLabel) part.getFigure());
        }
        else {
            IfPart part = (IfPart) getHost();
            return ((StateLabel) part.getFigure());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#hideFocus()
     */
    protected void hideFocus() {
        getLabel().setFocus(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#hideSelection()
     */
    protected void hideSelection() {
        getLabel().setSelected(false);
        getLabel().setFocus(false);

    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#showFocus()
     */
    protected void showFocus() {
        getLabel().setFocus(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.SelectionEditPolicy#showPrimarySelection()
     */
    protected void showPrimarySelection() {
        getLabel().setSelected(true);
        getLabel().setFocus(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#showSelection()
     */
    protected void showSelection() {
        getLabel().setSelected(true);
        getLabel().setFocus(false);
    }
}