/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * {@link RenameParticipant} to refactor rename attempts of {@link IBeansConfig}. 
 * @author Christian Dupuis
 * @since 2.3.0
 */
public class BeansConfigRenameRefactoringParticipant extends RenameParticipant {

	private IFile config;
	private BeansConfigId configId;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		return new RefactoringStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (!getArguments().getUpdateReferences()) {
			return null;
		}
		String newName = getArguments().getNewName();
		return new ModelChange(config, newName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return "Update references in Spring project configurations";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IFile && BeansCoreUtils.isBeansConfig((IResource) element, false)) {
			config = (IFile) element;
			configId = BeansConfigFactory.getConfigId(config);
			return true;
		}
		return false;
	}

	private static class ModelChange extends Change {

		private final IFile config;

		private final String newName;

        private BeansConfigId configId;

		public ModelChange(IFile config, String newName) {
			this.config = config;
			this.configId = BeansConfigFactory.getConfigId(config);
			this.newName = newName;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getModifiedElement() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getName() {
			return "Rename references to '" + config.getName() + "' in Spring Model";
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void initializeValidationData(IProgressMonitor pm) {
			// empty
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public RefactoringStatus isValid(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
			return new RefactoringStatus();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Change perform(IProgressMonitor pm) throws CoreException {
			if (pm.isCanceled()) {
				return null;
			}

			for (IBeansProject project : BeansCorePlugin.getModel().getProjects()) {
				boolean updated = false;
				BeansProject beansProject = (BeansProject) project;

				// Firstly rename references to config sets
				for (IBeansConfigSet configSet : beansProject.getConfigSets()) {
					if (configSet.hasConfig(configId)) {
                        ((BeansConfigSet) configSet).removeConfig(configId);
                        IPath path = config.getFullPath();
                        IPath newPath = path.removeLastSegments(1).append(newName);
                        ((BeansConfigSet) configSet).addConfig(configId.newName(newPath.toString()));
						updated = true;
					}
				}

				// Secondly rename configs
				if (project.hasConfig(configId)) {
					IPath path = config.getProjectRelativePath();
					IPath newPath = path.removeLastSegments(1).append(newName);
					((BeansProject) project).removeConfig(configId);
					((BeansProject) project).addConfig(configId.newName(newPath.toString()), IBeansConfig.Type.MANUAL);
					removeMarkers(config);
					updated = true;
				}

				if (updated) {
					((BeansProject) project).saveDescription();
					BeansModelLabelDecorator.update();
				}
			}
			
			
			return null;
//			IPath path = config.getFullPath().removeLastSegments(1).append(newName);
//			IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
//			return new ModelChange(newFile, config.getName());
		}

		private void removeMarkers(IFile file) {
			// Look for markers that have been created elsewhere in the workspace but originate from the given resource
			try {
				String originatingResourceValue = file.getFullPath().toString();
				IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(SpringCore.MARKER_ID, true,
						IResource.DEPTH_INFINITE);
				for (IMarker marker : markers) {
					if (originatingResourceValue.equals(marker.getAttribute(MarkerUtils.ORIGINATING_RESOURCE_KEY))) {
						marker.delete();
					}
				}
			}
			catch (CoreException e) {
			}

		}
	}

}
