package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public interface RemoteRunTarget<Client, Params> extends RunTarget<Params> {
	boolean isConnected();
	void addConnectionStateListener(ValueListener<Client> connectionListener);
	void removeConnectionStateListener(ValueListener<Client> connectionListener);
}
