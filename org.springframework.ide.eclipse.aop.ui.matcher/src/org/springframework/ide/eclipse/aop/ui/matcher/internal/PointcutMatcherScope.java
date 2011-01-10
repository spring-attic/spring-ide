/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.matcher.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.Assert;

/**
 * Class that serves as scope for matching pointcut expressions.
 * <p>
 * Using the various factory methods {@link #newSearchScope()},
 * {@link #newSearchScope(IWorkingSet[])} and
 * {@link #newSearchScope(ISelection,boolean)} the scope can be created. During
 * creation resource and working set selections will be translated to
 * {@link IBeansConfig}s.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class PointcutMatcherScope {

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
			}
			else if (resource instanceof IFile) {
				IBeansModel model = BeansCorePlugin.getModel();
				element = model.getConfig((IFile) resource);
			}
			else if (adaptable instanceof IBeansConfigSet) {
				for (IBeansConfig config : ((IBeansConfigSet) adaptable)
						.getConfigs()) {
					elements.add(config);
				}
			}
		}
		if (element != null && !elements.contains(element)
				&& (!isProjectsList || (element instanceof IBeansProject))) {
			elements.add(element);
		}
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

	private static IModelElement[] convertToElements(IWorkingSet[] workingSets) {
		List<IModelElement> elements = new ArrayList<IModelElement>();
		for (IWorkingSet element : workingSets) {
			IAdaptable[] wsElements = element.getElements();
			for (IAdaptable element0 : wsElements) {
				addToList(element0, elements, false);
			}
		}
		return elements.toArray(new IModelElement[elements.size()]);
	}

	public static PointcutMatcherScope newSearchScope() {
		return new PointcutMatcherScope(
				PointcutMatcherMessages.MatcherScope_workspace,
				new IModelElement[] { BeansCorePlugin.getModel() });
	}

	public static PointcutMatcherScope newSearchScope(ISelection selection,
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
			description = MessageUtils
					.format(
							PointcutMatcherMessages.MatcherScope_selectedProjects,
							args);
		}
		else {
			description = PointcutMatcherMessages.MatcherScope_selection;
		}
		return new PointcutMatcherScope(description, elements);
	}

	public static PointcutMatcherScope newSearchScope(IWorkingSet[] workingSets) {
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
		return new PointcutMatcherScope(MessageUtils.format(
				PointcutMatcherMessages.MatcherScope_workingSets, args),
				convertToElements(workingSets));
	}

	private String description;

	private final IModelElement[] modelElements;

	private PointcutMatcherScope(String description, IModelElement[] elements) {
		Assert.notNull(description);
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
