/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.navigator.filters;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IProfileAwareBeansComponent;
import org.springframework.util.CollectionUtils;

public class NonMatchingProfilesFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IProfileAwareBeansComponent) {
			IProfileAwareBeansComponent profileAwareBeansComponent = (IProfileAwareBeansComponent) element;
			IBeansConfigSet configSet = extractBeansConfigs((TreePath) parentElement);
			
			// Not part of a config set
			if (configSet == null) {
				return true;
			}
			
			// No profiles defined
			if (!profileAwareBeansComponent.hasProfiles()) {
				return true;
			}
			// Profiles defined
			if (CollectionUtils.containsAny(profileAwareBeansComponent.getProfiles(), configSet.getProfiles())) {
				return true;
			}
			
			return false;
		}
		return true;
	}
	
	private IBeansConfigSet extractBeansConfigs(TreePath path) {
		for (int i = 0; i < path.getSegmentCount(); i++) {
			Object obj = path.getSegment(i);
			if (obj instanceof IBeansConfigSet) {
				return ((IBeansConfigSet) obj);
			}
		}
		return null;
	}
}
