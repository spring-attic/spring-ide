/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.part.EditorPart;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.preferences.URLBookmark;

/**
 * New Dashboard that will replace the old. On some yet to be determined
 * activation date.
 * 
 * @author Kris De Volder
 */
public class DashboardEditor extends EditorPart {
	
	private CTabFolder folder;
	private DashboardPageContainer[] pages = null; //Lazy initialized
	
	public DashboardEditor() {
	}
	
	public DashboardPageContainer[] getPages() {
		if (pages==null) {
			List<IDashboardPage> _pages = createPages();
			List<DashboardPageContainer> containers = new ArrayList<DashboardPageContainer>(_pages.size());
			for (IDashboardPage page : _pages) {
				boolean add = true;
				if (page instanceof IEnablableDashboardPart) {
					add = ((IEnablableDashboardPart) page).shouldAdd();
				}
				if (add) {
					containers.add(new DashboardPageContainer(page));
				}
			}
			pages = containers.toArray(new DashboardPageContainer[containers.size()]);
		}
		return pages;
	}
	
	public void createPartControl(Composite _parent) {
		folder = new CTabFolder(_parent, SWT.BOTTOM|SWT.FLAT);
		CTabItem defaultSelection = null;
		for (final DashboardPageContainer page : getPages()) {
			CTabItem pageWidget = new CTabItem(folder, SWT.NONE);
			if (defaultSelection==null) {
				defaultSelection = pageWidget; //select the first
			}
			pageWidget.setData(page);
			pageWidget.setText(page.getName());
			page.setWidget(pageWidget);
			pageWidget.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					page.dispose();
				} 
			});
		}
		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ensureSelectedTabInitialized();
			}
		});
		folder.setSelection(defaultSelection);
		ensureSelectedTabInitialized();
	}
	
	private void ensureSelectedTabInitialized() {
		if (folder!=null && !folder.isDisposed()) {
			CTabItem tab = folder.getSelection();
			if (tab!=null) {
				DashboardPageContainer page = (DashboardPageContainer) tab.getData();
				if (page!=null) {
					page.initialize(getSite());
				}
			}
		}
	}
	

	/**
	 * Called when we need the pages. This will be called only once per
	 * DashboardEditor instance. Clients should override. This implementation
	 * is just so there's something here rather than nothing.
	 */
	protected List<IDashboardPage> createPages() {
		List<IDashboardPage> pages = new ArrayList<IDashboardPage>();
		addDashboadWebPages(pages);
		
//		try {
//			pages.add(new GeneratedGuidesDashboardPage());
//		} catch (Exception e) {
//			GettingStartedActivator.log(e);
//		}
		
		pages.add(new DashboardExtensionsPage());
//		pages.add(new GuidesDashboardPageWithPreview());
//		for (int i = 1; i < 3; i++) {
//			pages.add(new DemoDashboardPage("Demo "+i, "Contents for page "+i));
//		}
		return pages;
	}

	private void addDashboadWebPages(List<IDashboardPage> pages) {
		URLBookmark[] bookmarks = GettingStartedActivator.getDefault().getPreferences().getDashboardWebPages();

		for (URLBookmark bm : bookmarks) {
			pages.add(new WebDashboardPage(bm.getName(), bm.getUrl()));
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		setSite(site);
		setInput(input);
	}
	
	@Override
	public void setFocus() {
		if (folder != null) {
			folder.setFocus();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		//This isn't a real editor. So there's really nothing to save.
	}
	@Override
	public void doSaveAs() {
		//This isn't a real editor. So there's really nothing to save.
	}
	@Override
	public boolean isDirty() {
		//This isn't a real editor. It's never dirty.
		return false;
	}
	@Override
	public boolean isSaveAsAllowed() {
		//This isn't a real editor. There's nothing to save.
		return false;
	}

}