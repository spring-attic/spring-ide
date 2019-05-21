/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springsource.ide.eclipse.commons.livexp.core.FilterBoxModel;
import org.springsource.ide.eclipse.commons.livexp.ui.util.TreeNodeFilter;
import org.springsource.ide.eclipse.commons.livexp.ui.util.UpdateExpansionStates;
import org.springsource.ide.eclipse.commons.livexp.ui.util.SwtConnect;
import org.springsource.ide.eclipse.commons.livexp.ui.util.FilteringLazyTreeContentProvider;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * Tree viewer control with search box that searches for any text pattern in the
 * tree. This control is specifically meant for a page container.
 *
 * <p/>
 *
 * Will switch between a simple message label control and the searchable tree
 * control if no content is found on refresh. Therefore a no-content message
 * supplier is required.
 *
 * @author Alex Boyko
 * @author Nieraj Singh
 *
 */
public class SearchableTreeControl {

	private TreeViewer treeViewer;
	private TabbedPropertySheetPage page;
	private StackLayout layout;
	private Text searchBox;
	private Composite treeViewerComposite;
	private Composite missingInfoComp;
	private Label missingContentsLabel;
	private Hyperlink externalDocLink;

	private final FormToolkit widgetFactory;
	private final Supplier<String> missingContentSupplier;
	private FilteringLazyTreeContentProvider wrappedContentProvider;

	/**
	 *
	 * @param widgetFactory
	 * @param missingContentSupplier supplies a message if no content for tree is
	 *                               available. Supplier MUST return null if content
	 *                               is available.
	 */
	public SearchableTreeControl(FormToolkit widgetFactory, Supplier<String> missingContentSupplier) {
		this.widgetFactory = widgetFactory;
		this.missingContentSupplier = missingContentSupplier;
	}

	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage,
			ITreeContentProvider baseContentProvider, LabelProvider labelProvider) {
		page = aTabbedPropertySheetPage;

		Composite composite = widgetFactory.createComposite(parent, SWT.NONE);

		composite.setLayout(layout = new SectionStackLayout());

		layout.marginWidth = ITabbedPropertyConstants.HSPACE + 2;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE + 4;

		missingInfoComp = widgetFactory.createComposite(composite, SWT.NONE);
		missingInfoComp.setLayout(GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 1).numColumns(1).create());

		missingContentsLabel = widgetFactory.createLabel(missingInfoComp, "", SWT.WRAP);
		externalDocLink = widgetFactory.createHyperlink(missingInfoComp, "", SWT.WRAP);

		externalDocLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (!externalDocLink.getText().isEmpty()) {
					UiUtil.openUrl(externalDocLink.getText());
				}
			}
		});
		externalDocLink.setText(MissingLiveInfoMessages.EXTERNAL_DOCUMENT_LINK);


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

		treeViewer = new TreeViewer(treeViewerComposite, SWT.VIRTUAL);

		UpdateExpansionStates expansionStates = new UpdateExpansionStates(treeViewer);

		TreeNodeFilter viewerFilter = new TreeNodeFilter(Filters.acceptAll(), labelProvider, expansionStates);

		wrappedContentProvider = new FilteringLazyTreeContentProvider(treeViewer, viewerFilter, baseContentProvider);

		treeViewer.setContentProvider(wrappedContentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setAutoExpandLevel(2);
		treeViewer.setUseHashlookup(true);

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

		SwtConnect.connectTextToLazy(treeViewer, searchBoxModel.getFilter(), labelProvider, wrappedContentProvider, expansionStates);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(treeViewer.getTree());

		refreshControlsVisibility();

	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	private void refreshControlsVisibility() {
		if (missingContentSupplier.get() != null) {
			layout.topControl = missingInfoComp;
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

		treeViewer.refresh();

		SectionStackLayout.reflow(page);
	}

	public void setInput(BootDashElement bootDashElement) {
		if (wrappedContentProvider != null) {
			wrappedContentProvider.setInput(bootDashElement);
		}
	}

}
