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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
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
import org.springframework.ide.eclipse.boot.dash.views.AddRunTargetAction;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springframework.ide.eclipse.boot.dash.views.RunStateAction;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * Displays all runtargets and elements in a single 'unified' tree viewer.
 *
 * @author Kris De Volder
 */
public class BootDashUnifiedTreeSection extends PageSection implements MultiSelectionSource {

	//TODO: update nodes in representing section when element in any of section changes
	//TODO: update treeviewer when models / added removed
	//TODO: update treeviewer when elements in section added / removed
	//TODO: label provider for section nodes
	//TODO: label provider for element nodes with runstate animation

	private CustomTreeViewer tv;
	private BootDashViewModel model;
	private MultiSelection<BootDashElement> selection;
	private BootDashActions actions;
	private UserInteractions ui;
	private LiveExpression<Filter<BootDashElement>> searchFilterModel;
	private Stylers stylers;

	public class MySorter extends ViewerSorter {
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

	public class CustomTreeViewer extends TreeViewer {

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

	public BootDashUnifiedTreeSection(IPageWithSections owner, BootDashViewModel model) {
		super(owner);
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

		actions = new BootDashActions(model, null, getSelection(), ui);
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

		//TODO: must listen on all section models and change listener when section models get added / removed.
		//model.getElements().addListener(ELEMENTS_SET_LISTENER);

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
									if (o instanceof BootDashElement) {
										set.add((BootDashElement) o);
									}
									//TODO: if section is selected then add all elements from the section to the selection?
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
			addViewerSelectionListener();
		}
		return selection;
	}

	private void addViewerSelectionListener() {
		tv.setSelection(new StructuredSelection(Arrays.asList(selection.getValue().toArray(new BootDashElement[selection.getValue().size()]))));
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selection.getElements().refresh();
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
		return Validator.OK;
	}
}
