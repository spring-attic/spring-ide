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
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;

/**
 * 
 */
public class DeleteStatePropertyCommand extends Command {

    /**
     * 
     */
    private IAttribute child;

    /**
     * 
     */
    private int index = -1;

    /**
     * 
     */
    private IAttributeEnabled parent;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        primExecute();
    }

    /**
     * 
     */
    protected void primExecute() {
        index = parent.getAttributes().indexOf(child);
        parent.removeAttribute(child);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        primExecute();
    }

    /**
     * 
     * 
     * @param a 
     */
    public void setChild(IAttribute a) {
        child = a;
    }

    /**
     * 
     * 
     * @param sa 
     */
    public void setParent(IAttributeEnabled sa) {
        parent = sa;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        parent.addAttribute(child, index);
    }
}