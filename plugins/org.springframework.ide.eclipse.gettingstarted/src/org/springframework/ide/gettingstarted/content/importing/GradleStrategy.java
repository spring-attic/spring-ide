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
package org.springframework.ide.gettingstarted.content.importing;

import static org.springsource.ide.eclipse.gradle.core.util.expression.LiveExpression.constant;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.gettingstarted.util.IOUtil;
import org.springframework.ide.gettingstarted.content.CodeSet;
import org.springframework.ide.gettingstarted.content.CodeSet.CodeSetEntry;
import org.springsource.ide.eclipse.gradle.core.samples.SampleProject;
import org.springsource.ide.eclipse.gradle.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.gradle.core.wizards.NewGradleProjectOperation;

/**
 * Importer strategy implementation for importing CodeSets into the workspace and set them
 * up to use Gradle Tooling.
 * 
 * TODO: Gradle support should be in a separate 'plugin' that contributes the import strategy
 * as an extension point. So that we can make Gradle support optional.
 * 
 * @author Kris De Volder
 */
public class GradleStrategy extends ImportStrategy {

	private static class GradleCodeSetImport implements IRunnableWithProgress {

		private NewGradleProjectOperation op;

		public GradleCodeSetImport(ImportConfiguration conf) {
			this.op = new NewGradleProjectOperation();
			
			//Get the present values from the ImportConfiguration.
			String location = conf.getLocationField().getValue();
			final String projectName = conf.getProjectNameField().getValue();
			final CodeSet codeset = conf.getCodeSetField().getValue();
			
			//Use these values to populate the
			op.setProjectNameField(constant(projectName));
			op.setLocationField(constant(location));
			op.setSampleProjectField(constant(asSample(projectName, codeset)));
		}

		private SampleProject asSample(final String projectName, final CodeSet codeset) {
			return new SampleProject() {
				@Override
				public String getName() {
					//Probably nobody cares about the name but anyway...
					return projectName;
				}

				@Override
				public void createAt(final File location) throws CoreException {
					try {
						codeset.each(new CodeSet.Processor<Void>() {
							public Void doit(CodeSetEntry e) throws Exception {
								IPath path = e.getPath();
								File target = new File(location, path.toString());
								if (e.isDirectory()) {
									target.mkdirs();
								} else {
									IOUtil.pipe(e.getData(), target);
								}
								return null;
							}
						});
					} catch (Exception e) {
						throw ExceptionUtil.coreException(e);
					}
				}
			};
		}



		@Override
		public void run(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
			try {
				op.perform(mon);
			} catch (CoreException e) {
				throw new InvocationTargetException(e);
			}
		}

	}

	@Override
	public IRunnableWithProgress createOperation(ImportConfiguration conf) {
		return new GradleCodeSetImport(conf);
	}

	
	
}
