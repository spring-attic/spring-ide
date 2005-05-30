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

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.EditPropertiesCommand;

public class WebFlowEditPolicy extends RootComponentEditPolicy {

    public Command getCommand(Request request) {
        if (EditPropertiesAction.EDITPROPERTIES_REQUEST.equals(request
                .getType())) {
            return getEditPropertiesCommand();
        }
        return super.getCommand(request);
    }

    protected Command getEditPropertiesCommand() {
        EditPropertiesCommand command = new EditPropertiesCommand();
        command.setChild((ICloneableModelElement) getHost().getModel());
        return command;
    }
}