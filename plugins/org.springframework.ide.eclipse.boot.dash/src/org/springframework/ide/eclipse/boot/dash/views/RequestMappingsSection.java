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

import static org.springframework.ide.eclipse.boot.dash.model.BootDashElementUtil.getUrl;
import static org.springsource.ide.eclipse.commons.ui.UiUtil.openUrl;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.springframework.ide.eclipse.boot.dash.livexp.ui.ReflowUtil;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;

/**
 * @author Kris De Volder
 */
public class RequestMappingsSection extends PageSection implements Disposable {

	private static final Object[] NO_ELEMENTS = new Object[0];
	private LiveExpression<BootDashElement> input;
	private Composite page;
	private TableViewer tv;
	private BootDashViewModel model;
	private ElementStateListener modelListener;
	private RequestMappingLabelProvider labelProvider;
	private Stylers stylers;
	private ViewerSorter sorter = new ViewerSorter() {

		 @Override
		 public int compare(Viewer viewer, Object e1, Object e2) {
			 if (e1 instanceof RequestMapping && e2 instanceof RequestMapping) {
				 RequestMapping rm1 = (RequestMapping) e1;
				 RequestMapping rm2 = (RequestMapping) e2;
				 int cat1 = getCategory(rm1);
				 int cat2 = getCategory(rm2);
				 if (cat1!=cat2) {
					 return cat1-cat2;
				 } else {
					 return rm1.getPath().compareTo(rm2.getPath());
				 }
			 }
			 return 0;
		 }

		private int getCategory(RequestMapping rm) {
			if (rm.isUserDefined()) {
				return 0;
			} else {
				return 1;
			}
		}
	};

	public RequestMappingsSection(IPageWithSections owner, BootDashViewModel model, LiveExpression<BootDashElement> selection) {
		super(owner);
		this.model = model;
		this.input = selection;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return OK_VALIDATOR;
	}

	@Override
	public void createContents(final Composite page) {
		this.page = page;
		this.tv = new TableViewer(page, SWT.BORDER|SWT.FULL_SELECTION|SWT.V_SCROLL);
		tv.setContentProvider(new ContentProvider());
		tv.setSorter(sorter);
//		tv.setLabelProvider(labelProvider = new RequestMappingLabelProvider(tv.getTable().getFont(), input));
		tv.setInput(model);
		tv.getTable().setHeaderVisible(true);
		stylers = new Stylers(tv.getTable().getFont());

		for (RequestMappingsColumn colType : RequestMappingsColumn.values()) {
			TableViewerColumn col = new TableViewerColumn(tv, colType.getAlignment());
			col.setLabelProvider(new RequestMappingLabelProvider(stylers, input, colType));
			TableColumn colWidget = col.getColumn();
			colWidget.setText(colType.getLabel());
			colWidget.setWidth(colType.getDefaultWidth());
		}

		GridDataFactory.fillDefaults().grab(true, true).applyTo(tv.getTable());
		this.input.addListener(new UIValueListener<BootDashElement>() {
			public void uiGotValue(LiveExpression<BootDashElement> exp, BootDashElement value) {
				if (!tv.getControl().isDisposed()) {
					tv.setInput(value);
				}
			}
		});
		tv.getTable().addControlListener(new ControlListener() {
			public void controlResized(ControlEvent e) {
				ReflowUtil.reflow(owner, tv.getControl());
			}

			@Override
			public void controlMoved(ControlEvent e) {
			}
		});
		tv.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				Object clicked = sel.getFirstElement();
				if(clicked instanceof RequestMapping){
					RequestMapping rm = (RequestMapping) clicked;
					BootDashElement el = input.getValue();
					String url = getUrl(el, rm);
					if (url!=null) {
						openUrl(url);
					}
//					MessageDialog.openInformation(page.getShell(), "clickety click!",
//							"Double-click on : "+ clicked);
				}
			}

		});
		model.addElementStateListener(modelListener = new ElementStateListener() {
			public void stateChanged(BootDashElement e) {
				if (e.equals(input.getValue())) {
					if (!tv.getControl().isDisposed()) {
						tv.getControl().getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (!tv.getControl().isDisposed()) {
									tv.refresh();
									page.layout(new Control[] {tv.getControl()});
								}
							}
						});
					}
				}
			}
		});
	}

	public class ContentProvider implements IStructuredContentProvider {


		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			tv.refresh();
			tv.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!page.isDisposed()) {
						page.layout(new Control[] {tv.getControl()});
					}
				}
			});
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof BootDashElement) {
				BootDashElement el = (BootDashElement) inputElement;
				List<RequestMapping> elements = el.getLiveRequestMappings();
				if (elements!=null) {
					return elements.toArray();
				} else {
					//null means we couldn't determine the request mappings.
					return new Object[] {
							"'"+el.getName()+"' must be running...",
							"and the actuator 'mappings' ...",
							"endpoint must be enabled ...",
							"to obtain request mappings.",
					};
				}
			}
			return NO_ELEMENTS;
		}

	}

	@Override
	public void dispose() {
		if (modelListener!=null) {
			model.removeElementStateListener(modelListener);
			modelListener = null;
		}
		if (labelProvider!=null) {
			labelProvider.dispose();
			labelProvider = null;
		}
		if (stylers!=null) {
			stylers.dispose();
			stylers = null;
		}
	}

}
