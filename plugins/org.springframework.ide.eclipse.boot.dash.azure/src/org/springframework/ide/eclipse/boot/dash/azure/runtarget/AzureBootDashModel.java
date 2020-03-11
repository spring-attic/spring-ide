package org.springframework.ide.eclipse.boot.dash.azure.runtarget;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.azure.client.SpringServiceClient;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

import com.google.common.collect.ImmutableSet;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.AppResource;

public class AzureBootDashModel extends RemoteBootDashModel {

	private final ObservableSet<AzureAppElement> apps;

	public AzureBootDashModel(AzureRunTarget target, BootDashViewModel parent) {
		super(target, parent);
		apps = ObservableSet.<AzureAppElement>builder()
		.refresh(AsyncMode.ASYNC)
		.compute(() -> fetchApps())
		.build();

		//TODO: apps objects are never disposed. Should be disposed when they are removed as children from
		// the 'apps' ObservableSet. Otherwise the are leaking listeners attached to the parent's liveexps.
	}

	private ImmutableSet<AzureAppElement> fetchApps() {
		AzureRunTarget target = this.getRunTarget();
		SpringServiceClient client = target.client.getValue();
		if (client!=null) {
			String resourceGroupName = target.getResourceGroupName();
			String serviceName = target.getClusterName();
			Iterable<AppResource> apps = client.getSpringManager().apps().listAsync(resourceGroupName, serviceName).toBlocking().toIterable();
			ImmutableSet.Builder<AzureAppElement> builder = ImmutableSet.builder();
			for (AppResource appResource : apps) {
				System.out.println(appResource);
				System.out.println(appResource.properties().provisioningState());
				builder.add(new AzureAppElement(this, appResource));
			}
			return builder.build();
		}
		return ImmutableSet.of();
	}


	@Override
	public void performDeployment(Set<IProject> of, UserInteractions ui, RunState runOrDebug) throws Exception {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public ObservableSet<BootDashElement> getElements() {
		//yuck:
		return (ObservableSet<BootDashElement>) ((Object)apps);
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

	public LiveExpression<Integer> refreshCount() {
		return apps.refreshCount();
	}
}
