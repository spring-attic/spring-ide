/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.boot.core.StarterId;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;

public class AddStarterModel implements OkButtonHandler {
	
	private final ISpringBootProject project;
	public final LiveVariable<SpringBootStarter> chosen = new LiveVariable<SpringBootStarter>(null);
	
	public final Validator validator = new Validator() {
		@Override
		protected ValidationResult compute() {
			try {
				SpringBootStarter starter = chosen.getValue();
				if (starter==null) {
					return ValidationResult.error("No starter selected");
				} else if (project.getBootStarters().contains(starter)) {
					return ValidationResult.error("Starter '"+starter.getLabel()+"' already exists on project '"+getProjectName()+"'");
				}
			} catch (Exception e) {
				//ensure a trace of the error in the error log.
				BootActivator.log(e); 
				return ValidationResult.error("Unexpected error: "+ExceptionUtil.getMessage(e));
			}
			return ValidationResult.OK;
		}
	};
	{
		validator.dependsOn(chosen);
	}
	
	public AddStarterModel(IProject project) throws CoreException {
		this.project = SpringBootCore.create(project);
	}

	public String getProjectName() {
		return this.project.getProject().getName();
	}

	public void performOk() throws CoreException {
		final SpringBootStarter starter = chosen.getValue();
		if (chosen.getValue()!=null) {
			Job job = new Job("Modifying starters for "+getProjectName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						project.addStarter(starter);
						return Status.OK_STATUS;
					} catch (CoreException e) {
						BootActivator.log(e);
						return ExceptionUtil.status(e);
					}
				}
			};
			job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
			job.schedule();
		}
	}

	public SpringBootStarter[] getAvailableStarters() throws CoreException {
// naive implementation shows all starters
//		List<SpringBootStarter> all = project.getKnownStarters();
//		return all.toArray(new SpringBootStarter[all.size()]);
		
// code below only returns 'truely' available starters. I.e. those that are not yet applied to the
// project (note that this doesn't exclude starters that are added implicitly as dependencies of
// something else (e.g. 'tomcat' is added implicitly as a dependency from 'web'
		
		List<SpringBootStarter> all = project.getKnownStarters();
		List<SpringBootStarter> useds = project.getBootStarters();
		HashSet<StarterId> usedIds = new HashSet<StarterId>();
		for (SpringBootStarter used : useds) {
			usedIds.add(used.getId());
		}
		List<SpringBootStarter> available = new ArrayList<SpringBootStarter>();
		for (SpringBootStarter starter : all) {
			if (!usedIds.contains(starter.getId())) {
				available.add(starter);
			}
		}
		return available.toArray(new SpringBootStarter[available.size()]);
	}

}
