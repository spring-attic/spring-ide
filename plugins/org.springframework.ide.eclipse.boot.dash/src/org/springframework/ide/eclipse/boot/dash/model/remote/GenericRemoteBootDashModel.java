package org.springframework.ide.eclipse.boot.dash.model.remote;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.livexp.DisposingFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class GenericRemoteBootDashModel<Client, Params> extends RemoteBootDashModel {

	private LiveSetVariable<String> existingAppIds = new LiveSetVariable<>();

	DisposingFactory<String, GenericRemoteAppElement> elementFactory = new DisposingFactory<String, GenericRemoteAppElement>(existingAppIds) {
		@Override
		protected GenericRemoteAppElement create(String appId) {
			return new GenericRemoteAppElement(GenericRemoteBootDashModel.this, appId);
		}
	};

	private final ObservableSet<BootDashElement> elements;

	public GenericRemoteBootDashModel(RemoteRunTarget<Client, Params> target, BootDashViewModel parent) {
		super(target, parent);
		elements = ObservableSet.<BootDashElement>builder()
		.refresh(AsyncMode.ASYNC)
		.compute(() -> fetchApps())
		.build();
		elements.dependsOn(getRunTarget().getClientExp());
	}

	private ImmutableSet<BootDashElement> fetchApps() throws Exception {
		return refreshTracker.call("Fetching apps...", () ->  {
			Collection<App> apps = getRunTarget().fetchApps();
			Set<String> validAppIds = new HashSet<>();
			for (App app : apps) {
				validAppIds.add(app.getId());
			}
			existingAppIds.replaceAll(validAppIds);
			Builder<BootDashElement> bdes = ImmutableSet.builder();
			for (App app : apps) {
				GenericRemoteAppElement bde = elementFactory.createOrGet(app.getId());
				bde.setAppData(app);
				bdes.add(bde);
			}
			return bdes.build();
		});
	}

	@Override
	public void performDeployment(Set<IProject> of, UserInteractions ui, RunState runOrDebug) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public ObservableSet<BootDashElement> getElements() {
		return elements;
	}

	@Override
	public BootDashModelConsoleManager getElementConsoleManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(UserInteractions ui) {
		elements.refresh();
	}

	@SuppressWarnings("unchecked")
	@Override
	public RemoteRunTarget<Client, Params> getRunTarget() {
		return (RemoteRunTarget<Client, Params>) super.getRunTarget();
	}

	public LiveExpression<Integer> refreshCount() {
		return elements.refreshCount();
	}

	@Override
	public RefreshState getRefreshState() {
		return refreshTracker.refreshState.getValue();
	}

}
