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
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;

public class AopProject implements IAopProject {

    private List<IAopReference> references = new ArrayList<IAopReference>();

    private IJavaProject project;

    public AopProject(IJavaProject project) {
        this.project = project;
    }

    public void addAopReference(IAopReference reference) {
        this.references.add(reference);
    }

    public List<IAopReference> getAllReferences() {
        return this.references;
    }

    public IJavaProject getProject() {
        return this.project;
    }

    public void clearReferencesForResource(IResource resource) {
        List<IAopReference> toRemove = new ArrayList<IAopReference>();
        for (IAopReference reference : this.references) {
            if (reference.getDefinition().getResource().equals(resource)) {
                toRemove.add(reference);
            }
        }
        this.references.removeAll(toRemove);
    }

    public List<IAopReference> getReferencesForResource(IResource resource) {
        List<IAopReference> list = new ArrayList<IAopReference>();
        for (IAopReference reference : this.references) {
            if (reference.getResource().equals(resource)
                    || reference.getDefinition().getResource().equals(resource)) {
                list.add(reference);
            }
        }
        return list;
    }
}
