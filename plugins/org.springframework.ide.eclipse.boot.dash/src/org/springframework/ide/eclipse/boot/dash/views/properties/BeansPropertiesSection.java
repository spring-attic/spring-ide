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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.beans.ui.live.tree.LiveBeansTreeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.live.tree.ResourceGroupedBeansContentProvider;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

/**
 * Live beans property section
 *
 * @author Alex Boyko
 *
 */
public class BeansPropertiesSection extends AbstractBdePropertiesSection {

	private TreeViewer treeViewer;
	private Label labelText;
	private TabbedPropertySheetPage page;
	private StackLayout layout;

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
					return new Point(r.width, r.height);
				}
				return size;
			}
		});

		layout.marginWidth = ITabbedPropertyConstants.HSPACE + 2;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE + 4;

		labelText = getWidgetFactory().createLabel(composite, "", SWT.WRAP); //$NON-NLS-1$

		treeViewer = new TreeViewer(composite/*, SWT.NO_SCROLL*/);
		treeViewer.setContentProvider(new BeansContentProvider(ResourceGroupedBeansContentProvider.INSTANCE));
		treeViewer.setLabelProvider(LiveBeansTreeLabelProvider.INSTANCE);

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
			labelText.setText("Select single element in Boot Dashboard to see live Beans");
		} else if (bde.getLiveBeans() == null) {
			labelText.setText("'" + bde.getName() + "' must be running with JMX enabled; and actuator 'beans' endpoint must be enabled to obtain beans.");
		} else {
			labelText.setText("");
		}
		treeViewer.refresh();
		reflow(page);
	}

	private void reflow(TabbedPropertySheetPage page) {
		final Composite target = page.getControl().getParent();
		if (target!=null) {
			target.getDisplay().asyncExec(new Runnable() {
				public void run() {
					target.layout(true, true);
					page.resizeScrolledComposite();
				}
			});
		}
	}

	private void refreshControlsVisibility() {
		BootDashElement bde = getBootDashElement();
		if (bde == null || bde.getLiveRequestMappings() == null) {
			layout.topControl = labelText;
		} else {
			layout.topControl = treeViewer.getTree();
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
