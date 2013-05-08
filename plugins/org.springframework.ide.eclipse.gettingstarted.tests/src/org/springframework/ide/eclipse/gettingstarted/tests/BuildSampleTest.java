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

import static org.springframework.ide.eclipse.wizard.template.util.ExampleProjectsDashboardPart.importSample;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestUtil.*;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.wizard.template.util.NameUrlPair;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * A BuildSample test checks that a particular sample project builds properly
 * when it gets imported into STS.
 * 
 * This test class is intended to be instantiated with data about a particular
 * sample project. It provides a static suite method fetches the samples
 * and creates one test for each sample.
 * 
 * @author Kris De Volder
 */
public class BuildSampleTest extends TestCase {

	private static final int SECOND = 1000;
	private static final int MINUTE = 60*SECOND;
	
	/**
	 * Data defining the sample under test
	 */
	private NameUrlPair sample;

	public BuildSampleTest(NameUrlPair sample) {
		super(sample.getName());
		this.sample = sample;
	}

	@Override
	protected void runTest() throws Throwable {
		setAutoBuilding(false);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		final Job importJob = importSample(sample.getName(), sample.getUrl().toURI(), shell);
		new ACondition() {			
			@Override
			public boolean test() throws Exception {
				return importJob.getResult()!=null;
			}
		}.waitFor(3*MINUTE);
		IProject project = getProject(sample.getName());
		StsTestUtil.assertNoErrors(project);
	}
	
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(BuildSampleTest.class.getName());
		
		ArrayList<NameUrlPair> samples = new ArrayList<NameUrlPair>(SampleTests.getSamples());
		//The next one is a 'meta-test'. We do not have pet-clinic in the list of official samples yet.
		// And the current version has errors. so the test should fail.
		samples.add(new NameUrlPair("spring-pet-clinic", "https://github.com/SpringSource/spring-petclinic"));
		for (NameUrlPair sample : samples) {
			suite.addTest(new BuildSampleTest(sample));
		}
		
		return suite;
	}

}
