package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.v2.serviceinstances.ServiceInstanceResource;
import org.cloudfoundry.client.v2.serviceplans.ServicePlanEntity;
import org.cloudfoundry.client.v2.serviceplans.ServicePlanResource;
import org.cloudfoundry.client.v2.services.ServiceResource;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class CFWrappingV2 {

	public static CFApplication wrap(ApplicationSummary app) {
		return new CFApplication() {

			@Override
			public String getName() {
				return app.getName();
			}

			@Override
			public List<String> getUris() {
				return app.getUrls();
			}

			@Override
			public Integer getTimeout() {
				return null;
			}

			@Override
			public CFAppState getState() {
				try {
					return CFAppState.valueOf(app.getRequestedState());
				} catch (Exception e) {
					BootActivator.log(e);
					return CFAppState.UNKNOWN;
				}
			}

			@Override
			public String getStack() {
				//XXX CF V2
				return null;
			}

			@Override
			public List<String> getServices() {
				//XXX CF V2
				return ImmutableList.of();
			}

			@Override
			public int getRunningInstances() {
				return app.getRunningInstances();
			}

			@Override
			public int getMemory() {
				return app.getMemoryLimit();
			}

			@Override
			public int getInstances() {
				return app.getInstances();
			}

			@Override
			public UUID getGuid() {
				return UUID.fromString(app.getId());
			}

			@Override
			public Map<String, String> getEnvAsMap() {
				//XXX CF V2
				return ImmutableMap.of();
			}

			@Override
			public int getDiskQuota() {
				return app.getDiskQuota();
			}

			@Override
			public String getDetectedBuildpack() {
				//XXX CF V2
				return null;
			}

			@Override
			public String getCommand() {
				//XXX CF V2
				return null;
			}

			@Override
			public String getBuildpackUrl() {
				//XXX CF V2
				return null;
			}
		};
	}

	public static CFService wrap(final ServicePlanResource plan, final ServiceInstanceResource instance) {
		return new CFService() {
			public String getName() {
				return instance.getEntity().getName();
			}

			@Override
			@Deprecated
			public String getVersion() {
				return null;
			}

			@Override
			public String getType() {
				return instance.getEntity().getType();
			}

			/**
			 * This info is deprecated, client probably doesn't even return it anymore!
			 */
			@Override
			@Deprecated
			public String getProvider() {
				return null;
//				return service.getEntity().getProvider();
			}

			@Override
			public String getPlan() {
				//XXX CF V2 an id is probably not what the caller expects
				return plan.getEntity().getName();
			}

			@Override
			public String getDashboardUrl() {
				return instance.getEntity().getDashboardUrl();
			}
		};
	}

}
