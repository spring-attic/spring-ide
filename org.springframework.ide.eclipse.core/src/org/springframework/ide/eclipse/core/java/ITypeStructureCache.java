/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Interface to be implemented by clients that can cache instances of {@link TypeStructure}.
 * @author Christian Dupuis
 * @since 2.2.1
 */
public interface ITypeStructureCache {

	/** Flag indicating that changes to class and/or method-level annotations are of interest */
	int FLAG_ANNOTATION = 1 << 3;

	/** Flag indicating that changes to class and/or method-level annotation values are of interest */
	int FLAG_ANNOTATION_VALUE = 1 << 4;
	
	/** Flag indicating that changes to tagbits and standard annotations are of interest */
	int FLAG_TAB_BITS = 1 << 5;

	/**
	 * Returns <code>true</code> if the java types represented by the given <code>resource</code>
	 * have structural changes.
	 * <p>
	 * The parameter <code>flags</code> can be used to specify changes of interest.
	 * @param resource the changed java type(s)
	 * @param flags flags indicate changes of interest
	 */
	boolean hasStructuralChanges(IResource resource, int flags);

	/**
	 * Returns <code>true</code> if the given <code>project</code> has recorded type structures.
	 * @param project the project to check
	 */
	boolean hasRecordedTypeStructures(IProject project);
	
	/**
	 * Recored type structures for the given <code>project</code> and resources.
	 * @param project the project to record changes for
	 * @param resources the changed resources to process
	 */
	void recordTypeStructures(IProject project, IResource... resources);
	
	/**
	 * Clear recored type structures for the given <code>project</code>.
	 * @param project the project to clear
	 */
	void clearStateForProject(IProject project);

}
