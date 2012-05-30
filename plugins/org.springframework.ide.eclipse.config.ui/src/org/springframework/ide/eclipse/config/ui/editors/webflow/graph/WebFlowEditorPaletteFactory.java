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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.springframework.ide.eclipse.config.core.schemas.WebFlowSchemaConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.ConfigGraphCommonImages;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.model.TransitionCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.ActionStateModelElement;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.DecisionStateModelElement;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.EndStateModelElement;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.SubflowStateModelElement;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.ViewStateModelElement;


/**
 * @author Leo Dos Santos
 */
public class WebFlowEditorPaletteFactory extends AbstractConfigPaletteFactory {

	public WebFlowEditorPaletteFactory(AbstractConfigGraphicalEditor editor) {
		super(editor);
	}

	@Override
	protected List<PaletteDrawer> createComponentDrawers() {
		List<PaletteDrawer> categories = new ArrayList<PaletteDrawer>();
		PaletteDrawer drawer = new PaletteDrawer(Messages.WebFlowEditorPaletteFactory_STATE_PALETTE_TITLE);
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				WebFlowSchemaConstants.ELEM_ACTION_STATE,
				Messages.WebFlowEditorPaletteFactory_ACTION_STATE_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(ActionStateModelElement.class, getDiagram()),
				WebFlowImages.ACTION_SMALL, WebFlowImages.ACTION);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(WebFlowSchemaConstants.ELEM_DECISION_STATE,
				Messages.WebFlowEditorPaletteFactory_DECISION_STATE_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(DecisionStateModelElement.class, getDiagram()),
				WebFlowImages.DECISION_SMALL, WebFlowImages.DECISION);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(WebFlowSchemaConstants.ELEM_END_STATE,
				Messages.WebFlowEditorPaletteFactory_END_STATE_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						EndStateModelElement.class, getDiagram()), WebFlowImages.END_SMALL, WebFlowImages.END);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(WebFlowSchemaConstants.ELEM_SUBFLOW_STATE,
				Messages.WebFlowEditorPaletteFactory_SUBFLOW_STATE_COMPONENT_DESCRIPTION,
				new ModelElementCreationFactory(SubflowStateModelElement.class, getDiagram()),
				WebFlowImages.SUBFLOW_SMALL, WebFlowImages.SUBFLOW);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(WebFlowSchemaConstants.ELEM_VIEW_STATE,
				Messages.WebFlowEditorPaletteFactory_VIEW_STATE_COMPONENT_DESCRIPTION, new ModelElementCreationFactory(
						ViewStateModelElement.class, getDiagram()), WebFlowImages.VIEW_SMALL, WebFlowImages.VIEW);
		entries.add(entry);

		drawer.addAll(entries);
		categories.add(drawer);
		return categories;
	}

	@Override
	protected List<PaletteEntry> createConnectionTools() {
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();
		ToolEntry tool = new ConnectionCreationToolEntry(Messages.WebFlowEditorPaletteFactory_TO_COMPONENT_TITLE,
				Messages.WebFlowEditorPaletteFactory_TO_COMPONENT_DESCRIPTION, new TransitionCreationFactory(),
				ConfigGraphCommonImages.CONNECTION_SOLID, ConfigGraphCommonImages.CONNECTION_SOLID);
		entries.add(tool);
		return entries;
	}

}
