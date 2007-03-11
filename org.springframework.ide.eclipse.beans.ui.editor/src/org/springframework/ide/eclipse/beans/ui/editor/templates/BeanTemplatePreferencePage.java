/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.templates;

import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;

/**
 * Preference page for Spring beans config templates.
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
