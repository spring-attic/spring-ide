/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

/** 
 * This interface is implemented by objects that visit model element trees.
 * <p> 
 * Usage:
 * <pre>
 * class Visitor implements IModelElementVisitor {
 *    public boolean visit(IModelElement element, IProgressMonitor) {
 *       // your code here
 *       return true;
 *    }
 * }
 * IModelElement root = ...;
 * root.accept(new Visitor(), monitor);
 * </pre>
 * </p> 
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see IModelElement#accept(IModelElementVisitor, IProgressMonitor)
 * @author Torsten Juergeleit
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
	public boolean visit(IModelElement element, IProgressMonitor monitor);
}
