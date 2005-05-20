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

package org.springframework.ide.eclipse.web.flow.ui.editor.policies;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.swt.graphics.Color;

public class StateContainerHighlightEditPolicy extends GraphicalEditPolicy {

    private static Color highLightColor = new Color(null, 200, 200, 240);

    private Color revertColor;

    public void eraseTargetFeedback(Request request) {
        if (revertColor != null) {
            setContainerBackground(revertColor);
            revertColor = null;
        }
    }

    private Color getContainerBackground() {
        return getContainerFigure().getBackgroundColor();
    }

    private IFigure getContainerFigure() {
        return ((GraphicalEditPart) getHost()).getFigure();
    }

    public EditPart getTargetEditPart(Request request) {
        return request.getType().equals(RequestConstants.REQ_SELECTION_HOVER) ? getHost()
                : null;
    }

    private void setContainerBackground(Color c) {
        getContainerFigure().setBackgroundColor(c);
    }

    protected void showHighlight() {
        if (revertColor == null) {
            revertColor = getContainerBackground();
            setContainerBackground(highLightColor);
        }
    }

    public void showTargetFeedback(Request request) {
        if (request.getType().equals(RequestConstants.REQ_CREATE)
                || request.getType().equals(RequestConstants.REQ_ADD))
            showHighlight();
    }
}