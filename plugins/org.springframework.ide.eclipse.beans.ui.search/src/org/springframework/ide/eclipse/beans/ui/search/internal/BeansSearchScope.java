/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansSearchScope {

	private String description;
	private final IModelElement[] modelElements;

	/**
	 * Returns a workspace scope.
	 */
	public static BeansSearchScope newSearchScope() {
		return new BeansSearchScope(BeansSearchMessages.SearchScope_workspace,
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
			description = MessageUtils.format(
					BeansSearchMessages.SearchScope_selectedProjects, args);
		} else {
			description = BeansSearchMessages.SearchScope_selection;
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
		return new BeansSearchScope(MessageUtils.format(
				BeansSearchMessages.SearchScope_workingSets, args),
				convertToElements(workingSets));
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
		List<IModelElement> elements = new ArrayList<IModelElement>();
		for (IWorkingSet element : workingSets) {
			IAdaptable[] wsElements = element.getElements();
			for (IAdaptable element0 : wsElements) {
				addToList(element0, elements, true);
			}
		}
		return elements.toArray(new IModelElement[elements.size()]);
	}

	private static IModelElement[] convertToElements(ISelection selection,
			boolean isProjectsSelection) {
		List<IModelElement> elements = new ArrayList<IModelElement>();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Iterator selectedElements = structuredSelection.iterator();
			while (selectedElements.hasNext()) {
				Object selectedElement = selectedElements.next();
				if (selectedElement instanceof IAdaptable) {
					addToList((IAdaptable) selectedElement, elements,
							isProjectsSelection);
				}
			}
		}
		return elements.toArray(new IModelElement[elements.size()]);
	}

	private static void addToList(IAdaptable adaptable,
			List<IModelElement> elements, boolean isProjectsList) {
		IModelElement element = (IModelElement) adaptable
				.getAdapter(IModelElement.class);
		if (element == null) {
			IResource resource = (IResource) adaptable
					.getAdapter(IResource.class);
			if (resource instanceof IProject) {
				if (SpringCoreUtils.isSpringProject(resource)) {
					IBeansModel model = BeansCorePlugin.getModel();
					element = model.getProject((IProject) resource);
				}
			} else if (resource instanceof IFile) {
				IBeansModel model = BeansCorePlugin.getModel();
				element = model.getConfig(BeansConfigId.create((IFile) resource));
			}
		}
		if (element != null && !elements.contains(element)
				&& (!isProjectsList || (element instanceof IBeansProject))) {
			elements.add(element);
		}
	}

	@Override
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
