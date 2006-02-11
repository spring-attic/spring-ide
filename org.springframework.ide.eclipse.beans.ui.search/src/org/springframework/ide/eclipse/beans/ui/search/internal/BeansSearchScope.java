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
 *
 * Created on 22-Aug-2004
 */

package org.springframework.ide.eclipse.beans.ui.search.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.search.BeansSearchPlugin;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * 
 * @author Torsten Juergeleit
 */
public class BeansSearchScope {

	private String description;
	private final IModelElement[] modelElements;

	/**
	 * Returns a workspace scope.
	 */
	public static BeansSearchScope newSearchScope() {
		return new BeansSearchScope(
				  BeansSearchPlugin.getResourceString("SearchScope.workspace"),
				  new IModelElement[] { BeansCorePlugin.getModel() }); 
	}

	/**
	 * Returns a scope for the given <code>ISelection</code>.
	 * @param selection the selection to be contained
	 * @param isProjectsSelection <code>true</code> if the selection contains
	 * 							 <code>IProject</code>s
	 */
	public static BeansSearchScope newSearchScope(ISelection selection,
												 boolean isProjectsSelection) {
		IModelElement[] elements = convertToElements(selection,
													  isProjectsSelection); 
		String description;
		if (isProjectsSelection) {
			StringBuffer text = new StringBuffer();
			for (int i = 0; i < elements.length; i++) {
				IModelElement element = elements[i];
				text.append("'");
				text.append(element.getElementName());
				text.append("'");
				if (i < (elements.length - 1)) {
					text.append(", ");
				}
			}
			Object[] args = new Object[] { text.toString() };
			description = BeansSearchPlugin.getFormattedMessage(
										 "SearchScope.selectedProjects", args);
		} else {
			description = BeansSearchPlugin.getResourceString(
													  "SearchScope.selection");
		}
		return new BeansSearchScope(description, elements);
	}

	/**
	 * Returns a scope for the given working sets.
	 * @param description description of the scope
	 * @param workingSets the working sets to be contained
	 */
	public static BeansSearchScope newSearchScope(IWorkingSet[] workingSets) {
		StringBuffer text = new StringBuffer();
		for (int i = 0; i < workingSets.length; i++) {
			IWorkingSet ws = workingSets[i];
			text.append("'");
			text.append(ws.getName());
			text.append("'");
			if (i < (workingSets.length - 1)) {
				text.append(", ");
			}
		}
		Object[] args = new Object[] { text.toString() };
		return new BeansSearchScope(BeansSearchPlugin.getFormattedMessage(
			 "SearchScope.workingSets", args), convertToElements(workingSets));
	}

	private BeansSearchScope(String description, IModelElement[] elements) {
		Assert.isNotNull(description);
		this.description = description;
		this.modelElements = elements;
	}

	/**
	 * Returns the description of the scope.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the model elements of this scope.
	 */
	public IModelElement[] getModelElements() {
		return modelElements;
	}

	private static IModelElement[] convertToElements(IWorkingSet[] workingSets) {
		List elements = new ArrayList();
		for (int i= 0; i < workingSets.length; i++) {
			IAdaptable[] wsElements = workingSets[i].getElements();
			for (int j = 0; j < wsElements.length; j++) {
				addToList(wsElements[j], elements, true);
			}
		}
		return (IModelElement[]) elements.toArray(
										   new IModelElement[elements.size()]);
	}

	private static IModelElement[] convertToElements(ISelection selection,
												 boolean isProjectsSelection) {
		List elements = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)
																	 selection;
			Iterator selectedElements = structuredSelection.iterator();
			while (selectedElements.hasNext()) {
				Object selectedElement = (Object) selectedElements.next();
				if (selectedElement instanceof IAdaptable) {
					addToList((IAdaptable) selectedElement, elements,
							  isProjectsSelection);
				}
			}
		}
		return (IModelElement[]) elements.toArray(
										   new IModelElement[elements.size()]);
	}

	private static void addToList(IAdaptable adaptable, List elements,
								  boolean isProjectsList) {
		IModelElement element = (IModelElement)
									 adaptable.getAdapter(IModelElement.class);
		if (element == null) {
			IResource resource = (IResource)
										 adaptable.getAdapter(IResource.class);
			if (resource instanceof IProject) {
				if (SpringCoreUtils.isSpringProject(resource)) {
					IBeansModel model = BeansCorePlugin.getModel();
					element = model.getProject((IProject) resource);
				}
			} else if (resource instanceof IFile) {
				IBeansModel model = BeansCorePlugin.getModel();
				element = model.getConfig((IFile) resource);
			}
		}
		if (element != null && !elements.contains(element) &&
					 (!isProjectsList || (element instanceof IBeansProject))) {
			elements.add(element);
		}
	}

	public String toString() {
		StringBuffer text = new StringBuffer(description);
		text.append(" [");
		for (int i = 0; i < modelElements.length; i++) {
			IModelElement element = modelElements[i];
			text.append(element);
			if (i < (modelElements.length - 1)) {
				text.append(", ");
			}
		}
		text.append(']');
		return text.toString();
	}
}
