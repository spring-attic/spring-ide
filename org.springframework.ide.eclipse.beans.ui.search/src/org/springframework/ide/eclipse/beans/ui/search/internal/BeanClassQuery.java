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
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This implementation of <code>ISearchQuery</code> looks for all
 * <code>IBean</code>s which bean class match a given class.
 *
 * @see org.eclipse.search.ui.ISearchQuery
 * @see org.springframework.ide.eclipse.beans.core.model.IBean
 *
 * @author Torsten Juergeleit
 */
public class BeanClassQuery extends AbstractBeansQuery {

	private boolean includeSubtypes;
	private Map subtypesCache;

	public BeanClassQuery(BeansSearchScope scope, String pattern,
						  boolean isCaseSensitive, boolean isRegexSearch,
						  boolean includeSubtypes) {
		super(scope, pattern, isCaseSensitive, isRegexSearch);
		this.includeSubtypes = includeSubtypes;
		this.subtypesCache = new HashMap();
	}

	public String getLabel() {
		Object[] args = new Object[] { getPattern(),
									   getScope().getDescription() };
		return MessageUtils.format(
						BeansSearchMessages.SearchQuery_searchFor_class, args);
	}

	protected boolean doesMatch(IModelElement element, Pattern pattern,
								IProgressMonitor monitor) {
		if (element instanceof IBean) {
			IBean bean = (IBean) element;
			String beanClassName = bean.getClassName();
			if (beanClassName != null) {

				// Compare given class name with bean's one
				if (pattern.matcher(beanClassName).matches()) {
					return true;
//				} else if (includeSubtypes) {
//
//					// Check if this class is a subclass of the one we are
//					// looking for
//					IProject project = bean.getElementResource().getProject();
//					if (project != null) {
//						IJavaProject javaProject = JavaCore.create(project);
//						if (javaProject != null) {
//							try {
//								IType beanType = javaProject.findType(
//																beanClassName);
//								if (beanType != null &&
//											isSubtype(javaProject, beanType)) {
//									return true;
//								}
//							} catch (JavaModelException e) {
//								// Do nothing
//							}
//						}
//					}
				}
			}
		}
		return false;
	}

//	private boolean isSubtype(IJavaProject project, IType subtype) {
//		IType[] subtypes = getSubtypes(project);
//		for (int i = 0; i < subtypes.length; i++) {
//			IType type = subtypes[i];
//			if (type.equals(subtype)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private IType[] getSubtypes(IJavaProject project)  {
//		IType[] types = (IType[]) subtypesCache.get(project.getElementName());
//		if (types == null) {
//			try {
//				ITypeHierarchy hierarchy = beanClass.newTypeHierarchy(
//													  project, null);
//				types = hierarchy.getAllSubtypes(beanClass);
//				subtypesCache.put(project.getElementName(), types);
//			} catch (JavaModelException e) {
//				// Do nothing
//			}
//		}
//		return types;
//	}
}
