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

package org.springframework.ide.eclipse.beans.core.internal.model.resources;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * Defines callbacks for the <code>SpringResourceChangeListener</code>.
 * 
 * @see SpringResourceChangeListener
 */
public interface IBeansResourceChangeEvents {

	boolean isSpringProject(IProject project);

	void springNatureAdded(IProject project);

	void springNatureRemoved(IProject project);

	void projectAdded(IProject project);

	void projectOpened(IProject project);

	void projectClosed(IProject project);

	void projectDeleted(IProject project);

	void projectDescriptionChanged(IFile file);

	void configAdded(IFile file);

	void configChanged(IFile file);

	void configRemoved(IFile file);

	void beanClassChanged(String className, Collection configs);
}
