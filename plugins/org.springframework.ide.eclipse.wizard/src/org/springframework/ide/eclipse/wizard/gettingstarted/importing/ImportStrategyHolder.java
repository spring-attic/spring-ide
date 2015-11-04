package org.springframework.ide.eclipse.wizard.gettingstarted.content;

import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategy;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.NullImportStrategy;

public class ImportStrategyFactory {

	private BuildType buildType;
	private String klass; //Class name for import strategy. May not be able to classload if requisite tooling isn't installed.
	private String notInstalledMessage; //Message tailored to the particular tooling that is needed for an
	private String name; //Short name that can be used to identify strategy to the user (this is only used when more than one
						// strategy is available for a single build-type.

	private ImportStrategy instance = null;

	public ImportStrategyFactory(BuildType buildType, String klass, String notInstalledMessage, String name) {
		this.buildType = buildType;
		this.klass = klass;
		this.notInstalledMessage = notInstalledMessage;
		this.name = name;
	}

	public String displayName() {
		if (buildType.getImportStrategies().size()>1) {
			//Name needs disambiguation
			return buildType.displayName() + " ("+name+")";
		} else {
			//Just the buildtype name is enough
			return buildType.displayName();
		}
	}

	public ImportStrategy get() {
		if (instance == null) {
			try {
				this.instance = (ImportStrategy) Class.forName(klass).newInstance();
			} catch (Throwable e) {
				//THe most likely cause of this error is that optional dependencies needed to support
				// this import strategy are not installed.
				WizardPlugin.log(e);
				this.instance = new NullImportStrategy(displayName(), notInstalledMessage);
			}
		}
		return instance;
	}

}
