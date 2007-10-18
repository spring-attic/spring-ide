/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.mylyn.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCorePlugin;
import org.eclipse.mylyn.context.core.IImplicitlyIntersting;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.ui.InterestFilter;
import org.eclipse.swt.widgets.Tree;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IReferenceNode;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;

/**
 * Extension to the Mylyn {@link InterestFilter} that is used to filter the
 * Spring Aop cross references view.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class AopReferenceModelNavigatorInterestFilter extends InterestFilter {

	private Object temporarilyUnfiltered = null;

	@Override
	public boolean select(Viewer viewer, Object parent, Object object) {
		try {
			if (!(viewer instanceof StructuredViewer)
					|| !containsMylynInterestFilter((StructuredViewer) viewer)) {
				return true;
			}
			if (isTemporarilyUnfiltered(parent)) {
				return true;
			}
			else if (temporarilyUnfiltered instanceof Tree
					&& isRootElement(object)) {
				return true;
			}

			IInteractionElement element = null;
			if (object instanceof IImplicitlyIntersting) {
				return true;
			}
			else if (object instanceof IInteractionElement) {
				element = (IInteractionElement) object;
			}
			else if (object instanceof IReferenceNode) {
				return isInterestingReferenceParticipant(((IReferenceNode) object));
			}
			if (element != null) {
				return isInteresting(element);
			}
		}
		catch (Throwable t) {
		}
		return false;
	}

	private boolean isInterestingReferenceParticipant(IReferenceNode object) {
		if (object != null) {
			if (object.getReferenceParticipant() != null) {
				Object element = object.getReferenceParticipant();
				if (element instanceof IAspectDefinition) {
					IAspectDefinition def = (IAspectDefinition) element;
					element = BeansModelUtils.getMostSpecificModelElement(def
							.getAspectStartLineNumber(), def
							.getAspectEndLineNumber(), (IFile) def
							.getResource(), null);
				}
				AbstractContextStructureBridge bridge = ContextCorePlugin
						.getDefault().getStructureBridge(element);
				if (bridge != null) {
					String handle = bridge.getHandleIdentifier(element);
					IInteractionElement interestElement = ContextCorePlugin
							.getContextManager().getElement(handle);
					if (element != null && isInteresting(interestElement)) {
						return true;
					}
					// TODO CD uncomment this if *really* only interested
					// elements
					// should be displayed
					/*
					 * else { return false; }
					 */
				}
			}
			if (object.getChildren() != null && object.getChildren().length > 0) {
				for (Object child : object.getChildren()) {
					if (child instanceof IReferenceNode) {
						if (isInterestingReferenceParticipant((IReferenceNode) child)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	protected boolean isRootElement(Object object) {
		return object instanceof IReferenceNode;
	}

	protected boolean isInteresting(IInteractionElement element) {
		if (element.getInterest().isPredicted()) {
			return false;
		}
		else {
			return element.getInterest().isInteresting();
		}
	}

	private boolean isTemporarilyUnfiltered(Object parent) {
		if (parent instanceof TreePath) {
			TreePath treePath = (TreePath) parent;
			parent = treePath.getLastSegment();
		}
		return temporarilyUnfiltered != null
				&& temporarilyUnfiltered.equals(parent);
	}

	protected boolean containsMylynInterestFilter(StructuredViewer viewer) {
		for (ViewerFilter filter : viewer.getFilters()) {
			if (filter instanceof InterestFilter) {
				return true;
			}
		}
		return false;
	}

	public void setTemporarilyUnfiltered(Object temprarilyUnfiltered) {
		this.temporarilyUnfiltered = temprarilyUnfiltered;
	}

	/**
	 * @return true if there was an unfiltered node
	 */
	public boolean resetTemporarilyUnfiltered() {
		if (temporarilyUnfiltered != null) {
			this.temporarilyUnfiltered = null;
			return true;
		}
		else {
			return false;
		}
	}

	public Object getTemporarilyUnfiltered() {
		return temporarilyUnfiltered;
	}
}
