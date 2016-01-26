package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * Event handler for application operations that checks if the operations should
 * terminate based on if the app current run state boot dash (but not
 * necessarily the same in CF) is no longer STARTING (for example, another concurrent
 * operation like a stop operation changed the runstate to INACTIVE) the
 * operation should terminate
 *
 */
public class StartingOperationHandler extends ApplicationOperationEventHandler {

	public StartingOperationHandler(CloudFoundryBootDashModel model) {
		super(model);
	}

	@Override
	public void checkTerminate(CloudAppInstances appInstances) throws OperationCanceledException {
		if (appInstances == null) {
			return;
		}

		CloudAppDashElement element = model.getElement(appInstances.getApplication().getName());
		RunState runState = element != null ? element.getRunState() : RunState.UNKNOWN;
		if (runState != RunState.STARTING) {
			throw new OperationCanceledException();
		}
	}

}
