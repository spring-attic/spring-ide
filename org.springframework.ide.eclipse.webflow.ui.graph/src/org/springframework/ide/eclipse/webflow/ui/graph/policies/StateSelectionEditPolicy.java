/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.policies;

import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.StateLabel;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.IfPart;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.StatePart;

/**
 * @author Christian Dupuis
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
