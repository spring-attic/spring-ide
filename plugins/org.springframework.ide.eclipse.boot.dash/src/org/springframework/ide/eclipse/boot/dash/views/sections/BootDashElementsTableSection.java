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
package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelectionSource;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.views.BootDashContentProvider;
import org.springframework.ide.eclipse.boot.dash.views.BootDashLabelProvider;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;
import org.springsource.ide.eclipse.commons.ui.TableResizeHelper;

/**
 * A section that contains a table viewer widget displaying Boot Dash Elements from a model.
 *
 * @author Kris De Volder
 */
public class BootDashElementsTableSection extends PageSection implements MultiSelectionSource<BootDashElement> {

	private TableViewer tv;
	private BootDashModel model;
	private BootDashColumn[] enabledColumns = BootDashColumn.values();
	private MultiSelection<BootDashElement> selection;

	public BootDashElementsTableSection(IPageWithSections owner, BootDashModel model) {
		super(owner);
		this.model = model;
	}

	class NameSorter extends ViewerSorter {
	}

	public void setColumns(BootDashColumn... columns) {
		this.enabledColumns = columns;
	}

	@Override
	public void createContents(final Composite page) {
		tv = new TableViewer(page, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION); // Note: No SWT.SCROLL options.
																	// Assumes its up to the page to be scrollable.
		tv.setContentProvider(new BootDashContentProvider(model));
		//tv.setLabelProvider(new ViewLabelProvider());
		tv.setSorter(new NameSorter());
		tv.setInput(model);
		tv.getTable().setHeaderVisible(true);
				tv.getTable().setLinesVisible(true);

		GridDataFactory.fillDefaults().grab(true, false).applyTo(tv.getControl());

		for (BootDashColumn columnType : enabledColumns ) {
			TableViewerColumn c1viewer = new TableViewerColumn(tv, columnType.getAllignment());
			c1viewer.getColumn().setWidth(columnType.getDefaultWidth());
			c1viewer.getColumn().setText(columnType.getLabel());
			c1viewer.setLabelProvider(new BootDashLabelProvider(columnType));
		}
		new TableResizeHelper(tv).enableResizing();

		model.getElements().addListener(new UIValueListener<Set<BootDashElement>>() {
			@Override
			protected void uiGotValue(LiveExpression<Set<BootDashElement>> exp,
					Set<BootDashElement> value) {
				if (!tv.getControl().isDisposed()) {
					tv.refresh();
					page.layout(new Control[]{tv.getControl()});
				}
			}
		});

		model.addElementStateListener(new ElementStateListener() {
			public void stateChanged(final BootDashElement e) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						tv.update(e, null);
//						updateActionEnablement();
					}
				});
			}
		});


//		tv.addSelectionChangedListener(new ISelectionChangedListener() {
//			public void selectionChanged(SelectionChangedEvent event) {
//				updateActionEnablement();
//			}
//		});

	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return OK_VALIDATOR;
	}

	@Override
	public synchronized MultiSelection<BootDashElement> getSelection() {
		if (selection==null) {
			selection = MultiSelection.from(BootDashElement.class, new ObservableSet<BootDashElement>() {
				@Override
				protected Set<BootDashElement> compute() {
					if (tv!=null) {
						ISelection s = tv.getSelection();
						if (s instanceof IStructuredSelection) {
							Object[] elements = ((IStructuredSelection) s).toArray();
							if (elements!=null && elements.length>0) {
								HashSet<BootDashElement> set = new HashSet<BootDashElement>();
								for (Object o : elements) {
									set.add((BootDashElement) o);
								}
								return set;
							}
						}
					}
					return Collections.emptySet();
				}
			});
		}
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selection.getElements().refresh();
			}
		});
		return selection;
	}

}
