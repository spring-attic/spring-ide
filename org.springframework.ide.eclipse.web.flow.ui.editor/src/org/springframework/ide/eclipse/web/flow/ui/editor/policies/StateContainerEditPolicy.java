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

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.ActionOrphanChildCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.AttributeMapperOrphanChildCommand;
import org.springframework.ide.eclipse.web.flow.ui.editor.commands.IfOrphanChildCommand;

public class StateContainerEditPolicy extends ContainerEditPolicy {

    protected Command getCreateCommand(CreateRequest request) {
        return null;
    }

    protected Command getOrphanChildrenCommand(GroupRequest request) {
        List parts = request.getEditParts();
        CompoundCommand result = new CompoundCommand();
        for (int i = 0; i < parts.size(); i++) {
            EditPart part = (EditPart) parts.get(i);
            if (part.getModel() instanceof IAction) {
                ActionOrphanChildCommand orphan = new ActionOrphanChildCommand();
                orphan.setChild((IAction) ((EditPart) parts.get(i)).getModel());
                orphan.setParent((IActionState) getHost().getModel());
                result.add(orphan);
            }
            else if (part.getModel() instanceof IAttributeMapper) {
                AttributeMapperOrphanChildCommand orphan = new AttributeMapperOrphanChildCommand();
                orphan.setChild((IAttributeMapper) ((EditPart) parts.get(i))
                        .getModel());
                orphan.setParent((ISubFlowState) getHost().getModel());
                result.add(orphan);
            }
            else if (part.getModel() instanceof IIf) {
                IfOrphanChildCommand orphan = new IfOrphanChildCommand();
                orphan.setChild((IIf) ((EditPart) parts.get(i)).getModel());
                orphan.setParent((IDecisionState) getHost().getModel());
                result.add(orphan);
            }
        }
        return result.unwrap();
    }
}