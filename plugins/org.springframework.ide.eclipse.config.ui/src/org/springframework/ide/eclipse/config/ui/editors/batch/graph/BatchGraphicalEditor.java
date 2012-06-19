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
package org.springframework.ide.eclipse.config.ui.editors.batch.graph;

import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigEditPartFactory;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.BatchDiagram;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.parts.BatchEditPartFactory;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class BatchGraphicalEditor extends AbstractConfigGraphicalEditor {

	@Override
	protected AbstractConfigEditPartFactory createEditPartFactory() {
		return new BatchEditPartFactory(this);
	}

	@Override
	protected AbstractConfigGraphDiagram createFlowDiagram() {
		return new BatchDiagram(this);
	}

	@Override
	protected AbstractConfigPaletteFactory createPaletteFactory() {
		return new BatchEditorPaletteFactory(this);
	}

}
