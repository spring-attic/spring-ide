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
package org.springframework.ide.eclipse.config.ui.editors;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.ISourceViewerActionBarContributor;
import org.eclipse.wst.xml.ui.internal.tabletree.SourcePageActionContributor;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.actions.ExportAction;
import org.springframework.ide.eclipse.config.graph.actions.MultiPageZoomComboContributionItem;
import org.springframework.ide.eclipse.config.graph.actions.ToggleLayoutAction;
import org.springframework.ide.eclipse.config.ui.actions.ToggleMarkOccurrencesAction;

/**
 * Manages the installation/deinstallation of global actions for multi-page
 * editors. Responsible for the redirection of global actions to the active
 * editor. Multi-page contributor replaces the contributors for the individual
 * editors in the multi-page editor.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Terry Denney
 */
@SuppressWarnings("restriction")
public class SpringConfigEditorContributor extends MultiPageEditorActionBarContributor {

	private final String ID_GROUP = "springConfig"; //$NON-NLS-1$

	private AbstractConfigEditor configEditor;

	private IEditorPart activeEditorPart;

	private ToggleMarkOccurrencesAction occurrencesAction;

	private ToggleLayoutAction layoutAction;

	private MultiPageZoomComboContributionItem zoomCombo;

	protected IEditorActionBarContributor sourceViewerActionContributor;

	/**
	 * Creates a multi-page contributor.
	 */
	public SpringConfigEditorContributor() {
		super();
		sourceViewerActionContributor = new SourcePageActionContributor();
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		occurrencesAction = new ToggleMarkOccurrencesAction();
		layoutAction = new ToggleLayoutAction();
		zoomCombo = new MultiPageZoomComboContributionItem(getPage());
		toolBarManager.add(new GroupMarker(ID_GROUP));
		toolBarManager.appendToGroup(ID_GROUP, occurrencesAction);
		toolBarManager.appendToGroup(ID_GROUP, layoutAction);
		toolBarManager.appendToGroup(ID_GROUP, zoomCombo);
		setActionEnablement();
	}

	@Override
	public void dispose() {
		if (sourceViewerActionContributor != null) {
			sourceViewerActionContributor.dispose();
		}
		if (occurrencesAction != null) {
			occurrencesAction.setActiveEditor(null);
		}
		super.dispose();
	}

	/**
	 * Returns the action registered with the given text editor.
	 * @return IAction or null if editor is null.
	 */
	protected IAction getAction(ITextEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}

	@Override
	public void init(IActionBars bars) {
		super.init(bars);
		if (bars != null && sourceViewerActionContributor != null) {
			sourceViewerActionContributor.init(bars, getPage());
		}
	}

	private void setActionEnablement() {
		occurrencesAction.setEnabled(activeEditorPart instanceof StructuredTextEditor);
		layoutAction.setEnabled(activeEditorPart instanceof AbstractConfigGraphicalEditor);
	}

	@Override
	public void setActiveEditor(IEditorPart part) {
		if (part instanceof AbstractConfigEditor && part != configEditor) {
			configEditor = (AbstractConfigEditor) part;
			occurrencesAction.setActiveEditor(configEditor);
		}
		updateActionBars();
		super.setActiveEditor(part);
	}

	/*
	 * (non-JavaDoc) Method declared in
	 * AbstractMultiPageEditorActionBarContributor.
	 */
	@Override
	public void setActivePage(IEditorPart part) {
		if (activeEditorPart == part) {
			return;
		}

		if (activeEditorPart instanceof AbstractConfigGraphicalEditor) {
			// If leaving a graph page, clear the GEF command stack. This
			// ensures that graph pages can only undo/redo recent edits that
			// were performed in the page itself. All XML edits should remain
			// undo/redoable from the source and form pages.
			AbstractConfigGraphicalEditor editor = (AbstractConfigGraphicalEditor) activeEditorPart;
			CommandStack stack = (CommandStack) editor.getAdapter(CommandStack.class);
			stack.flush();
		}
		activeEditorPart = part;

		if (part instanceof StructuredTextEditor) {
			sourceViewerActionContributor.setActiveEditor(part);
			((ISourceViewerActionBarContributor) sourceViewerActionContributor)
					.setViewerSpecificContributionsEnabled(true);
		}
		else {
			sourceViewerActionContributor.setActiveEditor(configEditor);
			((ISourceViewerActionBarContributor) sourceViewerActionContributor)
					.setViewerSpecificContributionsEnabled(false);
		}
		updateActionBars();

		setActionEnablement();
		if (zoomCombo != null && configEditor != null) {
			zoomCombo.setZoomManager((ZoomManager) configEditor.getAdapter(ZoomManager.class));
		}
	}

	private void updateActionBars() {
		IActionBars actionBars = getActionBars();
		if (actionBars != null && configEditor != null) {
			if (activeEditorPart instanceof AbstractConfigGraphicalEditor) {
				AbstractConfigGraphicalEditor editor = (AbstractConfigGraphicalEditor) activeEditorPart;
				Object obj = editor.getAdapter(ActionRegistry.class);
				if (obj instanceof ActionRegistry) {
					ActionRegistry registry = (ActionRegistry) obj;
					actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
							registry.getAction(ActionFactory.UNDO.getId()));
					actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
							registry.getAction(ActionFactory.REDO.getId()));
					actionBars.setGlobalActionHandler(ActionFactory.PRINT.getId(),
							registry.getAction(ActionFactory.PRINT.getId()));
					actionBars
							.setGlobalActionHandler(ActionFactory.EXPORT.getId(), registry.getAction(ExportAction.ID));
				}
			}
			else if (activeEditorPart instanceof SpringConfigGraphPage) {
				SpringConfigGraphPage editor = (SpringConfigGraphPage) activeEditorPart;
				Object obj = editor.getAdapter(ActionRegistry.class);
				if (obj instanceof ActionRegistry) {
					ActionRegistry registry = (ActionRegistry) obj;
					actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), null);
					actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), null);
					actionBars.setGlobalActionHandler(ActionFactory.PRINT.getId(),
							registry.getAction(ActionFactory.PRINT.getId()));
					actionBars
							.setGlobalActionHandler(ActionFactory.EXPORT.getId(), registry.getAction(ExportAction.ID));
				}
			}
			else {
				ITextEditor editor = configEditor.getSourcePage();
				actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
						getAction(editor, ITextEditorActionConstants.UNDO));
				actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
						getAction(editor, ITextEditorActionConstants.REDO));
				actionBars.setGlobalActionHandler(ActionFactory.PRINT.getId(), null);
				actionBars.setGlobalActionHandler(ActionFactory.EXPORT.getId(), null);
			}
			actionBars.updateActionBars();
		}
	}

}
