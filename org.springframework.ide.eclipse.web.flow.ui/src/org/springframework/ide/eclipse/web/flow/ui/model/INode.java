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

import org.eclipse.core.runtime.IAdaptable;

public interface INode extends IAdaptable {

    /**
     * Returns this node's parent or <code>null</code> if none.
     * 
     * @return this node's parent node
     */
    public INode getParent();

    /**
     * Returns this node's name or <code>null</code> if none.
     * 
     * @return this node's name or <code>null</code> if the name attribute is
     * 		   optional for this node
     */
    public String getName();

    /**
     * Returns this node's location within config file.
     *
     * @return this node's location within config file or -1 if location is
     * 		   unknown 
     */
    public int getStartLine();
}
