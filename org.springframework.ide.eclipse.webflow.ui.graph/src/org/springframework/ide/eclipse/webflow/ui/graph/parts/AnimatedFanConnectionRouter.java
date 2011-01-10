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
package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.FanRouter;

/**
 * @author Christian Dupuis
 */
public class AnimatedFanConnectionRouter extends FanRouter {

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.AutomaticRouter#route(org.eclipse.draw2d.Connection)
     */
    public void route(Connection conn) {
        GraphAnimation.recordInitialState(conn);
        if (!GraphAnimation.playbackState(conn))
            super.route(conn);
    }
}
