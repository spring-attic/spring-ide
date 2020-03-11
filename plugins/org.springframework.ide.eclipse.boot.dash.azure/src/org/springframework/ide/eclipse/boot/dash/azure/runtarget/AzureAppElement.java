package org.springframework.ide.eclipse.boot.dash.azure.runtarget;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.boot.dash.azure.client.SpringServiceClient;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springframework.ide.eclipse.boot.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

import com.google.common.collect.ImmutableSet;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.AppResource;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.DeploymentResource;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.DeploymentResourceProvisioningState;

public class AzureAppElement extends WrappingBootDashElement<String> {

	private AppResource app;

	public static final Set<DeploymentResourceProvisioningState> BUSY = ImmutableSet.of(
			DeploymentResourceProvisioningState.CREATING,
			DeploymentResourceProvisioningState.UPDATING
	);

	private LiveExpression<RunState> runState = new AsyncLiveExpression<RunState>(RunState.UNKNOWN) {

		@Override
		protected RunState compute() {
			RunState v = extracted();
			System.out.println(app.name() + " => "+v);
			return v;
		}

		private RunState extracted() {
			AzureBootDashModel model = ((AzureBootDashModel)getBootDashModel());
			AzureRunTarget target = model.getRunTarget();
			String rg = target.getResourceGroupName();
			String sn = target.getClusterName();
			SpringServiceClient client = target.client.getValue();
			if (client!=null) {
				String activeDepName = app.properties().activeDeploymentName();
				if (activeDepName!=null) {
					DeploymentResource dep = client.getSpringManager().deployments().getAsync(rg, sn, app.name(), activeDepName).toBlocking().single();
					if (dep!=null) {
						DeploymentResourceProvisioningState state = dep.properties().provisioningState();
						if (BUSY.contains(state)) {
							return RunState.STARTING;
						} else if (DeploymentResourceProvisioningState.SUCCEEDED.equals(state)) {
							if (dep.properties().active()) {
								return RunState.RUNNING;
							}
						} else if (DeploymentResourceProvisioningState.FAILED.equals(state)) {
							return RunState.CRASHED;
						} else {
							return RunState.UNKNOWN;
						}
					}
				}
				return RunState.INACTIVE;
			}
			return RunState.UNKNOWN;
		}

	};

	@Override
	public void dispose() {
		runState.dispose();
	}

	public AzureAppElement(AzureBootDashModel bootDashModel, AppResource app) {
		super(bootDashModel, app.id());
		this.app = app;
		runState.dependsOn(bootDashModel.getRunTarget().client);
		runState.dependsOn(bootDashModel.refreshCount());
		addDisposableChild(runState);
		addElementNotifier(runState);
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	public RunState getRunState() {
		return this.runState.getValue();
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
		return getBootDashModel();
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return EnumSet.noneOf(RunState.class);
	}

	@Override
	public String getName() {
		return app.name();
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		return null;
	}

}
