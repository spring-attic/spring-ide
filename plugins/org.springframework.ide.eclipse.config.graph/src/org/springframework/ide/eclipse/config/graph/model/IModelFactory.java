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
public interface IModelFactory {

	/**
	 * Generates a child model element from an XML input and adds it to the
	 * collection of children for the given parent model element. All XML inputs
	 * into this factory method will be scoped to one specific namespace.
	 * 
	 * @param list collection of model elements
	 * @param input an XML element to be used as the child model element's input
	 * @param parent the parent model element
	 */
	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent);

	/**
	 * Generates a child model element from an XML input and adds it to the
	 * collection of children for the given diagram model element. This method
	 * differs from {@link #getChildrenFromXml(List, IDOMElement, Activity)} in
	 * that it is intended for generating container parts at the root of the
	 * diagram which are connected to anchor parts that may be nested throughout
	 * diagram. All XML inputs into this factory method will be scoped to one
	 * specific namespace.
	 * 
	 * @param list collection of model elements
	 * @param input an XML element to be used as the child model element's input
	 * @param diagram the parent diagram
	 */
	public void getNestedChildrenFromXml(List<Activity> list, IDOMElement input, AbstractConfigGraphDiagram diagram);

}
