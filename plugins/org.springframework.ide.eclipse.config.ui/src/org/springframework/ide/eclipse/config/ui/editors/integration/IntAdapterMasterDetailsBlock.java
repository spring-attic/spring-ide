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
package org.springframework.ide.eclipse.config.ui.editors.integration;

import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceMasterDetailsBlock;


/**
 * @author Leo Dos Santos
 */
public class IntAdapterMasterDetailsBlock extends AbstractNamespaceMasterDetailsBlock {

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractConfigDetailsPart getDetailsPage(Object key) {
		return new AbstractNamespaceDetailsPart(getMasterPart(), IntegrationMasterDetailsBlock.DOCS_SPRINGINT);
	}

}
