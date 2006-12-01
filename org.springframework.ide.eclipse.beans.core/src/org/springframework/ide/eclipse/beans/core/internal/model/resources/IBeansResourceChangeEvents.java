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

package org.springframework.ide.eclipse.beans.core.internal.model.resources;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;

/**
 * Defines callbacks for the <code>BeansResourceChangeListener</code>.
 * @see BeansResourceChangeListener
 * @author Torsten Juergeleit
 */
public interface IBeansResourceChangeEvents {

	boolean isSpringProject(IProject project, int eventType);

	void springNatureAdded(IProject project, int eventType);

	void springNatureRemoved(IProject project, int eventType);

	void projectAdded(IProject project, int eventType);

	void projectOpened(IProject project, int eventType);

	void projectClosed(IProject project, int eventType);

	void projectDeleted(IProject project, int eventType);

	void projectDescriptionChanged(IFile file, int eventType);

	void configAdded(IFile file, int eventType);

	void configChanged(IFile file, int eventType);

	void configRemoved(IFile file, int eventType);

	void beanClassChanged(String className, Set<IBeansConfig> configs,
			int eventType);
}
