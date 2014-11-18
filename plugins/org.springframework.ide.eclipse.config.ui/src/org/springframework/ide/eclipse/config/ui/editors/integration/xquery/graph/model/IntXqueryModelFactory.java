/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.xquery.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntXquerySchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractIntegrationModelFactory;

@SuppressWarnings("restriction")
public class IntXqueryModelFactory extends AbstractIntegrationModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(IntXquerySchemaConstants.ELEM_XQUERY_ROUTER)) {
			XqueryRouterModelElement adapter = new XqueryRouterModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntXquerySchemaConstants.ELEM_XQUERY_TRANSFORMER)) {
			XqueryTransformerModelElement adapter = new XqueryTransformerModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
	}

}
