package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationDetail.InstanceDetail;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFInstanceState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFInstanceStats;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class CFWrappingV2 {

	public static CFApplicationDetail wrap(ApplicationDetail details) {
		return new CFApplicationDetailData(details);
	}

	public static CFInstanceStats wrap(InstanceDetail instanceDetail) {
		return new CFInstanceStats() {
			@Override
			public CFInstanceState getState() {
				try {
					return CFInstanceState.valueOf(instanceDetail.getState());
				} catch (Exception e) {
					BootActivator.log(e);
					return CFInstanceState.UNKNOWN;
				}
			}
		};
	}

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
				//XXX CF V2: getTimeout
				return null;
			}

			@Override
			public String getStack() {
				//XXX CF V2: getStack
				return null;
			}

			@Override
			public List<String> getServices() {
				//XXX CF V2: Application.getServices
				return ImmutableList.of();
			}

			@Override
			public Map<String, String> getEnvAsMap() {
				//XXX CF V2: Application.getEnv
				return ImmutableMap.of();
			}

			@Override
			public String getDetectedBuildpack() {
				//XXX CF V2: Application.getDetectedBuildpack
				return null;
			}

			@Override
			public String getCommand() {
				//XXX CF V2: Application.getCommand
				return null;
			}

			@Override
			public String getBuildpackUrl() {
				//XXX CF V2: Application.getBuildpackUrl
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
			public int getDiskQuota() {
				return app.getDiskQuota();
			}
		};
	}

	public static CFService wrap(final ServiceInstance service) {
		return new CFService() {

			//XXX CF V2: cf service properties
			// Realign these properties and the corresponding properties page in the GUI as well
			// so that it displays similar infos as what is shown by 'cf service' UI.

			@Override
			public String getName() {
				return service.getName();
			}

			/**
			 * Deprecated because this information is not available in recent
			 * versions of CF. We should replace this info in our views with information
			 * similar to what CF CLI displays instead.
			 */
			@Override
			@Deprecated
			public String getVersion() {
				return null;
			}

			@Override
			public String getType() {
				//XXX CF V2: does this really mean the same thing as what it meant when we got it from v1?
				return service.getType();
			}

			/**
			 * Deprecated because this information is not available in recent
			 * versions of CF. We should replace this info in our views with information
			 * similar to what CF CLI displays instead.
			 */
			@Override
			@Deprecated
			public String getProvider() {
				return null;
			}

			@Override
			public String getPlan() {
				return service.getPlan();
			}

			@Override
			public String getDashboardUrl() {
				//TODO: see https://github.com/cloudfoundry/cf-java-client/issues/426
				//Actually... the issue was resolved and then the commit fixing it reverted again
				return null;
			}
		};
	}

	public static CFAppState wrapAppState(String s) {
		try {
			return CFAppState.valueOf(s);
		} catch (Exception e) {
			BootActivator.log(e);
			return CFAppState.UNKNOWN;
		}
	}

}
