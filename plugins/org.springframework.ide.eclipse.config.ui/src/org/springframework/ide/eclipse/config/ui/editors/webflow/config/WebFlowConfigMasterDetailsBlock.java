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
package org.springframework.ide.eclipse.config.ui.editors.webflow.config;

import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceMasterDetailsBlock;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class WebFlowConfigMasterDetailsBlock extends AbstractNamespaceMasterDetailsBlock {

	public static final String DOCS_SPRINGWEBFLOW_10 = "http://static.springframework.org/spring-webflow/docs/1.0.x/reference/index.html"; //$NON-NLS-1$

	public static final String DOCS_SPRINGWEBFLOW_20 = "http://static.springframework.org/spring-webflow/docs/2.0.x/reference/html/index.html"; //$NON-NLS-1$

	public static final String DOCS_SPRINGWEBFLOW_21 = "http://static.springframework.org/spring-webflow/docs/2.1.x/reference/html/index.html"; //$NON-NLS-1$

	public WebFlowConfigMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent) {
		return new WebFlowConfigMasterPart(page, parent);
	}

	@Override
	public AbstractConfigDetailsPart getDetailsPage(Object key) {
		if (getFormPage().getSchemaVersion().compareTo(new Version("2.1")) >= 0) { //$NON-NLS-1$
			return new WebFlowConfigDetailsPart(getMasterPart(), DOCS_SPRINGWEBFLOW_21);
		}
		else if (getFormPage().getSchemaVersion().compareTo(new Version("2.0")) >= 0) { //$NON-NLS-1$
			return new WebFlowConfigDetailsPart(getMasterPart(), DOCS_SPRINGWEBFLOW_20);
		}
		else {
			return new WebFlowConfigDetailsPart(getMasterPart(), DOCS_SPRINGWEBFLOW_10);
		}
	}

}
