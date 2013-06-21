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
package org.springframework.ide.eclipse.osgi.ui.editor.blueprint;

import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceMasterDetailsBlock;
import org.springframework.ide.eclipse.osgi.ui.editor.OsgiMasterDetailsBlock;

/**
 * @author Leo Dos Santos
 */
public class BlueprintMasterDetailsBlock extends AbstractNamespaceMasterDetailsBlock {

	public BlueprintMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent) {
		return new BlueprintMasterPart(page, parent);
	}

	@Override
	public AbstractConfigDetailsPart getDetailsPage(Object key) {
		return new BlueprintDetailsPart(getMasterPart(), OsgiMasterDetailsBlock.DOCS_SPRINGOSGI_20);
	}

}
