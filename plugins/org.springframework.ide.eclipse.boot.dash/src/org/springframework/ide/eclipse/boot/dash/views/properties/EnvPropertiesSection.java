/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.ActiveProfiles;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvLabelProvider;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.PropertySource;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.PropertySources;
import org.springsource.ide.eclipse.commons.livexp.ui.util.TreeElementWrappingContentProvider;

/**
 * Live env property section
 *
 *
 */
public class EnvPropertiesSection extends AbstractBdePropertiesSection {

	private SearchableTreeControl searchableTree;
	private TabbedPropertySheetPage page;


	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		page = aTabbedPropertySheetPage;

		LabelProvider labelProvider = new LiveEnvLabelProvider();
		ITreeContentProvider treeContentProvider = new TreeElementWrappingContentProvider(new LiveEnvContentProvider());

		searchableTree = new SearchableTreeControl(getWidgetFactory(), getMissingContentHandler());

		searchableTree.createControls(parent, page, treeContentProvider, labelProvider);
	}

	private Supplier<String> getMissingContentHandler() {
		return () ->  {
			BootDashElement bde = getBootDashElement();
			if (bde == null) {
				return "Select single element in Boot Dashboard to see live environment properties";
			} else if (bde.getLiveEnv() == null) {
				return "'" + bde.getName()
						+ "' must be running with JMX enabled; and actuator 'env' endpoint must be enabled to obtain all properties.";
			} else {
				// Must return null if there is content
				return null;
			}
		};
	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		searchableTree.getTreeViewer().setInput(getBootDashElement());
	}

	@Override
	public void refresh() {
		searchableTree.refresh();
	}

	private static class LiveEnvContentProvider implements ITreeContentProvider {

		LiveEnvContentProvider() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof BootDashElement) {
				BootDashElement bde = (BootDashElement) inputElement;
				LiveEnvModel liveEnv = bde.getLiveEnv();
				if (liveEnv != null) {
					List<Object> elements = new ArrayList<>();

					ActiveProfiles activeProfiles = liveEnv.getActiveProfiles();
					if (activeProfiles != null) {
						elements.add(activeProfiles);
					}
					PropertySources propertySources = liveEnv.getPropertySources();
					if (propertySources != null) {
						elements.add(propertySources);
					}
					return elements.toArray();
				}
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			List<Object> children = new ArrayList<>();

			if (parentElement instanceof ActiveProfiles) {
				return ((ActiveProfiles) parentElement).getProfiles().toArray();
			} else if (parentElement instanceof PropertySources) {
				return ((PropertySources) parentElement).getPropertySources().toArray();
			} else if (parentElement instanceof PropertySource) {
				return ((PropertySource) parentElement).getProperties().toArray();
			}
			return children.toArray();
		}

		@Override
		public Object getParent(Object element) {
			return new Object[0];
		}

		@Override
		public boolean hasChildren(Object element) {
			Object[] children = getChildren(element);
			return children != null && children.length > 0;
		}
	}
}
