package org.springframework.ide.eclipse.beans.ui.wizards.wizards;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

public interface IResourcesAwareWizard {
	public void init(IBeansProject project, IBeansConfig config, IType type);
}
