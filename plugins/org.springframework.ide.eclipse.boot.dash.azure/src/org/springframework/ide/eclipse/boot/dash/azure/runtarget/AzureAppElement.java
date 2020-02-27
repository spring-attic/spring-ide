package org.springframework.ide.eclipse.boot.dash.azure.runtarget;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springframework.ide.eclipse.boot.pstore.PropertyStoreApi;

import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;

public class AzureAppElement extends WrappingBootDashElement<UUID> {

	public AzureAppElement(BootDashModel bootDashModel, UUID delegate) {
		super(bootDashModel, delegate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	public RunState getRunState() {
		// TODO Auto-generated method stub
		return RunState.UNKNOWN;
	}

	@Override
	public int getLivePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLiveHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RequestMapping> getLiveRequestMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LiveBeansModel getLiveBeans() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LiveEnvModel getLiveEnv() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stopAsync(UserInteractions ui) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void openConfig(UserInteractions ui) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getActualInstances() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDesiredInstances() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}
