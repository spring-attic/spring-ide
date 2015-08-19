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
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.dash.livexp.ElementwiseListener;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelectionSource;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.livexp.ui.ReflowUtil;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElementUtil;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ModelStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.Filter;
import org.springframework.ide.eclipse.boot.dash.model.ModifiableModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.util.HiddenElementsLabel;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashAction;
import org.springframework.ide.eclipse.boot.dash.views.AddRunTargetAction;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.views.RunStateAction;
import org.springframework.ide.eclipse.boot.properties.editor.util.ArrayUtils;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

import com.google.common.collect.ImmutableSet;

/**
 * Displays all runtargets and elements in a single 'unified' tree viewer.
 *
 * @author Kris De Volder
 */
public class BootDashUnifiedTreeSection extends PageSection implements MultiSelectionSource {

	private static final boolean DEBUG = false;

	private <T> void debug(final String name, LiveExpression<T> watchable) {
		if (DEBUG) {
			watchable.addListener(new ValueListener<T>() {
				public void gotValue(LiveExpression<T> exp, T value) {
					System.out.println(name +": "+ value);
				}
			});
		}
	}

	protected static final Object[] NO_OBJECTS = new Object[0];

	private CustomTreeViewer tv;
	private BootDashViewModel model;
	private MultiSelection<Object> mixedSelection; // selection that may contain section or element nodes or both.
	private MultiSelection<BootDashElement> selection;
	private LiveExpression<BootDashModel> sectionSelection;
	private BootDashActions actions;
	private UserInteractions ui;
	private LiveExpression<Filter<BootDashElement>> searchFilterModel;
	private Stylers stylers;

