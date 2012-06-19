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
package org.springframework.ide.eclipse.config.ui.editors.integration.stream.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntStreamSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractIntegrationModelFactory;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class IntStreamModelFactory extends AbstractIntegrationModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(IntStreamSchemaConstants.ELEM_STDERR_CHANNEL_ADAPTER)) {
			StderrChannelAdapterModelElement adapter = new StderrChannelAdapterModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntStreamSchemaConstants.ELEM_STDIN_CHANNEL_ADAPTER)) {
			StdinChannelAdapterModelElement adapter = new StdinChannelAdapterModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntStreamSchemaConstants.ELEM_STDOUT_CHANNEL_ADAPTER)) {
			StdoutChannelAdapterModelElement adapter = new StdoutChannelAdapterModelElement(input, parent.getDiagram());
			list.add(adapter);
		}
	}

}
