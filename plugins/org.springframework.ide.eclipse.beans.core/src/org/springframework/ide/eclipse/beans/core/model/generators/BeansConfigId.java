/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.beans.core.model.generators;

import org.eclipse.core.runtime.Assert;

/**
 * 
 * @author Andrew Eisenberg
 * @since 3.4.0
 */
public class BeansConfigId {
    
    public static BeansConfigId toConfigId(String idString) {
        int colonIndex = idString.indexOf(':');
        int nextColonIndex = idString.indexOf(colonIndex+1, ':');
        Assert.isLegal(colonIndex > 0, "Invalid bean config id: " + idString);
        Assert.isLegal(nextColonIndex > 0, "Invalid bean config id: " + idString);
        return new BeansConfigId(idString.substring(0, colonIndex), idString.substring(colonIndex+1, nextColonIndex), idString.substring(nextColonIndex)+1);
    }
    
    public final String name;
    public final String kind;
    public final String project;
    
    public BeansConfigId(String kind, String project, String name) {
        super();
        Assert.isNotNull(kind, "Bean kind must not be null");
        Assert.isNotNull(name, "Bean name must not be null");
        this.kind = kind;
        this.name = name;
        if (project == null && kind.equals("xml")) {
            int configNamePos = name.indexOf('/', (name.charAt(0) == '/' ? 1 : 0));
            if (configNamePos > 0) {
                project = name.substring(1, configNamePos);
            }
        }
        Assert.isNotNull(project, "Bean project must not be null");
        this.project = project;
    }
    
    @Override
    public String toString() {
        return kind + ":" + project + ":" + name;
    }
    
    public BeansConfigId newName(String newName) {
        return new BeansConfigId(this.kind, this.project, newName);
    }

    public BeansConfigId newProject(String newProject) {
        return new BeansConfigId(this.kind, newProject, this.name);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + kind.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + project.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BeansConfigId other = (BeansConfigId) obj;
        if (!kind.equals(other.kind)) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        if (!project.equals(other.project)) {
            return false;
        }
        return true;
    }
}
