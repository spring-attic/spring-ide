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
package org.springframework.ide.eclipse.config.ui.editors.batch.graph.model;

import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.IDiagramModelFactory;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class BatchDiagram extends AbstractConfigGraphDiagram {

	private int stepCount;

	public BatchDiagram(AbstractConfigGraphicalEditor editor) {
		super(editor);
	}

	@Override
	protected IDiagramModelFactory getModelFactory() {
		return new BatchModelFactory();
	}

	public String getNewStepId() {
		return Integer.toString(++stepCount);
	}

}
