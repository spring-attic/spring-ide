/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal.wizard;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.core.model.IRooInstall;
import org.springframework.ide.eclipse.roo.ui.RooUiActivator;
import org.springframework.ide.eclipse.roo.ui.internal.RooUiUtil;
import org.springframework.ide.eclipse.roo.ui.internal.StyledTextAppender;
import org.springframework.ide.eclipse.roo.ui.internal.actions.OpenShellJob;
import org.springframework.roo.shell.eclipse.Bootstrap;
import org.springframework.roo.shell.eclipse.ProjectRefresher;


/**
 * @author Christian Dupuis
 * @since 2.2.0
 */
@SuppressWarnings("restriction")
public class NewRooProjectWizard extends NewElementWizard implements INewWizard {

	private static final Map<String, String> ROO_JAVA_VERSION_MAPPING;

	private static final Map<String, String> FACET_JAVA_VERSION_MAPPING;

	static {
		ROO_JAVA_VERSION_MAPPING = new HashMap<String, String>();
		ROO_JAVA_VERSION_MAPPING.put("1.5", "5");
		ROO_JAVA_VERSION_MAPPING.put("1.6", "6");

		FACET_JAVA_VERSION_MAPPING = new HashMap<String, String>();
		FACET_JAVA_VERSION_MAPPING.put("1.5", "5.0");
		FACET_JAVA_VERSION_MAPPING.put("1.6", "6.0");
	}

	private static final String CLASSPATH_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<classpath>\n"
			+ "	<classpathentry kind=\"src\" output=\"target/classes\" path=\"src/main/java\"/>\n"
			+ "	<classpathentry excluding=\"**\" kind=\"src\" output=\"target/classes\" path=\"src/main/resources\"/>\n"
			+ "	<classpathentry kind=\"src\" output=\"target/test-classes\" path=\"src/test/java\"/>\n"
			+ "	<classpathentry excluding=\"**\" kind=\"src\" output=\"target/test-classes\" path=\"src/test/resources\"/>\n"
			+ "	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
			+ "	<classpathentry kind=\"output\" path=\"target/classes\"/>\n" + "</classpath>\n" + "";

	private static final String ADDON_CLASSPATH_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<classpath>\n"
			+ "	<classpathentry kind=\"src\" output=\"target/classes\" path=\"src/main/java\"/>\n"
			+ "	<classpathentry excluding=\"**\" kind=\"src\" output=\"target/classes\" path=\"src/main/resources\"/>\n"
			+ "	<classpathentry kind=\"src\" output=\"target/test-classes\" path=\"src/test/java\"/>\n"
			+ "	<classpathentry excluding=\"**\" kind=\"src\" output=\"target/test-classes\" path=\"src/test/resources\"/>\n"
			+ "	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
			+ "	<classpathentry kind=\"output\" path=\"target/classes\"/>\n" + "</classpath>\n" + "";

	private static final String PROJECT_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<projectDescription>\n" + "	<name>%PROJECTNAME%</name>\n" + "	<comment></comment>\n" + "	<projects>\n"
			+ "	</projects>\n" + "	<buildSpec>\n" +  "		<buildCommand>\n"
			+ "			<name>org.eclipse.ajdt.core.ajbuilder</name>\n" + "			<arguments>\n" + "			</arguments>\n"
			+ "		</buildCommand>\n" + "		<buildCommand>\n"
			+ "			<name>org.springframework.ide.eclipse.core.springbuilder</name>\n" + "			<arguments>\n"
			+ "			</arguments>\n" + "		</buildCommand>\n" + "	</buildSpec>\n" + "	<natures>\n"
			+ "		<nature>org.eclipse.jdt.core.javanature</nature>\n"
			+ "		<nature>org.eclipse.ajdt.ui.ajnature</nature>\n"
			+ "		<nature>com.springsource.sts.roo.core.nature</nature>\n"
			+ "		<nature>org.springframework.ide.eclipse.core.springnature</nature>\n"
			+ "		</natures>\n" + "</projectDescription>\n"
			+ "";

