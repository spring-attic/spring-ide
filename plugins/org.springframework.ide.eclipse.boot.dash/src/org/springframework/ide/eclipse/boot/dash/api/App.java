package org.springframework.ide.eclipse.boot.dash.api;

import java.util.EnumSet;

import org.springframework.ide.eclipse.boot.dash.model.Nameable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;

public interface App extends Nameable {
	default EnumSet<RunState> supportedGoalStates() {
		return EnumSet.noneOf(RunState.class);
	}
	default void setGoalState(RunState inactive) {}

	RunTarget getTarget();

	default void setContext(AppContext context) {}
	default void restart(RunState runingOrDebugging) {}
}
