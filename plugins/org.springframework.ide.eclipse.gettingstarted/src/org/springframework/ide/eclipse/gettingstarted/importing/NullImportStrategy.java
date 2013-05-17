package org.springframework.ide.eclipse.gettingstarted.importing;

import org.eclipse.jface.operation.IRunnableWithProgress;

public class NullImportStrategy extends ImportStrategy {

	private String buildType;

	public NullImportStrategy(String buildType) {
		this.buildType = buildType;
	}

	@Override
	public IRunnableWithProgress createOperation(ImportConfiguration conf) {
		throw new Error("Strategy not implemented for "+buildType);
	}

	@Override
	public boolean isSupported() {
		return false;
	}
	
}
