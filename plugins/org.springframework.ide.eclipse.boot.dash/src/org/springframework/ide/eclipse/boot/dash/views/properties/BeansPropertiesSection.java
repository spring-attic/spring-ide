/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.beans.ui.live.tree.ContextGroupedBeansContentProvider;
import org.springframework.ide.eclipse.beans.ui.live.tree.LiveBeansTreeLabelProvider;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springsource.ide.eclipse.commons.livexp.core.FilterBoxModel;
import org.springsource.ide.eclipse.commons.livexp.ui.util.SwtConnect;
import org.springsource.ide.eclipse.commons.livexp.ui.util.TreeElementWrappingContentProvider;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;

/**
 * Live beans property section
 *
 * @author Alex Boyko
 *
 */
public class BeansPropertiesSection extends AbstractBdePropertiesSection {

	private TreeViewer treeViewer;
	private Label missingContentsLabel;
	private TabbedPropertySheetPage page;
	private StackLayout layout;
	private Text searchBox;
	private Composite treeViewerComposite;

	/**
	 * Searches for the upper level <code>ScrolledComposite</code>. Returns the passed composite if not found.
	 * @param composite
	 * @return
	 */
	private static Composite getScrolledComposite(Composite composite) {
		Composite c = composite;
		while(c != null && !(c instanceof ScrolledComposite)) {
			c = c.getParent();
		}
		return c == null ? composite : c;
	}

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		page = aTabbedPropertySheetPage;

		Composite composite = getWidgetFactory().createComposite(parent, SWT.NONE);

		// Layout variant to have owner composite size to be equal the client area size of the next upper level ScrolledComposite
		composite.setLayout(layout = new StackLayout() {
			@Override
			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				Point size = super.computeSize(composite, wHint, hHint, flushCache);
				if (page.getControl() instanceof Composite) {
					Composite container = getScrolledComposite(composite);
					Rectangle r = container.getClientArea();
					size = new Point(r.width, r.height);
				}
				return size;
			}
		});

		layout.marginWidth = ITabbedPropertyConstants.HSPACE + 2;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE + 4;

		missingContentsLabel = getWidgetFactory().createLabel(composite, "", SWT.WRAP); //$NON-NLS-1$

		treeViewerComposite = new Composite(composite, SWT.NONE);
		treeViewerComposite.setLayout(GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 1).numColumns(1).create());

		searchBox = getWidgetFactory().createText(treeViewerComposite, "", SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
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

		treeViewer = new TreeViewer(treeViewerComposite /*, SWT.NO_SCROLL*/);
		// BootDashElement should be input rather than the LiveBeansModel. Due to
		// polling the model often to show changes in the model it's best to refresh the
		// tree viewer rather then set the whole input that would remove the selection
		// and collapse expanded nodes
		ITreeContentProvider treeContent = new TreeElementWrappingContentProvider(new BeansContentProvider(ContextGroupedBeansContentProvider.INSTANCE));
		LabelProvider labelProvider = LiveBeansTreeLabelProvider.INSTANCE;

		treeViewer.setContentProvider(treeContent);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setAutoExpandLevel(2);

		treeViewer.getTree().addTreeListener (new TreeListener () {
			@Override
			public void treeExpanded (TreeEvent e) {
				reflow(page);
			}
			@Override
			public void treeCollapsed (TreeEvent e) {
				reflow(page);
			}
		});

		SwtConnect.connectTextBasedFilter(treeViewer, searchBoxModel.getFilter(), labelProvider, treeContent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(treeViewer.getTree());

		refreshControlsVisibility();
	}

	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		treeViewer.setInput(getBootDashElement());
	}

	public void refresh() {
		refreshControlsVisibility();
		BootDashElement bde = getBootDashElement();
		if (bde == null) {
			missingContentsLabel.setText("Select single element in Boot Dashboard to see live Beans");
		} else if (bde.getLiveBeans() == null) {
			missingContentsLabel.setText("'" + bde.getName() + "' must be running with JMX enabled; and actuator 'beans' endpoint must be enabled to obtain beans.");
		} else {
			missingContentsLabel.setText("");
		}
		// No tree widgets means that BootDashElement probably wasn't "running" or had no beans
		boolean firstTimeTreePopulated = treeViewer.getTree().getItems().length == 0;
		treeViewer.refresh();
		if (firstTimeTreePopulated) {
			// If tree is populated for the first time then auto expand to level 2 manually because new input is not set in this case
			treeViewer.expandToLevel(2);
		}
		reflow(page);
	}

	private void reflow(TabbedPropertySheetPage page) {
		final Composite target = page.getControl().getParent();
		if (target!=null) {
			target.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!target.isDisposed()) {
						target.layout(true, true);
						page.resizeScrolledComposite();
					}
				}
			});
		}
	}

	private void refreshControlsVisibility() {
		BootDashElement bde = getBootDashElement();
		if (bde == null || bde.getLiveRequestMappings() == null) {
			layout.topControl = missingContentsLabel;
		} else {
			layout.topControl = treeViewerComposite;
		}
	}

	private static class BeansContentProvider implements ITreeContentProvider {

		private ITreeContentProvider delegateContentProvider;

		BeansContentProvider(ITreeContentProvider delegate) {
			this.delegateContentProvider = delegate;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof BootDashElement) {
				BootDashElement bde = (BootDashElement) inputElement;
				return delegateContentProvider.getElements(bde.getLiveBeans());
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return delegateContentProvider.getChildren(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			return delegateContentProvider.getParent(element);
		}

		@Override
		public boolean hasChildren(Object element) {
			return delegateContentProvider.hasChildren(element);
		}

	}

}
