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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelectionSource;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.TagFilterBoxModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.sections.SashSection;
import org.springframework.ide.eclipse.boot.dash.views.sections.ScrollerSection;
import org.springframework.ide.eclipse.boot.dash.views.sections.TagSearchSection;
import org.springframework.ide.eclipse.boot.dash.views.sections.ViewPartWithSections;
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
	 * Adds scroll support to the whole view. You probably want to disable this
	 * if view is broken into pieces that have their own scrollbars
	 */
	private static final boolean ENABLE_SCROLLING = false;

	private BootDashViewModel model = BootDashActivator.getDefault().getModel();
	private TagFilterBoxModel filterBoxModel = new TagFilterBoxModel();

	// private Action refreshAction;
	// private Action doubleClickAction;

	private BootDashActions actions;

	private UserInteractions ui = new DefaultUserInteractions(this);

	private MultiSelection<BootDashElement> selection = null; // lazy init

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
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
		if (actions != null) {
			actions.dispose();
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// Create the help context id for the viewer's control
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(tv.getControl(),
		// "org.springframework.ide.eclipse.boot.dash.viewer");
		actions = new BootDashActions(model, getSelection(), ui);
		// hookContextMenu();
		// hookDoubleClickAction();
		contributeToActionBars();
	}

	public synchronized MultiSelection<BootDashElement> getSelection() {
		if (this.selection == null) {
			MultiSelection<BootDashElement> selection = MultiSelection.empty(BootDashElement.class);
			for (IPageSection section : getSections()) {
				if (section instanceof MultiSelectionSource) {
					MultiSelectionSource source = (MultiSelectionSource) section;
					MultiSelection<BootDashElement> subSelection = source.getSelection().filter(BootDashElement.class);
					selection = MultiSelection.union(selection, subSelection);
				}
			}
			this.selection = selection;
		}
		return this.selection;
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
		// manager.add(refreshAction);
		// manager.add(new Separator());
		// manager.add(action2);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		for (RunStateAction a : actions.getRunStateActions()) {
			manager.add(a);
		}
		manager.add(actions.getOpenBrowserAction());
		manager.add(actions.getOpenConsoleAction());
		manager.add(actions.getOpenConfigAction());
		createAddRunTargetPulldown(manager);
		// manager.add(refreshAction);
		// manager.add(action2);
	}

	public void createAddRunTargetPulldown(IToolBarManager toolbar) {
		Action dropdownAction=new Action("Create Target",SWT.DROP_DOWN){};
		dropdownAction.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/add_target.png"));
		dropdownAction.setMenuCreator(new IMenuCreator() {
			Menu theMenu;

			@Override
			public Menu getMenu(Menu parent) {
				return null;
			}

			@Override
			public Menu getMenu(Control parent) {
				if (theMenu==null) {
					MenuManager menu = new MenuManager();
					for (AddRunTargetAction a : actions.getAddRunTargetActions()) {
						menu.add(a);
					}
					theMenu = menu.createContextMenu(parent);
				}
				return theMenu;
			}

			@Override
			public void dispose() {
			}
		});

		toolbar.add(dropdownAction);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (page != null) {
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

		sections.add(new TagSearchSection(BootDashView.this, filterBoxModel.getText(), model));

		DynamicRunTargetSection runTargetSections = new DynamicRunTargetSection(this,
				model.getSectionModels(),
				new RunTargetSectionFactory(this, model, filterBoxModel.getFilter(), ui)
		);

		BootDashElementDetailsSection detailsSection = new BootDashElementDetailsSection(
				this, model, runTargetSections.getSelection().cast(BootDashElement.class).toSingleSelection()
		);

		sections.add(new SashSection(this,
				new ScrollerSection(this, runTargetSections),
				detailsSection)
		);
		return sections;
	}

}
