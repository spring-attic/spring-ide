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
package org.springframework.ide.eclipse.osgi.ui.editor.compendium;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.schemas.OsgiSchemaConstants;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceMasterDetailsBlock;
import org.springframework.ide.eclipse.osgi.ui.editor.OsgiMasterDetailsBlock;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class OsgiCompendiumMasterDetailsBlock extends AbstractNamespaceMasterDetailsBlock {

	public OsgiCompendiumMasterDetailsBlock() {
		super();
	}

	public OsgiCompendiumMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent) {
		return new OsgiCompendiumMasterPart(page, parent);
	}

	@Override
	public AbstractConfigDetailsPart getDetailsPage(Object key) {
		IDOMDocument doc = getFormPage().getEditor().getDomDocument();
		if (ConfigCoreUtils.getSchemaVersion(doc, OsgiSchemaConstants.URI).compareTo(new Version("2.0")) >= 0) { //$NON-NLS-1$
			return new OsgiCompendiumDetailsPart(getMasterPart(), OsgiMasterDetailsBlock.DOCS_SPRINGOSGI_20);
		}
		else {
			return new OsgiCompendiumDetailsPart(getMasterPart(), OsgiMasterDetailsBlock.DOCS_SPRINGOSGI);
		}
	}

}
