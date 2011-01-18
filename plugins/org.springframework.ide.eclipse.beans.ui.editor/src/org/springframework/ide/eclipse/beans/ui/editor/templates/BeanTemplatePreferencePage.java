/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.templates;

import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;

/**
 * Preference page for Spring beans config templates.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public class BeanTemplatePreferencePage extends TemplatePreferencePage {

	public BeanTemplatePreferencePage() {
		Activator plugin = Activator.getDefault();

		setPreferenceStore(plugin.getPreferenceStore());
		setTemplateStore(plugin.getTemplateStore());
		setContextTypeRegistry(plugin.getTemplateContextRegistry());

		setMessage(Activator.getResourceString("preferences.message"));
	}

	//
	// protected Control createContents(Composite ancestor) {
	// IWorkbenchHelpSystem helpSystem = BeansEditorPlugin.getDefault()
	// .getWorkbench().getHelpSystem();
	// Control c = super.createContents(ancestor);
	// helpSystem.setHelp(c, IHelpContextIds.XML_PREFWEBX_TEMPLATES_HELPID);
	// return c;
	// }

	@Override
	protected boolean isShowFormatterSetting() {
		// template formatting has not been implemented
		return false;
	}

	@Override
	public boolean performOk() {
		boolean ok = super.performOk();
		Activator.getDefault().savePluginPreferences();
		return ok;
	}
}
