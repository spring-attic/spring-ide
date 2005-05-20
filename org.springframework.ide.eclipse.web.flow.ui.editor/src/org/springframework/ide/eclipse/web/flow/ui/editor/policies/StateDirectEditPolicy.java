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

import org.eclipse.draw2d.Label;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.RenameStateCommand;

public class StateDirectEditPolicy extends DirectEditPolicy {

    protected Command getDirectEditCommand(DirectEditRequest request) {
        RenameStateCommand cmd = new RenameStateCommand();
        cmd.setSource((IState) getHost().getModel());
        cmd.setOldId(((IState) getHost().getModel()).getId());
        cmd.setId((String) request.getCellEditor().getValue());
        return cmd;
    }

    protected void showCurrentEditValue(DirectEditRequest request) {
        String value = (String) request.getCellEditor().getValue();
        ((Label) getHostFigure()).setText(value);
    }
}