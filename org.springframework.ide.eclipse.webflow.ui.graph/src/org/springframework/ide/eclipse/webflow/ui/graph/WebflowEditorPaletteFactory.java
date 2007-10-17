/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.SimpleFactory;
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.internal.model.ActionState;
import org.springframework.ide.eclipse.webflow.core.internal.model.AttributeMapper;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.DecisionState;
import org.springframework.ide.eclipse.webflow.core.internal.model.EndState;
import org.springframework.ide.eclipse.webflow.core.internal.model.EntryActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.EvaluateAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExitActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.IfTransition;
import org.springframework.ide.eclipse.webflow.core.internal.model.RenderActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.Set;
import org.springframework.ide.eclipse.webflow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.webflow.core.internal.model.SubflowState;
import org.springframework.ide.eclipse.webflow.core.internal.model.ViewState;
import org.springframework.ide.eclipse.webflow.core.model.IAction;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IEntryActions;
import org.springframework.ide.eclipse.webflow.core.model.IExitActions;
import org.springframework.ide.eclipse.webflow.core.model.IRenderActions;
import org.springframework.ide.eclipse.webflow.ui.editor.Activator;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelDecorator;

/**
 * Utility class that bzilds the {@link WebflowEditor} toolbar
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowEditorPaletteFactory {

	/**
	 * @param root
	 * @return
	 */
	private static List createCategories(PaletteRoot root) {
		List<PaletteContainer> categories = new ArrayList<PaletteContainer>();
		categories.add(createControlGroup(root));
		categories.add(createComponentsDrawer());
		categories.add(createActionDrawer());
		categories.add(createRenderActionsDrawer());
		categories.add(createEntryActionDrawer());
		categories.add(createExitActionDrawer());
		return categories;
	}

	/**
	 * @return
	 */
	private static PaletteContainer createComponentsDrawer() {

		PaletteDrawer drawer = new PaletteDrawer("Web Flow Elements", null);

		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
				"Action State", "Create a new Action State", ActionState.class,
				new SimpleFactory(ActionState.class),
				WebflowUIImages.DESC_OBJS_ACTION_STATE,
				WebflowUIImages.DESC_OBJS_ACTION_STATE);
		entries.add(combined);

		combined = new CombinedTemplateCreationEntry("View State",
				"Create a View State", ViewState.class, new SimpleFactory(
						ViewState.class), WebflowUIImages.DESC_OBJS_VIEW_STATE,
				WebflowUIImages.DESC_OBJS_VIEW_STATE);
		entries.add(combined);

		combined = new CombinedTemplateCreationEntry("Subflow State",
				"Create a Subflow State", SubflowState.class,
				new SimpleFactory(SubflowState.class),
				WebflowUIImages.DESC_OBJS_SUBFLOW_STATE,
				WebflowUIImages.DESC_OBJS_SUBFLOW_STATE);
		entries.add(combined);

		combined = new CombinedTemplateCreationEntry("Decision State",
				"Create a Decision State", DecisionState.class,
				new SimpleFactory(DecisionState.class),
				WebflowUIImages.DESC_OBJS_DECISION_STATE,
				WebflowUIImages.DESC_OBJS_DECISION_STATE);
		entries.add(combined);

		combined = new CombinedTemplateCreationEntry("End State",
				"Create an End State", EndState.class, new SimpleFactory(
						EndState.class), WebflowUIImages.DESC_OBJS_END_STATE,
				WebflowUIImages.DESC_OBJS_END_STATE);
		entries.add(combined);

		PaletteSeparator sep = new PaletteSeparator(Activator.PLUGIN_ID
				+ ".palette.sep3");
		sep
				.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
		entries.add(sep);
		
		combined = new CombinedTemplateCreationEntry("Exception Handler",
				"Create an Exception Handler", ExceptionHandler.class,
				new SimpleFactory(ExceptionHandler.class),
				WebflowUIImages.DESC_OBJS_EXCEPTION_HANDLER,
				WebflowUIImages.DESC_OBJS_EXCEPTION_HANDLER);
		entries.add(combined);

		combined = new CombinedTemplateCreationEntry("Attribute Mapper",
				"Create an Attribute Mapper", AttributeMapper.class,
				new SimpleFactory(AttributeMapper.class),
				WebflowUIImages.DESC_OBJS_ATTRIBUTE_MAPPER,
				WebflowUIImages.DESC_OBJS_ATTRIBUTE_MAPPER);
		entries.add(combined);

		drawer.addAll(entries);
		return drawer;
	}

	/**
	 * @param root
	 * @return
	 */
	private static PaletteContainer createControlGroup(PaletteRoot root) {
		PaletteGroup controlGroup = new PaletteGroup("Control Group");

		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		ToolEntry tool = new SelectionToolEntry();
		entries.add(tool);
		root.setDefaultEntry(tool);

		tool = new MarqueeToolEntry();
		entries.add(tool);

		PaletteSeparator sep = new PaletteSeparator(Activator.PLUGIN_ID
				+ ".palette.sep2");
		sep
				.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
		entries.add(sep);

		tool = new ConnectionCreationToolEntry("Transition",
				"Create a Transition",
				new SimpleFactory(StateTransition.class),
				WebflowImages.DESC_OBJS_CONNECTION,
				WebflowImages.DESC_OBJS_ELSE_CONNECTION);
		entries.add(tool);
		tool = new ConnectionCreationToolEntry("Else Transition",
				"Create a Transition", new SimpleFactory(IfTransition.class),
				WebflowImages.DESC_OBJS_ELSE_CONNECTION,
				WebflowImages.DESC_OBJS_ELSE_CONNECTION);
		;
		entries.add(tool);
		controlGroup.addAll(entries);
		return controlGroup;
	}

	/**
	 * Creates the PaletteRoot and adds all Palette elements.
	 * @return the root
	 */
	public static PaletteRoot createPalette() {
		PaletteRoot flowPalette = new PaletteRoot();
		flowPalette.addAll(createCategories(flowPalette));
		return flowPalette;
	}

	/**
	 * @return
	 */
	private static PaletteContainer createActionDrawer() {
		List<ToolEntry> entries = new ArrayList<ToolEntry>();

		PaletteDrawer drawer = new PaletteDrawer("Actions", null);
		drawer.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
		CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
				"Action", "Create a new Action", Action.class,
				new ActionModelElementFactory(Action.class,
						IActionElement.ACTION_TYPE.ACTION),
				WebflowUIImages.DESC_OBJS_ACTION,
				WebflowUIImages.DESC_OBJS_ACTION);
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry("Bean Action",
				"Create a new Bean Action", BeanAction.class,
				new ActionModelElementFactory(BeanAction.class,
						IActionElement.ACTION_TYPE.ACTION),
				WebflowUIImages.DESC_OBJS_BEAN_ACTION,
				WebflowUIImages.DESC_OBJS_BEAN_ACTION);
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry("Evaluation Action",
				"Create a new Evaluation Action", EvaluateAction.class,
				new ActionModelElementFactory(EvaluateAction.class,
						IActionElement.ACTION_TYPE.ACTION),
				WebflowUIImages.DESC_OBJS_EVALUATION_ACTION,
				WebflowUIImages.DESC_OBJS_EVALUATION_ACTION);
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry("Set", "Create a new Set",
				Set.class, new ActionModelElementFactory(Set.class,
						IActionElement.ACTION_TYPE.ACTION),
				WebflowUIImages.DESC_OBJS_SET_ACTION,
				WebflowUIImages.DESC_OBJS_SET_ACTION);
		entries.add(combined);
		drawer.addAll(entries);
		return drawer;
	}

	/**
	 * @return
	 */
	private static PaletteContainer createRenderActionsDrawer() {
		List<ToolEntry> entries = new ArrayList<ToolEntry>();

		IRenderActions exit = new RenderActions();
		IAction action = new Action();
		action.setElementParent(exit);

		WebflowModelLabelDecorator dec = new WebflowModelLabelDecorator();

		PaletteDrawer drawer = new PaletteDrawer("Render Actions", null);
		drawer.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
		CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
				"Render Action", "Create a new Render Action", Action.class,
				new ActionModelElementFactory(Action.class,
						IActionElement.ACTION_TYPE.RENDER_ACTION), dec
						.getDecoratedImageDescriptor(WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_ACTION),
								action), dec.getDecoratedImageDescriptor(
						WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_ACTION),
						action));
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry(
				"Render Bean Action",
				"Create a new Render Bean Action",
				BeanAction.class,
				new ActionModelElementFactory(BeanAction.class,
						IActionElement.ACTION_TYPE.RENDER_ACTION),
				dec
						.getDecoratedImageDescriptor(
								WebflowUIImages
										.getImage(WebflowUIImages.IMG_OBJS_BEAN_ACTION),
								action),
				dec
						.getDecoratedImageDescriptor(
								WebflowUIImages
										.getImage(WebflowUIImages.IMG_OBJS_BEAN_ACTION),
								action));
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry(
				"Render Evaluation Action",
				"Create a new Render Evaluation Action", EvaluateAction.class,
				new ActionModelElementFactory(EvaluateAction.class,
						IActionElement.ACTION_TYPE.RENDER_ACTION),
				dec.getDecoratedImageDescriptor(WebflowUIImages
						.getImage(WebflowUIImages.IMG_OBJS_EVALUATION_ACTION),
						action),
				dec.getDecoratedImageDescriptor(WebflowUIImages
						.getImage(WebflowUIImages.IMG_OBJS_EVALUATION_ACTION),
						action));
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry("Render Set",
				"Create a new Render Set", Set.class,
				new ActionModelElementFactory(Set.class,
						IActionElement.ACTION_TYPE.RENDER_ACTION), dec
						.getDecoratedImageDescriptor(WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_SET_ACTION),
								action), dec.getDecoratedImageDescriptor(
						WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_SET_ACTION),
						action));
		entries.add(combined);
		drawer.addAll(entries);
		return drawer;
	}

	/**
	 * @return
	 */
	private static PaletteContainer createEntryActionDrawer() {
		List<ToolEntry> entries = new ArrayList<ToolEntry>();

		IEntryActions exit = new EntryActions();
		IAction action = new Action();
		action.setElementParent(exit);

		WebflowModelLabelDecorator dec = new WebflowModelLabelDecorator();

		PaletteDrawer drawer = new PaletteDrawer("Entry Actions", null);
		drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
		CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
				"Entry Action", "Create a new Entry Action", Action.class,
				new ActionModelElementFactory(Action.class,
						IActionElement.ACTION_TYPE.ENTRY_ACTION), dec
						.getDecoratedImageDescriptor(WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_ACTION),
								action), dec.getDecoratedImageDescriptor(
						WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_ACTION),
						action));
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry(
				"Entry Bean Action",
				"Create a new Entry Bean Action",
				BeanAction.class,
				new ActionModelElementFactory(BeanAction.class,
						IActionElement.ACTION_TYPE.ENTRY_ACTION),
				dec
						.getDecoratedImageDescriptor(
								WebflowUIImages
										.getImage(WebflowUIImages.IMG_OBJS_BEAN_ACTION),
								action),
				dec
						.getDecoratedImageDescriptor(
								WebflowUIImages
										.getImage(WebflowUIImages.IMG_OBJS_BEAN_ACTION),
								action));
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry("Entry Evaluation Action",
				"Create a new Evaluation Action", EvaluateAction.class,
				new ActionModelElementFactory(EvaluateAction.class,
						IActionElement.ACTION_TYPE.ENTRY_ACTION),
				dec.getDecoratedImageDescriptor(WebflowUIImages
						.getImage(WebflowUIImages.IMG_OBJS_EVALUATION_ACTION),
						action),
				dec.getDecoratedImageDescriptor(WebflowUIImages
						.getImage(WebflowUIImages.IMG_OBJS_EVALUATION_ACTION),
						action));
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry("Entry Set",
				"Create a new Entry Set", Set.class,
				new ActionModelElementFactory(Set.class,
						IActionElement.ACTION_TYPE.ENTRY_ACTION), dec
						.getDecoratedImageDescriptor(WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_SET_ACTION),
								action), dec.getDecoratedImageDescriptor(
						WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_SET_ACTION),
						action));
		entries.add(combined);
		drawer.addAll(entries);
		return drawer;
	}

	/**
	 * @return
	 */
	private static PaletteContainer createExitActionDrawer() {
		List<ToolEntry> entries = new ArrayList<ToolEntry>();

		IExitActions exit = new ExitActions();
		IAction action = new Action();
		action.setElementParent(exit);

		WebflowModelLabelDecorator dec = new WebflowModelLabelDecorator();

		PaletteDrawer drawer = new PaletteDrawer("Exit Actions", null);
		drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
		CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
				"Exit Action", "Create a new Exit Action", Action.class,
				new ActionModelElementFactory(Action.class,
						IActionElement.ACTION_TYPE.EXIT_ACTION), dec
						.getDecoratedImageDescriptor(WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_ACTION),
								action), dec.getDecoratedImageDescriptor(
						WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_ACTION),
						action));
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry(
				"Exit Bean Action",
				"Create a new Exit Bean Action",
				BeanAction.class,
				new ActionModelElementFactory(BeanAction.class,
						IActionElement.ACTION_TYPE.EXIT_ACTION),
				dec
						.getDecoratedImageDescriptor(
								WebflowUIImages
										.getImage(WebflowUIImages.IMG_OBJS_BEAN_ACTION),
								action),
				dec
						.getDecoratedImageDescriptor(
								WebflowUIImages
										.getImage(WebflowUIImages.IMG_OBJS_BEAN_ACTION),
								action));
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry("Exit Evaluation Action",
				"Create a new Exit Evaluation Action", EvaluateAction.class,
				new ActionModelElementFactory(EvaluateAction.class,
						IActionElement.ACTION_TYPE.EXIT_ACTION),
				dec.getDecoratedImageDescriptor(WebflowUIImages
						.getImage(WebflowUIImages.IMG_OBJS_EVALUATION_ACTION),
						action),
				dec.getDecoratedImageDescriptor(WebflowUIImages
						.getImage(WebflowUIImages.IMG_OBJS_EVALUATION_ACTION),
						action));
		entries.add(combined);
		combined = new CombinedTemplateCreationEntry("Exit Set",
				"Create a new Exit Set", Set.class,
				new ActionModelElementFactory(Set.class,
						IActionElement.ACTION_TYPE.EXIT_ACTION), dec
						.getDecoratedImageDescriptor(WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_SET_ACTION),
								action), dec.getDecoratedImageDescriptor(
						WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_SET_ACTION),
						action));
		entries.add(combined);
		drawer.addAll(entries);
		return drawer;
	}

	/**
	 * 
	 */
	private static class ActionModelElementFactory extends SimpleFactory {

		/**
		 * 
		 */
		private IActionElement.ACTION_TYPE type;

		/**
		 * @param type
		 * @param aClass
		 */
		public ActionModelElementFactory(Class aClass,
				IActionElement.ACTION_TYPE type) {
			super(aClass);
			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.gef.requests.SimpleFactory#getNewObject()
		 */
		public Object getNewObject() {
			IActionElement action = (IActionElement) super.getNewObject();
			action.setType(this.type);
			return action;
		}
	}
}
