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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class SpringConfigContentOutline extends Page implements IContentOutlinePage, ISelectionChangedListener {

	private final AbstractConfigEditor editor;

	private ArrayList<ISelectionChangedListener> listeners;

	private Set<IContentOutlinePage> pages;

	private PageBook pagebook;

	private IContentOutlinePage currentOutline;

	private ISelection selection;

	public SpringConfigContentOutline(AbstractConfigEditor editor) {
		this.editor = editor;
		listeners = new ArrayList<ISelectionChangedListener>();
		pages = new HashSet<IContentOutlinePage>();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public void createControl(Composite parent) {
		pagebook = new PageBook(parent, SWT.NONE);
	}

	@Override
	public void dispose() {
		if (pagebook != null && !pagebook.isDisposed()) {
			pagebook.dispose();
		}
		pagebook = null;
		listeners = null;
		releasePages();
	}

	@Override
	public Control getControl() {
		return pagebook;
	}

	public ISelection getSelection() {
		return selection;
	}

	@Override
	public void init(IPageSite pageSite) {
		if (listeners == null) {
			listeners = new ArrayList<ISelectionChangedListener>();
		}
		if (pages == null) {
			pages = new HashSet<IContentOutlinePage>();
		}
		super.init(pageSite);
	}

	private void releasePages() {
		for (IContentOutlinePage outline : pages) {
			if (outline != null) {
				try {
					outline.dispose();
				}
				catch (Exception e) {
					// STS-1421: can't figure out why sometimes a outline throws
					// a NPE
				}
			}
		}
		pages = null;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		setSelection(event.getSelection());
	}

	@Override
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);
		if (currentOutline != null) {
			setActiveOutline(currentOutline);
		}
	}

	public void setActiveOutline(IContentOutlinePage outline) {
		if (currentOutline != null) {
			currentOutline.removeSelectionChangedListener(this);
		}
		outline.addSelectionChangedListener(this);
		pages.add(outline);
		this.currentOutline = outline;
		if (pagebook == null) {
			return;
		}
		Control control = outline.getControl();
		if (control == null || control.isDisposed()) {
			if (outline instanceof Page) {
				Page page = (Page) outline;
				page.init(getSite());
			}
			outline.createControl(pagebook);
			control = outline.getControl();
		}
		pagebook.showPage(control);
		this.currentOutline = outline;
	}

	@Override
	public void setFocus() {
		if (currentOutline != null) {
			currentOutline.setFocus();
		}
	}

	public void setSelection(ISelection selection) {
		this.selection = selection;
		if (listeners == null) {
			return;
		}
		SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		for (ISelectionChangedListener listener : listeners) {
			listener.selectionChanged(e);
		}
	}

}
