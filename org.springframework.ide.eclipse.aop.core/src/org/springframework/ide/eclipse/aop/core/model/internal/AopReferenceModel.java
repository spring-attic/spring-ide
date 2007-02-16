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
package org.springframework.ide.eclipse.aop.core.model.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReferenceModel;
import org.springframework.ide.eclipse.aop.core.model.IAopModelChangedListener;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.builder.AopReferenceModelBuilder;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;

public class AopReferenceModel implements IAopReferenceModel {

    private Map<IJavaProject, IAopProject> projects = new ConcurrentHashMap<IJavaProject, IAopProject>();

    private List<IAopModelChangedListener> listeners = new LinkedList<IAopModelChangedListener>();

    public void addProject(IJavaProject project, IAopProject aopProject) {
        this.projects.put(project, aopProject);
    }

    public IAopProject getProject(IJavaProject project) {
        if (this.projects.containsKey(project)) {
            return this.projects.get(project);
        }
        else {
            createModel(project);
            return this.projects.get(project);
        }
    }

    private void createModel(IJavaProject project) {
        Set<IFile> resourcesToBuild = AopReferenceModelUtils.getFilesToBuildFromBeansProject(project
                .getProject());
        AopReferenceModelBuilder.buildAopModel(project.getProject(), resourcesToBuild);
    }

    public IAopProject getProjectWithInitialization(IJavaProject project) {
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

    public List<IAopReference> getAllReferences(IJavaProject project) {
        List<IAopReference> refs = new ArrayList<IAopReference>();
        for (Map.Entry<IJavaProject, IAopProject> e : projects.entrySet()) {
            refs.addAll(e.getValue().getAllReferences());
        }
        return refs;
    }

    public boolean isAdvised(IJavaElement je) {
        IJavaProject project = je.getJavaProject();
        List<IAopReference> references = getAllReferences(project);

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
        IJavaProject project = je.getJavaProject();

        List<IAopReference> references = getAllReferences(project);
        for (IAopReference reference : references) {
            if (reference.getSource() != null && reference.getSource().equals(je)) {
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
