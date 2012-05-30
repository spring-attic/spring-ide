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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.ConfigGraphCommonImages;
import org.springframework.ide.eclipse.config.graph.model.ModelElementCreationFactory;
import org.springframework.ide.eclipse.config.graph.model.TransitionCreationFactory;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.DecisionModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.EndModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.FailModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.FlowModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.JobModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.NextModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.SplitContainerElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.StepModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.StopModelElement;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class BatchEditorPaletteFactory extends AbstractConfigPaletteFactory {

	public BatchEditorPaletteFactory(AbstractConfigGraphicalEditor editor) {
		super(editor);
	}

	@Override
	protected List<PaletteDrawer> createComponentDrawers() {
		List<PaletteDrawer> categories = new ArrayList<PaletteDrawer>();
		PaletteDrawer drawer = new PaletteDrawer(Messages
				.getString("BatchEditorPaletteFactory.COMPONENTS_PALETTE_TITLE")); //$NON-NLS-1$
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(BatchSchemaConstants.ELEM_JOB, Messages
				.getString("BatchEditorPaletteFactory.JOB_COMPONENT_DESCRIPTION"), new ModelElementCreationFactory( //$NON-NLS-1$
				JobModelElement.class, getDiagram()), ConfigGraphCommonImages.SEQUENCE_BEGIN_VERTICAL_SMALL,
				ConfigGraphCommonImages.SEQUENCE_BEGIN_VERTICAL);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(BatchSchemaConstants.ELEM_FLOW,
				Messages.getString("BatchEditorPaletteFactory.FLOW_COMPONENT_DESCRIPTION"), //$NON-NLS-1$
				new ModelElementCreationFactory(FlowModelElement.class, getDiagram()),
				ConfigGraphCommonImages.SEQUENCE_BEGIN_VERTICAL_SMALL, ConfigGraphCommonImages.SEQUENCE_BEGIN_VERTICAL);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(BatchSchemaConstants.ELEM_STEP,
				Messages.getString("BatchEditorPaletteFactory.STEP_COMPONENT_DESCRIPTION"), //$NON-NLS-1$
				new ModelElementCreationFactory(StepModelElement.class, getDiagram()), ConfigGraphCommonImages.ACTIVITY_SMALL,
				ConfigGraphCommonImages.ACTIVITY);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(BatchSchemaConstants.ELEM_SPLIT,
				Messages.getString("BatchEditorPaletteFactory.SPLIT_COMPONENT_DESCRIPTION"), //$NON-NLS-1$
				new ModelElementCreationFactory(SplitContainerElement.class, getDiagram()), BatchImages.SPLIT_SMALL,
				BatchImages.SPLIT);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(BatchSchemaConstants.ELEM_DECISION,
				Messages.getString("BatchEditorPaletteFactory.DECISION_COMPONENT_DESCRIPTION"), //$NON-NLS-1$
				new ModelElementCreationFactory(DecisionModelElement.class, getDiagram()), BatchImages.DECISION_SMALL,
				BatchImages.DECISION);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(BatchSchemaConstants.ELEM_END,
				Messages.getString("BatchEditorPaletteFactory.END_COMPONENT_DESCRIPTION"), //$NON-NLS-1$
				new ModelElementCreationFactory(EndModelElement.class, getDiagram()), BatchImages.END_SMALL,
				BatchImages.END);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(BatchSchemaConstants.ELEM_FAIL,
				Messages.getString("BatchEditorPaletteFactory.FAIL_COMPONENT_DESCRIPTION"), //$NON-NLS-1$
				new ModelElementCreationFactory(FailModelElement.class, getDiagram()), BatchImages.FAIL_SMALL,
				BatchImages.FAIL);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(BatchSchemaConstants.ELEM_NEXT,
				Messages.getString("BatchEditorPaletteFactory.NEXT_COMPONENT_DESCRIPTION"), //$NON-NLS-1$
				new ModelElementCreationFactory(NextModelElement.class, getDiagram()), BatchImages.NEXT_SMALL,
				BatchImages.NEXT);
		entries.add(entry);

		entry = new CombinedTemplateCreationEntry(BatchSchemaConstants.ELEM_STOP,
				Messages.getString("BatchEditorPaletteFactory.STOP_COMPONENT_DESCRIPTION"), //$NON-NLS-1$
				new ModelElementCreationFactory(StopModelElement.class, getDiagram()), BatchImages.STOP_SMALL,
				BatchImages.STOP);
		entries.add(entry);

		drawer.addAll(entries);
		categories.add(drawer);
		return categories;
	}

	@Override
	protected List<PaletteEntry> createConnectionTools() {
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();
		ToolEntry tool = new ConnectionCreationToolEntry(Messages
				.getString("BatchEditorPaletteFactory.NEXT_CONNECTION_TITLE"), Messages //$NON-NLS-1$
				.getString("BatchEditorPaletteFactory.NEXT_CONNECTION_DESCRIPTION"), new TransitionCreationFactory(), //$NON-NLS-1$
				ConfigGraphCommonImages.CONNECTION_SOLID, ConfigGraphCommonImages.CONNECTION_SOLID);
		entries.add(tool);
		return entries;
	}

}
