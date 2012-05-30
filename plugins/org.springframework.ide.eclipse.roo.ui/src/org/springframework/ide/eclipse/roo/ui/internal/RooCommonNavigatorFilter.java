/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal;

import java.util.regex.Pattern;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * {@link ViewerFilter} responsible for filtering Roo ITDs from the project source tree.
 * @author Christian Dupuis
 * @since 2.5.0.
 */
public class RooCommonNavigatorFilter extends ViewerFilter {

	private static final Pattern ROO_AJ_PATTERN = Pattern.compile(".*_Roo_.*.aj");

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof ICompilationUnit) {
			try {
				ICompilationUnit cu = (ICompilationUnit) element;
				if (cu.getUnderlyingResource() != null) {
					return !ROO_AJ_PATTERN.matcher(cu.getUnderlyingResource().getName()).matches();
				}
			}
			catch (JavaModelException e) {
				// just ignore this here
			}
		}
		return true;
	}

}
