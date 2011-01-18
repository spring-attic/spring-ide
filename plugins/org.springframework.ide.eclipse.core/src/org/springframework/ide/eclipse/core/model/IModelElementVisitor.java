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
package org.springframework.ide.eclipse.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface is implemented by objects that visit model element trees.
 * <p> 
 * Usage:
 * <pre>
 * class Visitor implements IModelElementVisitor {
 * public boolean visit(IModelElement element, IProgressMonitor) {
 * // your code here
 * return true;
 * }
 * }
 * IModelElement root = ...;
 * root.accept(new Visitor(), monitor);
 * </pre>
 * </p> 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @see IModelElement#accept(IModelElementVisitor,IProgressMonitor)
 */
public interface IModelElementVisitor {

	/** 
	 * Visits the given model element.
	 *
	 * @param element  the model element to visit
	 * @param monitor  the progress monitor used to give feedback on progress
	 * 					and to check for cancelation
	 * @return <code>true</code> if the elements's members should
	 *		be visited; <code>false</code> if they should be skipped
	 */
	boolean visit(IModelElement element, IProgressMonitor monitor);
}
