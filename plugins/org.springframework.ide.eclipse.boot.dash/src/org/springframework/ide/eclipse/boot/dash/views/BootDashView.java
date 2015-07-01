/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.DEFAULT_PATH;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.LIVE_PORT;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.PROJECT;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.RUN_STATE_ICN;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.TAGS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelectionSource;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.Filter;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashElementsTableSection;
import org.springframework.ide.eclipse.boot.dash.views.sections.ExpandableSectionWithSelection;
import org.springframework.ide.eclipse.boot.dash.views.sections.SashSection;
import org.springframework.ide.eclipse.boot.dash.views.sections.ScrollerSection;
import org.springframework.ide.eclipse.boot.dash.views.sections.TagSearchSection;
import org.springframework.ide.eclipse.boot.dash.views.sections.ViewPartWithSections;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;

/**
 * @author Kris De Volder
 */
public class BootDashView extends ViewPartWithSections {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.springframework.ide.eclipse.boot.dash.views.BootDashView";

	/**
	 * Adds scroll support to the whole view. You probably want to disable this if view is broken
	 * into pieces that have their own scrollbars
	 */
	private static final boolean ENABLE_SCROLLING = false;

	private BootDashModel model = BootDashActivator.getDefault().getModel();

//	private Action refreshAction;
//	private Action doubleClickAction;

	private BootDashActions actions;

	private UserInteractions ui = new DefaultUserInteractions(this);

	private MultiSelection<BootDashElement> selection = null; //lazy init

	private LiveVariable<Filter<BootDashElement>> searchFilterModel = new LiveVariable<Filter<BootDashElement>>(null);

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content
	 * (like Task List, for example).
	 */

	/**
	 * The constructor.
	 */
	public BootDashView() {
		super(ENABLE_SCROLLING);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (actions!=null) {
			actions.dispose();
		}
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		//Create the help context id for the viewer's control
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(tv.getControl(), "org.springframework.ide.eclipse.boot.dash.viewer");
		actions = new BootDashActions(model, getSelection(), ui);
//		hookContextMenu();
//		hookDoubleClickAction();
		contributeToActionBars();
	}

	public synchronized MultiSelection<BootDashElement> getSelection() {
		if (selection==null) {
			selection = MultiSelection.empty(BootDashElement.class);
			for (IPageSection section : getSections()) {
				if (section instanceof MultiSelectionSource) {
					MultiSelectionSource source = (MultiSelectionSource) section;
					MultiSelection<BootDashElement> subSelection = source.getSelection().filter(BootDashElement.class);
					selection = MultiSelection.union(selection, subSelection);
				}
			}
		}
		return selection;
	}

	public List<BootDashElement> getSelectedElements() {
		ArrayList<BootDashElement> elements = new ArrayList<BootDashElement>();
		for (Object e : getSelection().getValue()) {
			if (e instanceof BootDashElement) {
				elements.add((BootDashElement) e);
			}
		}
		return Collections.unmodifiableList(elements);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fills the pull-down menu for this view (accessible from the toolbar)
	 */
	private void fillLocalPullDown(IMenuManager manager) {
//		manager.add(refreshAction);
//		manager.add(new Separator());
//		manager.add(action2);
	}


	private void fillLocalToolBar(IToolBarManager manager) {
		for (RunStateAction a : actions.getRunStateActions()) {
			manager.add(a);
		}
		manager.add(actions.getOpenBrowserAction());
		manager.add(actions.getOpenConsoleAction());
		manager.add(actions.getOpenConfigAction());
//		manager.add(refreshAction);
//		manager.add(action2);
	}


//	private void hookDoubleClickAction() {
//		tv.addDoubleClickListener(new IDoubleClickListener() {
//			public void doubleClick(DoubleClickEvent event) {
//				doubleClickAction.run();
//			}
//		});
//	}
//	private void showMessage(String message) {
//		MessageDialog.openInformation(
//			tv.getControl().getShell(),
//			"Boot Dashboard",
//			message);
//	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (page!=null) {
			page.setFocus();
		}
	}

	public UserInteractions getUserInteractions() {
		return ui;
	}

	@Override
	public Shell getShell() {
		return getSite().getShell();
	}

	@Override
	protected List<IPageSection> createSections() throws CoreException {
		List<IPageSection> sections = new ArrayList<IPageSection>();

		sections.add(new TagSearchSection(BootDashView.this, searchFilterModel));

		BootDashElementsTableSection localApsTable = new BootDashElementsTableSection(BootDashView.this, model, searchFilterModel);
		localApsTable.setColumns(RUN_STATE_ICN, PROJECT, LIVE_PORT, DEFAULT_PATH ,TAGS);
		ExpandableSectionWithSelection localApsSection = new ExpandableSectionWithSelection(this, "Local Boot Apps", localApsTable);

		BootDashElementDetailsSection detailsSection = new BootDashElementDetailsSection(this, model,
				localApsTable.getSelection().toSingleSelection());

		sections.add(new SashSection(this,
				new ScrollerSection(this, localApsSection),
				detailsSection));
		return sections;
	}

//	/* Alternate 'createSections' for quick-and-dirty testing of the 'DynamicComposite'. */
//	@Override
//	protected List<IPageSection> createSections() throws CoreException {
//		final LiveSet<Integer> models = new LiveSet<Integer>();
//		DynamicCompositeSection<Integer> wrapper = new DynamicCompositeSection<Integer>(
//			this,
//			models,
//			new SectionFactory<Integer>() {
//				public IPageSection create(Integer i) {
//					BootDashElementsTableSection localApsTable = new BootDashElementsTableSection(BootDashView.this, model);
//					localApsTable.setColumns(RUN_STATE_ICN, PROJECT, LIVE_PORT);
//					return new ExpandableSectionWithSelection(BootDashView.this, "Local Boot Apps "+i, localApsTable);
//				}
//			},
//			BootDashElement.class
//		);
//		List<IPageSection> sections = new ArrayList<IPageSection>();
//		sections.add(wrapper);
//		for (int _i = 0; _i < 4; _i++) {
//			{   final int i = _i+1;
//				long delay = 5000 * i;
//				UIJob j = new UIJob("Test") {
//					@Override
//					public IStatus runInUIThread(IProgressMonitor monitor) {
//						if (i<=3) {
//							System.out.println("Adding model: "+i);
//							models.add(i);
//						} else {
//							System.out.println("Removing model: "+(i%3));
//							models.remove(i%3);
//						}
//						return Status.OK_STATUS;
//					}
//				};
//				j.schedule(delay);
//			}
//		}
//		return sections;
//	}
}