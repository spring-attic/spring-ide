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
package org.springframework.ide.eclipse.config.ui.editors.webflow;

import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceMasterDetailsBlock;
import org.springframework.ide.eclipse.config.ui.editors.webflow.config.WebFlowConfigMasterDetailsBlock;


/**
 * @author Leo Dos Santos
 */
public class WebFlowMasterDetailsBlock extends AbstractNamespaceMasterDetailsBlock {

	public WebFlowMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent) {
		return new WebFlowMasterPart(page, parent);
	}

	@Override
	public AbstractConfigDetailsPart getDetailsPage(Object key) {
		if (getFormPage().getSchemaVersion().compareTo(new Version("2.1")) >= 0) { //$NON-NLS-1$
			return new WebFlowDetailsPart(getMasterPart(), WebFlowConfigMasterDetailsBlock.DOCS_SPRINGWEBFLOW_21);
		}
		else if (getFormPage().getSchemaVersion().compareTo(new Version("2.0")) >= 0) { //$NON-NLS-1$
			return new WebFlowDetailsPart(getMasterPart(), WebFlowConfigMasterDetailsBlock.DOCS_SPRINGWEBFLOW_20);
		}
		else {
			return new WebFlowDetailsPart(getMasterPart(), WebFlowConfigMasterDetailsBlock.DOCS_SPRINGWEBFLOW_10);
		}
	}

}
