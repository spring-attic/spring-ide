/*******************************************************************************
 *  Copyright (c) 2015 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.buildship20;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.configuration.FixedRequestAttributesBuilder;
import org.eclipse.buildship.core.workspace.NewProjectHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.boot.wizard.content.BuildType;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportConfiguration;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportStrategy;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportStrategyFactory;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.util.NatureUtils;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

/**
 * Importer strategy implementation for importing CodeSets into the workspace and set them
 * up to use Buildship Gradle Tooling.
 *
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class Buildship20ImportStrategy extends ImportStrategy {

	protected static final NewProjectHandler IMPORT_AND_ADD_SPRING_NATURE = new NewProjectHandler() {

		@Override
		public boolean shouldImport(OmniEclipseProject projectModel) {
			return true;
		}

		@Override
		public void afterImport(IProject p, OmniEclipseProject projectModel) {
			try {
				NatureUtils.ensure(p, new NullProgressMonitor(), SpringCoreUtils.NATURE_ID);
			} catch (CoreException e) {
				FrameworkCoreActivator.log(e);
			}
		}
	};

	public Buildship20ImportStrategy(BuildType buildType, String name, String notInstalledMessage) {
		super(buildType, name, notInstalledMessage);
	}

	public static class Factory implements ImportStrategyFactory {
		@Override
		public ImportStrategy create(BuildType buildType, String name, String notInstalledMessage) throws Exception {
			Assert.isLegal(buildType==BuildType.GRADLE);
			Class.forName("org.eclipse.buildship.core.util.configuration.FixedRequestAttributesBuilder");
			return new Buildship20ImportStrategy(buildType, name, notInstalledMessage);
		}

	}

	@Override
	public IRunnableWithProgress createOperation(final ImportConfiguration conf) {
		return new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
				mon.beginTask("Import Gradle Buildship project", 10);
				try {
					File loc = new File(conf.getLocation());
					conf.getCodeSet().createAt(loc);

					CompletableFuture<Void> done = new CompletableFuture<>();
					FixedRequestAttributes attributes = FixedRequestAttributesBuilder.fromWorkspaceSettings(loc)
						.build();
					CorePlugin.gradleWorkspaceManager().getGradleBuild(attributes).synchronize(IMPORT_AND_ADD_SPRING_NATURE,
							(monitor, cancelation) -> {
								if (monitor.isCanceled() || cancelation.isCancellationRequested()) {
									done.cancel(true);
								} else {
									done.complete(null);
								}
							}
					);
					done.get();
				} catch (Exception e) {
					if (e instanceof InterruptedException) {
						throw (InterruptedException)e;
					}
					if (e instanceof InvocationTargetException) {
						throw (InvocationTargetException)e;
					}
					throw new InvocationTargetException(e);
				}
				finally {
					mon.done();
				}
			}
		};
	}

}
