package org.springframework.ide.eclipse.boot.dash.model.remote;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springframework.ide.eclipse.boot.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

public class GenericRemoteAppElement extends WrappingBootDashElement<String> {

	private static AtomicInteger instances = new AtomicInteger();

	private LiveVariable<App> app = new LiveVariable<>();

	private LiveExpression<RunState> runState = new AsyncLiveExpression<RunState>(RunState.UNKNOWN) {

		{
			dependsOn(app);
		}

		@Override
		protected RunState compute() {
			App data = app.getValue();
			if (data!=null) {
				RunState v = data.fetchRunState();
				System.out.println(data.getName() + " => "+v);
				return v;
			}
			return RunState.UNKNOWN;
		}
	};

	public GenericRemoteAppElement(GenericRemoteBootDashModel<?, ?> parent, String appId) {
		super(parent, appId);
		System.out.println("New GenericRemoteAppElement instances = " +instances.incrementAndGet());

		app.dependsOn(parent.getRunTarget().getClientExp());
		app.dependsOn(getBootDashModel().refreshCount());
		addDisposableChild(runState);
		addElementNotifier(runState);
		onDispose(d -> {
			System.out.println("Dispose GenericRemoteAppElement instances = " +instances.decrementAndGet());
		});
	}

	@Override
	public GenericRemoteBootDashModel<?, ?> getBootDashModel() {
		return (GenericRemoteBootDashModel<?, ?>) super.getBootDashModel();
	}

	@Override
	public String getName() {
		App data = app.getValue();
		return data!=null?data.getName():"Uknown";
	}

	public void setAppData(App appData) {
		this.app.setValue(appData);
	}

	@Override
	public RunState getRunState() {
		return runState.getValue();
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return EnumSet.noneOf(RunState.class);
	}

	@Override
	public IProject getProject() {
		// TODO Auto-generated method stub
		return null;
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
	public PropertyStoreApi getPersistentProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}
