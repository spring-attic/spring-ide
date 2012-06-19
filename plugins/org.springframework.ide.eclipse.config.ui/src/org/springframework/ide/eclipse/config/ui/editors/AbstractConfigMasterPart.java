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

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * This class implements the master portion of the master/details UI pattern.
 * Classes that extend {@code AbstractConfigMasterPart} will display a
 * structured overview of some portion of a Spring configuration file. This
 * class is a base only, and clients should consider extending
 * {@link AbstractNamespaceMasterPart} instead.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
public abstract class AbstractConfigMasterPart extends SectionPart {

	private final AbstractConfigFormPage page;

	/**
	 * The toolkit used by the form part.
	 */
	protected FormToolkit toolkit;

	private ToolBarManager toolBarManager;

	private ColumnViewer viewer;

	private SpringConfigContentProvider contentProv;

	private AbstractConfigLabelProvider labelProv;

	/**
	 * Constructs a master part with a reference to its container page and its
	 * parent composite.
	 * 
	 * @param page the hosting form page
	 * @param parent the parent composite
	 */
	public AbstractConfigMasterPart(AbstractConfigFormPage page, Composite parent) {
		super(parent, page.getManagedForm().getToolkit(), Section.TITLE_BAR | Section.DESCRIPTION);
		this.page = page;
		this.toolkit = page.getManagedForm().getToolkit();
	}

	protected abstract void createButtons(Composite client);

	/**
	 * Creates the content of the master part inside the form part. This method
	 * is called when the master/details block is created.
	 */
	protected void createContents() {
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumWidth = 250;
		data.grabExcessVerticalSpace = true;

		Section section = getSection();
		section.setLayout(new GridLayout());
		section.setLayoutData(data);
		section.setText(getSectionTitle());
		section.setDescription(getSectionDescription());

		Composite client = toolkit.createComposite(section);
		client.setLayout(new GridLayout(2, false));
		client.setLayoutData(new GridData(GridData.FILL_BOTH));
		section.setClient(client);

		toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);
		section.setTextClient(toolbar);

		toolbar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (handCursor != null && !handCursor.isDisposed()) {
					handCursor.dispose();
				}
			}
		});

		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 50;
		data.widthHint = 50;

		contentProv = createViewerContentProvider();
		labelProv = createViewerLabelProvider();

		viewer = createViewer(client);
		viewer.setContentProvider(contentProv);
		viewer.setLabelProvider(labelProv);
		viewer.setInput(getConfigEditor().getDomDocument());

		MenuManager menuMgr = new MenuManager();
		IMenuListener listener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		};
		menuMgr.addMenuListener(listener);
		menuMgr.setRemoveAllWhenShown(true);

		Control control = viewer.getControl();
		Menu menu = menuMgr.createContextMenu(control);
		control.setMenu(menu);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				getManagedForm().fireSelectionChanged(AbstractConfigMasterPart.this, event.getSelection());
			}
		});
		viewer.getControl().setLayoutData(data);

		Composite buttonComp = toolkit.createComposite(client);
		buttonComp.setLayout(new GridLayout());
		buttonComp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		createButtons(buttonComp);

		postCreateContents();
		toolBarManager.update(true);
	}

	/**
	 * This method is called automatically when the master part is created.
	 * Clients must create and return the desired column viewer here.
	 * 
	 * @param client the parent composite
	 * @return viewer of the master part
	 */
	protected abstract ColumnViewer createViewer(Composite client);

	/**
	 * This method is called automatically when the master part is created.
	 * Clients must create and return a content provider for their viewer here.
	 * 
	 * @return content provider for the master viewer
	 */
	protected abstract SpringConfigContentProvider createViewerContentProvider();

	/**
	 * This method is called automatically when the master part is created.
	 * Clients must create and return a label provider for their viewer here.
	 * 
	 * @return label provider for the master viewer
	 */
	protected abstract AbstractConfigLabelProvider createViewerLabelProvider();

	/**
	 * This method is called automatically when an element in the master viewer
	 * is right-clicked upon. Clients may override to create and add menu items
	 * to the menu manager that are appropriate to the selection.
	 * 
	 * @param manager the menu manager on the master viewer
	 */
	protected abstract void fillContextMenu(IMenuManager manager);

	/**
	 * Returns the the parent editor as an {@link AbstractConfigEditor} object.
	 * 
	 * @return parent editor instance
	 */
	public AbstractConfigEditor getConfigEditor() {
		return page.getEditor();
	}

	/**
	 * Returns the form page hosting this part.
	 * 
	 * @return page the hosting form page
	 */
	public AbstractConfigFormPage getFormPage() {
		return page;
	}

	/**
	 * This method is called automatically when the master part is created.
	 * Clients must return the description of their master section part here.
	 * 
	 * @return master part description
	 */
	protected abstract String getSectionDescription();

	/**
	 * This method is called automatically when the master part is created.
	 * Clients must return the title of their master section part here.
	 * 
	 * @return master part title
	 */
	protected abstract String getSectionTitle();

	/**
	 * Returns the toolbar manager for the section section header.
	 * 
	 * @return section's toolbar manager
	 */
	protected ToolBarManager getToolBarManager() {
		return toolBarManager;
	}

	/**
	 * Returns the viewer of the master part.
	 * 
	 * @return viewer of the master part
	 */
	public ColumnViewer getViewer() {
		return viewer;
	}

	/**
	 * Returns the content provider used by the master viewer.
	 * 
	 * @return content provider for the master viewer
	 */
	protected SpringConfigContentProvider getViewerContentProvider() {
		return contentProv;
	}

	/**
	 * This method is called automatically at the end of the
	 * {@link #createContents()} method. Clients may override to perform
	 * additional operations on the master viewer or add additional content to
	 * the part.
	 */
	protected abstract void postCreateContents();

	@Override
	public void refresh() {
		viewer.refresh();
		getManagedForm().fireSelectionChanged(AbstractConfigMasterPart.this, viewer.getSelection());
		super.refresh();
	}

}