	public static class MySorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof BootDashModel && e2 instanceof BootDashModel) {
				BootDashModel m1 = (BootDashModel) e1;
				BootDashModel m2 = (BootDashModel) e2;
				return m1.getRunTarget().compareTo(m2.getRunTarget());
			}
			return super.compare(viewer, e1, e2);
		}
	}

	final private ValueListener<Filter<BootDashElement>> FILTER_LISTENER = new ValueListener<Filter<BootDashElement>>() {
		public void gotValue(LiveExpression<Filter<BootDashElement>> exp, Filter<BootDashElement> value) {
			tv.refresh();
			final Tree t = tv.getTree();
			t.getDisplay().asyncExec(new Runnable() {
				public void run() {
					Composite parent = t.getParent();
					parent.layout();
				}
			});
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

	final private ValueListener<Set<RunTarget>> RUN_TARGET_LISTENER = new UIValueListener<Set<RunTarget>>() {
		protected void uiGotValue(LiveExpression<Set<RunTarget>> exp, Set<RunTarget> value) {
			if (tv != null && !tv.getControl().isDisposed()) {
				tv.refresh();
			}
		}
	};

	private final ValueListener<Set<BootDashElement>> ELEMENTS_SET_LISTENER = new UIValueListener<Set<BootDashElement>>() {
		protected void uiGotValue(LiveExpression<Set<BootDashElement>> exp, Set<BootDashElement> value) {
			if (tv != null && !tv.getControl().isDisposed()) {
				//TODO: refreshing the whole table is overkill, but is a bit tricky to figure out which BDM
				// this set of elements belong to. If we did know then we could just refresh the node representing its section
				// only.
				tv.refresh();
			} else {
				//This listener can't easily be removed because of the intermediary adapter that adds it to a numner of different
				// things. So at least remove it when model remains chatty after view got disposed.
				exp.removeListener(this);
			}
		}
	};

	private final ModelStateListener MODEL_STATE_LISTENER = new ModelStateListener() {
		@Override
		public void stateChanged(final BootDashModel model) {
			if (PlatformUI.isWorkbenchRunning()) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (tv != null && !tv.getControl().isDisposed()) {
							tv.update(model, null);
							/*
							 * TODO: ideally the above should do the repaint of
							 * the control's area where the tree item is
							 * located, but for some reason repaint doesn't
							 * happen. #refresh() didn't trigger the repaint either
							 */
							tv.getControl().redraw();
						} else {
							model.removeModelStateListener(MODEL_STATE_LISTENER);
						}
					}
				});
			}
		}
	};

	/**
	 * Listener which adds element set listener to each section model.
	 */
	final private ValueListener<Set<BootDashModel>> ELEMENTS_SET_LISTENER_ADAPTER = new ElementwiseListener<BootDashModel>() {
		protected void added(LiveExpression<Set<BootDashModel>> exp, BootDashModel e) {
			e.getElements().addListener(ELEMENTS_SET_LISTENER);
			e.addModelStateListener(MODEL_STATE_LISTENER);
		}
		protected void removed(LiveExpression<Set<BootDashModel>> exp, BootDashModel e) {
			e.getElements().removeListener(ELEMENTS_SET_LISTENER);
			e.removeModelStateListener(MODEL_STATE_LISTENER);
		}
	};

	public static class CustomTreeViewer extends TreeViewer {

		private LiveVariable<Integer> hiddenElementCount = new LiveVariable<Integer>(0);

		public CustomTreeViewer(Composite page, int style) {
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

	public BootDashUnifiedTreeSection(IPageWithSections owner, BootDashViewModel model, UserInteractions ui) {
		super(owner);
		Assert.isNotNull(ui);
		this.ui = ui;
		this.model = model;
		this.searchFilterModel = model.getFilter();
	}

	@Override
	public void createContents(Composite page) {
		tv = new CustomTreeViewer(page, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);

		tv.setContentProvider(new BootDashTreeContentProvider());
		tv.setSorter(new MySorter());
		tv.setInput(model);
		tv.getTree().setLinesVisible(true);

		stylers = new Stylers(tv.getTree().getFont());
		tv.setLabelProvider(new BootDashTreeLabelProvider(stylers, tv));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(tv.getTree());

		new HiddenElementsLabel(page, tv.hiddenElementCount);

		tv.getControl().addControlListener(new ControlListener() {
			public void controlResized(ControlEvent e) {
				ReflowUtil.reflow(owner, tv.getControl());
			}

			public void controlMoved(ControlEvent e) {
			}
		});

		actions = new BootDashActions(model, getSelection(), getSectionSelection(), ui);
		hookContextMenu();

		// Careful, either selection or tableviewer might be created first.
		// in either case we must make sure the listener is added when *both*
		// have been created.
		if (selection != null) {
			addViewerSelectionListener();
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

		model.getRunTargets().addListener(RUN_TARGET_LISTENER);
		model.getSectionModels().addListener(ELEMENTS_SET_LISTENER_ADAPTER);

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

		tv.getTree().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				model.removeElementStateListener(ELEMENT_STATE_LISTENER);
				model.getRunTargets().removeListener(RUN_TARGET_LISTENER);
				model.getSectionModels().removeListener(ELEMENTS_SET_LISTENER_ADAPTER);
				for (BootDashModel m : model.getSectionModels().getValue()) {
					m.removeModelStateListener(MODEL_STATE_LISTENER);
				}

				if (searchFilterModel!=null) {
					searchFilterModel.removeListener(FILTER_LISTENER);
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
		addDropSupport(tv);
	}

	private LiveExpression<BootDashModel> getSectionSelection() {
		if (sectionSelection==null) {
			sectionSelection = getMixedSelection().toSingleSelection().filter(BootDashModel.class);
			debug("sectionSelection", sectionSelection);
		}
		return sectionSelection;
	}

	private synchronized MultiSelection<Object> getMixedSelection() {
		if (mixedSelection==null) {
			mixedSelection = MultiSelection.from(Object.class, new ObservableSet<Object>() {
				@Override
				protected Set<Object> compute() {
					if (tv!=null) {
						ISelection s = tv.getSelection();
						if (s instanceof IStructuredSelection) {
							Object[] elements = ((IStructuredSelection) s).toArray();
							return ImmutableSet.copyOf(elements);
						}
					}
					return Collections.emptySet();
				}
			});
			debug("mixedSelection", mixedSelection.getElements());
		}
		if (tv!=null) {
			addViewerSelectionListener();
		}
		return mixedSelection;
	}


	@Override
	public synchronized MultiSelection<BootDashElement> getSelection() {
		if (selection==null) {
			selection = getMixedSelection().filter(BootDashElement.class);
			debug("selection", selection.getElements());
		}
		return selection;
	}

	private void addViewerSelectionListener() {
		tv.setSelection(new StructuredSelection(Arrays.asList(mixedSelection.getValue().toArray())));
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				mixedSelection.getElements().refresh();
			}
		});
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
			addVisible(manager, a);
		}
		addVisible(manager, actions.getOpenConfigAction());
		addVisible(manager, actions.getOpenConsoleAction());
		addVisible(manager, actions.getShowPropertiesViewAction());

		manager.add(new Separator());
		addVisible(manager, actions.getExposeRunAppAction());
		addVisible(manager, actions.getExposeDebugAppAction());
		manager.add(new Separator());

		for (AddRunTargetAction a : actions.getAddRunTargetActions()) {
			addVisible(manager, a);
		}

		IAction removeTargetAction = actions.getRemoveRunTargetAction();
		if (removeTargetAction != null) {
			addVisible(manager, removeTargetAction);
		}

		IAction refreshAction = actions.getRefreshRunTargetAction();
		if (refreshAction != null) {
			addVisible(manager, refreshAction);
		}

		IAction restartOnlyAction = actions.getRestartOnlyApplicationAction();
		if (restartOnlyAction != null) {
			addVisible(manager, restartOnlyAction);
		}

		IAction deleteAppsAction = actions.getDeleteApplicationsAction();
		if (deleteAppsAction != null) {
			addVisible(manager, deleteAppsAction);
		}

		IAction updatePasswordAction = actions.getUpdatePasswordAction();
		if (updatePasswordAction != null) {
			addVisible(manager, updatePasswordAction);
		}

		addPreferredConfigSelectionMenu(manager);

//		manager.add
//		addVisible(manager, new Separator());
//		addVisible(manager, refreshAction);
//		addVisible(manager, action2);
		// Other plug-ins can contribute there actions here
//		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void addVisible(IMenuManager manager, IAction a) {
		if (isVisible(a)) {
			manager.add(a);
		}
	}

	private boolean isVisible(IAction a) {
		if (a instanceof AbstractBootDashAction) {
			return ((AbstractBootDashAction) a).isVisible();
		}
		return true;
	}

	private void addPreferredConfigSelectionMenu(IMenuManager parent) {
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

	private void addDragSupport(final TreeViewer viewer) {
		int ops = DND.DROP_COPY;

		final Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };

		DragSourceAdapter listener = new DragSourceAdapter() {

//			@Override
//			public void dragSetData(DragSourceEvent event) {
//				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
//				event.data = selection.getFirstElement();
//				LocalSelectionTransfer.getTransfer().setSelection(selection);
//			}
//
//			@Override
//			public void dragStart(DragSourceEvent event) {
//				if (event.detail == DND.DROP_NONE || event.detail == DND.DROP_DEFAULT) {
//					event.detail = DND.DROP_COPY;
//				}
//				dragSetData(event);
//			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				Set<BootDashElement> selection = getSelection().getValue();
				BootDashElement[] elements = selection.toArray(new BootDashElement[selection.size()]);
				LocalSelectionTransfer.getTransfer().setSelection(new StructuredSelection(elements));
				event.detail = DND.DROP_COPY;
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				if (!canDeploySelection(getSelection().getValue())) {
					event.doit = false;
				} else {
					dragSetData(event);
				}
			}
		};
		viewer.addDragSupport(ops, transfers, listener);
	}

	private void addDropSupport(final TreeViewer tv) {
		int ops = DND.DROP_COPY;
		final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };
		DropTarget dropTarget = new DropTarget(tv.getTree(), ops);
		dropTarget.setTransfer(transfers);
		dropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent event) {
				checkDropable(event);
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				checkDropable(event);
			}

			@Override
			public void dropAccept(DropTargetEvent event) {
				checkDropable(event);
			}

			private void checkDropable(DropTargetEvent event) {
				if (canDrop(event)) {
					event.detail = DND.DROP_COPY & event.operations;
				} else {
					event.detail = DND.DROP_NONE;
				}
			}

			private boolean canDrop(DropTargetEvent event) {
				BootDashModel droppedOn = getDropTarget(event);
				if (droppedOn!=null && droppedOn instanceof ModifiableModel) {
					ModifiableModel target = (ModifiableModel) droppedOn;
					if (transfer.isSupportedType(event.currentDataType)) {
						Object[] elements = getDraggedElements();
						if (ArrayUtils.hasElements(elements) && target.canBeAdded(Arrays.asList(elements))) {
							return true;
						}
					}
				}
				return false;
			}


			/**
			 * Determines which BootDashModel a droptarget event represents (i.e. what thing
			 * are we dropping or dragging onto?
			 */
			private BootDashModel getDropTarget(DropTargetEvent event) {
				Point loc = tv.getTree().toControl(new Point(event.x, event.y));
				ViewerCell cell = tv.getCell(loc);
				if (cell!=null) {
					Object el = cell.getElement();
					if (el instanceof BootDashModel) {
						return (BootDashModel) el;
					}
				}
				//Not a valid place to drop
				return null;
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (canDrop(event)) {
					BootDashModel model = getDropTarget(event);
					final Object[] elements = getDraggedElements();
					if (model instanceof ModifiableModel) {
						final ModifiableModel modifiableModel = (ModifiableModel) model;
						Job job = new Job("Performing deployment to " + model.getRunTarget().getName()) {

							@Override
							protected IStatus run(IProgressMonitor monitor) {
								if (modifiableModel != null && selection != null) {
									try {
										modifiableModel.add(Arrays.asList(elements), ui);

									} catch (Exception e) {
										ui.errorPopup("Failed to Add Element", e.getMessage());
									}
								}
								return Status.OK_STATUS;
							}

						};
						job.schedule();
					}
				}
				super.drop(event);
			}

			private Object[] getDraggedElements() {
				ISelection sel = transfer.getSelection();
				if (sel instanceof IStructuredSelection) {
					return ((IStructuredSelection)sel).toArray();
				}
				return NO_OBJECTS;
			}

		});
	}

	private boolean canDeploySelection(Set<BootDashElement> selection) {
		if (selection.isEmpty()) {
			//Careful... don't return 'true' if nothing is selected.
			return false;
		}
		for (BootDashElement e : selection) {
			if (!e.getParent().getRunTarget().canDeployAppsFrom()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return Validator.OK;
	}

}
