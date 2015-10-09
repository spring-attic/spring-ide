package org.springframework.ide.eclipse.boot.dash.model;

public interface SecuredCredentialsStore {

	String getPassword(String string);
	void setPassword(String password, String runTargetId);

}
