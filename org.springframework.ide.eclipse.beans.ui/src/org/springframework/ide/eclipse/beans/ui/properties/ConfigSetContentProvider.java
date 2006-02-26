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

package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.views.model.INode;
import org.springframework.ide.eclipse.beans.ui.views.model.ProjectNode;

/**
 * This content provider is used to display a tree of the beans config sets
 * (and their beans configs) within a given project. 
 *
 * @author Torsten Juergeleit
 * @see org.springframework.ide.eclipse.beans.ui.properties.ConfigSetDialog
 */
public class ConfigSetContentProvider implements ITreeContentProvider {

	private static final Object[] NO_CHILDREN = new Object[0];

	private ProjectNode project;

	public ConfigSetContentProvider(ProjectNode project) {
		this.project = project;
	}

	public void inputChanged(Viewer viewer, Object oldInput,
							 Object newInput) {
	}

	public Object[] getElements(Object obj) {
		return getChildren(project);
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ProjectNode) {
			List configSets = ((ProjectNode) parentElement).getConfigSets();
			return (ConfigSetNode[])
					   configSets.toArray(new ConfigSetNode[configSets.size()]);
		} else if (parentElement instanceof ConfigSetNode) {
			List configs = ((ConfigSetNode) parentElement).getConfigs();
			return (ConfigNode[])
								configs.toArray(new ConfigNode[configs.size()]);
		} else if (parentElement instanceof ConfigNode) {
			return new Object[0];
		}
		return NO_CHILDREN;
	}

	public Object getParent(Object element) {
		if (!(element instanceof ConfigSetNode)) {
			return ((INode) element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return (getChildren(element).length > 0);
	}

	public void dispose() {
	}
}
