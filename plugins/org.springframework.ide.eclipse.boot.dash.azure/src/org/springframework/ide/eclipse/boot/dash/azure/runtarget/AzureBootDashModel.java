package org.springframework.ide.eclipse.boot.dash.azure.runtarget;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

import com.google.common.collect.ImmutableSet;

public class AzureBootDashModel extends RemoteBootDashModel {

	private final ObservableSet<AzureAppElement> apps;
	public AzureBootDashModel(AzureRunTarget target, BootDashViewModel parent) {
		super(target, parent);
		apps = ObservableSet.<AzureAppElement>builder()
		.refresh(AsyncMode.ASYNC)
		.compute(() -> this.getRunTarget().fetchApps())
		.build();
	}

	@Override
	public void performDeployment(Set<IProject> of, UserInteractions ui, RunState runOrDebug) throws Exception {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public ObservableSet<BootDashElement> getElements() {
		return ObservableSet.constant(ImmutableSet.of());
	}

	@Override
	public BootDashModelConsoleManager getElementConsoleManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(UserInteractions ui) {
		apps.refresh();
	}

	@Override
	public AzureRunTarget getRunTarget() {
		return (AzureRunTarget) super.getRunTarget();
	}
}
