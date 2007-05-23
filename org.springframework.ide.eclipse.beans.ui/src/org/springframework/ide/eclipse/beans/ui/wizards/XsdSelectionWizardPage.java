/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.wizards;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.core.xsd.IXmlSchemaDefinition;
import org.springframework.ide.eclipse.core.xsd.XmlSchemaDefinitionFactory;

/**
 * {@link WizardPage} that displays a list of {@link IXmlSchemaDefinition}s to
 * the user in order to allow for selecting the desired XSD namespace
 * declarations.
 * @author Christian Dupuis
 * @since 2.0
 */
public class XsdSelectionWizardPage extends WizardPage {

	public class XsdLabelProvider implements ILabelProvider {

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getImage(Object element) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}

		public String getText(Object element) {
			if (element instanceof IXmlSchemaDefinition) {
				IXmlSchemaDefinition xsdDef = (IXmlSchemaDefinition) element;
				return xsdDef.getNamespacePrefix() + " - "
						+ xsdDef.getNamespaceURI();
			}
			return "";
		}
	}

	private class XsdConfigContentProvider implements
			IStructuredContentProvider {

		public XsdConfigContentProvider() {
		}

		public Object[] getElements(Object obj) {
			return XmlSchemaDefinitionFactory.getXmlSchemaDefinitions()
					.toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}
	}

	private static final int XSD_LIST_VIEWER_HEIGHT = 150;

	private static final int LIST_VIEWER_WIDTH = 340;

	private CheckboxTableViewer xsdViewer;

	protected XsdSelectionWizardPage(String pageName) {
		super(pageName);
		setTitle(BeansWizardsMessages.NewConfig_title);
		setDescription(BeansWizardsMessages.NewConfig_xsdDescription);
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		// top level group
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginTop = 5;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());
		setControl(composite);

		Label beansLabel = new Label(composite, SWT.NONE);
		beansLabel.setText("Select desired XSD namespace declarations:");
		// config set list viewer
		xsdViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = LIST_VIEWER_WIDTH;
		gd.heightHint = XSD_LIST_VIEWER_HEIGHT;
		xsdViewer.getTable().setLayoutData(gd);
		xsdViewer.setContentProvider(new XsdConfigContentProvider());
		xsdViewer.setLabelProvider(new XsdLabelProvider());
		xsdViewer.setInput(this); // activate content provider
	}

	public Set<IXmlSchemaDefinition> getXmlSchemaDefinitions() {
		Set<IXmlSchemaDefinition> defs = new HashSet<IXmlSchemaDefinition>();
		Object[] checkedElements = xsdViewer.getCheckedElements();
		if (checkedElements != null) {
			for (int i = 0; i < checkedElements.length; i++) {
				defs.add((IXmlSchemaDefinition) checkedElements[i]);
			}
		}
		return defs;
	}
}