	private static final String ADDON_PROJECT_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<projectDescription>\n" + "	<name>%PROJECTNAME%</name>\n" + "	<comment></comment>\n" + "	<projects>\n"
			+ "	</projects>\n" + "	<buildSpec>\n" + "		<buildCommand>\n"
			+ "			<name>org.eclipse.jdt.core.javabuilder</name>\n" + "			<arguments>\n" + "			</arguments>\n"
			+ "		</buildCommand>\n" + "		<buildCommand>\n"
			+ "			<name>org.springframework.ide.eclipse.core.springbuilder</name>\n" + "			<arguments>\n"
			+ "			</arguments>\n" + "		</buildCommand>\n" + "	</buildSpec>\n" + "	<natures>\n"
			+ "		<nature>org.eclipse.jdt.core.javanature</nature>\n"
			+ "		<nature>org.springframework.ide.eclipse.core.springnature</nature>\n" + "	</natures>\n"
			+ "</projectDescription>\n" + "";

	private NewRooProjectWizardPageOne projectPage;

	private NewRooProjectWizardPageTwo shellPage;

	public NewRooProjectWizard() {
		super();
		setWindowTitle("New Roo Project");
		setDefaultPageImageDescriptor(RooUiActivator.getImageDescriptor("icons/full/wizban/roo_wizban.png"));
		setNeedsProgressMonitor(true);
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
	}

	@Override
	public boolean performFinish() {

		URI tempLocation = projectPage.getProjectLocationURI();
		if (tempLocation == null) {
			tempLocation = ResourcesPlugin.getWorkspace().getRoot().getLocationURI();
		}
		if (tempLocation == null) {
			tempLocation = ResourcesPlugin.getWorkspace().getRoot().getRawLocationURI();
		}
		final URI location = tempLocation;
		final String rooInstall = projectPage.getRooInstallName();
		final boolean useDefault = projectPage.useDefaultRooInstall();
		final DependencyManagement dependencyManagement = projectPage.getDependencyManagement();
		final String packagingProvider = projectPage.getPackagingProvider();

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {

				monitor.beginTask("Creating Roo project: ", 6);
				monitor.subTask("loading Spring Roo");
				IRooInstall install = (useDefault ? RooCoreActivator.getDefault().getInstallManager()
						.getDefaultRooInstall() : RooCoreActivator.getDefault().getInstallManager().getRooInstall(
						rooInstall));
				monitor.worked(1);

				Bootstrap bootstrap = null;
				try {
					// Create project location
					monitor.subTask("creating project location");
					File projectFile = new File(new File(location), projectPage.getProjectName());
					if (!projectFile.exists()) {
						projectFile.mkdirs();
					}
					monitor.worked(2);
					monitor.subTask("starting Spring Roo shell");

					String javaVersion = JavaCore.getOption("org.eclipse.jdt.core.compiler.compliance");
					String rooJavaVersion = (ROO_JAVA_VERSION_MAPPING.containsKey(javaVersion) ? ROO_JAVA_VERSION_MAPPING
							.get(javaVersion)
							: "6");

					// Create Roo project by launching Roo and invoking the
					// create project command
					String projectLocation = projectFile.getCanonicalPath();

					bootstrap = new Bootstrap(projectLocation, install.getHome(), install.getVersion(), new ProjectRefresher(null));

					// Init Roo shell
					bootstrap.start(new StyledTextAppender(shellPage.getRooShell()), projectPage.getProjectName());
					monitor.worked(3);

					ProjectType type = projectPage.getProjectType();

					// Create project
					monitor.subTask(String.format("execute Spring Roo '%s' command", type.getCommand()));
					StringBuilder builder = new StringBuilder();
					builder.append(type.getCommand());
					builder.append(" --topLevelPackage ").append(projectPage.getPackageName());
					if (type == ProjectType.PROJECT) {
						builder.append(" --projectName ").append(projectPage.getProjectName());
						builder.append(" --java ").append(rooJavaVersion);
						if (RooUiUtil.isRoo120OrGreater(install)) {
							builder.append(" --packaging ").append(packagingProvider);
						}
					}
					else {
						builder.append(" --description \"").append(projectPage.getDescription()).append("\"");
					}

					final String commandString = builder.toString();

					Display.getDefault().asyncExec(new Runnable() {

						public void run() {
							shellPage.getRooShell().append(commandString + StyledTextAppender.NL);
						}
					});

					bootstrap.execute(commandString);

					// Shutdown Roo
					bootstrap.shutdown();
					bootstrap = null;
					monitor.worked(4);

					monitor.subTask("configuring Eclipse project meta data");
					// Setup Eclipse metadata
					File projectDescriptor = new File(projectFile, ".project");
					FileWriter writer = new FileWriter(projectDescriptor);
					if (type == ProjectType.PROJECT) {
						writer.write(PROJECT_FILE.replace("%PROJECTNAME%", projectPage.getProjectName()));
					}
					else {
						writer.write(ADDON_PROJECT_FILE.replace("%PROJECTNAME%", projectPage.getProjectName()));
					}
					writer.flush();
					writer.close();

					File classpathDescriptor = new File(projectFile, ".classpath");
					writer = new FileWriter(classpathDescriptor);
					if (type == ProjectType.PROJECT) {
						writer.write(CLASSPATH_FILE);
					}
					else {
						writer.write(ADDON_CLASSPATH_FILE);
					}
					writer.flush();
					writer.close();

					monitor.subTask("importing project into workspace");
					IProjectDescription desc = ResourcesPlugin.getWorkspace().loadProjectDescription(
							new Path(projectDescriptor.getAbsolutePath()));
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IProject project = workspace.getRoot().getProject(projectPage.getProjectName());

					project.create(desc, new NullProgressMonitor());
					project.open(0, new NullProgressMonitor());
					project.setDescription(desc, new NullProgressMonitor());

					SpringCorePreferences.getProjectPreferences(project, RooCoreActivator.PLUGIN_ID).putBoolean(
							RooCoreActivator.PROJECT_PROPERTY_ID, useDefault);
					SpringCorePreferences.getProjectPreferences(project, RooCoreActivator.PLUGIN_ID).putString(
							RooCoreActivator.ROO_INSTALL_PROPERTY, rooInstall);
					
					configureProjectUi(project);
					
					if (DependencyManagementUtils.IS_M2ECLIPSE_PRESENT) {
						DependencyManagementUtils.installDependencyManagement(project, dependencyManagement);
					} else if (LegacyDependencyManagementUtils.IS_M2ECLIPSE_PRESENT) {
					    LegacyDependencyManagementUtils.installDependencyManagement(project, dependencyManagement);
					}
					
					new OpenShellJob(project).schedule();

					monitor.worked(6);
					monitor.done();
				}
				catch (Throwable e) {
					SpringCore.log(e);
				}
				finally {
					if (bootstrap != null) {
						// Shutdown Roo
						try {
							bootstrap.shutdown();
						}
						catch (Throwable e) {
						}
					}
				}
			}
		};

