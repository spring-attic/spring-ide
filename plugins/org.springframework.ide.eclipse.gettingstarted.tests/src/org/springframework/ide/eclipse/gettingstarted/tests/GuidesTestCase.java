/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.tests;

import junit.framework.TestCase;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.gettingstarted.content.GithubRepoContent;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.gradle.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.gradle.core.util.GradleRunnable;

/**
 * Some infrastucture shared among different dynamically generated testcases for
 * Guides.
 * 
 * @author Kris De Volder
 */
public class GuidesTestCase extends TestCase {
	
	/**
	 * The guide under test
	 */
	protected GithubRepoContent guide;

	public GuidesTestCase(GithubRepoContent guide) {
		super(guide.getName());
		this.guide = guide;
	}
	
	public static void buildJob(final GradleRunnable gradleRunnable) throws Exception {
		final Job job = new Job("buildJob") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					gradleRunnable.run(monitor);
				} catch (Throwable e) {
					return ExceptionUtil.status(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.schedule();
		new ACondition() {
			@Override
			public boolean test() throws Exception {
				return job.getResult()!=null;
			}
		}.waitFor(5*60*1000);
		assertOk(job.getResult());
	}

	private static void assertOk(IStatus result) throws CoreException {
		if (!result.isOK()) {
			throw ExceptionUtil.coreException(result);
		}
	}

}
