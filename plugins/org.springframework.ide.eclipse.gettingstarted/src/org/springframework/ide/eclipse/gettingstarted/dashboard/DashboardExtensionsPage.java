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
package org.springframework.ide.eclipse.gettingstarted.dashboard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.mylyn.commons.core.DelegatingProgressMonitor;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.mylyn.internal.discovery.ui.AbstractInstallJob;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.mylyn.internal.discovery.ui.InstalledItem;
import org.eclipse.mylyn.internal.discovery.ui.UninstallRequest;
import org.eclipse.mylyn.internal.discovery.ui.wizards.DiscoveryViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Version;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.core.ResourceProvider;
import org.springsource.ide.eclipse.commons.internal.configurator.Activator;
import org.springsource.ide.eclipse.commons.internal.configurator.IConfigurator;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.util.IdeUiUtils;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Terry Denney
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class DashboardExtensionsPage extends FormDashboardPage implements IRunnableContext, IEnablableDashboardPart {

	static final String MAGIC_STOP_THE_MADNESS_NO_UNINSTALL_SYSPROP = "no.auto.m2e.uninstall";

	static final boolean DONT_DO_UNINSTALL = Boolean.parseBoolean(System.getProperty(
			MAGIC_STOP_THE_MADNESS_NO_UNINSTALL_SYSPROP, Boolean.FALSE.toString()));

	static final String ID_PREFERENCE_PAGE = "com.springsource.sts.ide.ui.preferencePage.AutoConfiguration";

	public static final String RESOURCE_DISCOVERY_DIRECTORY = "discovery.directory";

	public static final Map<String, List<String>> FEATURE_MAPPING;

	public static final Set<String> SVN_FEATURES = Collections.unmodifiableSet(new HashSet<String>(Arrays
			.asList(new String[] { "org.eclipse.team.svn", "org.tigris.subversion.subclipse",
					"com.collabnet.desktop.feature", })));

	public static final String OLD_M2E_EXTENSION_ID = "org.maven.ide.eclipse.feature";

	public static final String NEW_M2E_EXTENSION_ID = "org.eclipse.m2e.feature";

	public static final Set<String> M2E_EXTENSION_IDS = Collections.unmodifiableSet(new HashSet<String>(Arrays
			.asList(new String[] { OLD_M2E_EXTENSION_ID, NEW_M2E_EXTENSION_ID })));

	public static final Set<String> NEW_M2E_FEATURES = Collections.unmodifiableSet(new HashSet<String>(Arrays
			.asList(new String[] { "org.eclipse.m2e.feature.feature.group",
					"org.eclipse.m2e.logback.feature.feature.group",
					"org.sonatype.m2e.mavenarchiver.feature.feature.group",
					"org.sonatype.m2e.buildhelper.feature.feature.group",
					"org.maven.ide.eclipse.wtp.feature.feature.group",
					"org.maven.ide.eclipse.ajdt.feature.feature.group" })));

	public static final Set<String> OLD_M2E_FEATURES = Collections.unmodifiableSet(new HashSet<String>(Arrays
			.asList(new String[] { "org.maven.ide.eclipse.feature.feature.group" })));

	private ProgressMonitorPart progressMonitorPart;

	private long activeRunningOperations = 0;

	private Button installButton;

	private DashboardDiscoveryViewer discoveryViewer;

	private Button cancelButton;

	private final DelegatingProgressMonitor monitor = new DelegatingProgressMonitor();

	private Button findUpdatesButton;

	public static final String ID = "extensions";

	static {

		// move that into an extension install/properties file

		FEATURE_MAPPING = new HashMap<String, List<String>>();
		FEATURE_MAPPING.put("com.google.gwt.eclipse.core", Arrays.asList("com.google.gdt.eclipse.suite.e35.feature",
				"com.google.appengine.eclipse.sdkbundle.e35.feature.1.3.",
				"com.google.gwt.eclipse.sdkbundle.e35.feature.2.1.0", "com.google.gdt.eclipse.suite.e36.feature",
				"com.google.appengine.eclipse.sdkbundle.e36.feature.1.3.5",
				"com.google.gwt.eclipse.sdkbundle.e36.feature.2.1.0"));
		FEATURE_MAPPING.put("org.datanucleus.ide.eclipse",
				Collections.singletonList("org.datanucleus.ide.eclipse.feature"));
	}

	public DashboardExtensionsPage() {
		super();
	}

	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		// The operation can only be canceled if it is executed in a separate
		// thread.
		// Otherwise the UI is blocked anyway.
		if (activeRunningOperations == 0) {
			aboutToStart(fork && cancelable);
		}
		activeRunningOperations++;
		try {
			ModalContext.run(runnable, fork, monitor, getShell().getDisplay());
		}
		finally {
			activeRunningOperations--;
			// Stop if this is the last one
			if (activeRunningOperations <= 0) {
				stopped();
			}
		}
	}

	public boolean shouldAdd() {
		String url = ResourceProvider.getUrl(RESOURCE_DISCOVERY_DIRECTORY);
		return StringUtils.hasText(url);
	}

	/**
	 * About to start a long running operation triggered through the wizard.
	 * Shows the progress monitor and disables the wizard's buttons and
	 * controls.
	 * 
	 * @param enableCancelButton <code>true</code> if the Cancel button should
	 * be enabled, and <code>false</code> if it should be disabled
	 * @return the saved UI state
	 */
	private void aboutToStart(boolean enableCancelButton) {
		cancelButton.setVisible(true);
		cancelButton.setEnabled(true);
		installButton.setEnabled(false);
		findUpdatesButton.setEnabled(false);
		CommonUiUtil.setEnabled((Composite) discoveryViewer.getControl(), false);
	}

	private void adaptRecursively(Control control, FormToolkit toolkit) {
		toolkit.adapt(control, false, false);
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				adaptRecursively(child, toolkit);
			}
		}
	}

	private void initialize(final DashboardDiscoveryViewer viewer) {
		Dictionary<Object, Object> environment = viewer.getEnvironment();
		// add the installed version to the environment so that we can
		// have connectors that are filtered based on the version
		Version version = IdeUiUtils.getVersion();
		environment.put("com.springsource.sts.version", version.toString()); //$NON-NLS-1$
		environment.put("com.springsource.sts.version.major", version.getMajor());
		environment.put("com.springsource.sts.version.minor", version.getMinor());
		environment.put("com.springsource.sts.version.micro", version.getMicro());
		environment.put("com.springsource.sts.nightly", version.getQualifier().contains("-CI-"));
		version = IdeUiUtils.getPlatformVersion();
		environment.put("platform.version", version.toString()); //$NON-NLS-1$
		environment.put("platform.major", version.getMajor());
		environment.put("platform.minor", version.getMinor());
		environment.put("platform.micro", version.getMicro());
		environment.put("platform", version.getMajor() + "." + version.getMinor());
		viewer.setEnvironment(environment);

		viewer.setShowInstalledFilterEnabled(true);
		viewer.addFilter(new ViewerFilter() {
			private Boolean svnInstalled;

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				DiscoveryConnector connector = (DiscoveryConnector) element;
				// filter out Collabnet on non-Windows platforms
				if (connector.getId().startsWith("com.collabnet") && !Platform.getOS().equals(Platform.OS_WIN32)) {
					return false;
				}
				// don't show SVN team providers if one is already
				// installed unless it is the installed connector
				if (SVN_FEATURES.contains(connector.getId()) && !isInstalled(connector) && isSvnInstalled()) {
					return false;
				}

				// filter out Atlassian JIRA connector
				if (connector.getId().startsWith("com.atlassian")) {
					return false;
				}
				return true;
			}

			private boolean isInstalled(DiscoveryConnector connector) {
				Set<String> installedFeatures = viewer.getInstalledFeatures();
				return installedFeatures != null && installedFeatures.contains(connector.getId() + ".feature.group");
			}

			/**
			 * Returns true, if an SVN team provider is installed.
			 */
			private boolean isSvnInstalled() {
				if (svnInstalled == null) {
					svnInstalled = Boolean.FALSE;
					Set<String> installedFeatures = viewer.getInstalledFeatures();
					if (installedFeatures != null) {
						for (String svn : SVN_FEATURES) {
							if (installedFeatures.contains(svn + ".feature.group")) {
								svnInstalled = Boolean.TRUE;
								break;
							}
						}
					}
				}
				return svnInstalled.booleanValue();
			}

		});
	}

	private void stopped() {
		if (getForm() == null || getForm().isDisposed()) {
			return;
		}

		if (!monitor.isCanceled()) {
			discoveryViewer.setSelection(StructuredSelection.EMPTY);
		}

		// refresh list to update selection state
		discoveryViewer.createBodyContents();

		progressMonitorPart.done();

		cancelButton.setVisible(false);
		cancelButton.setEnabled(false);
		installButton.setEnabled(discoveryViewer.isComplete());
		findUpdatesButton.setEnabled(true);
		CommonUiUtil.setEnabled((Composite) discoveryViewer.getControl(), true);
	}

	public boolean isRelatedToM2e(final Set<String> featuresToUninstall, String featureId) {
		if (DONT_DO_UNINSTALL) {
			return false;
		}
		return featuresToUninstall.contains(featureId) || featureId.contains("m2e") || featureId.contains("maven");
	}

	@Override
	protected void createFormContents(final Form parent) {
		Composite body = parent.getBody();
		body.setLayout(new GridLayout(5, false));

		discoveryViewer = new DashboardDiscoveryViewer(getSite(), this);
		initialize(discoveryViewer);
		discoveryViewer.setDirectoryUrl(ResourceProvider.getUrl(RESOURCE_DISCOVERY_DIRECTORY).replace("%VERSION%",
				IdeUiUtils.getShortVersion()));
		discoveryViewer.setShowConnectorDescriptorKindFilter(false);
		discoveryViewer.createControl(body);
		FormToolkit toolkit = getToolkit();
		adaptRecursively(discoveryViewer.getControl(), toolkit);
		GridDataFactory.fillDefaults().span(5, 1).grab(true, true).applyTo(discoveryViewer.getControl());

		findUpdatesButton = toolkit.createButton(body, "&Find Updates", SWT.NONE);
		findUpdatesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					handlerService.executeCommand("org.eclipse.equinox.p2.ui.sdk.update", new Event());
				}
				catch (Exception e) {
					StatusManager.getManager().handle(
							new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
									"Find updates failed with an unexpected error.", e),
							StatusManager.SHOW | StatusManager.LOG);
				}
			}
		});

		Hyperlink configureLink = toolkit.createHyperlink(body, "Configure Extensions...", SWT.NONE);
		configureLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent event) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getSite().getShell(),
						ID_PREFERENCE_PAGE, new String[] { ID_PREFERENCE_PAGE }, null);
				dialog.open();
			}
		});

		progressMonitorPart = new ProgressMonitorPart(body, null);
		monitor.attach(progressMonitorPart);
		progressMonitorPart.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				monitor.setCanceled(true);
				monitor.detach(progressMonitorPart);
			}
		});
		adaptRecursively(progressMonitorPart, toolkit);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(progressMonitorPart);

		// Button refreshButton = toolkit.createButton(body, "Refresh",
		// SWT.NONE);
		// refreshButton.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// pane.updateDiscovery();
		// }
		// });

		cancelButton = toolkit.createButton(body, "&Cancel", SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelButton.setEnabled(false);
				progressMonitorPart.setCanceled(true);
			}
		});

		installButton = toolkit.createButton(body, "&Install", SWT.NONE);
		installButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// check for conflicts
				List<ConnectorDescriptor> selection = discoveryViewer.getInstallableConnectors();
				IStatus conflictStatus = new MultiStatus(IdeUiPlugin.PLUGIN_ID, -1, new IStatus[] {
						checkForConflicts(SVN_FEATURES, " Please select only one SVN team provider.", selection),
						checkForConflicts(M2E_EXTENSION_IDS, " Please select only one m2e version to install.",
								selection) }, "Could not perform install due to conflicts.", null);
				if (!conflictStatus.isOK()) {
					StatusManager.getManager().handle(conflictStatus, StatusManager.SHOW | StatusManager.BLOCK);
					return;
				}

				// now, if m2e is going to be installed, ensure that all other
				// versions of m2e are uninstalled first.
				Set<String> featuresToUninstall = chooseUnwantedFeatures(selection);
				if (!featuresToUninstall.isEmpty()) {
					IStatus uninstallResult = uninstallFeatures(featuresToUninstall);
					if (!uninstallResult.isOK()) {
						if (uninstallResult.getSeverity() != IStatus.CANCEL) {
							StatusManager.getManager().handle(uninstallResult,
									StatusManager.SHOW | StatusManager.LOG | StatusManager.BLOCK);
						}
						return;
					}
				}

				DiscoveryUi.install(discoveryViewer.getInstallableConnectors(), DashboardExtensionsPage.this);
			}

			private IStatus uninstallFeatures(final Set<String> featuresToUninstall) {
				String allInstalled = findFeaturesToUninstall(featuresToUninstall,
						discoveryViewer.getInstalledFeatures());
				if (allInstalled.length() == 0) {
					return Status.OK_STATUS;
				}

				boolean res = MessageDialog.openQuestion(getShell(), "Perform uninstall?",
						"In order to switch versions of m2eclipse, the following features will be uninstalled:\n"
								+ allInstalled + "Do you want to continue?");

				if (!res) {
					return Status.CANCEL_STATUS;
				}

				// uninstall previous Maven tooling
				AbstractInstallJob job = DiscoveryUi.createInstallJob();
				try {

					return job.uninstall(new UninstallRequest() {
						/**
						 * Uninstall all features that are somehow related to
						 * m2eclipse
						 */
						@Override
						public boolean select(InstalledItem item) {
							String featureId = item.getId();
							return isRelatedToM2e(featuresToUninstall, featureId);
						}
					}, new NullProgressMonitor());
				}
				catch (Exception e) {
					return new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, NLS.bind(
							"Could not uninstall features:\n{0},\n try uninstalling manually.", featuresToUninstall), e);
				}
			}

			private String findFeaturesToUninstall(Set<String> featuresToUninstall, Set<String> installedFeatures) {
				StringBuilder sb = new StringBuilder();
				for (String featureId : installedFeatures) {
					if (isRelatedToM2e(featuresToUninstall, featureId)) {
						if (featureId.endsWith(".feature.group")) {
							featureId = featureId.substring(0, featureId.length() - ".feature.group".length());
						}
						sb.append("   " + featureId + "\n");
					}
				}
				return sb.toString();
			}

			private Set<String> chooseUnwantedFeatures(List<ConnectorDescriptor> selection) {
				boolean uninstallOld = false;
				boolean uninstallNew = false;
				for (ConnectorDescriptor feature : selection) {
					// if new m2e to be installed, then must uninstall the old
					// first and vice versa
					if (feature.getId().equals(NEW_M2E_EXTENSION_ID)) {
						uninstallOld = true;
					}
					else if (feature.getId().equals(OLD_M2E_EXTENSION_ID)) {
						uninstallNew = true;
					}
				}

				Set<String> maybeUninstall;
				if (uninstallOld) {
					maybeUninstall = OLD_M2E_FEATURES;
				}
				else if (uninstallNew) {
					maybeUninstall = NEW_M2E_FEATURES;
				}
				else {
					maybeUninstall = Collections.emptySet();
				}

				Set<String> installedFeatures = DashboardExtensionsPage.this.discoveryViewer.getInstalledFeatures();
				Set<String> definitelyUninstall = new HashSet<String>();
				for (String feature : maybeUninstall) {
					if (installedFeatures.contains(feature)) {
						definitelyUninstall.add(feature);
					}
				}

				if (definitelyUninstall.size() > 0) {
					IdeUiPlugin.log(new Status(IStatus.INFO, IdeUiPlugin.PLUGIN_ID,
							"To make way for a new version of m2eclipse, we will uninstall these features: "
									+ definitelyUninstall));
				}
				return definitelyUninstall;
			}

			/**
			 * This method checks for conflicts with requested installs. If a
			 * conflict is found, this method will pop up a dialog explaining
			 * the conflict and it will return true.
			 * @param featuresToCheck set of features of which only one can be
			 * installed at once.
			 * @param message message to add if there is an error
			 * @param selection
			 * 
			 * @return true iff there is a conflict.
			 */
			public IStatus checkForConflicts(Set<String> featuresToCheck, String prependedMessage,
					List<ConnectorDescriptor> selection) {
				StringBuilder message = new StringBuilder();
				List<ConnectorDescriptor> conflicting = new ArrayList<ConnectorDescriptor>();
				for (ConnectorDescriptor descriptor : selection) {
					if (featuresToCheck.contains(descriptor.getId())) {
						conflicting.add(descriptor);
						if (message.length() > 0) {
							message.append(", ");
						}
						message.append(descriptor.getName());
					}
				}
				if (conflicting.size() > 1) {
					return new Status(IStatus.WARNING, IdeUiPlugin.PLUGIN_ID, NLS.bind(
							"The following extensions can not be installed at the same time: {0}.", message.toString()));
				}
				else {
					return Status.OK_STATUS;
				}
			}
		});

		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				if (getForm()!=null && !getForm().isDisposed()) {
					discoveryViewer.updateDiscovery();
				}
			}
		});

		discoveryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				installButton.setEnabled(discoveryViewer.isComplete());
			}
		});
	}

	private final class DashboardDiscoveryViewer extends DiscoveryViewer {

		private static final String READ_ONLY_MESSAGE = "Cannot install Groovy-Eclipse because STS installation directory is read-only.  "
				+ "To install Groovy-Eclipse. please make sure that the STS install location is writable by the current user. ";

		private static final String PROGRAM_FILES_MESSAGE = "Cannot install Groovy-Eclipse because STS is located in 'C:\\Program Files'.  "
				+ "To install Groovy-Eclipse, please change the location of STS and try again.";

		private static final String GROOVY_FEATURE_PREFIX = "org.codehaus.groovy";

		private Set<String> installedFeatures;

		private DashboardDiscoveryViewer(IShellProvider shellProvider, IRunnableContext context) {
			super(shellProvider, context);
		}

		public Set<String> getInstalledFeatures() {
			return installedFeatures;
		}

		@Override
		protected Set<String> getInstalledFeatures(IProgressMonitor monitor) throws InterruptedException {
			this.installedFeatures = super.getInstalledFeatures(monitor);
			IConfigurator configurator = Activator.getConfigurator();
			if (configurator != null) {
				installedFeatures.addAll(configurator.getInstalledBundles());
			}
			for (Map.Entry<String, List<String>> entry : FEATURE_MAPPING.entrySet()) {
				if (Platform.getBundle(entry.getKey()) != null) {
					installedFeatures.addAll(entry.getValue());
				}
			}
			return installedFeatures;
		}

		@Override
		protected void postDiscovery(ConnectorDiscovery connectorDiscovery) {
			super.postDiscovery(connectorDiscovery);
			for (DiscoveryConnector connector : connectorDiscovery.getConnectors()) {
				if (connector.getSiteUrl() != null && connector.getSiteUrl().endsWith("-disabled")) {
					connector.setAvailable(Boolean.FALSE);
				}

				// disable Groovy-Eclipse for read-only installations
				// and provide an explanation why
				if (connector.getId() != null && connector.getId().startsWith(GROOVY_FEATURE_PREFIX)) {
					File file = getInstallLocation();
					boolean readOnly = isReadOnly(file);
					boolean inProgramFiles = isInProgramFiles(file);
					if (readOnly || inProgramFiles) {
						connector.setAvailable(Boolean.FALSE);
						connector.setName(connector.getName() + " (Cannot install)");
						connector.setDescription(inProgramFiles ? PROGRAM_FILES_MESSAGE : READ_ONLY_MESSAGE
								+ connector.getDescription());
					}
				}
			}
		}

		private File getInstallLocation() {
			URL url = Platform.getInstallLocation().getURL();
			if (url != null) {
				return new File(url.getFile());
			}
			else {
				return null;
			}
		}

		private boolean isReadOnly(File installFolder) {
			if (installFolder == null) {
				return false;
			}
			File configurationFolder = new File("configuration");
			// consider read-only if either the install location or the default
			// config folder is read-only.
			return (installFolder.exists() && !installFolder.canWrite())
					|| (configurationFolder.exists() && !configurationFolder.canWrite());
		}

		private boolean isInProgramFiles(File installFolder) {
			if (installFolder == null) {
				return false;
			}
			String absolutePath = installFolder.getAbsolutePath();
			return installFolder.exists()
					&& (absolutePath.startsWith("C:\\Program Files") || absolutePath.startsWith("C:/Program Files"));
		}
	}

	@Override
	public String getName() {
		return "Extensions";
	}
}
