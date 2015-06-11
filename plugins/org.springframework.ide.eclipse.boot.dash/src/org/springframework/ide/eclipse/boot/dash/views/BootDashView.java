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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.DefaultUserInteractions.UIContext;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.ui.TableResizeHelper;

/**
 * @author Kris De Volder
 */
public class BootDashView extends ViewPart implements UIContext {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.springframework.ide.eclipse.boot.dash.views.BootDashView";

	private BootDashModel model = BootDashActivator.getDefault().getModel();

	private TableViewer tv;
	private Action refreshAction;
//	private Action doubleClickAction;

	private RunStateAction[] runStateActions;

	private AbstractBootDashAction openConsoleAction;
	private OpenLaunchConfigAction openConfigAction;

	private UserInteractions ui = new DefaultUserInteractions(this);

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

		for (BootDashColumn columnType : BootDashColumn.values()) {
			TableViewerColumn c1viewer = new TableViewerColumn(tv, columnType.getAllignment());
			c1viewer.getColumn().setWidth(columnType.getDefaultWidth());
			c1viewer.getColumn().setText(columnType.getLabel());
			c1viewer.setLabelProvider(new BootDashLabelProvider(columnType));
		}
		new TableResizeHelper(tv).enableResizing();

		//Create the help context id for the viewer's control
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(tv.getControl(), "org.springframework.ide.eclipse.boot.dash.viewer");
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
						updateActionEnablement();
					}
				});
			}
		});

		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActionEnablement();
			}
		});

		updateActionEnablement();
	}

	protected void updateActionEnablement() {
		Collection<BootDashElement> selecteds = getSelectedElements();
		for (RunStateAction a : runStateActions) {
			a.updateEnablement(selecteds);
		}
		openConsoleAction.updateEnablement(selecteds);
		openConfigAction.updateEnablement(selecteds);
	}

	public List<BootDashElement> getSelectedElements() {
		try {
			IStructuredSelection selection = (IStructuredSelection)tv.getSelection();
			Object[] array = selection.toArray();
			if (array!=null && array.length>0) {
				ArrayList<BootDashElement> result = new ArrayList<BootDashElement>();
				for (Object o : array) {
					if (o instanceof BootDashElement) {
						result.add((BootDashElement) o);
					}
				}
				return result;
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return Collections.emptyList();
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
//		getSite().registerContextMenu(menuMgr, tv);
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
		for (RunStateAction a : runStateActions) {
			manager.add(a);
		}
		manager.add(openConfigAction);
		manager.add(openConsoleAction);
		addPreferedConfigSelectionMenu(manager);
		manager.add(new Separator());
		manager.add(refreshAction);
//		manager.add(action2);
		// Other plug-ins can contribute there actions here
//		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}


	private void addPreferedConfigSelectionMenu(IMenuManager parent) {
		List<BootDashElement> selecteds = getSelectedElements();
		if (selecteds.size()==1) {
			BootDashElement element = selecteds.get(0);
			ILaunchConfiguration defaultConfig = element.getConfig();
			List<ILaunchConfiguration> allConfigs = element.getTarget().getLaunchConfigs(element);
			if (!allConfigs.isEmpty()) {
				MenuManager menu = new MenuManager("Default Config...");
				parent.add(menu);
				for (ILaunchConfiguration conf : allConfigs) {
					menu.add(selectDefaultConfigAction(element, defaultConfig, conf));
				}
			}
		}

	}

	private IAction selectDefaultConfigAction(
			final BootDashElement target,
			final ILaunchConfiguration currentDefault,
			final ILaunchConfiguration newDefault
	) {
		Action action = new Action(newDefault.getName(), SWT.CHECK) {
			@Override
			public void run() {
				target.setConfig(newDefault);
				//target.openConfig(getSite().getShell());
			}
		};
		action.setChecked(newDefault.equals(currentDefault));
		action.setToolTipText("Make '"+newDefault.getName()+"' the default launch configuration. It will"
				+ "be used the next time you (re)launch '"+target.getName());
		return action;
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		for (RunStateAction a : runStateActions) {
			manager.add(a);
		}
		manager.add(openConsoleAction);
		manager.add(openConfigAction);
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

		RunStateAction restartAction = new RunOrDebugStateAction(RunState.RUNNING);
		restartAction.setText("(Re)start");
		restartAction.setToolTipText("Start or restart the process associated with the selected elements");
		restartAction.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/restart.gif"));
		restartAction.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/restart_disabled.gif"));

		RunStateAction rebugAction = new RunOrDebugStateAction(RunState.DEBUGGING);
		rebugAction.setText("(Re)debug");
		rebugAction.setToolTipText("Start or restart the process associated with the selected elements in debug mode");
		rebugAction.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/rebug.png"));
		rebugAction.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/rebug_disabled.png"));

		RunStateAction stopAction = new RunStateAction(RunState.RUNNING) {
			@Override
			protected boolean currentStateAcceptable(RunState s) {
				return s==RunState.DEBUGGING || s==RunState.RUNNING;
			}
			@Override
			protected Job createJob() {
				final Collection<BootDashElement> selecteds = getSelectedElements();
				if (!selecteds.isEmpty()) {
					return new Job("Stopping "+selecteds.size()+" Boot Dash Elements") {
						protected IStatus run(IProgressMonitor monitor) {
							monitor.beginTask("Stopping "+selecteds.size()+" Elements", selecteds.size());
							try {
								for (BootDashElement el : selecteds) {
									monitor.subTask("Stopping: "+el.getName());
									try {
										el.stopAsync();
									} catch (Exception e) {
										return BootActivator.createErrorStatus(e);
									}
									monitor.worked(1);
								}
								return Status.OK_STATUS;
							} finally {
								monitor.done();
							}
						}
					};
				}
				return null;
			}
		};
		stopAction.setText("Stop");
		stopAction.setToolTipText("Stop the process(es) associated with the selected elements");
		stopAction.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/stop.gif"));
		stopAction.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/stop_disabled.gif"));

		runStateActions = new RunStateAction[] {
			restartAction, rebugAction, stopAction
		};

		openConfigAction = new OpenLaunchConfigAction(this);
		openConsoleAction = new OpenConsoleAction(this);

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

	class RunOrDebugStateAction extends RunStateAction {

		public RunOrDebugStateAction(RunState goalState) {
			super(goalState);
			Assert.isLegal(goalState==RunState.RUNNING || goalState==RunState.DEBUGGING);
		}

		@Override
		protected Job createJob() {
			final Collection<BootDashElement> selecteds = getSelectedElements();
			if (!selecteds.isEmpty()) {
				return new Job("Restarting "+selecteds.size()+" Dash Elements") {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Restart Boot Dash Elements", selecteds.size());
						try {
							for (BootDashElement el : selecteds) {
								monitor.subTask("Restarting: "+el.getName());
								try {
									el.restart(goalState, getUserInteractions());
								} catch (Exception e) {
									return BootActivator.createErrorStatus(e);
								}
								monitor.worked(1);
							}
							return Status.OK_STATUS;
						} finally {
							monitor.done();
						}
					}
				};
			}
			return null;
		}
	}

	public UserInteractions getUserInteractions() {
		return ui;
	}

	@Override
	public Shell getShell() {
		return getSite().getShell();
	}

}