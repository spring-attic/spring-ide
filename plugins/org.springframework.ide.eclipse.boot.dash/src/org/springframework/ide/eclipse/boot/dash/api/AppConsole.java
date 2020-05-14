package org.springframework.ide.eclipse.boot.dash.api;

import org.springframework.ide.eclipse.boot.dash.console.LogType;

public interface AppConsole {

	void write(String string, LogType stdout);

}
