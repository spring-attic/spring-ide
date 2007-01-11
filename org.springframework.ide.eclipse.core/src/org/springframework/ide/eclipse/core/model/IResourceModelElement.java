/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.core.model;

import org.eclipse.core.resources.IResource;

/**
 * Common protocol for all model elements that map to a {@link IResource} in the
 * Eclipse workspace.
 * 
 * @author Torsten Juergeleit
 */
public interface IResourceModelElement extends IModelElement {

	/**
	 * Returns the nearest enclosing resource for this element.
	 */
	IResource getElementResource();

	/**
	 * Returns <code>true</code> if this element belongs to a ZIP file. In
	 * this case the element's resource specifies the ZIP file and the
	 * elements's name defines the ZIP file name plus the corresponding ZIP
	 * entry (delimited by {@link ZipEntryStorage#DELIMITER}).
	 */
	boolean isElementArchived();
}
