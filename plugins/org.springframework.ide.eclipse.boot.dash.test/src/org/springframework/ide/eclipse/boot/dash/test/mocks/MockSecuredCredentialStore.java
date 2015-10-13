package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;

public class MockSecuredCredentialStore implements SecuredCredentialsStore {

	private Map<String,String> store = new HashMap<String, String>();

	@Override
	public synchronized String getPassword(String runTargetId) {
		return store.get(runTargetId);
	}

	@Override
	public synchronized void setPassword(String password, String runTargetId) {
		store.put(runTargetId, password);
	}

}
