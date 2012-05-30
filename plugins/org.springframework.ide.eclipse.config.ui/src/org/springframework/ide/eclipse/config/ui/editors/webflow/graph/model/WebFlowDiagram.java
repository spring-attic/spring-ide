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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model;

import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.IDiagramModelFactory;


/**
 * @author Leo Dos Santos
 */
public class WebFlowDiagram extends AbstractConfigGraphDiagram {

	public WebFlowDiagram(AbstractConfigGraphicalEditor editor) {
		super(editor);
	}

	@Override
	protected IDiagramModelFactory getModelFactory() {
		return new WebFlowModelFactory();
	}

}
