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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
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
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashAction;
import org.springframework.ide.eclipse.boot.dash.views.AddRunTargetAction;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.views.BootDashContentProvider;
import org.springframework.ide.eclipse.boot.dash.views.BootDashLabelProvider;
import org.springframework.ide.eclipse.boot.dash.views.BootDashView;
import org.springframework.ide.eclipse.boot.dash.views.RunStateAction;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;
import org.springsource.ide.eclipse.commons.ui.TableResizeHelper;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * A section that contains a table viewer widget displaying Boot Dash Elements from a model.
 *
 * @author Kris De Volder
 */
public class BootDashElementsTableSection extends PageSection implements MultiSelectionSource, Disposable {
	
	private static final String PREF_KEY_SEPARATOR = "___";
	
	private static final String PREF_KEY_ORDER_SUFFIX = "order";

	private TableViewer tv;
	private BootDashViewModel viewModel;
	private BootDashModel model;
	private BootDashColumn[] enabledColumns = BootDashColumn.values();
	private MultiSelection<BootDashElement> selection;
	private BootDashActions actions;
	private UserInteractions ui;
	private LiveVariable<ViewerCell> hoverCell;
	private LiveExpression<BootDashElement> hoverElement;
	private LiveExpression<Filter<BootDashElement>> searchFilterModel;
	private ValueListener<Filter<BootDashElement>> filterListener;

	public BootDashElementsTableSection(IPageWithSections owner, BootDashViewModel viewModel, BootDashModel model) {
		this(owner, viewModel, model, null);
	}

	public BootDashElementsTableSection(IPageWithSections owner, BootDashViewModel viewModel, BootDashModel model, LiveExpression<Filter<BootDashElement>> searchFilterModel) {
		super(owner);
		this.viewModel = viewModel;
		this.model = model;
		this.ui = owner instanceof BootDashView ? ((BootDashView)owner).getUserInteractions() : null;
		this.searchFilterModel = searchFilterModel;
	}

	class NameSorter extends ViewerSorter {
	}

	public void setColumns(BootDashColumn... columns) {
		this.enabledColumns = columns;
	}

	protected CellLabelProvider getLabelProvider(BootDashColumn columnType) {
		return new BootDashLabelProvider(columnType);
	}

