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
package org.springframework.ide.eclipse.config.ui.editors.osgi;

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
public class OsgiMasterDetailsBlock extends AbstractNamespaceMasterDetailsBlock {

	public static final String DOCS_SPRINGOSGI = "http://static.springsource.org/osgi/docs/current/reference/html/"; //$NON-NLS-1$ 

	public static final String DOCS_SPRINGOSGI_20 = "http://static.springsource.org/osgi/docs/2.0.0.M1/reference/html/"; //$NON-NLS-1$ 

	public OsgiMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent) {
		return new OsgiMasterPart(page, parent);
	}

	@Override
	public AbstractConfigDetailsPart getDetailsPage(Object key) {
		if (getFormPage().getSchemaVersion().compareTo(new Version("2.0")) >= 0) { //$NON-NLS-1$
			return new OsgiDetailsPart(getMasterPart(), DOCS_SPRINGOSGI_20);
		}
		else {
			return new OsgiDetailsPart(getMasterPart(), DOCS_SPRINGOSGI);
		}
	}

}
