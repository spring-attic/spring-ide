/*******************************************************************************
 *  Copyright (c) 2012 - 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.ViewPart;
import org.springframework.ide.eclipse.roo.ui.internal.actions.CreateNewRooProjectAction;
import org.springframework.ide.eclipse.roo.ui.internal.actions.OpenRooCommandWizardAction;
import org.springframework.ide.eclipse.roo.ui.internal.actions.OpenRooShellForProjectsAction;
import org.springframework.ide.eclipse.roo.ui.internal.actions.RooAddOnManagerAction;


/**
 * Entry-point for the Roo Shell.
 * @author Christian Dupuis
 * @author Kris De Volder
 * @since 2.5.0
 */
public class RooShellView extends ViewPart {
	
	public static final String VIEW_ID = "com.springsource.sts.roo.ui.rooShellView";

	private CreateNewRooProjectAction createNewRooProject;

	private CTabFolder folder = null;
	
	private CTabItem infoTab;

	private OpenRooCommandWizardAction openRooCommandWizardAction;

	private OpenRooShellForProjectsAction openRooProjectsAction;

	private IResourceChangeListener projectChangeListener = null;

	private final Map<IProject, CTabItem> tabItems = new ConcurrentHashMap<IProject, CTabItem>();

	private final Map<IProject, RooShellTab> tabs = new ConcurrentHashMap<IProject, RooShellTab>();

	private RooAddOnManagerAction openRooAddOnManagerAction;

	@Override
	public void createPartControl(Composite parent) {
		folder = new CTabFolder(parent, SWT.BOTTOM | SWT.BORDER);

		Composite tabComposite = new Composite(folder, SWT.NONE);
		tabComposite.setFont(folder.getFont());
		GridLayout layout = new GridLayout(1, false);
		tabComposite.setLayout(layout);
		tabComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Hyperlink loginLink = new Hyperlink(tabComposite, SWT.NONE);
		loginLink.setText("Open Roo Shell for projects...");
		loginLink.setUnderlined(true);
		loginLink.setForeground(getViewSite().getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE));
		loginLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				openRooProjectsAction.run();
			}
		});

		infoTab = new CTabItem(folder, SWT.CLOSE);
		infoTab.setText("Info");
		infoTab.setShowClose(false);
		infoTab.setControl(tabComposite);
		folder.setSelection(infoTab);

		createToolbar();
	}

	@Override
	public void dispose() {
		for (IProject entry : tabItems.keySet()) {
			removeTab(entry);
		}
		// should not be necessary and causes an SWTException on Gtk
//		if (folder != null && !folder.isDisposed()) {
//			folder.dispose();
//		}
		super.dispose();
	}

	public IProject getActiveProject() {
		CTabItem item = folder.getSelection();
		for (Map.Entry<IProject, CTabItem> tabItem : tabItems.entrySet()) {
			if (tabItem.getValue().equals(item)) {
				return tabItem.getKey();
			}
		}
		return null;
	}

	public Set<IProject> getOpenProjects() {
		return tabs.keySet();
	}
	
	public synchronized RooShellTab openShell(IProject project) {
		return openShell(project, null);
	}

	public synchronized RooShellTab openShell(IProject project, String command) {
		ensureProjectListener();
		if (infoTab != null) {
			infoTab.dispose();
			infoTab = null;
		}
		if (!tabs.containsKey(project)) {
			RooShellTab tab = new RooShellTab(project, command, this);
			tabs.put(project, tab);
			CTabItem tabItem = tab.addTab(folder);
			tabItems.put(project, tabItem);
			openRooCommandWizardAction.update();
			openRooAddOnManagerAction.update();
			return tab;
		}
		else {
			folder.setSelection(tabItems.get(project));
			openRooCommandWizardAction.update();
			openRooAddOnManagerAction.update();
			return tabs.get(project);
		}
	}

	@Override
	public void setFocus() {
	}

	private void createToolbar() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		openRooCommandWizardAction = new OpenRooCommandWizardAction(this);
		mgr.add(openRooCommandWizardAction);
		openRooAddOnManagerAction = new RooAddOnManagerAction(this);
		mgr.add(openRooAddOnManagerAction);
		mgr.add(new Separator());
		openRooProjectsAction = new OpenRooShellForProjectsAction(this);
		mgr.add(openRooProjectsAction);
		mgr.add(new Separator());
		createNewRooProject = new CreateNewRooProjectAction(getSite().getShell());
		mgr.add(createNewRooProject);
	}

	private void ensureProjectListener() {
		if (this.projectChangeListener==null) {
			this.projectChangeListener = new IResourceChangeListener() {
				public void resourceChanged(IResourceChangeEvent event) {
					final IProject project = (IProject) event.getResource();
					if (folder!=null && !folder.isDisposed()) {
						folder.getDisplay().syncExec(new Runnable() {
							public void run() {
								removeTab(project);
							}
						});
					}
				}
			};
			ResourcesPlugin.getWorkspace().addResourceChangeListener(
					this.projectChangeListener, 
					IResourceChangeEvent.PRE_CLOSE +
					IResourceChangeEvent.PRE_DELETE);
		}
	}
	
	protected void removeTab(IProject project) {
		tabs.remove(project);
		CTabItem item = tabItems.remove(project);
		if (item != null) {
			item.dispose();
		}
		openRooCommandWizardAction.update();
		openRooAddOnManagerAction.update();
	}
}
