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

package org.springframework.ide.eclipse.beans.ui.search.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.util.Assert;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.search.BeansSearchPlugin;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Torsten Juergeleit
 */
public class BeansClassQuery extends AbstractBeansQuery {

	private IType type;
	private boolean checkSubtypes;
	private Map subtypesCache;

	public BeansClassQuery(BeansSearchScope scope, IType type,
						   boolean checkSubtypes) {
		super(scope);
		Assert.isNotNull(type);
		this.type = type;
		this.checkSubtypes = checkSubtypes;
		this.subtypesCache = new HashMap();
	}

	public IType getType() {
		return type;
	}

	public String getLabel() {
		Object[] args = new Object[] { type.getFullyQualifiedName(),
									   getSearchScope().getDescription() };
		return BeansSearchPlugin.getFormattedMessage("ClassSearch.label",
													 args);
	}

	protected boolean doesMatch(IModelElement element,
								IProgressMonitor monitor) {
		if (element instanceof IBean) {
			IBean bean = (IBean) element;
			String beanClassName = bean.getClassName();
			if (beanClassName != null) {

				// Compare given class name with bean's one
				if (type.getFullyQualifiedName().equals(beanClassName)) {
					return true;
				} else if (checkSubtypes) {

					// Check if this class is a subclass of the one we are
					// looking for
					IProject project = bean.getElementResource().getProject();
					if (project != null) {
						IJavaProject javaProject = JavaCore.create(project);
						if (javaProject != null) {
							try {
								IType beanType = javaProject.findType(
																beanClassName);
								if (beanType != null &&
											isSubtype(javaProject, beanType)) {
									return true;
								}
							} catch (JavaModelException e) {
								// Do nothing
							}
						}
					}
				}
			}
		}
		return false;
	}

    private boolean isSubtype(IJavaProject project, IType subtype) {
		IType[] subtypes = getSubtypes(project);
		for (int i = 0; i < subtypes.length; i++) {
			IType type = subtypes[i];
			if (type.equals(subtype)) {
				return true;
			}
		}
		return false;
	}

    private IType[] getSubtypes(IJavaProject project)  {
		IType[] types = (IType[]) subtypesCache.get(project.getElementName());
		if (types == null) {
			try {
				ITypeHierarchy hierarchy = type.newTypeHierarchy(
													  project, null);
				types = hierarchy.getAllSubtypes(type);
				subtypesCache.put(project.getElementName(), types);
			} catch (JavaModelException e) {
				// Do nothing
			}
		}
		return types;
    }
}
