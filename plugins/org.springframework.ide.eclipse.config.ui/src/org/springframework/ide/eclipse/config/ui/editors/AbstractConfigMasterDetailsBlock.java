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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;

/**
 * Abstraction of the master/details UI pattern present in every
 * {@link AbstractConfigFormPage}. This class is a base only, and clients should
 * consider extending {@link AbstractNamespaceMasterDetailsBlock} instead.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @see MasterDetailsBlock
 * @since 2.0.0
 */
public abstract class AbstractConfigMasterDetailsBlock extends MasterDetailsBlock implements IDetailsPageProvider {

	private AbstractConfigFormPage page;

	private AbstractConfigMasterPart master;

	/**
	 * Creates a new master/details block for the provided page.
	 * 
	 * @param page the parent form page
	 */
	public AbstractConfigMasterDetailsBlock(AbstractConfigFormPage page) {
		this.page = page;
	}

	@Override
	public void createContent(IManagedForm managedForm) {
		super.createContent(managedForm);
		managedForm.getForm().getBody().setLayout(new GridLayout());
	}

	@Override
	protected void createMasterPart(IManagedForm managedForm, Composite parent) {
		Composite container = managedForm.getToolkit().createComposite(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		master = createMasterSectionPart(page, container);
		managedForm.addPart(master);
		master.createContents();
	}

	/**
	 * This method is called when the master/details block is created. Clients
	 * must extend {@link AbstractConfigMasterPart} or
	 * {@link AbstractNamespaceMasterPart} and instantiate their class in this
	 * method.
	 * 
	 * @param page the hosting form page
	 * @param parent the parent composite
	 * @return section part hosting the master content
	 */
	protected abstract AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent);

	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns the form part hosting the details content.
	 * 
	 * @return details part
	 */
	public DetailsPart getDetailsPart() {
		return detailsPart;
	}

	/**
	 * Returns the form page hosting this part.
	 * 
	 * @return page the hosting form page
	 */
	protected AbstractConfigFormPage getFormPage() {
		return page;
	}

	/**
	 * Returns the section part hosting the master content.
	 * 
	 * @return master section part
	 */
	protected AbstractConfigMasterPart getMasterPart() {
		return master;
	}

	public abstract AbstractConfigDetailsPart getPage(Object key);

	public Object getPageKey(Object object) {
		return object;
	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.setPageLimit(10);
		detailsPart.setPageProvider(this);
	}

	public void setFormPage(AbstractConfigFormPage page) {
		this.page = page;
	}

}
