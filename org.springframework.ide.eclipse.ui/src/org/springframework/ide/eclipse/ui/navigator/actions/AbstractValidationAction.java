/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.navigator.actions;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.progress.IProgressConstants;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinitionFactory;
import org.springframework.ide.eclipse.ui.SpringUIImages;

/**
 * Abstract extension to {@link AbstractNavigatorAction} that can that handles
 * validation requests for the current selection.
 * <p>
 * This class is intended to be subclassed by clients. Subclasses should
 * implement the {@link #getResourcesFromSelectedObject(Object)} method,
 * returning a {@link Set} of {@link IResource} that should be validated.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public abstract class AbstractValidationAction extends AbstractNavigatorAction {

	private Set<IResource> selectedResources;

	public AbstractValidationAction(ICommonActionExtensionSite site) {
		super(site, "Validate", SpringUIImages.DESC_OBJS_SPRING);
	}

	/**
	 * Return a {@link Set} of {@link IResource} that should be validated.
	 * @param object one of the selected objects
	 */
	protected abstract Set<IResource> getResourcesFromSelectedObject(
			Object object);

	public boolean isEnabled(IStructuredSelection selection) {
		this.selectedResources = new LinkedHashSet<IResource>();
		if (selection.size() > 0) {
			for (Object obj : selection.toList()) {
				if (obj instanceof IFile) {
					this.selectedResources.add((IFile) obj);
				}
				else if (obj instanceof IAdaptable
						&& ((IAdaptable) obj).getAdapter(IResource.class) != null) {
					IResource resource = (IResource) ((IAdaptable) obj)
							.getAdapter(IResource.class);
					this.selectedResources.add(resource);
				}
				else {
					Set<IResource> resources = getResourcesFromSelectedObject(obj);
					if (resources != null) {
						this.selectedResources.addAll(resources);
					}
				}
			}
		}
		return this.selectedResources.size() > 0;
	}

	@Override
	public final void run() {
		Job job = new Job("Validating") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Set<ValidatorDefinition> validators = ValidatorDefinitionFactory
						.getValidatorDefinitions();
				monitor.beginTask("Validating selection", validators.size());
				int i = 0;
				for (final ValidatorDefinition validatorDefinition : validators) {
					runValidator(validatorDefinition, monitor);
					monitor.worked(i++);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.BUILD);
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.setProperty(IProgressConstants.ICON_PROPERTY,
				SpringUIImages.DESC_OBJS_SPRING);
		job.schedule();
	}

	/**
	 * Calls the given {@link ValidatorDefinition} to validate the calculated
	 * resources.
	 * <p>
	 * Note: This will not call {@link ValidatorDefinition#isEnabled(IProject)}.
	 * This is by intention because it provides a way to trigger a validation
	 * allthough it is disabled for automatic build.
	 */
	private void runValidator(final ValidatorDefinition validatorDefinition,
			final IProgressMonitor monitor) {
		ISafeRunnable code = new ISafeRunnable() {
			public void handleException(Throwable e) {
				// nothing to do - exception is already logged
			}

			public void run() throws Exception {
				Set<IResource> affectedResources = new LinkedHashSet<IResource>();
				for (IResource resource : selectedResources) {
					affectedResources.addAll(validatorDefinition.getValidator()
						.getAffectedResources(resource, 
								IncrementalProjectBuilder.INCREMENTAL_BUILD));
				}
				validatorDefinition.getValidator().validate(affectedResources,
						monitor);
			}
		};
		SafeRunner.run(code);
	}
}
