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
package org.springframework.ide.eclipse.ui.workingsets;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;

/**
 * Extension interface to be implemented to filter the Spring Explorer based on
 * the current selected {@link IWorkingSet}.
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IWorkingSetFilter {

	/**
	 * Verifies if the <code>element</code> is covered by the
	 * {@link IWorkingSet} given by the <code>elements</code>.
	 * @param elements the elements that are configured in the
	 * {@link IWorkingSet}.
	 * @param parentElement the parent element of the element under question.
	 * @param element the element under question
	 * @return true if the element is covered by the {@link IWorkingSet} and
	 * therefore should be displayed in the underlying view.
	 */
	boolean isInWorkingSet(IAdaptable[] elements, Object parentElement,
			Object element);

}
