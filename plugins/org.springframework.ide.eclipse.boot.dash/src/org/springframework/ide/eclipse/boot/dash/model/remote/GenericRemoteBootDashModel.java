package org.springframework.ide.eclipse.boot.dash.model.remote;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

import com.google.common.collect.ImmutableSet;

public class GenericRemoteBootDashModel<Client, Params> extends RemoteBootDashModel {

	private final ObservableSet<BootDashElement> apps;

	public GenericRemoteBootDashModel(RemoteRunTarget<Client, Params> target, BootDashViewModel parent) {
		super(target, parent);
		apps = ObservableSet.constant(ImmutableSet.of()); //TODO: popuplate the apps.
	}

	@Override
	public void performDeployment(Set<IProject> of, UserInteractions ui, RunState runOrDebug) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public ObservableSet<BootDashElement> getElements() {
		return apps;
	}

	@Override
	public BootDashModelConsoleManager getElementConsoleManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(UserInteractions ui) {
		// TODO Auto-generated method stub

	}

}
