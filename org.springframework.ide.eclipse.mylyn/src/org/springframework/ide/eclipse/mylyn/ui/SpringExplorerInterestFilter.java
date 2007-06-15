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
package org.springframework.ide.eclipse.mylyn.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
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
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * Copy of Mylyn's {@link InterestFilter} that understands the Spring Explorer
 * root node {@link ISpringProject}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class SpringExplorerInterestFilter extends InterestFilter {

	private Object temporarilyUnfiltered = null;

	public SpringExplorerInterestFilter() {
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
			else if (temporarilyUnfiltered instanceof Tree) {
				// HACK: should also work for trees without project as root
				if (object instanceof ISpringProject) {
					return true;
				}
			}

			IInteractionElement element = null;
			if (object instanceof IImplicitlyIntersting) {
				return true;
			}
			else if (object instanceof IInteractionElement) {
				element = (IInteractionElement) object;
			}
			else {
				AbstractContextStructureBridge bridge = ContextCorePlugin
						.getDefault().getStructureBridge(object);
				if (bridge.getContentType() == null) {
					// try to resolve the resource
					if (object instanceof IAdaptable) {
						Object adapted = ((IAdaptable) object)
								.getAdapter(IResource.class);
						if (adapted instanceof IResource) {
							object = adapted;
						}
						bridge = ContextCorePlugin.getDefault()
								.getStructureBridge(object);
					}
					else {
						return false;
					}
				}
				if (!bridge.canFilter(object)) {
					return true;
				}

				if (!object.getClass().getName().equals(
						Object.class.getCanonicalName())) {
					String handle = bridge.getHandleIdentifier(object);
					element = ContextCorePlugin.getContextManager().getElement(
							handle);
				}
				else {
					return true;
				}
			}
			if (element != null) {
				return isInteresting(element);
			}
		}
		catch (Throwable t) {
		}
		return false;
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
			if (filter instanceof SpringExplorerInterestFilter) {
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
