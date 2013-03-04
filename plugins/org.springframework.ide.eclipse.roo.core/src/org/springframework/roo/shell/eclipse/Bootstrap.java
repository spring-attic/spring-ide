/*******************************************************************************
 *  Copyright (c) 2012 - 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.roo.shell.eclipse;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.roo.felix.pgp.PgpKeyId;
import org.springframework.roo.felix.pgp.PgpService;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.CommandFactory;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandListener;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameterDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.JavaParameterDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ParameterFactory;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.Plugin;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginService;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginService.InstallOrUpgradeStatus;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginVersion;


/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class Bootstrap {

	private Object appender;

	private Map<String, String> commandDescription = new HashMap<String, String>();

	private Framework framework;

	private int identity;

	private final String projectLocation;

	private Object projectRefresher;

	private final String rooHome;

	private Object shell;

	private final String rooVersion;

	public Bootstrap(String projectLocation, String rooHome, String rooVersion, Object projectRefresher) {
		this.projectLocation = projectLocation;
		this.rooHome = rooHome;
		this.rooVersion = rooVersion;
		this.projectRefresher = projectRefresher;
	}

	public Integer complete(String command, Integer pos, List<String> completions) {
		try {
			return (Integer) ClassUtils.invokeMethod(shell, "complete", new Object[] { command, new Integer(pos),
					completions }, new Class[] { String.class, Integer.class, List.class });
		}
		catch (Throwable e) {
			RooCoreActivator.log(e);
		}
		return pos;
	}

	public void execute(String command) {
		if (shell != null) {
			try {
				ClassUtils.invokeMethod(shell, "executeCommand", command);
			}
			catch (Throwable e) {
				RooCoreActivator.log(e);
			}
		}
	}

	public Map<String, String> getCommandDescription() {
		return commandDescription;
	}

	public String getShellPrompt() {
		try {
			return (String) ClassUtils.invokeMethod(shell, "getShellPrompt");
		}
		catch (Throwable e) {
			RooCoreActivator.log(e);
		}
		return "roo> ";
	}

	public boolean isShutdown() {
		try {
			Object exitShellRequest = ClassUtils.invokeMethod(shell, "getExitShellRequest", null, null);
			if (exitShellRequest != null) {
				int exitCode = (Integer) ClassUtils.invokeMethod(exitShellRequest, "getExitCode", null, null);
				return exitCode > -1;
			}
		}
		catch (Throwable e) {
			RooCoreActivator.log(e);
		}
		return false;
	}

	public void shutdown() {
		try {
			// Obtain the UAA service and shutdown the transmission thread
			if (framework != null) {			
				ServiceReference reference = framework.getBundleContext().getServiceReference("org.springframework.uaa.client.UaaService");
				if (reference != null) {
					Object uaaService = framework.getBundleContext().getService(reference);
					ClassUtils.invokeMethod(uaaService, "stopTransmissionThread");
					ungetService(reference);
				}
			}

			//  STS-1884: wait for roobot bundle to shutdown in Roo <= 1.1.4
			loop: while (true) {
				Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
				for (Thread thread : stacks.keySet()) {
					if ("Spring Roo RooBot Add-In Index Eager Download".equals(thread.getName())) {
						Thread.sleep(500);
						continue loop;
					}
				}
				break;
			}
			
			ClassUtils.invokeMethod(shell, "close", null, null);
			if (framework != null) {
				framework.stop(0);
			}
		}
		catch (Throwable e) {
			RooCoreActivator.log(e);
		}
	}

	public void start(Object appender, String projectName) {
		try {
			this.appender = appender;
			Main felixLauncher = new Main();
			framework = felixLauncher.start(new File(projectLocation).getCanonicalPath(),
					new File(rooHome + "/bundle").getCanonicalPath(),
					new File(rooHome + "/sts-cache-" + projectName).getCanonicalPath(), new File(rooHome
							+ "/conf/config.properties").toURI().toURL().toString(), rooVersion);

			new Thread(new RooShellExitMonitor()).start();
			// We need to wait for the Roo shell to be ready
			Thread startupMonitor = new Thread(new RooShellStartupMonitor());
			startupMonitor.start();
			startupMonitor.join();
		}
		catch (Throwable e) {
			// TODO propagate the error message up to the shell
			RooCoreActivator.log(new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID,
					"Failed to start the Felix framework", e));
		}
	}

	public void addCommand(ICommandListener listener) {
		try {
			if (framework != null) {
				ServiceReference[] references = framework.getBundleContext().getAllServiceReferences(
						"org.springframework.roo.shell.CommandMarker", null);
				ServiceReference fieldConverterReference = framework.getBundleContext().getServiceReference(
						"org.springframework.roo.shell.converters.StaticFieldConverter");
				if (references != null && fieldConverterReference != null) {

					Object fieldConverter = framework.getBundleContext().getService(fieldConverterReference);

					for (ServiceReference commandReference : references) {
						Object command = framework.getBundleContext().getService(commandReference);
						extractCommand(command, listener, fieldConverter);
						ungetService(commandReference);
					}

					ungetService(fieldConverterReference);
				}
			}
		}
		catch (Throwable e) {
			RooCoreActivator.log(e);
		}
	}

//	public List<RooAddOn> searchAddOns() {
//		ServiceReference addOnServiceReference = null;
//		try {
//			ServiceReference[] references = framework.getBundleContext().getAllServiceReferences(
//					"org.springframework.roo.shell.CommandMarker", null);
//			if (references != null) {
//				for (ServiceReference reference : references) {
//					if ("org.springframework.roo.addon.roobot.client.AddOnCommands".equals(reference.getProperty("component.name"))) {
//						addOnServiceReference = reference;
//					}
//				}
//			}
//
//			//"org.springframework.roo.addon.roobot.client.AddOnCommands"
////			ServiceReference addOnServiceReference = framework.getBundleContext().getServiceReference("106"
////					);
//			if (addOnServiceReference != null) {
//				Object addOnService = framework.getBundleContext().getService(addOnServiceReference);
//				String searchTerms = null;
//				boolean refresh = false;
//				int linesPerResult = 20;
//				int maxResults = 20;
//				boolean trustedOnly = false;
//				boolean compatibleOnly = false;
//				String  requiresCommand = null;
////				List<Object> bundles = (List<Object>) ClassUtils.invokeMethod(addOnService, "search", new Object[] { searchTerms, refresh, linesPerResult, maxResults, trustedOnly, compatibleOnly, requiresCommand}, new Class[] {
////						String.class, boolean.class, int.class, int.class, boolean.class, boolean.class, String.class});
//				List<Object> bundles = (List<Object>) ClassUtils.invokeMethod(addOnService, "searchAddOns", new Object[] { searchTerms, refresh, trustedOnly, compatibleOnly, requiresCommand}, new Class[] {
//				String.class, boolean.class, boolean.class, boolean.class, String.class});
//				List<RooAddOn> addons = new ArrayList<RooAddOn>();
//				for (Object bundle : bundles) {
//					String symbolicName = (String) ClassUtils.invokeMethod(bundle, "getSymbolicName", null);
//					RooAddOn addOn = new RooAddOn(symbolicName);
//					List versions = (List) ClassUtils.invokeMethod(bundle, "getVersions", null);
//					for (Object version : versions) {
//						PluginVersion pluginVersion = new PluginVersion();
//						pluginVersion.setName((String)ClassUtils.invokeMethod(version, "getPresentationName", null));
//						pluginVersion.setVersion((String)ClassUtils.invokeMethod(version, "getVersion", null));
//						addOn.addVersion(pluginVersion);
//						addOn.setLatestReleasedVersion(pluginVersion);
//					}
//					if (addOn.getLatestReleasedVersion() != null)
//						addons.add(addOn);
//				}
//				return addons;
//
//			}
//		}
//		catch (Throwable e) {
//			RooCoreActivator.log(e);
//		} finally {
//			ungetService(addOnServiceReference);
//		}
//		return null;
//	}

	public void trust(PgpKeyId keyId) {
		ServiceReference reference = framework.getBundleContext().getServiceReference(PgpService.class.getName());
		if (reference != null) {
			try {
				PgpService service = (PgpService)framework.getBundleContext().getService(reference);
				service.trust(keyId);
			}
			catch (Throwable e) {
				RooCoreActivator.log(e);
			}
		}
		ungetService(reference);
	}

	public void trustAll() {
		ServiceReference reference = framework.getBundleContext().getServiceReference(PgpService.class.getName());
		if (reference != null) {
			try {
				Object service = framework.getBundleContext().getService(reference);
				ClassUtils.invokeMethod(service, "setAutomaticTrust", new Object[] { true });
			}
			catch (Throwable e) {
				RooCoreActivator.log(e);
			}
		}
		ungetService(reference);
	}

	public List<Plugin> searchAddOns(String searchTerms, boolean refresh, boolean trustedOnly, boolean compatibleOnly) {
		ServiceReference addOnServiceReference = null;
		try {
			addOnServiceReference = getAddOnService(addOnServiceReference);
			if (addOnServiceReference != null) {
				PluginService service = (PluginService) framework.getBundleContext().getService(addOnServiceReference);
				return service.search(searchTerms, refresh, trustedOnly, compatibleOnly);
			}
		}
		catch (Throwable e) {
			RooCoreActivator.log(e);
		} finally {
			ungetService(addOnServiceReference);
		}
		return null;
	}

	public IStatus install(PluginVersion version) {
		ServiceReference addOnServiceReference = null;
		try {
			addOnServiceReference = getAddOnService(addOnServiceReference);
			if (addOnServiceReference != null) {
				PluginService service = (PluginService) framework.getBundleContext().getService(addOnServiceReference);
				InstallOrUpgradeStatus result = service.install(version);
				return handleResult(result);
			}
		}
		catch (Throwable e) {
			RooCoreActivator.log(e);
		} finally {
			ungetService(addOnServiceReference);
		}
		return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, "RooBot Eclipse Service is not available.");
	}

	public IStatus update(PluginVersion version) {
		ServiceReference addOnServiceReference = null;
		try {
			addOnServiceReference = getAddOnService(addOnServiceReference);
			if (addOnServiceReference != null) {
				PluginService service = (PluginService) framework.getBundleContext().getService(addOnServiceReference);
				InstallOrUpgradeStatus result = service.upgrade(version);
				return handleResult(result);
			}
		}
		catch (Throwable e) {
			RooCoreActivator.log(e);
		} finally {
			ungetService(addOnServiceReference);
		}
		return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, "RooBot Eclipse Service is not available.");
	}

	private IStatus handleResult(InstallOrUpgradeStatus result) {
		if (result == InstallOrUpgradeStatus.SUCCESS) {
			return Status.OK_STATUS;
		}
		else if (result == InstallOrUpgradeStatus.INVALID_REPOSITORY_URL) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID,
					"Operation failed: Invalid repository URL.");
		}
		else if (result == InstallOrUpgradeStatus.VERIFICATION_NEEDED) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID,
					"Operation failed: Verification required.");
		}
		else {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, "Operation failed.");
		}
	}

	public IStatus uninstall(PluginVersion version) {
		ServiceReference addOnServiceReference = null;
		try {
			addOnServiceReference = getAddOnService(addOnServiceReference);
			if (addOnServiceReference != null) {
				PluginService service = (PluginService) framework.getBundleContext().getService(addOnServiceReference);
				InstallOrUpgradeStatus result = service.remove(version);
				if (result == InstallOrUpgradeStatus.SUCCESS) {
					return Status.OK_STATUS;
				}
				else {
					return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, "Operation failed.");
				}
			}
		}
		catch (Throwable e) {
			RooCoreActivator.log(e);
		} finally {
			ungetService(addOnServiceReference);
		}
		return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, "RooBot Eclipse Service is not available.");
	}

	private ServiceReference getAddOnService(ServiceReference addOnServiceReference) throws InvalidSyntaxException {
		if (framework != null) {
			ServiceReference[] references = framework.getBundleContext().getAllServiceReferences(
					"org.springframework.roo.shell.CommandMarker", null);
			if (references != null) {
				for (ServiceReference reference : references) {
					if ("org.springframework.roo.addon.roobot.eclipse.client.AddOnEclipseCommands".equals(reference
							.getProperty("component.name"))) {
						addOnServiceReference = reference;
					}
				}
			}
		}
		return addOnServiceReference;
	}

	@SuppressWarnings("unchecked")
	private void extractCommand(final Object command, final ICommandListener listener, final Object fieldConverter) {
		if (fieldConverter == null) {
			return;
		}

		// Collect all unavailable commands
		final List<String> unavailableCommands = new ArrayList<String>();

		ReflectionUtils.doWithMethods(command.getClass(), new ReflectionUtils.MethodCallback() {

			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				Annotation[] annotations = method.getAnnotations();
				for (Annotation annotation : annotations) {
					if (annotation.annotationType().getCanonicalName()
							.equals("org.springframework.roo.shell.CliAvailabilityIndicator")) {
						String[] values = (String[]) AnnotationUtils.getValue(annotation);
						try {
							if (!(Boolean) ReflectionUtils.invokeMethod(method, command)) {
								for (String value : values) {
									unavailableCommands.add(value);
								}
							}
						}
						catch (Exception e) {
							// ignore here
						}
					}
				}
			}
		});

		// Prepare static field converter values to populate multi option
		// parameters
		Field fieldsField = ReflectionUtils.findField(fieldConverter.getClass(), "fields");
		fieldsField.setAccessible(true);
		final Map<Class<?>, Map<String, Field>> fields = (Map<Class<?>, Map<String, Field>>) ReflectionUtils.getField(
				fieldsField, fieldConverter);

		// Collect actual commands and parameters; filter against un-available
		// commands
		ReflectionUtils.doWithMethods(command.getClass(), new ReflectionUtils.MethodCallback() {

			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				Annotation[] annotations = method.getAnnotations();
				for (Annotation annotation : annotations) {
					if (annotation.annotationType().getCanonicalName()
							.equals("org.springframework.roo.shell.CliCommand")) {

						// Filter commands against those unavailable
						List<String> commands = new ArrayList<String>();
						for (String value : (String[]) AnnotationUtils.getValue(annotation)) {
							if (!unavailableCommands.contains(value)) {
								commands.add(value);
							}
						}

						if (commands.size() > 0) {

							List<ICommandParameterDescriptor> parameters = new ArrayList<ICommandParameterDescriptor>();

							Annotation[][] parameterAnnotations = method.getParameterAnnotations();
							for (int i = 0; i < parameterAnnotations.length; i++) {
								Annotation[] parameterAnnotation = parameterAnnotations[i];

								for (int j = 0; j < parameterAnnotation.length; j++) {
									Annotation cliOption = parameterAnnotation[j];
									if (cliOption.annotationType().getCanonicalName()
											.equals("org.springframework.roo.shell.CliOption")) {

										if (!Boolean.parseBoolean(AnnotationUtils.getValue(cliOption, "systemProvided")
												.toString())) {
											Class<?> type = method.getParameterTypes()[i];
											// Search for proper key
											String[] names = ((String[]) AnnotationUtils.getValue(cliOption, "key"));
											String name = null;
											String prefix = null;
											for (String n : names) {
												if (StringUtils.hasText(n)) {
													name = n;
													prefix = "--";
													break;
												}
											}
											String description = (String) AnnotationUtils.getValue(cliOption, "help");
											Boolean mandatory = (Boolean) AnnotationUtils.getValue(cliOption,
													"mandatory");

											if (type.getCanonicalName()
													.equals("org.springframework.roo.model.JavaType")) {
												parameters.add(ParameterFactory.createJavaParameterDescriptor(name,
														description, mandatory, null, true, prefix, " ",
														JavaParameterDescriptor.FLAG_CLASS));
											}
											else if (type.getCanonicalName().equals(
													"org.springframework.roo.model.JavaPackage")) {
												parameters.add(ParameterFactory.createJavaParameterDescriptor(name,
														description, mandatory, null, true, prefix, " ",
														JavaParameterDescriptor.FLAG_PACKAGE));
											}
											else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
												parameters.add(ParameterFactory.createBooleanParameterDescriptor(name,
														description, mandatory, false, true, prefix, " "));
											}
											else {
												if (fields.containsKey(type)) {
													Map<String, Field> options = fields.get(type);
													parameters.add(ParameterFactory.createComboParameterDescriptor(
															name, description, mandatory, null, true, prefix, " ",
															options.keySet().toArray(new String[0])));
												}
												else {
													parameters.add(ParameterFactory.createBaseParameterDescriptor(name,
															description, mandatory, null, true, "--", " "));
												}
											}
										}
									}
								}
							}

							Object description = AnnotationUtils.getValue(annotation, "help");
							for (String command : commands) {
								listener.addCommandDescriptor(CommandFactory.createCommandDescriptor(command,
										(description != null ? description.toString() : null),
										parameters.toArray(new ICommandParameterDescriptor[parameters.size()])));
							}
						}
					}
				}
			}
		});
	}

	private void extractCommandDescription(Class<?> clazz, final Map<String, String> commandDescriptions) {
		ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {

			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				Annotation[] annotations = method.getAnnotations();
				for (Annotation annotation : annotations) {
					if (annotation.annotationType().getCanonicalName()
							.equals("org.springframework.roo.shell.CliCommand")) {
						String[] commands = (String[]) AnnotationUtils.getValue(annotation);
						Object description = AnnotationUtils.getValue(annotation, "help");
						if (description != null) {
							for (String command : commands) {
								commandDescriptions.put(command, description.toString());
							}
						}
					}
				}

			}
		});
	}

	private void initShell() {
		ServiceReference reference = framework.getBundleContext().getServiceReference(
				"org.springframework.roo.shell.Shell");
		ServiceReference pmReference = framework.getBundleContext().getServiceReference(
				"org.springframework.roo.process.manager.ProcessManager");

		if (reference != null && pmReference != null) {

			try {
				this.identity = System.identityHashCode(framework.getBundleContext().getService(pmReference));

				Object tempShell = framework.getBundleContext().getService(reference);
				ClassUtils.invokeMethod(tempShell, "init", new Object[] { appender, identity, projectRefresher,
						new Object(), new Object(), new Object(), rooHome, projectLocation }, new Class[] {
						Object.class, int.class, Object.class, Object.class, Object.class, Object.class, String.class,
						String.class });
				this.shell = tempShell;
			}
			catch (Throwable e) {
				RooCoreActivator.log(e);
			}
		}

		ungetService(pmReference);

		try {
			ServiceReference[] references = framework.getBundleContext().getAllServiceReferences(
					"org.springframework.roo.shell.CommandMarker", null);
			if (references != null) {
				for (ServiceReference commandReference : references) {
					Object command = framework.getBundleContext().getService(commandReference);
					extractCommandDescription(command.getClass(), commandDescription);
					ungetService(commandReference);
				}
			}
		}
		catch (InvalidSyntaxException e) {
			RooCoreActivator.log(e);
		}
	}

	private void ungetService(ServiceReference reference) {
		if (reference != null) {
			framework.getBundleContext().ungetService(reference);
		}
	}

	public class RooShellExitMonitor implements Runnable {

		public void run() {
			try {
				framework.waitForStop(0);
				// TODO close tab
			}
			catch (Exception e) {
				// TODO present on the shell
			}
		}
	}

	public class RooShellStartupMonitor implements Runnable {

		private static final long STARTUP_TIMEOUT = 60 * 1000;

		private boolean failed = true;

		public boolean isFailed() {
			return failed;
		}

		public void run() {
			long time = System.currentTimeMillis();
			try {
				while (shell == null && System.currentTimeMillis() - time < STARTUP_TIMEOUT) {
					initShell();
					if (shell != null) {
						failed = false;
						return;
					}
					Thread.sleep(200);
				}
			}
			catch (Exception e) {
				// TODO present on the shell
			}
		}

	}

}
