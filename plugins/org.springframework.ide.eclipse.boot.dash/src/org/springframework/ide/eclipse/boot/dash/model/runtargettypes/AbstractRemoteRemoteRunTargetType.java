package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;

public abstract class AbstractRemoteRemoteRunTargetType<Params> extends AbstractRunTargetType<Params> implements RemoteRunTargetType<Params> {

	public AbstractRemoteRemoteRunTargetType(SimpleDIContext injections, String name) {
		super(injections, name);
	}

	@Override
	public final boolean canInstantiate() {
		return true;
	}
}
