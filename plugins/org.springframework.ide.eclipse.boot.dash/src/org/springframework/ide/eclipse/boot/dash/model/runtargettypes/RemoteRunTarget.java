package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import java.util.Collection;

import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public interface RemoteRunTarget<Client, Params> extends RunTarget<Params> {
	default boolean isConnected() {
		return getClientExp().getValue()!=null;
	}
	LiveExpression<Client> getClientExp();
	default Client getClient() {
		return getClientExp().getValue();
	}

	/**
	 * Typically long-running (network access), avoid calling in UI thread).
	 */
	Collection<App> fetchApps();
}
