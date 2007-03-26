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
package org.springframework.ide.eclipse.webflow.ui.graph.policies;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;

/**
 * 
 */
public class StateTreeEditPolicy extends AbstractEditPolicy {

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getCommand(org.eclipse.gef.Request)
     */
    public Command getCommand(Request req) {
        if (REQ_MOVE.equals(req.getType()))
            return getMoveCommand(req);
        return null;
    }

    /**
     * 
     * 
     * @param req 
     * 
     * @return 
     */
    protected Command getMoveCommand(Request req) {
        EditPart parent = getHost().getParent();
        if (parent != null) {
            req.setType(REQ_MOVE_CHILDREN);
            Command cmd = parent.getCommand(req);
            req.setType(REQ_MOVE);
            return cmd;
        }
        else {
            return UnexecutableCommand.INSTANCE;
        }
    }

}
