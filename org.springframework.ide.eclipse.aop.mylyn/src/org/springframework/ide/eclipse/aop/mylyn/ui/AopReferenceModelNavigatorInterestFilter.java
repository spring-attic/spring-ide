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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCorePlugin;
import org.eclipse.mylyn.context.core.IImplicitlyIntersting;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.ui.InterestFilter;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.swt.widgets.Tree;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IReferenceNode;

/**
 * Extension of Mylyn's {@link InterestFilter} that understands the Spring
 * Explorer root node {@link ISpringProject}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelNavigatorInterestFilter extends InterestFilter {

	private Object temporarilyUnfiltered = null;

	public AopReferenceModelNavigatorInterestFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parent, Object object) {
		try {
			if (!(viewer instanceof StructuredViewer)
					|| !containsMylarInterestFilter((StructuredViewer) viewer)) {
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
				AbstractContextStructureBridge bridge = ContextCorePlugin
						.getDefault().getStructureBridge(
								object.getReferenceParticipant());
				if (bridge != null) {
					String handle = bridge.getHandleIdentifier(object.getReferenceParticipant());
					IInteractionElement element = ContextCorePlugin
							.getContextManager().getElement(handle);
					if (element != null && isInteresting(element)) {
						return true;
					}
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
		// TODO CD add logic here
		return object instanceof IReferenceNode;
	}

	protected boolean isInteresting(IInteractionElement element) {
		if (element.getInterest().isPredicted()) {
			return false;
		}
		else {
			return element.getInterest().getValue() > InteractionContextManager
					.getScalingFactors().getInteresting();
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

	protected boolean containsMylarInterestFilter(StructuredViewer viewer) {
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
