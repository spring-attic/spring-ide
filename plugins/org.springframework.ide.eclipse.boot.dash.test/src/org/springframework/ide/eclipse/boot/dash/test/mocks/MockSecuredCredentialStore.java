package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;

public class MockSecuredCredentialStore implements SecuredCredentialsStore {

	private Map<String,String> store = new HashMap<>();

	@Override
	public synchronized String getCredentials(String runTargetId) {
		return store.get(runTargetId);
	}

	@Override
	public synchronized void setCredentials(String password, String runTargetId) {
		store.put(runTargetId, password);
	}

}
