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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigEditPartFactory;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.IntegrationDiagram;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.IntegrationEditPartFactory;


/**
 * @author Leo Dos Santos
 */
public class IntegrationGraphicalEditor extends AbstractConfigGraphicalEditor {

	@Override
	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action = new CreateExplicitChannelAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
	}

	@Override
	protected ContextMenuProvider createContextMenuProvider() {
		return new IntegrationContextMenuProvider(getGraphicalViewer(), getActionRegistry());
	}

	@Override
	protected AbstractConfigEditPartFactory createEditPartFactory() {
		return new IntegrationEditPartFactory(this);
	}

	@Override
	protected AbstractConfigGraphDiagram createFlowDiagram() {
		return new IntegrationDiagram(this);
	}

	@Override
	protected AbstractConfigPaletteFactory createPaletteFactory() {
		return new IntegrationEditorPaletteFactory(this);
	}

}
