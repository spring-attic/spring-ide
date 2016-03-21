package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;

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

}
