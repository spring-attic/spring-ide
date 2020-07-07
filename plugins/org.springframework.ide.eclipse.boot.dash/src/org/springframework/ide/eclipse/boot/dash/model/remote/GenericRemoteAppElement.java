/*******************************************************************************
 * Copyright (c) 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.remote;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.StyledString;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.boot.dash.api.ActualInstanceCount;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.DebuggableApp;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.api.DesiredInstanceCount;
import org.springframework.ide.eclipse.boot.dash.api.JmxConnectable;
import org.springframework.ide.eclipse.boot.dash.api.PortConnectable;
import org.springframework.ide.eclipse.boot.dash.api.ProjectRelatable;
import org.springframework.ide.eclipse.boot.dash.api.RunStateProvider;
import org.springframework.ide.eclipse.boot.dash.api.Styleable;
import org.springframework.ide.eclipse.boot.dash.livexp.DisposingFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.actuator.ActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.actuator.JMXActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springframework.ide.eclipse.boot.pstore.IPropertyStore;
import org.springframework.ide.eclipse.boot.pstore.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.pstore.PropertyStores;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class GenericRemoteAppElement extends WrappingBootDashElement<String> implements Deletable, AppContext, Styleable, ElementStateListener, JmxConnectable {

	private static final boolean DEBUG = false;

	private static AtomicInteger instances = new AtomicInteger();

	private LiveVariable<App> app = new LiveVariable<>();

	private final Object parent;

	private LiveSetVariable<String> existingChildIds = new LiveSetVariable<>();

	private final RefreshStateTracker refreshTracker = new RefreshStateTracker(this);

	final protected IPropertyStore backingStore;

	DisposingFactory<String, GenericRemoteAppElement> childFactory = new DisposingFactory<String, GenericRemoteAppElement>(existingChildIds) {
		@Override
		protected GenericRemoteAppElement create(String appId) {
			return new GenericRemoteAppElement(getBootDashModel(), GenericRemoteAppElement.this, appId, backingStore);
		}
	};

	private ObservableSet<BootDashElement> children = ObservableSet.<BootDashElement>builder().refresh(AsyncMode.ASYNC).compute(() -> {

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
					if (childElement!=null) {
						child.setContext(childElement);
						childElement.setAppData(child);
						builder.add(childElement);
					} else {
						Log.warn("No boot dash element for child: "+child);
					}
				}
				return builder.build();
			} catch (Exception e) {
				Log.log(e);
			}
		}
		return ImmutableSet.of();
	}).build();
	{
		children.dependsOn(app);
		children.dependsOn(refreshTracker.refreshState);
	}

	LiveExpression<RunState> baseRunState = new AsyncLiveExpression<RunState>(RunState.UNKNOWN) {
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

	private JmxRunStateTracker jmxRunStateTracker = new JmxRunStateTracker(this, baseRunState, app);


	private ObservableSet<Integer> livePorts = ObservableSet.<Integer>builder().refresh(AsyncMode.ASYNC).compute(() -> {

		ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();

		if (getRunState() == RunState.RUNNING || getRunState() == RunState.DEBUGGING) {
			App appVal = app.getValue();
			debug("appVal: " + appVal);
			if (appVal instanceof PortConnectable) {

				Set<Integer> appLivePorts = ((PortConnectable) appVal).getPorts();

				debug("from PortConnectable: " +appLivePorts);

				if (appLivePorts != null) {
					builder.addAll(appLivePorts);
				}
			}
			else {
				debug("not PortConnectable");
			}

			ImmutableSet<BootDashElement> children = this.children.getValue();
			if (children != null && !children.isEmpty()) {
				for (BootDashElement child : children) {
					ImmutableSet<Integer> childPorts = child.getLivePorts();
					debug("from child: " + child.getName() + " - " + childPorts);
					if (childPorts != null) {
						builder.addAll(childPorts);
					}
				}
			}
			else {
				debug("No Children");
			}
		}
		return builder.build();
	}).build();

	{
		livePorts.dependsOn(children);
		livePorts.dependsOn(app);
		livePorts.dependsOn(getRunStateExp());
	}

	private LiveExpression<Integer> actualInstanceCounts = new AsyncLiveExpression<Integer>(0) {
		{
			dependsOn(children);
			dependsOn(app);
			dependsOn(getRunStateExp());
		}

		@Override
		protected Integer compute() {
			int count = 0;

			if (getRunState() == RunState.RUNNING || getRunState() == RunState.DEBUGGING) {
				App appVal = app.getValue();
				debug("appVal: " + appVal);

				if (appVal instanceof ActualInstanceCount) {
					count += ((ActualInstanceCount) appVal).getActualInstances();
				}

				ImmutableSet<BootDashElement> children = GenericRemoteAppElement.this.children.getValue();
				if (children != null && !children.isEmpty()) {
					for (BootDashElement child : children) {
						count += child.getActualInstances();
					}
				}
			}
			return count;
		}
	};

	private void debug(String message) {
		if (DEBUG) {
			System.out.println(this + ": " + message);
		}
	}

	public GenericRemoteAppElement(GenericRemoteBootDashModel<?,?> model, Object parent, String appId, IPropertyStore parentPropertyStore) {
		super(model, appId);
		backingStore = PropertyStores.createSubStore(getName(), parentPropertyStore);
		children.dependsOn(model.refreshCount());
		addDisposableChild(children);
		addElementNotifier(children);

		baseRunState.dependsOn(model.refreshCount());
		addDisposableChild(this.childFactory);
		this.parent = parent;
		System.out.println("New GenericRemoteAppElement instances = " +instances.incrementAndGet());

		app.dependsOn(model.getRunTarget().getClientExp());
		app.dependsOn(getBootDashModel().refreshCount());
		addDisposableChild(baseRunState);
		addDisposableChild(jmxRunStateTracker);
		addElementNotifier(getRunStateExp());
		addDisposableChild(livePorts);
		addElementNotifier(livePorts);
		addDisposableChild(actualInstanceCounts);
		addElementNotifier(actualInstanceCounts);

		model.addElementStateListener(this);

		onDispose(d -> {
			System.out.println("Dispose GenericRemoteAppElement instances = " +instances.decrementAndGet());
			model.removeElementStateListener(this);
		});
	}

	@Override
	public GenericRemoteBootDashModel<?, ?> getBootDashModel() {
		return (GenericRemoteBootDashModel<?, ?>) super.getBootDashModel();
	}

	@Override
	public String getName() {
		return super.delegate;
	}

	public App getAppData() {
		return this.app.getValue();
	}

	public void setAppData(App appData) {
		this.app.setValue(appData);
	}

	@Override
	public RunState getRunState() {
		return getRunStateExp().getValue();
	}

	private LiveExpression<RunState> getRunStateExp() {
		return jmxRunStateTracker.augmentedRunState;
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
		App data = this.app.getValue();
		if (data instanceof ProjectRelatable) {
			return ((ProjectRelatable) data).getProject();
		}
		return null;
	}

	@Override
	public int getLivePort() {
		ImmutableSet<Integer> ports = getLivePorts();
		if (ports != null && !ports.isEmpty()) {
			return ports.iterator().next();
		}
		return 0;
	}

	@Override
	public ImmutableSet<Integer> getLivePorts() {
		return livePorts.getValue();
	}

	@Override
	public String getLiveHost() {
		return "localhost";
	}

	@Override
	public List<RequestMapping> getLiveRequestMappings() {
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
		App a = this.app.getValue();
		a.setGoalState(RunState.INACTIVE);
	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {
		App a = this.app.getValue();
		a.restart(runingOrDebugging);
	}

	@Override
	public void openConfig(UserInteractions ui) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getActualInstances() {
		return actualInstanceCounts.getValue();
	}

	@Override
	public int getDesiredInstances() {
		App data = getAppData();
		if (data instanceof DesiredInstanceCount) {
			return ((DesiredInstanceCount)data).getDesiredInstances();
		}
		return -1;
	}

	@Override
	public Object getParent() {
		return parent;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		return PropertyStores.createApi(backingStore);
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

	@Override
	public StyledString getStyledName(Stylers stylers) {
		App app = this.app.getValue();
		if (app instanceof Styleable) {
			return ((Styleable) app).getStyledName(stylers);
		}
		return null;
	}

	@Override
	public void stateChanged(BootDashElement e) {
		this.livePorts.refresh();
		this.actualInstanceCounts.refresh();
		RemoteJavaLaunchUtil.synchronizeWith(this);
	}

	/**
	 * Summarise all the debug ports either defined by this node or it's children.
	 */
	public ImmutableSet<Integer> getDebugPortSummary() {
		ImmutableSet.Builder<Integer> ports = ImmutableSet.builder();
		collectDebugPorts(ports);
		return ports.build();
	}

	private void collectDebugPorts(Builder<Integer> ports) {
		int port = getDebugPort();
		if (port>0) {
			ports.add(port);
		}
		for (BootDashElement c : children.getValues()) {
			if (c instanceof GenericRemoteAppElement) {
				((GenericRemoteAppElement)c).collectDebugPorts(ports);
			}
		}
	}

	@Override
	public ImmutableSet<ILaunchConfiguration> getLaunchConfigs() {
		return RemoteJavaLaunchUtil.getLaunchConfigs(this);
	}

	/**
	 * Summarise all the debug ports either defined by this node or it's children.
	 */
	public int getDebugPort() {
		App data = getAppData();
		if (data instanceof DebuggableApp) {
			return ((DebuggableApp) data).getDebugPort();
		}
		return -1;
	}

	@Override
	public String getJmxUrl() {
		App data = getAppData();
		if (data!=null && data instanceof JmxConnectable) {
			return ((JmxConnectable)data).getJmxUrl();
		}
		return null;
	}


	private LiveExpression<String> actuatorUrl;

	public synchronized LiveExpression<String> getActuatorUrl() {
		if (actuatorUrl==null) {
			actuatorUrl = new LiveExpression<String>() {
				{
					dependsOn(app);
					dependsOn(getRunStateExp());
					GenericRemoteAppElement.this.addDisposableChild(this);
//					onChange((e,v) -> {
//						if (getRunState()==RunS;
//						System.out.println("actuatorUrl = "+e.getValue());
//					});
				}

				@Override
				protected String compute() {
					RunState rs = getRunState();
					if (rs.isActive()) {
						return getJmxUrl();
					}
					return null;
				}
			};

		}
		return actuatorUrl;
	}
	private LiveExpression<LiveBeansModel> liveBeans;


	@Override
	public LiveBeansModel getLiveBeans() {
		synchronized (this) {
			if (liveBeans == null) {
				LiveExpression<String> actuatorUrl = getActuatorUrl();
				liveBeans = new AsyncLiveExpression<LiveBeansModel>(null, "Fetch beans for '"+getName()+"'") {
					@Override
					protected LiveBeansModel compute() {
						String target = actuatorUrl.getValue();
						if (target != null) {
							ActuatorClient client = JMXActuatorClient.forUrl(getTypeLookup(), () -> target);
							return client.getBeans();
						}
						return null;
					}

				};
				liveBeans.dependsOn(actuatorUrl);
				addElementState(liveBeans);
				addDisposableChild(liveBeans);
			}
		}
		return liveBeans.getValue();
	}

}
