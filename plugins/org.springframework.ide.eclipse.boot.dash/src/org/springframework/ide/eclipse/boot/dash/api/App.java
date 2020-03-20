package org.springframework.ide.eclipse.boot.dash.api;

import org.springframework.ide.eclipse.boot.core.initializr.IdAble;
import org.springframework.ide.eclipse.boot.dash.model.Nameable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

public interface App extends Nameable, IdAble {
	RunState fetchRunState();
}
