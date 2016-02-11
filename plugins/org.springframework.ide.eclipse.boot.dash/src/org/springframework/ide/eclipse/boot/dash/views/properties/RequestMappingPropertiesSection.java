/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import static org.springsource.ide.eclipse.commons.ui.UiUtil.openUrl;

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springframework.ide.eclipse.boot.dash.util.Utils;
import org.springframework.ide.eclipse.boot.dash.views.RequestMappingLabelProvider;
import org.springframework.ide.eclipse.boot.dash.views.RequestMappingsColumn;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * Tabbed properties view section for live request mappings
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class RequestMappingPropertiesSection extends AbstractBdePropertiesSection {

	private class DoubleClickListener extends MouseAdapter {
		DoubleClickListener(TableViewer tv) {
			tv.getTable().addMouseListener(this);
		}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			ViewerCell cell = tv.getCell(new Point(e.x, e.y));
			if (cell!=null) {
				Object clicked = cell.getElement();
				if (clicked instanceof RequestMapping){
					RequestMapping rm = (RequestMapping) clicked;
					int colIdx = cell.getColumnIndex();
					RequestMappingsColumn col = RequestMappingsColumn.values()[colIdx];
					switch (col) {
					case PATH:
						BootDashElement bde = input.getValue();
						String url = Utils.createUrl(bde.getLiveHost(), bde.getLivePort(), rm.getPath());
						if (url!=null) {
							openUrl(url);
						}
						break;
					case SRC:
						IMethod method = rm.getMethod();
						if (method!=null) {
							SpringUIUtils.openInEditor(method);
						}
					default:
						break;
					}
	//						MessageDialog.openInformation(page.getShell(), "clickety click!",
	//								"Double-click on : "+ clicked);
				}
			}
		}

	}

	private Label labelText;

	private LiveVariable<BootDashElement> input = new LiveVariable<BootDashElement>();

	private static final Object[] NO_ELEMENTS = new Object[0];
	private TabbedPropertySheetPage page;
	private Composite composite;
	private TableViewer tv;
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

		new DoubleClickListener(tv);
	}

	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		input.setValue(getBootDashElement());
		tv.setInput(getBootDashElement());
	}

	public void refresh() {
		refreshControlsVisibility();
		BootDashElement bde = getBootDashElement();
		if (bde == null) {
			labelText.setText("Select single element in Boot Dashboard to see Request Mappings");
		} else if (bde.getLiveRequestMappings() == null) {
			labelText.setText("'" + bde.getName() + "' must be running and actuator 'mappings' endpoint must be enabled to obtain request mappings.");
		} else {
			labelText.setText("");
		}
		tv.refresh();
		reflow(page);
	}

	private void reflow(TabbedPropertySheetPage page) {
		final Composite target = getReflowTarget(page);
		if (target!=null) {
			target.getDisplay().asyncExec(new Runnable() {
				public void run() {
					target.layout(true, true);
				}
			});
		}
	}

	private Composite getReflowTarget(TabbedPropertySheetPage page) {
		return page.getControl().getParent();
//		Control c = page.getControl();
//		Composite composite = null;
//		while (c!=null) {
//			if (c instanceof Composite) {
//				composite = (Composite) c;
//			}
//			c = c.getParent();
//		}
//		return composite;
	}

	private void refreshControlsVisibility() {
		BootDashElement bde = getBootDashElement();
		if (bde == null || bde.getLiveRequestMappings() == null) {
			tv.getControl().setVisible(false);
			labelText.setVisible(true);
		} else {
			tv.getControl().setVisible(true);
			labelText.setVisible(false);
		}
	}

	@Override
	public void dispose() {
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
			/*
			 * Nothing. Rely on the section refresh mechanism that should refresh the table
			 */
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
