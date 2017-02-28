/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springframework.ide.eclipse.boot.wizard.guides.ChooseOneSection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;

public class ChooseCategorySection extends ChooseOneSection {

	private SelectionModel<String> selection;
	private String[] categories; //The elements to choose from
	private NewSpringBootWizardModel model;
	private TableViewer viewer;
	List<String> hiddenCategories = new ArrayList<>();
	
	public ChooseCategorySection(IPageWithSections owner, NewSpringBootWizardModel model, SelectionModel<String> selection) {
		super(owner);
		this.selection = selection;
		this.model = model;
		
		this.categories = model.dependencies.getCategories().toArray(new String[]{});
	
		model.getDependencyFilter().addListener(new UIValueListener<Filter<CheckBoxModel<Dependency>>>() {
			@Override
			protected void uiGotValue(
					LiveExpression<Filter<CheckBoxModel<Dependency>>> exp,
					Filter<CheckBoxModel<Dependency>> value
			) {
				applyFilter(value);
			}

		});
	
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return selection.validator;
	}
	
	protected Composite area(Composite parent) {

		Composite area = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(area);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(area);
	
		return area;
	}

	@Override
	public void createContents(Composite page) {
		Composite area = area(page);

		viewer = new TableViewer(area, SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL);
		viewer.addFilter(new ChoicesFilter());
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(categories);
		
		GridDataFactory grab = GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, getPreferredHeight());
		grab.applyTo(viewer.getTable());
		

		whenVisible(viewer.getControl(), new Runnable() {
			public void run() {
				String preSelect = selection.selection.getValue();
				if (preSelect!=null) {
					viewer.setSelection(new StructuredSelection(preSelect), true);
				} else {
					viewer.setSelection(StructuredSelection.EMPTY, true);
				}
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = viewer.getSelection();
				if (sel.isEmpty()) {
					selection.selection.setValue(null);
				} else if (sel instanceof IStructuredSelection){
					IStructuredSelection ss = (IStructuredSelection) sel;
					selection.selection.setValue((String)ss.getFirstElement());
				}
			}
		});
	}

	private int getPreferredHeight() {
		int heightCategories = 20*categories.length;
		int minHeight = 300;
		return heightCategories  > minHeight ? heightCategories : minHeight;
	}

	private void whenVisible(final Control control, final Runnable runnable) {
		PaintListener l = new PaintListener() {
			public void paintControl(PaintEvent e) {
				runnable.run();
				control.removePaintListener(this);
			}
		};
		control.addPaintListener(l);
	}
	

	private void applyFilter(Filter<CheckBoxModel<Dependency>> filter) {
		hiddenCategories.clear();
		if (viewer != null) {
			
			
			for (String cat : model.dependencies.getCategories()) {
			
				MultiSelectionFieldModel<Dependency> dependencyGroup = model.dependencies.getContents(cat);
				List<CheckBoxModel<Dependency>> checkBoxModels = dependencyGroup.getCheckBoxModels();
				boolean showCategory = false;

				if (checkBoxModels !=  null) {
					for (CheckBoxModel<Dependency> checkBoxModel : checkBoxModels) {
						// If filter applies to at least one item, show the category
						if (applyFilterOnItem(filter, checkBoxModel)) {
							showCategory = true;
							break;
						}
					}
				}
				
				if (!showCategory) {
					hiddenCategories.add(cat);
				}
				
			}
			viewer.refresh(true);
		}
	}
	
	public boolean applyFilterOnItem(Filter<CheckBoxModel<Dependency>> filter, CheckBoxModel<Dependency> model) {
		return filter.accept(model);
	}
	
	private class ChoicesFilter extends ViewerFilter {

		public ChoicesFilter() {

		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			String label = labelProvider.getText(element);
			if (match(label)) {
				return true;
			}
			return false;
		}

		private boolean match(String categoryLabel) {
			return !hiddenCategories.contains(categoryLabel);
		}
	}

}
