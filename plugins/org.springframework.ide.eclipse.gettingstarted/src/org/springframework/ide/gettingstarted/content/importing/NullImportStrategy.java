package org.springframework.ide.gettingstarted.content.importing;

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

}
