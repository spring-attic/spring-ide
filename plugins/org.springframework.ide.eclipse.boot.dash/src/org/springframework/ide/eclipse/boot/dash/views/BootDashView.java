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


import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.views.BootDashLabelProvider.BootDashColumn;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.ui.TableResizeHelper;

import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class BootDashView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.springframework.ide.eclipse.boot.dash.views.BootDashView";

	private BootDashModel model = new BootDashModel(ResourcesPlugin.getWorkspace());

	private TableViewer tv;
	private Action refreshAction;
//	private Action doubleClickAction;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content
	 * (like Task List, for example).
	 */

//	@SuppressWarnings("restriction")
//	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
//
//		private AppearanceAwareLabelProvider javaLabels = new AppearanceAwareLabelProvider();
//
//		public String getColumnText(Object obj, int index) {
//			if (obj instanceof BootDashElement) {
//				return javaLabels.getText(((BootDashElement)obj).getJavaProject());
//			}
//			return super.getText(obj);
//		}
//		public Image getColumnImage(Object obj, int index) {
//			if (obj instanceof BootDashElement) {
//				return javaLabels.getImage(((BootDashElement)obj).getJavaProject());
//			}
//			return super.getImage(obj);
//		}
//
//		public Image getImage(Object obj) {
//			if (obj instanceof BootDashElement) {
//				return javaLabels.getImage(((BootDashElement)obj).getJavaProject());
//			}
//			return super.getImage(obj);
//		}
//		@Override
//		public void dispose() {
//			super.dispose();
//			javaLabels.dispose();
//		}
//	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public BootDashView() {
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		tv = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
		tv.setContentProvider(new BootDashContentProvider(model));
		//tv.setLabelProvider(new ViewLabelProvider());
		tv.setSorter(new NameSorter());
		tv.setInput(getViewSite());
		tv.getTable().setHeaderVisible(true);
				tv.getTable().setLinesVisible(true);

		TableViewerColumn c1viewer = new TableViewerColumn(tv, SWT.LEFT);
		c1viewer.getColumn().setWidth(200);
		c1viewer.getColumn().setText("Project");
		c1viewer.setLabelProvider(new BootDashLabelProvider(BootDashColumn.PROJECT));
		TableViewerColumn c2viewer = new TableViewerColumn(tv, SWT.LEFT);
		c2viewer.getColumn().setWidth(100);
		c2viewer.getColumn().setText("State");
		c2viewer.setLabelProvider(new BootDashLabelProvider(BootDashColumn.RUN_STATE));
		new TableResizeHelper(tv).enableResizing();

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tv.getControl(), "org.springframework.ide.eclipse.boot.dash.viewer");
		makeActions();
		hookContextMenu();
//		hookDoubleClickAction();
		contributeToActionBars();

		model.getElements().addListener(new UIValueListener<Set<BootDashElement>>() {
			@Override
			protected void uiGotValue(LiveExpression<Set<BootDashElement>> exp,
					Set<BootDashElement> value) {
				tv.refresh();
			}
		});

		model.addElementStateListener(new ElementStateListener() {
			public void stateChanged(final BootDashElement e) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						tv.update(e, null);
					}
				});
			}
		});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BootDashView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(tv.getControl());
		tv.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tv);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
//		manager.add(new Separator());
//		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
//		manager.add(action2);
		// Other plug-ins can contribute there actions here
//		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
//		manager.add(action2);
	}

	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				model.refresh();
				tv.refresh();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Manually trigger a view refresh");
		refreshAction.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/refresh.gif"));

//		action2 = new Action() {
//			public void run() {
//				showMessage("Action 2 executed");
//			}
//		};
//		action2.setText("Action 2");
//		action2.setToolTipText("Action 2 tooltip");
//		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

//		doubleClickAction = new Action() {
//			public void run() {
//				ISelection selection = tv.getSelection();
//				Object obj = ((IStructuredSelection)selection).getFirstElement();
//				showMessage("Double-click detected on "+obj.toString());
//			}
//		};
	}

//	private void hookDoubleClickAction() {
//		tv.addDoubleClickListener(new IDoubleClickListener() {
//			public void doubleClick(DoubleClickEvent event) {
//				doubleClickAction.run();
//			}
//		});
//	}
//	private void showMessage(String message) {
//		MessageDialog.openInformation(
//			tv.getControl().getShell(),
//			"Boot Dashboard",
//			message);
//	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		tv.getControl().setFocus();
	}
}