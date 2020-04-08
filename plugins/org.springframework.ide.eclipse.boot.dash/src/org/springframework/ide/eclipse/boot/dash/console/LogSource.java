package org.springframework.ide.eclipse.boot.dash.console;

import reactor.core.Disposable;

public interface LogSource {
	Disposable connectLog(ApplicationLogConsole logConsole);
}
