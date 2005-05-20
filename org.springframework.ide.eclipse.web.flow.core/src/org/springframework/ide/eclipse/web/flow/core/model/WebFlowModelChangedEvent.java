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

package org.springframework.ide.eclipse.web.flow.core.model;

import java.util.EventObject;

public class WebFlowModelChangedEvent extends EventObject {

    public static final int ADDED = 1;

    public static final int REMOVED = 2;

    public static final int CHANGED = 3;

    private int type;

    /**
     * Creates an new element changed event (based on a
     * <code>IBeansElementDelta</code>).
     * 
     * @param element
     *            the Beans element delta.
     * @param type
     *            the type of modification (ADDED, REMOVED, CHANGED) this event
     *            contains
     */
    public WebFlowModelChangedEvent(IWebFlowModelElement element, int type) {
        super(element);
        this.type = type;
    }

    /**
     * Returns the modified element.
     */
    public IWebFlowModelElement getElement() {
        return (IWebFlowModelElement) getSource();
    }

    /**
     * Returns the type of modification.
     */
    public int getType() {
        return type;
    }
}