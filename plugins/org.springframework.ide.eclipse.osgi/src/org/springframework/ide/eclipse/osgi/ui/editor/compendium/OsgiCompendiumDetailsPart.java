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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigDetailsSectionPart;

/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class OsgiCompendiumDetailsPart extends AbstractNamespaceDetailsPart {

	public OsgiCompendiumDetailsPart(AbstractConfigMasterPart master) {
		super(master);
	}

	public OsgiCompendiumDetailsPart(AbstractConfigMasterPart master, String docsUrl) {
		super(master, docsUrl);
	}

	@Override
	public SpringConfigDetailsSectionPart createDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input,
			Composite parent, FormToolkit toolkit) {
		return new OsgiCompendiumDetailsSectionPart(editor, input, parent, toolkit);
	}

}
