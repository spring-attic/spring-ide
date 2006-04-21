package org.springframework.ide.eclipse.beans.ui.wizards.wizards;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ProjectNode;

public interface IBeanModelAwareWizard {
	public void init(ProjectNode projectNode, ConfigNode configNode,
			IResource[] resources);

}
