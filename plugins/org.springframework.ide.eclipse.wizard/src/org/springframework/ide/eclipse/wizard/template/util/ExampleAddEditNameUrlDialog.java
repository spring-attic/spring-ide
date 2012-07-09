package org.springframework.ide.eclipse.wizard.template.util;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

public class ExampleAddEditNameUrlDialog extends AddEditNameUrlDialog {

	public ExampleAddEditNameUrlDialog(Shell parent, AbstractNameUrlPreferenceModel aModel, NameUrlPair nameUrl,
			String headerText) {
		super(parent, aModel, nameUrl, headerText);
	}

	@Override
	protected boolean validateUrl(String urlString) {
		if (!super.validateUrl(urlString)) {
			return false;
		}

		if (urlString.indexOf("github.com") <= 0) {
			errorTextLabel.setText(NLS.bind("Example projects must be at github.com", null));
			composite.update();
			return false;
		}

		return true;
	}

}
