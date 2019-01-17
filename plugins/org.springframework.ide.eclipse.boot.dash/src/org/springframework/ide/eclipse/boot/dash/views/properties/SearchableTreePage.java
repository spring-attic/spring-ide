/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import java.util.function.Supplier;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springsource.ide.eclipse.commons.livexp.core.FilterBoxModel;
import org.springsource.ide.eclipse.commons.livexp.ui.util.SwtConnect;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;

/**
 * Tree property section with search box that searches for any text pattern in the tree.
 *
 * <p/>
 *
 * Will switch between a message and the searchable tree controls if no content is found on refresh. There
 * a no-content message supplier is required.
 *
 * @author Alex Boyko
 *
 */
public class SearchableTreePage {


	private TreeViewer treeViewer;
	private TabbedPropertySheetPage page;
	private StackLayout layout;
	private Text searchBox;
	private Composite treeViewerComposite;
	private Label missingContentsLabel;


	private final FormToolkit widgetFactory;
	private final Supplier<String> missingContentSupplier;


	/**
	 *
	 * @param widgetFactory
	 * @param missingContentSupplier supplies a message if no content for tree is available. Supplier MUST return null if content is available.
	 */
	public SearchableTreePage(FormToolkit widgetFactory, Supplier<String> missingContentSupplier) {
		this.widgetFactory = widgetFactory;
		this.missingContentSupplier = missingContentSupplier;
	}

	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage,
			ITreeContentProvider treeContentProvider, LabelProvider labelProvider) {
		page = aTabbedPropertySheetPage;

		Composite composite = widgetFactory.createComposite(parent, SWT.NONE);

		composite.setLayout(layout = new SectionStackLayout());

		layout.marginWidth = ITabbedPropertyConstants.HSPACE + 2;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE + 4;

		missingContentsLabel = widgetFactory.createLabel(composite, "", SWT.WRAP);

		treeViewerComposite = new Composite(composite, SWT.NONE);
		treeViewerComposite
				.setLayout(GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 1).numColumns(1).create());

		searchBox = widgetFactory.createText(treeViewerComposite, "",
				SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		searchBox.setMessage("Enter search string");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(searchBox);
		FilterBoxModel<String> searchBoxModel = new FilterBoxModel<String>() {
			@Override
			protected Filter<String> createFilterForInput(String pattern) {
				return Filters.caseInsensitiveSubstring(pattern);
			}
		};
		SwtConnect.connect(searchBox, searchBoxModel.getText());
		searchBox.addDisposeListener(de -> searchBoxModel.close());

		treeViewer = new TreeViewer(treeViewerComposite /* , SWT.NO_SCROLL */);

		treeViewer.setContentProvider(treeContentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setAutoExpandLevel(2);

		treeViewer.getTree().addTreeListener(new TreeListener() {
			@Override
			public void treeExpanded(TreeEvent e) {
				SectionStackLayout.reflow(page);
			}

			@Override
			public void treeCollapsed(TreeEvent e) {
				SectionStackLayout.reflow(page);
			}
		});

		SwtConnect.connectTextBasedFilter(treeViewer, searchBoxModel.getFilter(), labelProvider, treeContentProvider);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(treeViewer.getTree());

		refreshControlsVisibility();

	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	private void refreshControlsVisibility() {
		if (missingContentSupplier.get() != null) {
			layout.topControl = missingContentsLabel;
		} else {
			layout.topControl = treeViewerComposite;
		}
	}

	public void refresh() {
		refreshControlsVisibility();
		String message = missingContentSupplier.get();
		if (message == null) {
			message = "";
		}
		missingContentsLabel.setText(message);

		// If tree is populated for the first time then auto expand to level 2 manually
		// because new input is not set in this case
		boolean firstTimeTreePopulated = treeViewer.getTree().getItems().length == 0;
		treeViewer.refresh();

		if (firstTimeTreePopulated) {
			treeViewer.expandToLevel(2);
		}
		SectionStackLayout.reflow(page);
	}
}
