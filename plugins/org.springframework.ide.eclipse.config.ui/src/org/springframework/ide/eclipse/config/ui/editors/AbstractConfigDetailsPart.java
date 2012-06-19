/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.graph.parts.ActivityPart;


/**
 * This class implements the details portion of the master/details UI pattern.
 * Classes that extend {@code AbstractConfigDetailsPart} will display a detailed
 * overview of a single element in a Spring configuration file. This class is a
 * base only, and clients should consider extending
 * {@link AbstractNamespaceDetailsPart} instead.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractConfigDetailsPart extends AbstractFormPart implements IDetailsPage {

	private AbstractConfigMasterPart master;

	private AbstractConfigEditor cEditor;

	/**
	 * The toolkit used by the form part.
	 */
	protected FormToolkit toolkit;

	/**
	 * The details section of the form part.
	 */
	protected AbstractConfigSectionPart detailsSection;

	private IDOMElement input;

	/**
	 * Constructs a details part with a reference to its master part.
	 * 
	 * @param master the page's master part
	 */
	public AbstractConfigDetailsPart(AbstractConfigMasterPart master) {
		this.master = master;
		if (master != null) {
			cEditor = master.getConfigEditor();
		}
	}

	/**
	 * Aligns the header of the given section with the header of the master part
	 * section. Misalignment can be caused by one header containing toolbar
	 * icons and the other not.
	 * 
	 * @param section the section of the details part to be aligned
	 */
	protected void alignSectionHeaderWithMaster(Section section) {
		if (master != null) {
			Section masterSection = master.getSection();
			if (masterSection != null) {
				section.descriptionVerticalSpacing += masterSection.getTextClientHeightDifference();
			}
		}
	}

	public void createContents(Composite parent) {
		toolkit = getManagedForm().getToolkit();
		parent.setLayout(new GridLayout());
		if (getMasterViewer() != null) {
			updateInput(getMasterViewer().getSelection());
		}
		createDetailsSection(parent);
	}

	protected Composite createDetailsClient(Section details) {
		details.setLayout(new GridLayout());
		details.setLayoutData(new GridData(GridData.FILL_BOTH));
		details.setText(getDetailsSectionTitle());
		details.setDescription(getDetailsSectionDescription());

		Composite detailsClient = toolkit.createComposite(details);
		detailsClient.setLayout(new GridLayout());
		detailsClient.setLayoutData(new GridData(GridData.FILL_BOTH));
		details.setClient(detailsClient);
		return detailsClient;
	}

	/**
	 * This method is called automatically when the details section is created.
	 * Client must create and add the content of the details section here, or
	 * override {@link #createDetailsSection(Composite)} to do nothing.
	 * 
	 * @param client the parent composite
	 */
	protected abstract void createDetailsContent(Composite client);

	/**
	 * Creates the details section of the form part. Clients may override if
	 * necessary.
	 * 
	 * @param parent the parent composite
	 */
	protected void createDetailsSection(Composite parent) {
		detailsSection = createDetailsSectionPart(getConfigEditor(), getInput(), parent, toolkit);
		detailsSection.createContent();
		alignSectionHeaderWithMaster(detailsSection.getSection());
	}

	protected AbstractConfigSectionPart createDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input,
			Composite parent, final FormToolkit toolkit) {
		final Section details = toolkit.createSection(parent, Section.TITLE_BAR | Section.DESCRIPTION);
		return new AbstractConfigSectionPart(cEditor, input, details) {
			@Override
			public void createContent() {
				Composite detailsClient = createDetailsClient(details);
				createDetailsContent(detailsClient);
			}
		};
	}

	/**
	 * Returns the the parent editor as an {@link AbstractConfigEditor} object.
	 * 
	 * @return parent editor instance
	 */
	public AbstractConfigEditor getConfigEditor() {
		return cEditor;
	}

	/**
	 * Returns the details section of the form part.
	 * 
	 * @return details section
	 */
	public AbstractConfigSectionPart getDetailsSection() {
		return detailsSection;
	}

	/**
	 * This method is called automatically when the details section is created.
	 * Clients must return the description of their details section here, or
	 * override {@link #createDetailsSection(Composite)} to do nothing.
	 * 
	 * @return details section description
	 */
	protected abstract String getDetailsSectionDescription();

	/**
	 * This method is called automatically when the details section is created.
	 * Clients must return the title of their details section here, or override
	 * {@link #createDetailsSection(Composite)} to do nothing.
	 * 
	 * @return details section title
	 */
	protected abstract String getDetailsSectionTitle();

	/**
	 * Returns the XML element that serves as the model for the part.
	 * 
	 * @return element displayed by the part
	 */
	public IDOMElement getInput() {
		return input;
	}

	/**
	 * Returns the master part of the parent form page. May be null.
	 * 
	 * @return master part of the parent form page
	 */
	public AbstractConfigMasterPart getMasterPart() {
		return master;
	}

	/**
	 * Returns the viewer of the master part. May be null.
	 * 
	 * @return viewer of the master part
	 */
	protected ColumnViewer getMasterViewer() {
		if (master != null) {
			return master.getViewer();
		}
		return null;
	}

	@Override
	public void refresh() {
		if (detailsSection != null) {
			detailsSection.refresh();
		}
		super.refresh();
	}

	public void selectionChanged(IFormPart part, ISelection selection) {
		updateInput(selection);
		refresh();
	}

	public void setConfigEditor(AbstractConfigEditor editor) {
		cEditor = editor;
	}

	/**
	 * Sets a reference to the master part of the parent form page.
	 * 
	 * @param master the master part of the parent form page
	 */
	public void setMasterPart(AbstractConfigMasterPart master) {
		if (this.master != null) {
			this.master.dispose();
		}
		this.master = master;
		if (master != null) {
			cEditor = master.getConfigEditor();
		}
	}

	protected void updateInput(ISelection selection) {
		IDOMElement oldInput = input;
		input = null;
		IStructuredSelection sel = (IStructuredSelection) selection;
		if (sel != null) {
			Object obj = sel.getFirstElement();
			if (obj instanceof ActivityPart) {
				ActivityPart activity = (ActivityPart) obj;
				obj = activity.getModelElement().getInput();
			}
			if (obj instanceof IDOMElement) {
				if (obj != oldInput) {
					input = (IDOMElement) obj;
				}
				else {
					input = oldInput;
				}
			}
		}
		if (detailsSection != null) {
			detailsSection.setFormInput(input);
		}
	}

}
