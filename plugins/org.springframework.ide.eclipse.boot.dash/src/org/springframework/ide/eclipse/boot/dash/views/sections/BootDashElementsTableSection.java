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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ViewSettingsDialog;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelectionSource;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.livexp.ui.ReflowUtil;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElementUtil;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.Filter;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.util.HiddenElementsLabel;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashAction;
import org.springframework.ide.eclipse.boot.dash.views.AddRunTargetAction;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.views.BootDashCellLabelProvider;
import org.springframework.ide.eclipse.boot.dash.views.BootDashContentProvider;
import org.springframework.ide.eclipse.boot.dash.views.RunStateAction;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * A section that contains a table viewer widget displaying Boot Dash Elements from a model.
 *
 * @author Kris De Volder
 */
public class BootDashElementsTableSection extends PageSection implements MultiSelectionSource, Disposable {

	public class CustomTableViewer extends TableViewer {

		private LiveVariable<Integer> hiddenElementCount = new LiveVariable<Integer>(0);

		public CustomTableViewer(Composite page, int style) {
			super(page, style);
		}


		@Override
		protected Object[] getFilteredChildren(Object parent) {
			int totalElements = sizeof(getRawChildren(parent));
			Object[] filteredElements = super.getFilteredChildren(parent);
			hiddenElementCount.setValue(totalElements - sizeof(filteredElements));
			return filteredElements;
		}

		private int sizeof(Object[] os) {
			if (os!=null) {
				return os.length;
			}
			return 0;
		}

	}

	private static final String COLUMN_DATA = "boot-dash-column-data";

	private CustomTableViewer tv;
	private BootDashViewModel viewModel;
	private BootDashModel model;
	private MultiSelection<BootDashElement> selection;
	private BootDashActions actions;
	private UserInteractions ui;
	private LiveVariable<ViewerCell> hoverCell;
	private LiveExpression<BootDashElement> hoverElement;
	private LiveExpression<Filter<BootDashElement>> searchFilterModel;

	final private ValueListener<Filter<BootDashElement>> FILTER_LISTENER = new ValueListener<Filter<BootDashElement>>() {
		public void gotValue(LiveExpression<Filter<BootDashElement>> exp, Filter<BootDashElement> value) {
			tv.refresh();
			final Table t = tv.getTable();
			t.getDisplay().asyncExec(new Runnable() {
				public void run() {
					Composite parent = t.getParent();
					parent.layout();
				}
			});
		}
	};

	final private UIValueListener<Set<BootDashElement>> ELEMENTS_SET_LISTENER = new UIValueListener<Set<BootDashElement>>() {
		@Override
		protected void uiGotValue(LiveExpression<Set<BootDashElement>> exp,
				Set<BootDashElement> value) {
			final Control control = tv.getControl();
			if (!control.isDisposed()) {
				tv.refresh();
				control.getDisplay().asyncExec(new Runnable() {
					public void run() {
						control.getParent().layout();
					}
				});
			}
		}
	};

