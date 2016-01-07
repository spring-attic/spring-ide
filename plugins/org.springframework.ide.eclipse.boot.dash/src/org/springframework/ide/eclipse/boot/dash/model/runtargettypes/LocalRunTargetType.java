package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

public class LocalRunTargetType extends AbstractRunTargetType {
	LocalRunTargetType(String name) {
		super(name);
	}

	@Override
	public boolean canInstantiate() {
		return false;
	}

	public String toString() {
		return "RunTargetType(LOCAL)";
	}

	@Override
	public void openTargetCreationUi(LiveSet<RunTarget> targets) {
		throw new UnsupportedOperationException(
				this + " is a Singleton, it is not possible to create additional targets of this type.");
	}

	@Override
	public RunTarget createRunTarget(TargetProperties properties) {
		return null;
	}

	@Override
	public ImageDescriptor getIcon() {
		return BootDashActivator.getImageDescriptor("icons/boot-icon.png");
	}
}