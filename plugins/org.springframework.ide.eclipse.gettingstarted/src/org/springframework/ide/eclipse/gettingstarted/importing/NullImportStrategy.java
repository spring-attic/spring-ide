package org.springframework.ide.eclipse.gettingstarted.importing;

import org.eclipse.jface.operation.IRunnableWithProgress;

public class NullImportStrategy extends ImportStrategy {

	private String buildType;
	private String notInstalledMessage;

	public NullImportStrategy(String buildType, String notInstalledMessage) {
		this.buildType = buildType;
		this.notInstalledMessage = notInstalledMessage;
	}

	@Override
	public IRunnableWithProgress createOperation(ImportConfiguration conf) {
		throw new Error("Can not import using '"+buildType+"' because "+notInstalledMessage);
	}

	@Override
	public boolean isSupported() {
		return false;
	}
	
}
