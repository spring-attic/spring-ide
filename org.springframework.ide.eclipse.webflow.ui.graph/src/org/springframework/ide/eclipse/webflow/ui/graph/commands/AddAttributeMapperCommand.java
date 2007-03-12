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

package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import org.eclipse.gef.commands.Command;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;

/**
 * 
 */
public class AddAttributeMapperCommand extends Command {

    /**
     * 
     */
    private IAttributeMapper child;

    /**
     * 
     */
    private ISubflowState parent;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        parent.setAttributeMapper(child);
    }

    /**
     * 
     * 
     * @return 
     */
    public ISubflowState getParent() {
        return parent;
    }

    /**
     * 
     * 
     * @param newChild 
     */
    public void setChild(IAttributeMapper newChild) {
        child = newChild;
    }

    /**
     * 
     * 
     * @param newParent 
     */
    public void setParent(ISubflowState newParent) {
        parent = newParent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        parent.removeAttributeMapper();
    }

}