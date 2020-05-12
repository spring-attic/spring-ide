package org.springframework.ide.eclipse.boot.dash.model.remote;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.livexp.DisposingFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springframework.ide.eclipse.boot.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

import com.google.common.collect.ImmutableSet;

public class GenericRemoteAppElement extends WrappingBootDashElement<String> implements Deletable {

	private static AtomicInteger instances = new AtomicInteger();

	private LiveVariable<App> app = new LiveVariable<>();

	private final Object parent;

	private LiveSetVariable<String> existingChildIds = new LiveSetVariable<>();

	DisposingFactory<String, GenericRemoteAppElement> childFactory = new DisposingFactory<String, GenericRemoteAppElement>(existingChildIds) {
		@Override
		protected GenericRemoteAppElement create(String appId) {
			return new GenericRemoteAppElement(GenericRemoteAppElement.this, appId);
		}
	};

	private ObservableSet<BootDashElement> children = new ObservableSet<BootDashElement>() {

		{
			dependsOn(app);
		}

		@Override
		protected ImmutableSet<BootDashElement> compute() {
			App appVal = app.getValue();
			if (appVal instanceof ChildBearing) {
				List<App> children = ((ChildBearing)appVal).getChildren();
				ImmutableSet.Builder<String> existingIds = ImmutableSet.builder();
				for (App app : children) {
					existingIds.add(app.getId());
				}
				existingChildIds.replaceAll(existingIds.build());

				ImmutableSet.Builder<BootDashElement> builder = ImmutableSet.builder();
				for (App child : children) {
					GenericRemoteAppElement childElement = childFactory.createOrGet(child.getId());
					childElement.setAppData(child);
					builder.add(childElement);
				}
				return builder.build();
			}
			return ImmutableSet.of();
		}
	};

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

	public GenericRemoteAppElement(GenericRemoteBootDashModel<?, ?> model, String appId) {
		this(model, model, appId);
	}

	public GenericRemoteAppElement(GenericRemoteBootDashModel<?, ?> model, Object parent, String appId) {
		super(model, appId);
		addDisposableChild(this.childFactory);
		this.parent = parent;
		System.out.println("New GenericRemoteAppElement instances = " +instances.incrementAndGet());

		app.dependsOn(model.getRunTarget().getClientExp());
		app.dependsOn(getBootDashModel().refreshCount());
		addDisposableChild(runState);
		addElementNotifier(runState);
		onDispose(d -> {
			System.out.println("Dispose GenericRemoteAppElement instances = " +instances.decrementAndGet());
		});
	}

	public GenericRemoteAppElement(GenericRemoteAppElement parent, String appId) {
		this(parent.getBootDashModel(), parent, appId);
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
		App app = this.app.getValue();
		return app!=null?app.supportedGoalStates():EnumSet.noneOf(RunState.class);
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
		return parent;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservableSet<BootDashElement> getChildren() {

		return children;
	}

	@Override
	public boolean canDelete() {
		App app = this.app.getValue();
		if (app instanceof Deletable) {
			return ((Deletable) app).canDelete();
		}
		return false;
	}

	@Override
	public void delete() throws Exception {
		App app = this.app.getValue();
		if (app instanceof Deletable) {
			((Deletable) app).delete();
		}
	}

}
