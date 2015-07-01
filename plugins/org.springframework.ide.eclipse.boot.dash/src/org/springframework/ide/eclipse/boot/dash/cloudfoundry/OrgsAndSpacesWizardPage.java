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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudOrganization;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

/**
 * Wizard page to allow users to select a target cloud space when cloning an
 * existing server.
 */
class OrgsAndSpacesWizardPage extends WizardPage {

	protected TreeViewer orgsSpacesViewer;

	protected CloudSpace selectedSpace;

	private final OrgsAndSpaces spaces;

	OrgsAndSpacesWizardPage(OrgsAndSpaces spaces, String url) {
		super("Select an Org and Space");

		setTitle("Select an Org and Space");
		setDescription("Select a space in " + url);
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/wizban_cloudfoundry.png"));

		this.spaces = spaces;

	}

	@Override
	public boolean isPageComplete() {
		return getSpaceSelection() != null;
	}

	@Override
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(5, 5).applyTo(mainComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);

		Label orgLabel = new Label(mainComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(orgLabel);
		orgLabel.setText("Orgs:");

		Tree orgTable = new Tree(mainComposite, SWT.BORDER | SWT.SINGLE);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(orgTable);

		orgsSpacesViewer = new TreeViewer(orgTable);

		orgsSpacesViewer.setContentProvider(new TableContentProvider());
		orgsSpacesViewer.setLabelProvider(new SpacesLabelProvider());
		orgsSpacesViewer.setSorter(new SpacesSorter());

		orgsSpacesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				refresh();
			}
		});

		setControl(mainComposite);

		setInput();
	}

	public void setInput() {
		List<CloudOrganization> orgInput = spaces.getOrgs();

		CloudOrganization[] organizationInput = orgInput.toArray(new CloudOrganization[orgInput.size()]);
		orgsSpacesViewer.setInput(organizationInput);

		// Expand all first, so that child elements can be selected
		orgsSpacesViewer.setExpandedElements(organizationInput);

		setInitialSelectionInViewer();
	}

	protected void setInitialSelectionInViewer() {

		selectedSpace = spaces.getAllSpaces().get(0);

		if (selectedSpace != null) {
			setSelectionInViewer(selectedSpace);
		}
	}

	protected void setSelectionInViewer(CloudSpace selectedSpace) {
		// Now set the cloud space in the tree
		Tree tree = orgsSpacesViewer.getTree();
		TreeItem[] orgItems = tree.getItems();
		if (orgItems != null) {
			TreeItem orgItem = null;

			// Find the tree item corresponding to the cloud space's
			// org
			for (TreeItem item : orgItems) {
				Object treeObj = item.getData();
				if (treeObj instanceof CloudOrganization
						&& ((CloudOrganization) treeObj).getName().equals(selectedSpace.getOrganization().getName())) {
					orgItem = item;
					break;

				}
			}

			if (orgItem != null) {
				TreeItem[] children = orgItem.getItems();
				if (children != null) {
					for (TreeItem childItem : children) {
						Object treeObj = childItem.getData();
						if (treeObj instanceof CloudSpace
								&& ((CloudSpace) treeObj).getName().equals(selectedSpace.getName())) {
							tree.select(childItem);
							break;
						}
					}
				}
			}
		}
	}

	protected CloudSpace getSpaceSelection() {
		return selectedSpace;
	}

	protected void refresh() {
		if (orgsSpacesViewer != null) {

			Tree tree = orgsSpacesViewer.getTree();
			TreeItem[] selectedItems = tree.getSelection();
			if (selectedItems != null && selectedItems.length > 0) {
				// It's a single selection tree, so only get the first selection
				Object selectedObj = selectedItems[0].getData();
				selectedSpace = selectedObj instanceof CloudSpace ? (CloudSpace) selectedObj : null;
			}
		}
		refreshWizardUI();
	}

	private void refreshWizardUI() {
		if (getWizard() != null && getWizard().getContainer() != null) {
			setPageComplete(getSpaceSelection() != null);

			getWizard().getContainer().updateButtons();
		}
	}

	static class SpacesSorter extends ViewerSorter {

		public SpacesSorter() {

		}

		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof CloudEntity && e2 instanceof CloudEntity) {
				String name1 = ((CloudEntity) e1).getName();
				String name2 = ((CloudEntity) e2).getName();
				return name1.compareTo(name2);
			}

			return super.compare(viewer, e1, e2);
		}

	}

	class TableContentProvider implements ITreeContentProvider {
		private Object[] elements;

		public TableContentProvider() {
		}

		public void dispose() {
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof CloudOrganization) {
				List<CloudSpace> children = spaces.getOrgSpaces(((CloudOrganization) parentElement).getName());
				if (children != null) {
					return children.toArray(new CloudSpace[children.size()]);
				}
			}
			return null;
		}

		public Object[] getElements(Object inputElement) {
			return elements;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof Object[]) {
				elements = (Object[]) newInput;
			}
		}
	}

	static class SpacesLabelProvider extends LabelProvider {

		public SpacesLabelProvider() {

		}

		public String getText(Object element) {
			if (element instanceof CloudEntity) {
				CloudEntity cloudEntity = (CloudEntity) element;
				return cloudEntity.getName();
			}
			return super.getText(element);
		}
	}
}