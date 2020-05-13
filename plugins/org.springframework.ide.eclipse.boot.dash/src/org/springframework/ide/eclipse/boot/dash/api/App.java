package org.springframework.ide.eclipse.boot.dash.api;

import java.util.EnumSet;

import org.springframework.ide.eclipse.boot.core.initializr.IdAble;
import org.springframework.ide.eclipse.boot.dash.model.Nameable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

public interface App extends Nameable, IdAble {
	default EnumSet<RunState> supportedGoalStates() {
		return EnumSet.noneOf(RunState.class);
	}

	void setGoalState(RunState state);
}
