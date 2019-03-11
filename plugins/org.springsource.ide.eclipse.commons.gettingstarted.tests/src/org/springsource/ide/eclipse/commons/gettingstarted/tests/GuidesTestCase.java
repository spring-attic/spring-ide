/*******************************************************************************
 *  Copyright (c) 2013, 2016 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.tests;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.boot.wizard.content.GithubRepoContent;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import junit.framework.TestCase;

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
	
	public static abstract class GradleRunnable implements IRunnableWithProgress {
		
		private String jobName;

		public GradleRunnable(String jobName) {
			this.jobName = jobName == null ? "buildJob" : jobName;
		}

		public String getJobName() {
			return jobName;
		}

		/**
		 * This method is here so that GradleRunnable can be used as an {@link IRunnableWithProgress}. It is final
		 * as you are not supposed to implement it directly. Implement the doit method instead.
		 */
		@Override
		public final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			try {
				doit(monitor);
			} catch (InterruptedException e) {
				throw e;
			} catch (OperationCanceledException e) {
				throw new InterruptedException("Canceled by user");
			} catch (InvocationTargetException e) {
				throw e;
			} catch (Throwable e) {
				throw new InvocationTargetException(e);
			}
		}

		public abstract void doit(IProgressMonitor mon) throws Exception;
		
	}
	
	public static void buildJob(final GradleRunnable gradleRunnable) throws Exception {
		final Job job = new Job(gradleRunnable.getJobName()) {
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
