package org.springframework.ide.eclipse.boot.dash.model.remote;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.api.RunStateProvider;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.livexp.DisposingFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
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
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableSet;

public class GenericRemoteAppElement extends WrappingBootDashElement<String> implements Deletable, AppContext {

	private static AtomicInteger instances = new AtomicInteger();

	private LiveVariable<App> app = new LiveVariable<>();

	private final Object parent;

	private LiveSetVariable<String> existingChildIds = new LiveSetVariable<>();

	private final RefreshStateTracker refreshTracker = new RefreshStateTracker(this);

	DisposingFactory<String, GenericRemoteAppElement> childFactory = new DisposingFactory<String, GenericRemoteAppElement>(existingChildIds) {
		@Override
		protected GenericRemoteAppElement create(String appId) {
			return new GenericRemoteAppElement(GenericRemoteAppElement.this, appId);
		}
	};

	private ObservableSet<BootDashElement> children = new ObservableSet<BootDashElement>() {

		{
			dependsOn(app);
			dependsOn(refreshTracker.refreshState);
		}

		@Override
		protected ImmutableSet<BootDashElement> compute() {
			App appVal = app.getValue();
			if (appVal instanceof ChildBearing) {
				try {
					List<App> children = ((ChildBearing)appVal).fetchChildren();
					ImmutableSet.Builder<String> existingIds = ImmutableSet.builder();
					for (App app : children) {
						existingIds.add(app.getName());
					}
					existingChildIds.replaceAll(existingIds.build());

					ImmutableSet.Builder<BootDashElement> builder = ImmutableSet.builder();
					for (App child : children) {
						GenericRemoteAppElement childElement = childFactory.createOrGet(child.getName());
						child.setContext(childElement);
						childElement.setAppData(child);
						builder.add(childElement);
					}
					return builder.build();
				} catch (Exception e) {
					Log.log(e);
				}
			}
			return ImmutableSet.of();
		}
	};

	private LiveExpression<RunState> runState = new AsyncLiveExpression<RunState>(RunState.UNKNOWN) {
		{
			dependsOn(app);
			dependsOn(children);
			dependsOn(refreshTracker.refreshState);
		}

		@Override
		protected RunState compute() {
			RefreshState r = refreshTracker.refreshState.getValue();
			if (r.isLoading()) {
				return RunState.STARTING;
			}
			App data = app.getValue();
			Assert.isLegal(!(data instanceof RunStateProvider && data instanceof ChildBearing));
			if (data instanceof RunStateProvider) {
				RunState v = ((RunStateProvider) data).fetchRunState();
				System.out.println(data.getName() + " => "+v);
				return v;
			} else if (data instanceof ChildBearing) {
				RunState v = RunState.INACTIVE;
				for (BootDashElement child : children.getValues()) {
					v = v.merge(child.getRunState());
				}
				return v;
			}
			return RunState.UNKNOWN;
		}
	};


	public GenericRemoteAppElement(RemoteBootDashModel model, String appId) {
		this(model, model, appId);
	}

	public GenericRemoteAppElement(RemoteBootDashModel model, Object parent, String appId) {
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
	public RefreshStateTracker getRefreshTracker() {
		return refreshTracker;
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
