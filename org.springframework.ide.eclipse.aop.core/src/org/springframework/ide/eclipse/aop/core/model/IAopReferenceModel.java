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

package org.springframework.ide.eclipse.aop.core.model;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

public interface IAopReferenceModel {

	void addProject(IJavaProject project, IAopProject aopProject);

	void fireModelChanged();

	List<IAopReference> getAdviceDefinition(IJavaElement je);

	public List<IAopReference> getAllReferences(IJavaProject project);

	IAopProject getProject(IJavaProject project);

	List<IAopProject> getProjects();

	boolean isAdvice(IJavaElement je);

	boolean isAdvised(IJavaElement je);

	void registerAopModelChangedListener(IAopModelChangedListener listener);

	void unregisterAopModelChangedListener(IAopModelChangedListener listener);

}
