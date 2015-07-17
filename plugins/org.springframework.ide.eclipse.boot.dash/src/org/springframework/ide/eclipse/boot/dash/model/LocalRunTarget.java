package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.*;

public class LocalRunTarget extends AbstractRunTarget {

	public static final RunTarget INSTANCE = new LocalRunTarget();
	private static final BootDashColumn[] DEFAULT_COLUMNS = {RUN_STATE_ICN, PROJECT, LIVE_PORT, DEFAULT_PATH, TAGS};

	private LocalRunTarget() {
		super(RunTargetTypes.LOCAL, "local");
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return RunTargets.LOCAL_RUN_GOAL_STATES;
	}

	@Override
	public List<ILaunchConfiguration> getLaunchConfigs(BootDashElement element) {
		IProject p = element.getProject();
		if (p != null) {
			return BootLaunchConfigurationDelegate.getLaunchConfigs(p);
		}
		return Collections.emptyList();
	}

	public ILaunchConfiguration createLaunchConfig(IJavaProject jp, IType mainType) throws Exception {
		if (mainType != null) {
			return BootLaunchConfigurationDelegate.createConf(mainType);
		} else {
			return BootLaunchConfigurationDelegate.createConf(jp);
		}
	}

	@Override
	public BootDashColumn[] getDefaultColumns() {
		return DEFAULT_COLUMNS;
	}

	@Override
	public BootDashModel createElementsTabelModel(BootDashModelContext context) {
		return new LocalBootDashModel(context);
	}

	@Override
	public boolean canRemove() {
		return false;
	}

	@Override
	public boolean canDeployAppsTo() {
		return false;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return true;
	}
}