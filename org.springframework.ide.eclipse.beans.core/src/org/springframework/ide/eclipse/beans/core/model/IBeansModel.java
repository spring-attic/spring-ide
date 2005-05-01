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

package org.springframework.ide.eclipse.beans.core.model;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.core.model.IModel;

/**
 * The <code>IBeansModel</code> manages instances of <code>IBeansProject</code>s.
 * <code>IBeansModelChangedListener</code>s register with the <code>IBeansModel</code>,
 * and receive <code>BeansModelChangedEvent</code>s for all changes.
 * <p>
 * The single instance of <code>IBeansModel</code> is available from
 * the static method <code>BeansCorePlugin.getModel()</code>.
 */
public interface IBeansModel extends IModel {

	IBeansProject getProject(IProject project);

	IBeansProject getProject(String name);

	/**
	 * Returns a collection of all <code>IBeansProject</code>s defined in this
	 * model.
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansProject
	 */
	Collection getProjects();

	IBeansConfig getConfig(IFile configFile);

	/**
	 * Returns a list of all <code>IBeanConfig</code>s which contain a bean with
	 * given bean class.
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfig
	 */
	Collection getConfigs(String className);
}
