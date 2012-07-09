package org.springframework.ide.eclipse.wizard.template.util;

import org.eclipse.swt.widgets.Shell;

public class TemplateAddEditNameUrlDialog extends AddEditNameUrlDialog {

	public TemplateAddEditNameUrlDialog(Shell parent, AbstractNameUrlPreferenceModel aModel, NameUrlPair nameUrl,
			String headerText) {
		super(parent, aModel, nameUrl, headerText);
	}

	@Override
	protected boolean validateUrl(String urlString) {
		return super.validateUrl(urlString);
	}
}
