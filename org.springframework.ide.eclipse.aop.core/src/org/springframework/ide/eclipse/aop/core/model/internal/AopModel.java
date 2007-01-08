/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.ide.eclipse.aop.core.model.internal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.springframework.ide.eclipse.aop.core.model.IAopModelChangedListener;
import org.springframework.ide.eclipse.aop.core.model.IAopModel;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;

public class AopModel implements IAopModel {

    private Map<IProject, IAopProject> projects = new HashMap<IProject, IAopProject>();

    private List<IAopModelChangedListener> listeners = new LinkedList<IAopModelChangedListener>();

    public void addProject(IProject project, IAopProject aopProject) {
        this.projects.put(project, aopProject);
    }

    public IAopProject getProject(IProject project) {
        if (this.projects.containsKey(project)) {
            return this.projects.get(project);
        }
        else {
            IAopProject aopProject = new AopProject(project);
            addProject(project, aopProject);
            return aopProject;
        }
    }

    public List<IAopProject> getProjects() {
        return null;
    }

    public boolean isAdvised(IJavaElement je) {
        IProject project = je.getJavaProject().getProject();

        IAopProject aopProject = getProject(project);
        List<IAopReference> references = aopProject.getAllReferences();

        for (IAopReference reference : references) {
            if (reference.getTarget().equals(je)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdvice(IJavaElement je) {
        return getAdviceDefinition(je).size() > 0;
    }

    public void registerAopModelChangedListener(IAopModelChangedListener listener) {
        this.listeners.add(listener);
    }

    public void unregisterAopModelChangedListener(IAopModelChangedListener listener) {
        this.listeners.remove(listener);
    }

    public List<IAopReference> getAdviceDefinition(IJavaElement je) {
        List<IAopReference> advices = new LinkedList<IAopReference>();
        IProject project = je.getJavaProject().getProject();

        IAopProject aopProject = getProject(project);
        List<IAopReference> references = aopProject.getAllReferences();

        for (IAopReference reference : references) {
            if (reference.getSource().equals(je)) {
                advices.add(reference);
            }
        }
        return advices;
    }

	public void fireModelChanged() {
		for (IAopModelChangedListener listener : listeners) {
			listener.changed();
		}
	}

}
