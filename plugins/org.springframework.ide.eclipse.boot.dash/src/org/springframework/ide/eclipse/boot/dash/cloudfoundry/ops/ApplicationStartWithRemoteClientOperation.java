package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

public class ApplicationStartWithRemoteClientOperation extends CloudApplicationOperation {

	final private RunState runOrDebug;

	public ApplicationStartWithRemoteClientOperation(String opName, CloudFoundryBootDashModel model, String appName, RunState runOrDebug) {
		super(opName, model, appName);
		this.runOrDebug = runOrDebug;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		List<CloudApplicationOperation> ops = new ArrayList<CloudApplicationOperation>();

		CloudAppInstances instances = getCachedApplication();
		Map<String, String> envVars = instances.getApplication().getEnvAsMap();

		CloudDashElement cde = model.getElement(appName);
		if (cde == null || cde.getProject() == null) {
			throw new CoreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "Local project not associated to CF app '" + appName + "'"));
		}

		if (!DevtoolsUtil.isEnvVarSetupForRemoteClient(envVars, DevtoolsUtil.getSecret(cde.getProject()), runOrDebug)) {
			ops.add(new FullApplicationDeployment(cde.getProject(), model, null, true, runOrDebug));
		} else if (cde.getRunState() == RunState.INACTIVE) {
			ops.add(new ApplicationStartOperation(appName, model));
		}

		ops.add(new RemoteDevClientStartOperation(model, appName, runOrDebug));

		new ApplicationOperationWithModelUpdate(getName(), model, appName, ops, false).run(monitor);
	}

}