		try {
			getContainer().run(true, true, operation);
		}
		catch (InvocationTargetException e) {
			SpringCore.log(e);
		}
		catch (InterruptedException e) {
			SpringCore.log(e);
		}

		return true;
	}

	@Override
	public IJavaElement getCreatedElement() {
		return null;
	}

	@Override
	public void addPages() {
		projectPage = new NewRooProjectWizardPageOne();
		addPage(projectPage);
		shellPage = new NewRooProjectWizardPageTwo();
		addPage(shellPage);
	}

	private void configureProjectUi(final IProject project) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (projectPage.getWorkingSets().length > 0) {
					PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(project,
							projectPage.getWorkingSets());
				}
				BasicNewResourceWizard.selectAndReveal(project, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			}
		});
	}

	public enum ProjectType {

		PROJECT, ADDON_SIMPLE, ADDON_ADVANCED;

		public String getCommand() {
			if (this == PROJECT) {
				return "project";
			}
			else if (this == ADDON_SIMPLE) {
				return "addon create simple";
			}
			else if (this == ADDON_ADVANCED) {
				return "addon create advanced";
			}
			return "";
		}

		public String getDisplayString() {
			if (this == PROJECT) {
				return "Standard";
			}
			else if (this == ADDON_SIMPLE) {
				return "Add-on simple";
			}
			else if (this == ADDON_ADVANCED) {
				return "Add-on advanced";
			}
			return null;
		}

		public static ProjectType fromDisplayString(String display) {
			if (display.equals("Standard")) {
				return PROJECT;
			}
			else if (display.equals("Add-on simple")) {
				return ADDON_SIMPLE;
			}
			else if (display.equals("Add-on advanced")) {
				return ADDON_ADVANCED;
			}
			return null;
		}

	}
}
