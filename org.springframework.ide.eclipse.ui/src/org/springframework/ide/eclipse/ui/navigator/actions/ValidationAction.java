/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.navigator.actions;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.progress.IProgressConstants;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinitionFactory;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidator;
import org.springframework.ide.eclipse.ui.SpringUIImages;

/**
 * {@link CommonNavigator} action which triggers all {@link IValidator} defined for the selected
 * {@link IModelElement}.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0.1
 */
public class ValidationAction extends AbstractNavigatorAction {

	private Map<ValidatorDefinition, Set<IResource>> validatorResources;

	public ValidationAction(ICommonActionExtensionSite site) {
		super(site, "Validate", SpringUIImages.DESC_OBJS_SPRING);
	}

	/**
	 * Creates a map of all {@link ValidatorDefinition}s and their {@link IResource}s which should
	 * be validated for the currently selected {@link IModelElement}s.
	 * <p>
	 * Note: This will not call {@link ValidatorDefinition#isEnabled(IProject)}. This is by
	 * intention because it provides a way to trigger a validation even though it is disabled for
	 * automatic build.
	 */
	public final boolean isEnabled(IStructuredSelection selection) {
		validatorResources = new LinkedHashMap<ValidatorDefinition, Set<IResource>>();
		if (selection.size() > 0) {
			for (ValidatorDefinition validatorDefinition : ValidatorDefinitionFactory
					.getValidatorDefinitions()) {
				IValidator validator = validatorDefinition.getValidator();
				for (Object object : selection.toList()) {
					Set<IResource> resources = validator.deriveResources(object);
					if (resources != null && resources.size() > 0) {
						Set<IResource> valResources = validatorResources.get(validatorDefinition);
						if (valResources == null) {
							valResources = new LinkedHashSet<IResource>();
							validatorResources.put(validatorDefinition, valResources);
						}
						valResources.addAll(resources);
					}
				}
			}
		}
		return validatorResources.size() > 0;
	}

	@Override
	public final void run() {
		Job job = new Job("Validating") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Validating selected model elements", validatorResources.size());
				for (final ValidatorDefinition validatorDefinition : validatorResources.keySet()) {
					runValidator(validatorDefinition, validatorResources.get(validatorDefinition),
							monitor);
					monitor.worked(1);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.BUILD);
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.setProperty(IProgressConstants.ICON_PROPERTY, SpringUIImages.DESC_OBJS_SPRING);
		job.schedule();
	}

	/**
	 * Calls the given {@link ValidatorDefinition} to validate the given resources.
	 */
	private void runValidator(final ValidatorDefinition validatorDefinition,
			final Set<IResource> resources, final IProgressMonitor monitor) {
		ISafeRunnable code = new ISafeRunnable() {

			public void handleException(Throwable e) {
				// nothing to do - exception is already logged
			}

			public void run() throws Exception {
				IValidator validator = validatorDefinition.getValidator();
				Set<IResource> affectedResources = new LinkedHashSet<IResource>();
				for (IResource resource : resources) {
					affectedResources.addAll(validator.getAffectedResources(resource,
							IncrementalProjectBuilder.INCREMENTAL_BUILD, IResourceDelta.CHANGED));
				}
				validator.validate(affectedResources, IncrementalProjectBuilder.INCREMENTAL_BUILD,
						monitor);
			}
		};
		SafeRunner.run(code);
	}
}
