package org.springframework.ide.eclipse.boot.dash.api;

import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;

public interface AppContext {

	RefreshStateTracker getRefreshTracker();

}
