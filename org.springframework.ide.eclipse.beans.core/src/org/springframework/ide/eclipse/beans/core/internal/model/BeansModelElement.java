/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;

public abstract class BeansModelElement implements IBeansModelElement {

	private IBeansModelElement parent;
	private String name;
	private int startLine;
    private int endLine;

	protected BeansModelElement(IBeansModelElement parent, String name) {
		this.parent = parent;
		this.name = name;
		this.startLine = -1;
	}

	public final void setElementParent(IBeansModelElement parent) {
		this.parent = parent;
	}

	public final IBeansModelElement getElementParent() {
		return parent;
	}

	public final void setElementName(String name) {
		this.name = name;
	}

	public final String getElementName() {
		return this.name;
	}

	public final void setElementStartLine(int line) {
		this.startLine = line;
	}

	public final int getElementStartLine() {
		return this.startLine;
	}

    /**
     * @param endLine
     */
    public void setElementEndLine(int endLine)
    {
       	this.endLine = endLine;
    }

    /*
     * @see BeansModelElement.getElementEndLine
     */
	public final int getElementEndLine() {
	    return this.endLine;
	}
}
