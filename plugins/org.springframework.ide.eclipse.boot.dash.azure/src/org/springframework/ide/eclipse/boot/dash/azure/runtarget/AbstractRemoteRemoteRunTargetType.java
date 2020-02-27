package org.springframework.ide.eclipse.boot.dash.azure.runtarget;

import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTargetType;

public abstract class AbstractRemoteRemoteRunTargetType<Params> extends AbstractRunTargetType<Params>
implements RemoteRunTargetType<Params> {

	public AbstractRemoteRemoteRunTargetType(SimpleDIContext injections, String name) {
		super(injections, name);
	}

	@Override
	public boolean canInstantiate() {
		return true;
	}
}