	final private ElementStateListener ELEMENT_STATE_LISTENER = new ElementStateListener() {
		public void stateChanged(final BootDashElement e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (tv != null && !tv.getControl().isDisposed()) {
						tv.update(e, null);
					}
				}
			});
		}
	};

	private LiveVariable<MouseEvent> tableMouseEvent;

	private ValueListener<MouseEvent> tableMouseEventListener;
	private Stylers stylers;

	private BootDashColumnModel[] columnModels;

	public BootDashElementsTableSection(IPageWithSections owner, BootDashViewModel viewModel, BootDashModel model,
			LiveExpression<Filter<BootDashElement>> searchFilterModel,
			LiveVariable<MouseEvent> tableMouseEvent,
			UserInteractions ui
	) {
		super(owner);
		this.viewModel = viewModel;
		this.model = model;
		this.ui = ui;
		this.searchFilterModel = searchFilterModel;
		this.tableMouseEvent = tableMouseEvent;
	}

	class NameSorter extends ViewerSorter {
	}

	protected CellLabelProvider getLabelProvider(BootDashColumn columnType) {
		return new BootDashCellLabelProvider(tv, columnType, stylers);
	}

	@Override
	public void createContents(Composite page) {
		// Cleanup first
		if (tv != null && !tv.getControl().isDisposed()) {
			tv.getControl().dispose();
		}

		// Initialize column table state objects
		initColumnModels();

		// Create table viewer again and attach all listeners.
		tv = new CustomTableViewer(page, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.NO_SCROLL); // Note: No SWT.SCROLL options.
		// Assumes its up to the page to be scrollable.
		tv.setContentProvider(new BootDashContentProvider(model));
		tv.setSorter(new NameSorter());
		tv.setInput(model);
		tv.getTable().setHeaderVisible(true);
		tv.getTable().setLinesVisible(true);

		stylers = new Stylers(tv.getTable().getFont());

		GridDataFactory.fillDefaults().grab(true, false).applyTo(tv.getControl());

		HiddenElementsLabel hiddenElementsLabel = new HiddenElementsLabel(page, tv.hiddenElementCount);

		Arrays.sort(columnModels, BootDashColumnModel.INDEX_COMPARATOR);
		for (final BootDashColumnModel columnModel :  columnModels) {
			if (columnModel.getVisibility()) {
				createColumn(columnModel);
			}
		}
		addSingleClickHandling();

		tv.getControl().addControlListener(new ControlListener() {
			public void controlResized(ControlEvent e) {
				ReflowUtil.reflow(owner, tv.getControl());
			}

			public void controlMoved(ControlEvent e) {
			}
		});

		actions = new BootDashActions(viewModel, model, getSelection(), ui);
		hookContextMenu();

		// Careful, either selection or tableviewer might be created first.
		// in either case we must make sure the listener is added when *both*
		// have been created.
		if (selection != null) {
			addTableSelectionListener();
		}

		tv.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (selection != null) {
					BootDashElement selected = selection.getSingle();
					if (selected != null) {
						String url = BootDashElementUtil.getUrl(selected, selected.getDefaultRequestMappingPath());
						if (url != null) {
							UiUtil.openUrl(url);
						}
					}
				}
			}
		});

		model.getElements().addListener(ELEMENTS_SET_LISTENER);

		model.addElementStateListener(ELEMENT_STATE_LISTENER);

		if (searchFilterModel != null) {
			searchFilterModel.addListener(FILTER_LISTENER);
			tv.addFilter(new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (searchFilterModel.getValue() != null && element instanceof BootDashElement) {
						return searchFilterModel.getValue().accept((BootDashElement) element);
					}
					return true;
				}
			});
		}

		tv.getTable().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				tableMouseEvent.setValue(null);

				model.getElements().removeListener(ELEMENTS_SET_LISTENER);

				model.removeElementStateListener(ELEMENT_STATE_LISTENER);

				if (searchFilterModel!=null) {
					searchFilterModel.removeListener(FILTER_LISTENER);
				}

				hoverCell = null;
				hoverElement = null;

				if (tableMouseEventListener!=null) {
					tableMouseEvent.removeListener(tableMouseEventListener);
					tableMouseEventListener = null;
				}
				if (actions!=null) {
					actions.dispose();
					actions = null;
				}
				if (stylers != null) {
					stylers.dispose();
					stylers = null;
				}
			}
		});

		addDragSupport(tv);

		ReflowUtil.reflow(owner, tv.getControl());
	}

	private void initColumnModels() {
		if (model.getRunTarget() == null || model.getRunTarget().getAllColumns().length == 0) {
			columnModels = new BootDashColumnModel[0];
		} else {
			columnModels = new BootDashColumnModel[model.getRunTarget().getAllColumns().length];
			EnumSet<BootDashColumn> defaultVisibleColumns = EnumSet.copyOf(Arrays.asList(model.getRunTarget().getDefaultColumns()));
			for (int i = 0; i < model.getRunTarget().getAllColumns().length; i++) {
				BootDashColumn column = model.getRunTarget().getAllColumns()[i];
				columnModels[i] = new BootDashColumnModel(column, defaultVisibleColumns.contains(column), i, model.getRunTarget().getId());
			}
		}
	}

	public void addSingleClickHandling() {
		final AbstractBootDashAction[] singleClickActions = new AbstractBootDashAction[model.getRunTarget().getAllColumns().length];
		boolean hasSingleClickActions = false;
		for (int i = 0; i < model.getRunTarget().getAllColumns().length; i++) {
			BootDashColumn columnType = model.getRunTarget().getAllColumns()[i];
			BootDashActionFactory factory = columnType.getSingleClickAction();
			if (factory!=null) {
				hasSingleClickActions = true;
				singleClickActions[i] = factory.create(viewModel, getHoverElement(), ui);
			}
		}

		if (hasSingleClickActions) {
			final Cursor defaultCursor = tv.getTable().getCursor();
			getHoverCell().addListener(new ValueListener<ViewerCell>() {
				public void gotValue(LiveExpression<ViewerCell> exp, ViewerCell cell) {
					Table table = tv.getTable();
					if (cell!=null) {
						int colIdx = cell.getColumnIndex();
						AbstractBootDashAction action = singleClickActions[colIdx];
						if (action!=null && action.isEnabled()) {
							table.setCursor(CursorProviders.HAND_CURSOR.getCursor(cell));
							return;
						}
					}
					table.setCursor(defaultCursor);
				}
			});

			tv.getTable().addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					if (tableMouseEvent!=null) {
						tableMouseEvent.setValue(e);
					}

					ViewerCell cell = tv.getCell(new Point(e.x,e.y));
					if (cell!=null) {
						Object el = cell.getElement();
						if (el instanceof BootDashElement) {
							int idx = cell.getColumnIndex();
							AbstractBootDashAction action = singleClickActions[idx];
							if (action!=null) {
								action.run();
							}
						}
					}
				}
			});
		}
	}

	/**
	 * Selection used for 'single click' actions. This represents the
	 * element the mouse is hovering over.
	 */
	private synchronized LiveExpression<ViewerCell> getHoverCell() {
		if (hoverCell==null) {
			hoverCell = new LiveVariable<ViewerCell>();
			tv.getTable().addMouseMoveListener(new MouseMoveListener() {
				public void mouseMove(MouseEvent e) {
					Point pos = new Point(e.x, e.y);
					hoverCell.setValue(tv.getCell(pos));
				}
			});
		}
		return hoverCell;
	}

	/**
	 * Selection used for 'single click' actions. This represents the
	 * element the mouse is hovering over.
	 */
	private synchronized LiveExpression<BootDashElement> getHoverElement() {
		if (hoverElement==null) {
			final LiveExpression<ViewerCell> hoverCell = getHoverCell();
			hoverElement = new LiveExpression<BootDashElement>() {
				{
					dependsOn(hoverCell);
				}
				protected BootDashElement compute() {
					ViewerCell cell = hoverCell.getValue();
					if (cell!=null) {
						Object e = cell.getElement();
						if (e instanceof BootDashElement) {
							return (BootDashElement) e;
						}
					}
					return null;
				}
			};
		}
		return hoverElement;
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(tv.getControl());
		tv.getControl().setMenu(menu);
	}

	private void fillContextMenu(IMenuManager manager) {
		for (RunStateAction a : actions.getRunStateActions()) {
			manager.add(a);
		}
		manager.add(actions.getOpenConfigAction());
		manager.add(actions.getOpenConsoleAction());
		manager.add(actions.getShowPropertiesViewAction());
		for (AddRunTargetAction a : actions.getAddRunTargetActions()) {
			manager.add(a);
		}

		IAction removeTargetAction = actions.getRemoveRunTargetAction();
		if (removeTargetAction != null) {
			manager.add(removeTargetAction);
		}

		IAction refreshAction = actions.getRefreshRunTargetAction();
		if (refreshAction != null) {
			manager.add(refreshAction);
		}

		IAction deleteAppsAction = actions.getDeleteApplicationsAction();
		if (deleteAppsAction != null) {
			manager.add(deleteAppsAction);
		}

		IAction updatePasswordAction = actions.getUpdatePasswordAction();
		if (updatePasswordAction != null) {
			manager.add(updatePasswordAction);
		}

		addPreferedConfigSelectionMenu(manager);

		if (tv.getTable().getColumnCount() > 0) {
			manager.add(new RestoreDefaultColumnsAction("Restore Default Columns"));
			manager.add(new CustomizeColumnsAction("Customize Columns..."));
		}

//		manager.add
//		manager.add(new Separator());
//		manager.add(refreshAction);
//		manager.add(action2);
		// Other plug-ins can contribute there actions here
//		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void addPreferedConfigSelectionMenu(IMenuManager parent) {
		BootDashElement element = selection.getSingle();
		if (element!=null) {
			ILaunchConfiguration defaultConfig = element.getPreferredConfig();
			List<ILaunchConfiguration> allConfigs = element.getTarget().getLaunchConfigs(element);
			if (!allConfigs.isEmpty()) {
				MenuManager menu = new MenuManager("Default Config...");
				parent.add(menu);
				for (ILaunchConfiguration conf : allConfigs) {
					menu.add(actions.selectDefaultConfigAction(element, defaultConfig, conf));
				}
			}
		}
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
		if (tv!=null) {
			addTableSelectionListener();
		}
		tableMouseEvent.addListener(tableMouseEventListener = new ValueListener<MouseEvent>() {
			public void gotValue(LiveExpression<MouseEvent> exp, MouseEvent evt) {
				//When user clicks in another table than the current table. Clear the current-table's selection
				if (evt!=null) {
					if (evt.widget!=tv.getTable()) { //only interested in clicks in other tables.
						if ((evt.stateMask&SWT.CTRL)==0) { //CTRL not held down.
							tv.setSelection(StructuredSelection.EMPTY);
						}
					}
				}
			}
		});
		return selection;
	}

	private void addTableSelectionListener() {
		tv.setSelection(new StructuredSelection(Arrays.asList(selection.getValue().toArray(new BootDashElement[selection.getValue().size()]))));
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selection.getElements().refresh();
			}
		});
	}

	public void dispose() {
	}

	private void createColumn(final BootDashColumnModel columnModel) {
		TableViewerColumn viewer = new TableViewerColumn(tv, columnModel.getAllignment());
		final TableColumn tc = viewer.getColumn();
		tc.setText(columnModel.getLabel());
		tc.setWidth(columnModel.getWidth());
		tc.setResizable(true);
		tc.setMoveable(true);
		tc.setData(COLUMN_DATA, columnModel);

		viewer.setLabelProvider(getLabelProvider(columnModel.getType()));

		if (columnModel.getEditingSupport() != null) {
			viewer.setEditingSupport(columnModel.getEditingSupport().createEditingSupport(tv, getSelection().toSingleSelection(), viewModel, stylers));
		}

		tc.addControlListener(new ControlAdapter() {

			private int currentIndex = tv.getTable().getColumnCount() - 1;

			@Override
			public void controlMoved(ControlEvent e) {
				int i = 0;
				int[] order = tv.getTable().getColumnOrder();
				while (i < order.length && tv.getTable().getColumn(order[i]) != tc) {
					i++;
				}
				/*
				 * Move event is caused by resizing as well therefore update the
				 * column order if it was changed indeed
				 */
				if (i != currentIndex) {
					columnModel.setIndex(i);
					currentIndex = i;
				}
			}

			@Override
			public void controlResized(ControlEvent e) {
				columnModel.setWidth(tc.getWidth());
				ReflowUtil.reflow(owner, tv.getControl());
			}

		});
	}

	private class RestoreDefaultColumnsAction extends Action {

		RestoreDefaultColumnsAction(String label) {
			super(label);
		}

		@Override
		public boolean isEnabled() {
			return tv.getTable().getColumnCount() > 0;
		}

		@Override
		public void run() {
			for (BootDashColumnModel columnModel : columnModels) {
				columnModel.restoreDefaults();
			}
			BootDashElementsTableSection.this.createContents(tv.getControl().getParent());
		}
	}

	private class CustomizeColumnsAction extends Action {

		CustomizeColumnsAction(String label) {
			super(label);
		}

		@Override
		public boolean isEnabled() {
			return tv.getTable().getColumnCount() > 0;
		}

		@Override
		public void run() {
			ViewSettingsDialog viewSettingsDialog = new ViewSettingsDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell()) {

				private Button[] editors;
				private BootDashColumnModel[] sortedModels;

				@Override
				protected Control createDialogArea(Composite parent) {
					Composite top = (Composite) super.createDialogArea(parent);

					Group group = new Group(top, SWT.NONE);
					group.setText("Columns Visibility");
					group.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
					group.setLayout(new GridLayout());

					editors = new Button[columnModels.length];

					sortedModels = Arrays.copyOf(columnModels, columnModels.length);
					Arrays.sort(sortedModels, BootDashColumnModel.DEFAULT_INDEX_COMPARATOR);
					for (int i = 0; i < sortedModels.length; i++) {
						Button editor = new Button(group, SWT.CHECK);
						editor.setText(sortedModels[i].getType().getLongLabel());
						editor.setSelection(sortedModels[i].getVisibility());
						editors[i] = editor;
					}

					Dialog.applyDialogFont(top);

					return top;
				}

				@Override
				protected void performDefaults() {
					for (int i = 0; i < editors.length; i++) {
						editors[i].setSelection(sortedModels[i].getDefaultVisibility());
					}
				}

				@Override
				protected void okPressed() {
					for (int i = 0; i < editors.length; i++) {
						sortedModels[i].setVisibility(editors[i].getSelection());
					}
					super.okPressed();
					BootDashElementsTableSection.this.createContents(tv.getControl().getParent());
				}

				@Override
				protected void configureShell(Shell newShell) {
					super.configureShell(newShell);
					newShell.setText("Columns Settings");
				}

			};
			viewSettingsDialog.open();
		}

	}

	private void addDragSupport(final TableViewer viewer) {

		if (model.getRunTarget().canDeployAppsFrom()) {
			int ops = DND.DROP_COPY;

			Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };

			DragSourceAdapter listener = new DragSourceAdapter() {
				@Override
				public void dragSetData(DragSourceEvent event) {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					event.data = selection.getFirstElement();
					LocalSelectionTransfer.getTransfer().setSelection(selection);
				}

				@Override
				public void dragStart(DragSourceEvent event) {
					if (event.detail == DND.DROP_NONE || event.detail == DND.DROP_DEFAULT) {
						event.detail = DND.DROP_COPY;
					}
					dragSetData(event);
				}
			};

			viewer.addDragSupport(ops, transfers, listener);
		}
	}
}
