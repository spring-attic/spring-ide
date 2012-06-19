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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.requests.CreationFactory;
import org.springframework.ide.eclipse.config.graph.ConfigGraphPlugin;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * @author Leo Dos Santos
 */
public class ModelElementCreationFactory implements CreationFactory {

	private final Class<? extends AbstractConfigGraphModelElement> type;

	private final AbstractConfigGraphDiagram diagram;

	private final String uri;

	public ModelElementCreationFactory(Class<? extends AbstractConfigGraphModelElement> type,
			AbstractConfigGraphDiagram diagram) {
		this(type, diagram, null);
	}

	public ModelElementCreationFactory(Class<? extends AbstractConfigGraphModelElement> type,
			AbstractConfigGraphDiagram diagram, String namespaceUri) {
		this.type = type;
		this.diagram = diagram;
		this.uri = namespaceUri;
	}

	public AbstractConfigGraphModelElement getNewObject() {
		try {
			AbstractConfigGraphModelElement model = type.newInstance();
			model.setDiagram(diagram);
			model.createInput(uri);
			return model;
		}
		catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigGraphPlugin.PLUGIN_ID,
					Messages.ModelElementCreationFactory_ERROR_CREATING_ELEMENT_MODEL, e));
			return null;
		}
	}

	public Class<? extends AbstractConfigGraphModelElement> getObjectType() {
		return type;
	}

}
