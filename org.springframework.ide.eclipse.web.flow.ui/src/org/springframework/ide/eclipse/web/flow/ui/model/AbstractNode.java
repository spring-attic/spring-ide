/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.ui.model;

public abstract class AbstractNode implements INode {

    private INode parent;

    private String name;

    private int startLine;

    /**
     * Creates a new node with the given name
     * 
     * @param name the new node's name
     */
    public AbstractNode(String name) {
        this(null, name);
    }

    /**
     * Creates a new node with the given parent and the given name
     * 
     * @param parent the new node's parent node
     * @param name the new node's name
     */
    public AbstractNode(INode parent, String name) {
        this.parent = parent;
        this.name = name;

        startLine = -1;
    }

    /**
     * Returns this node's parent or <code>null</code> if none.
     * 
     * @return this node's parent node
     */
    public INode getParent() {
        return parent;
    }

    /**
     * Sets this node's parent node to the given node
     * 
     * @param parent the parent node
     */
    public void setParent(INode parent) {
        this.parent = parent;
    }

    /**
     * Sets this node's name to the given name
     * 
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setStartLine(int line) {
        startLine = line;
    }

    public int getStartLine() {
        return startLine;
    }

    public void propertyChanged(INode node, int propertyId) {
        AbstractNode parent = (AbstractNode) getParent();
        if (parent != null) {
            parent.propertyChanged(node, propertyId);
        }
    }

    public void refreshViewer() {
        if (parent != null && parent instanceof AbstractNode) {
            ((AbstractNode) parent).refreshViewer();
        }
    }

    public Object getAdapter(Class adapter) {
        return null;
    }

    public String toString() {
        return name;
    }
}
