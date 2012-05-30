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
package org.springframework.ide.eclipse.config.ui.editors.overview;

import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.config.core.extensions.FormPagesExtensionPointConstants;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterDetailsBlock;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceDetailsPart;
import org.springframework.ide.eclipse.config.ui.extensions.ConfigUiExtensionPointReader;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class OverviewMasterDetailsBlock extends AbstractConfigMasterDetailsBlock {

	public OverviewMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent) {
		return new OverviewMasterPart(page, parent);
	}

	@Override
	public AbstractConfigDetailsPart getPage(Object key) {
		AbstractConfigEditor editor = getFormPage().getEditor();
		Set<IConfigurationElement> definitions = ConfigUiExtensionPointReader.getPageDefinitions();
		for (IConfigurationElement def : definitions) {
			String uri = def.getAttribute(FormPagesExtensionPointConstants.ATTR_NAMESPACE_URI);
			AbstractConfigFormPage formPage = editor.getFormPageForUri(uri);
			if (formPage != null) {
				AbstractConfigDetailsPart page = formPage.getMasterDetailsBlock().getPage(key);
				if (page != null) {
					page.setMasterPart(getMasterPart());
					return page;
				}
			}
		}
		return new AbstractNamespaceDetailsPart(getMasterPart());
	}

}