	@Override
	public void createContents(final Composite page) {
		tv = new TableViewer(page, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.NO_SCROLL); // Note: No SWT.SCROLL options.
																	// Assumes its up to the page to be scrollable.
		tv.setContentProvider(new BootDashContentProvider(model));
		//tv.setLabelProvider(new ViewLabelProvider());
		tv.setSorter(new NameSorter());
		tv.setInput(model);
		tv.getTable().setHeaderVisible(true);

		//tv.getTable().setLinesVisible(true);

		GridDataFactory.fillDefaults().grab(true, false).applyTo(tv.getControl());

		for (final BootDashColumn columnType : enabledColumns ) {
			initColumn(new TableViewerColumn(tv, columnType.getAllignment()), columnType);
		}
		
		initColumnsOrder();

		addSingleClickHandling();

		new TableResizeHelper(tv).enableResizing();

		model.getElements().addListener(new UIValueListener<Set<BootDashElement>>() {
			@Override
			protected void uiGotValue(LiveExpression<Set<BootDashElement>> exp,
					Set<BootDashElement> value) {
				if (!tv.getControl().isDisposed()) {
					tv.refresh();
					page.getParent().layout(new Control[]{tv.getControl()});
				}
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

		tv.getControl().addControlListener(new ControlListener() {
			public void controlResized(ControlEvent e) {
				ReflowUtil.reflow(owner, tv.getControl());
			}
			public void controlMoved(ControlEvent e) {
			}
		});

		actions = new BootDashActions(viewModel, model, getSelection(), ui);
		hookContextMenu();

		//Careful, either selection or tableviewer might be created first.
		// in either case we must make sure the listener is added when *both*
		// have been created.
		if (selection!=null) {
			addTableSelectionListener();
		}
		tv.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (selection!=null) {
					BootDashElement selected = selection.getSingle();
					if (selected!=null) {
						String url = BootDashElementUtil.getUrl(selected, selected.getDefaultRequestMappingPath());
						if (url!=null) {
							UiUtil.openUrl(url);
						}
					}
				}
			}
		});

		if (searchFilterModel!=null) {
			tv.addFilter(new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (searchFilterModel.getValue() != null && element instanceof BootDashElement) {
						return searchFilterModel.getValue().accept((BootDashElement) element);
					}
					return true;
				}
			});
			searchFilterModel.addListener(this.filterListener = new ValueListener<Filter<BootDashElement>>() {
				public void gotValue(LiveExpression<Filter<BootDashElement>> exp, Filter<BootDashElement> value) {
					tv.refresh();
					Table t = tv.getTable();
					Composite parent = t.getParent();
					parent.layout(new Control[]{t});
				}
			});
		}
		
		tv.getTable().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				persistTableVisuals();
				tv.getTable().removeDisposeListener(this);
			}
		});

	}

	public void addSingleClickHandling() {
		final AbstractBootDashAction[] singleClickActions = new AbstractBootDashAction[enabledColumns.length];
		boolean hasSingleClickActions = false;
		for (int i = 0; i < enabledColumns.length; i++) {
			BootDashColumn columnType = enabledColumns[i];
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
		for (AddRunTargetAction a : actions.getAddRunTargetActions()) {
			manager.add(a);
		}
		
		IAction refreshAction = actions.getRefreshRunTargetAction();
		if (refreshAction != null) {
			manager.add(refreshAction);
		}

		addPreferedConfigSelectionMenu(manager);
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
				target.setPreferredConfig(newDefault);
				//target.openConfig(getSite().getShell());
			}
		};
		action.setChecked(newDefault.equals(currentDefault));
		action.setToolTipText("Make '"+newDefault.getName()+"' the default launch configuration. It will"
				+ "be used the next time you (re)launch '"+target.getName());
		return action;
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
		return selection;
	}

	private void addTableSelectionListener() {
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selection.getElements().refresh();
			}
		});
	}

	public void dispose() {
		if (filterListener!= null) {
			searchFilterModel.removeListener(filterListener);
			filterListener = null;
		}
		if (actions!=null) {
			actions.dispose();
			actions = null;
		}
	}
	
	private void persistTableVisuals() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(BootDashActivator.PLUGIN_ID);
		String prefix = model.getRunTarget() == null || model.getRunTarget().getId() == null ? "" : model.getRunTarget().getId();
		if (isDefaultOrder(tv.getTable().getColumnOrder())) {
			prefs.remove(prefix + PREF_KEY_SEPARATOR + PREF_KEY_ORDER_SUFFIX);			
		} else {
			prefs.put(prefix + PREF_KEY_SEPARATOR + PREF_KEY_ORDER_SUFFIX, serializeColumnOrder(tv.getTable().getColumnOrder()));
		}
		for (TableColumn tc : tv.getTable().getColumns()) {
			if (tc.getData() instanceof BootDashColumn) {
				BootDashColumn columnType = (BootDashColumn) tc.getData();
				if (columnType.getDefaultWidth() == tc.getWidth()) {
					prefs.remove(prefix + PREF_KEY_SEPARATOR + columnType.toString());
				} else {
					if (tc.getWidth() == 0 && !tc.getMoveable() && !tc.getResizable()) {
						prefs.putInt(prefix + PREF_KEY_SEPARATOR + columnType.toString(), -1);
					} else {
						prefs.putInt(prefix + PREF_KEY_SEPARATOR + columnType.toString(), tc.getWidth());
					}
				}
			}
		}
	}
	
	private boolean isDefaultOrder(int[] order) {
		int i = 0;
		for (; i < order.length && order[i] == i; i++);
		return i == order.length;
	}
	
	private static String serializeColumnOrder(int[] order) {
		StringBuilder sb = new StringBuilder();
		if (order.length > 0) {
			for (int i = 0; i < order.length -1; i++) {
				sb.append(order[i]);
				sb.append(',');
			}
			sb.append(order[order.length - 1]);
		}
		return sb.toString();
	}
	
	private static int[] parseColumnOrder(String s) {
		String[] split = s == null || s.isEmpty() ? new String[0] : s.split(",");
		int[] order = new int[split.length];
		for (int i = 0; i < split.length; i++) {
			order[i] = Integer.parseInt(split[i]);
		}
		return order;
	}
	
	private void initColumnsOrder() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(BootDashActivator.PLUGIN_ID);
		String prefix = model.getRunTarget() == null || model.getRunTarget().getId() == null ? "" : model.getRunTarget().getId();
		String orderStr = prefs.get(prefix + PREF_KEY_SEPARATOR + PREF_KEY_ORDER_SUFFIX, null);
		if (orderStr != null) {
			try {
				tv.getTable().setColumnOrder(parseColumnOrder(orderStr));
			} catch (Throwable t) {
				BootDashActivator.getDefault().getLog().log(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "Failed to initialize column order for Boot Dashboard table '" + prefix + "'", t));
			}
		}
	}
	
	private void initColumn(TableViewerColumn viewer, BootDashColumn column) {
		TableColumn tc = viewer.getColumn();
		tc.setText(column.getLabel());
		tc.setData(column);
		String prefix = model.getRunTarget() == null || model.getRunTarget().getId() == null ? ""
				: model.getRunTarget().getId();
		int width = InstanceScope.INSTANCE.getNode(BootDashActivator.PLUGIN_ID)
				.getInt(prefix + PREF_KEY_SEPARATOR + column.toString(), column.getDefaultWidth());
		if (width == -1) {
			tc.setWidth(0);
			tc.setMoveable(false);
			tc.setResizable(false);
		} else {
			tc.setWidth(width);
			tc.setMoveable(true);
			tc.setResizable(true);
		}
		viewer.setLabelProvider(getLabelProvider(column));
		if (column.getEditingSupportClass() != null) {
			try {
				viewer.setEditingSupport(column.getEditingSupportClass().getConstructor(TableViewer.class, LiveExpression.class)
						.newInstance(tv, getSelection().toSingleSelection()));
			} catch (Throwable t) {
				BootDashActivator.getDefault().getLog().log(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "Failed to initialize cell editor for column " + column.getLabel(), t));
			}
		}
	}
	
}
