/*******************************************************************************
 *  Copyright (c) 2012 - 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.editor.faces;

import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceMasterDetailsBlock;
import org.springframework.ide.eclipse.webflow.ui.editor.config.WebFlowConfigMasterDetailsBlock;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class FacesMasterDetailsBlock extends AbstractNamespaceMasterDetailsBlock {

	public FacesMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent) {
		return new FacesMasterPart(page, parent);
	}

	@Override
	public AbstractConfigDetailsPart getDetailsPage(Object key) {
		if (getFormPage().getSchemaVersion().compareTo(new Version("2.1")) >= 0) { //$NON-NLS-1$
			return new FacesDetailsPart(getMasterPart(), WebFlowConfigMasterDetailsBlock.DOCS_SPRINGWEBFLOW_21);
		}
		else {
			return new FacesDetailsPart(getMasterPart(), WebFlowConfigMasterDetailsBlock.DOCS_SPRINGWEBFLOW_20);

		}
	}

}
