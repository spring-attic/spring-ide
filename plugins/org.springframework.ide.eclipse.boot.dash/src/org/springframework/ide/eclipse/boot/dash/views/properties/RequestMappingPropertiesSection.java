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
package org.springframework.ide.eclipse.boot.dash.views.properties;

import static org.springframework.ide.eclipse.boot.dash.model.BootDashElementUtil.getUrl;
import static org.springsource.ide.eclipse.commons.ui.UiUtil.openUrl;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springframework.ide.eclipse.boot.dash.views.RequestMappingLabelProvider;
import org.springframework.ide.eclipse.boot.dash.views.RequestMappingsColumn;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

/**
 * Tabbed properties view section for live request mappings
 *
 * @author Alex Boyko
 *
 */
public class RequestMappingPropertiesSection extends AbstractPropertySection {

	private Label labelText;

	private LiveVariable<BootDashElement> input = new LiveVariable<BootDashElement>();

	private static final Object[] NO_ELEMENTS = new Object[0];
	private TabbedPropertySheetPage page;
	private Composite composite;
	private TableViewer tv;
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

	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		this.page = aTabbedPropertySheetPage;
		composite = getWidgetFactory().createFlatFormComposite(parent);
		page.getControl().setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		FormData data;

		labelText = getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		labelText.setLayoutData(data);

		this.tv = new TableViewer(composite, SWT.BORDER|SWT.FULL_SELECTION|SWT.NO_SCROLL);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		tv.getControl().setLayoutData(data);

		tv.setContentProvider(new ContentProvider());
		tv.setSorter(sorter);
//		tv.setLabelProvider(labelProvider = new RequestMappingLabelProvider(tv.getTable().getFont(), input));
		tv.setInput(input.getValue());
		tv.getTable().setHeaderVisible(true);
		stylers = new Stylers(tv.getTable().getFont());

		for (RequestMappingsColumn colType : RequestMappingsColumn.values()) {
			TableViewerColumn col = new TableViewerColumn(tv, colType.getAlignment());
			col.setLabelProvider(new RequestMappingLabelProvider(stylers, input, colType));
			TableColumn colWidget = col.getColumn();
			colWidget.setText(colType.getLabel());
			colWidget.setWidth(colType.getDefaultWidth());
		}

		tv.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				Object clicked = sel.getFirstElement();
				if(clicked instanceof RequestMapping){
					RequestMapping rm = (RequestMapping) clicked;
					String url = getUrl(input.getValue(), rm);
					if (url!=null) {
						openUrl(url);
					}
//					MessageDialog.openInformation(page.getShell(), "clickety click!",
//							"Double-click on : "+ clicked);
				}
			}

		});
		BootDashActivator.getDefault().getModel().addElementStateListener(modelListener = new ElementStateListener() {
			public void stateChanged(BootDashElement e) {
				if (e.equals(input.getValue())) {
					if (!page.getControl().isDisposed()) {
						page.getControl().getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (!page.getControl().isDisposed()) {
									refresh();
									tv.refresh();
									page.getControl().getParent().layout(true, true);
								}
							}
						});
					}
				}
			}
		});

	}

	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof IStructuredSelection);
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		if (structuredSelection.size() > 1) {
			input.setValue(null);
		} else {
			Object inputObj = structuredSelection.getFirstElement();
			Assert.isTrue(inputObj instanceof BootDashElement);
			System.out.println("input changed: "+inputObj);
			input.setValue((BootDashElement) inputObj);
			System.out.println(" bde name: "+input.getValue().getName());
			System.out.println(" bde defaultPath: "+input.getValue().getDefaultRequestMappingPath());
		}
		refresh();
	}

	public void refresh() {
		refreshControlsVisibility();
		BootDashElement input = this.input.getValue();
		tv.setInput(input);
		if (input == null) {
			labelText.setText("Select single element in Boot Dashboard to see Request Mappings");
		} else if (input.getLiveRequestMappings() == null) {
			labelText.setText("'" + input.getName() + "' must be running and actuator 'mappings' endpoint must be enabled to obtain request mappings.");
		} else {
			labelText.setText("");
		}
	}

	private void refreshControlsVisibility() {
		BootDashElement input = this.input.getValue();
		if (input == null || input.getLiveRequestMappings() == null) {
			tv.getControl().setVisible(false);
			labelText.setVisible(true);
		} else {
			tv.getControl().setVisible(true);
			labelText.setVisible(false);
		}
	}

	@Override
	public void dispose() {
		if (modelListener!=null) {
			BootDashActivator.getDefault().getModel().removeElementStateListener(modelListener);
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
		super.dispose();
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
					if (!page.getControl().isDisposed()) {
						page.getControl().getParent().layout(true, true);
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

}
