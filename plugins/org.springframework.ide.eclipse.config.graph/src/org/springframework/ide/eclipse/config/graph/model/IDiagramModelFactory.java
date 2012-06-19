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
package org.springframework.ide.eclipse.config.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;

/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public interface IDiagramModelFactory extends IModelFactory {

	/**
	 * Generates a child model element from an XML input and adds it to the
	 * collection of children for the given parent model element. This method
	 * differs from {@link #getChildrenFromXml(List, IDOMElement, Activity)} in
	 * that it is intended for generating a model element that has no known
	 * behaviour. Additionally, the XML inputs into this factory method will not
	 * be scoped to a specific namespace. Implementors must ensure that the
	 * given XML input has not already generated an existing model element.
	 * 
	 * @param list collection of model elements
	 * @param input an XML element to be used as the child model element's input
	 * @param parent the parent model element
	 */
	public void getGenericChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent);

}
