package org.springframework.ide.eclipse.xterm.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.springframework.ide.eclipse.xterm.XtermPlugin;

public class XtermPreferencesInitializr extends AbstractPreferenceInitializer {

	public XtermPreferencesInitializr() {
	}

	@Override
	public void initializeDefaultPreferences() {
		XtermPlugin.getDefault().getPreferenceStore().setDefault(XtermPlugin.PREFS_DEFAULT_SHELL_CMD, Platform.getOS() == Platform.OS_WIN32 ? "cmd.exe" : "/bin/bash --login");
	}

}
