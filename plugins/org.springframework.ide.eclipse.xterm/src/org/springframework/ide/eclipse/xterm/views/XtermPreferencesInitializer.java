package org.springframework.ide.eclipse.xterm.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.springframework.ide.eclipse.xterm.XtermPlugin;

public class XtermPreferencesInitializer extends AbstractPreferenceInitializer {

	public XtermPreferencesInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		XtermPlugin.getDefault().getPreferenceStore().setDefault(XtermPlugin.PREFS_DEFAULT_SHELL_CMD, Platform.OS_WIN32.equals(Platform.getOS()) ? "cmd.exe" : "/bin/bash");
	}

}
