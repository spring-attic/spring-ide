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