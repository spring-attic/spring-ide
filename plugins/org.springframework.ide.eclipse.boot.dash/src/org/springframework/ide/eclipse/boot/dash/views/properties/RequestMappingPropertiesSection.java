/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import static org.springsource.ide.eclipse.commons.ui.UiUtil.openUrl;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.util.Utils;
import org.springframework.ide.eclipse.boot.dash.views.RequestMappingLabelProvider;
import org.springframework.ide.eclipse.boot.dash.views.RequestMappingsColumn;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

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
						BootDashElement bde = getBootDashElement();
						String url = Utils.createUrl(bde.getLiveHost(), bde.getLivePort(), rm.getPath());
						if (url!=null) {
							openUrl(url);
						}
						break;
					case SRC:
						IJavaElement javaElement = rm.getMethod();
						if (javaElement == null) {
							javaElement = rm.getType();
						}

						if (javaElement != null) {
							SpringUIUtils.openInEditor(javaElement);
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

	private static final Object[] NO_ELEMENTS = new Object[0];
	private TabbedPropertySheetPage page;
	private Composite composite;
	private Composite missingInfoComp;
	private Hyperlink externalDocLink;

	private StackLayout layout;
	private TableViewer tv;
	private RequestMappingLabelProvider labelProvider;
	private Stylers stylers;
	private ViewerComparator sorter = new ViewerComparator() {

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
		composite = getWidgetFactory().createComposite(parent, SWT.NONE);

		// Layout variant to have owner composite size to be equal the client area size of the next upper level ScrolledComposite
		composite.setLayout(layout = new SectionStackLayout());

		layout.marginWidth = ITabbedPropertyConstants.HSPACE + 2;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE + 4;

		page.getControl().setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));

		missingInfoComp = getWidgetFactory().createComposite(composite, SWT.NONE);
		missingInfoComp.setLayout(GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 1).numColumns(1).create());

		labelText = getWidgetFactory().createLabel(missingInfoComp, "", SWT.WRAP);
		externalDocLink = getWidgetFactory().createHyperlink(missingInfoComp, "", SWT.WRAP);

		externalDocLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (!externalDocLink.getText().isEmpty()) {
					UiUtil.openUrl(externalDocLink.getText());
				}
			}
		});
		externalDocLink.setText(MissingLiveInfoMessages.EXTERNAL_DOCUMENT_LINK);

		this.tv = new TableViewer(composite, SWT.BORDER|SWT.FULL_SELECTION/*|SWT.NO_SCROLL*/);

		tv.setContentProvider(new ContentProvider());
		tv.setComparator(sorter);
//		tv.setLabelProvider(labelProvider = new RequestMappingLabelProvider(tv.getTable().getFont(), input));
		tv.setInput(getBootDashElement());
		tv.getTable().setHeaderVisible(true);
		stylers = new Stylers(tv.getTable().getFont());

		refreshControlsVisibility();

		for (RequestMappingsColumn colType : RequestMappingsColumn.values()) {
			TableViewerColumn col = new TableViewerColumn(tv, colType.getAlignment());
			col.setLabelProvider(new RequestMappingLabelProvider(stylers, getBootDashElementLiveExpression(), colType));
			TableColumn colWidget = col.getColumn();
			colWidget.setText(colType.getLabel());
			colWidget.setWidth(colType.getDefaultWidth());
		}

		createContextMenu(tv);

		new DoubleClickListener(tv);
	}

	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		tv.setInput(getBootDashElement());
	}

	public void refresh() {
		refreshControlsVisibility();
		BootDashElement bde = getBootDashElement();
		if (bde == null) {
			labelText.setText(MissingLiveInfoMessages.noSelectionMessage("Request Mappings"));
		} else if (bde.getLiveRequestMappings() == null) {
			labelText.setText(MissingLiveInfoMessages.getMissingInfoMessage(bde.getName(), "mappings"));
		} else {
			labelText.setText("");
		}
		tv.refresh();
		SectionStackLayout.reflow(page);
	}

	private void refreshControlsVisibility() {
		BootDashElement bde = getBootDashElement();
		if (bde == null || bde.getLiveRequestMappings() == null) {
			layout.topControl = missingInfoComp;
		} else {
			layout.topControl = tv.getControl();
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

	private void createContextMenu(Viewer viewer) {
	    MenuManager contextMenu = new MenuManager("#ViewerMenu"); //$NON-NLS-1$
	    contextMenu.setRemoveAllWhenShown(true);
	    contextMenu.addMenuListener(new IMenuListener() {
	        @Override
	        public void menuAboutToShow(IMenuManager mgr) {
	            fillContextMenu(mgr);
	        }

	    });

	    Menu menu = contextMenu.createContextMenu(viewer.getControl());
	    viewer.getControl().setMenu(menu);
	}

	private void fillContextMenu(IMenuManager contextMenu) {
		if (getStructuredSelection().size() == 1) {
			final RequestMapping rm = (RequestMapping) getStructuredSelection().getFirstElement();
			final BootDashElement bde = getBootDashElement();
			Action makeDefaultAction = new Action("Make Default") {
				@Override
				public void run() {
					bde.setDefaultRequestMappingPath(rm.getPath());
					tv.refresh();
					/*
					 * Just refresh doesn't cause repaint for some reason
					 */
					tv.getTable().redraw();
				}
			};
			makeDefaultAction.setEnabled(!rm.getPath().equals(bde.getDefaultRequestMappingPath()));
			contextMenu.add(makeDefaultAction);
		}
	}

	private IStructuredSelection getStructuredSelection() {
		//Watch out, this is not Eclipse 4.4 api:
		//return tv.getStructuredSelection();
		//So do this instead:
		return (IStructuredSelection) tv.getSelection();
	}

}
