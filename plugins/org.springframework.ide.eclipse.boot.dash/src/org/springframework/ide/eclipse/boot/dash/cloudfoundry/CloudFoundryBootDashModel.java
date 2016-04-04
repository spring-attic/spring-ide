/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import static org.springframework.ide.eclipse.boot.dash.model.AbstractLaunchConfigurationsDashElement.READY_STATES;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.CloudAppLogManager;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentPropertiesDialog;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlFileInput;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlGraphDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlInput;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ApplicationDeploymentOperations;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ConnectOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.OperationsExecution;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ProjectsDeployer;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.RefreshSomeApplications;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.TargetApplicationsRefreshOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.CloudApplicationArchiverStrategies;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.CloudApplicationArchiverStrategy;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging.ICloudApplicationArchiver;
import org.springframework.ide.eclipse.boot.dash.livexp.DisposingFactory;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSetVariable;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSets;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.AbstractBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.ModifiableModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.DisposeListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class CloudFoundryBootDashModel extends AbstractBootDashModel implements ModifiableModel {

	private IPropertyStore modelStore;

	public static final String APP_TO_PROJECT_MAPPING = "projectToAppMapping";

	private static final Comparator<BootDashElement> ELEMENT_COMPARATOR = new Comparator<BootDashElement>() {
		@Override
		public int compare(BootDashElement o1, BootDashElement o2) {
			int cat1 = getCategory(o1);
			int cat2 = getCategory(o2);
			if (cat1!=cat2) {
				return cat1 - cat2;
			} else {
				return o1.getName().compareTo(o2.getName());
			}
		}

		private int getCategory(BootDashElement o1) {
			if (o1 instanceof CloudAppDashElement) {
				return 1;
			} else if (o1 instanceof CloudServiceDashElement) {
				return 2;
			} else {
				//Not really possible but anyhow...
				return 999;
			}
		}
	};

	private CloudDashElementFactory elementFactory;

	private final LiveSetVariable<CloudServiceDashElement> services = new LiveSetVariable<>(AsyncMode.SYNC);
	private final CloudDashApplications applications = new CloudDashApplications(this);
	private final ObservableSet<BootDashElement> allElements = LiveSets.union(applications.getApplications(), services);

	private BootDashModelConsoleManager consoleManager;

	private DevtoolsDebugTargetDisconnector debugTargetDisconnector;

	private ApplicationDeploymentOperations appDeploymentOperations;

	final private IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				if (event.getDelta() == null && event.getSource() != ResourcesPlugin.getWorkspace()) {
					return;
				}
				/*
				 * Collect data on renamed and removed projects
				 */
				Map<IPath, IProject> renamedFrom = new HashMap<IPath, IProject>();
				Map<IPath, IProject> renamedTo = new HashMap<IPath, IProject>();
				List<IProject> removedProjects = new ArrayList<IProject>();
				for (IResourceDelta delta : event.getDelta().getAffectedChildren(IResourceDelta.CHANGED | IResourceDelta.ADDED | IResourceDelta.REMOVED)) {
					IResource resource = delta.getResource();
					if (resource instanceof IProject) {
						IProject project = (IProject) resource;
						if (delta.getKind() == IResourceDelta.REMOVED) {
							if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
								renamedFrom.put(delta.getMovedToPath(), project);
							} else {
								removedProjects.add(project);
							}
						} else if (delta.getKind() == IResourceDelta.ADDED && (delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
							renamedTo.put(project.getFullPath(), project);
						}

					}
				}

				/*
				 * Update CF app cache and collect apps that have local project
				 * updated
				 */
				List<CloudAppDashElement> appsToRefresh = new ArrayList<>();
				for (IProject project : removedProjects) {
					CloudAppDashElement app = getApplication(project);
					if (app!=null && app.setProject(null)) {
						appsToRefresh.add(app);
					}
				}
				for (Map.Entry<IPath, IProject> entry : renamedFrom.entrySet()) {
					IPath path = entry.getKey();
					IProject oldProject = entry.getValue();
					IProject newProject = renamedTo.get(path);
					if (oldProject != null) {
						CloudAppDashElement app = getApplication(oldProject);
						if (app!=null && app.setProject(newProject)) {
							appsToRefresh.add(app);
						}
					}
				}

				/*
				 * Update BDEs
				 */
				for (CloudAppDashElement app : appsToRefresh) {
					notifyElementChanged(app);
				}
			} catch (OperationCanceledException oce) {
				BootDashActivator.log(oce);
			} catch (Exception e) {
				BootDashActivator.log(e);
			}
		}
	};

	final private ValueListener<ClientRequests> RUN_TARGET_CONNECTION_LISTENER = new ValueListener<ClientRequests>() {
		@Override
		public void gotValue(LiveExpression<ClientRequests> exp, ClientRequests value) {
			CloudFoundryBootDashModel.this.notifyModelStateChanged();
		}
	};

	private DisposingFactory<BootDashElement, LiveExpression<URI>> actuatorUrlFactory;


	public CloudFoundryBootDashModel(CloudFoundryRunTarget target, BootDashModelContext context, BootDashViewModel parent) {
		super(target, parent);
		RunTargetType type = target.getType();
		IPropertyStore typeStore = PropertyStoreFactory.createForScope(type, context.getRunTargetProperties());
		this.modelStore = PropertyStoreFactory.createSubStore(target.getId(), typeStore);
		this.elementFactory = new CloudDashElementFactory(context, modelStore, this);
		this.consoleManager = new CloudAppLogManager(target);
		this.debugTargetDisconnector = DevtoolsUtil.createDebugTargetDisconnector(this);
		this.appDeploymentOperations = new ApplicationDeploymentOperations(this);
		getRunTarget().addConnectionStateListener(RUN_TARGET_CONNECTION_LISTENER);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
		if (getRunTarget().getTargetProperties().get(CloudFoundryTargetProperties.DISCONNECTED) == null) {
			getOperationsExecution().runOpAsynch(new ConnectOperation(this, true));
		}
	}

	@Override
	public ObservableSet<BootDashElement> getElements() {
		return allElements;
	}

	public DisposingFactory<BootDashElement, LiveExpression<URI>> getActuatorUrlFactory() {
		if (actuatorUrlFactory==null) {
			this.actuatorUrlFactory = new DisposingFactory<BootDashElement,LiveExpression<URI>>(getElements()) {
				protected LiveExpression<URI> create(final BootDashElement key) {
					final LiveExpression<URI> uriExp = new LiveExpression<URI>() {
						protected URI compute() {
							try {
								RunState runstate = key.getRunState();
								if (READY_STATES.contains(runstate)) {
									String host = key.getLiveHost();
									if (StringUtil.hasText(host)) {
										return new URI("https://"+host);
									}
								}
							} catch (URISyntaxException e) {
								BootDashActivator.log(e);
							}
							return null;
						}
					};
					final ElementStateListener elementListener = new ElementStateListener() {
						public void stateChanged(BootDashElement e) {
							if (e.equals(key)) {
								uriExp.refresh();
							}
						}
					};
					uriExp.onDispose(new DisposeListener() {
						public void disposed(Disposable disposed) {
							removeElementStateListener(elementListener);
						}
					});
					addElementStateListener(elementListener);
					return uriExp;
				}
			};
			addDisposableChild(actuatorUrlFactory);
		}
		return actuatorUrlFactory;
	}

	@Override
	public void dispose() {
		getRunTarget().removeConnectionStateListener(RUN_TARGET_CONNECTION_LISTENER);
		if (debugTargetDisconnector!=null) {
			debugTargetDisconnector.dispose();
			debugTargetDisconnector = null;
		}
		applications.dispose();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		super.dispose();
	}

	@Override
	public void refresh(UserInteractions ui) {
		getOperationsExecution(ui).runOpAsynch(new TargetApplicationsRefreshOperation(this, ui));
		getOperationsExecution(ui).runOpAsynch(new ServicesRefreshOperation(this));
	}

	@Override
	public Comparator<BootDashElement> getElementComparator() {
		return ELEMENT_COMPARATOR;
	}

	@Override
	public CloudFoundryRunTarget getRunTarget() {
		return (CloudFoundryRunTarget) super.getRunTarget();
	}

	@Override
	public boolean canBeAdded(List<Object> sources) {
		if (sources != null && !sources.isEmpty() && getRunTarget().isConnected()) {
			for (Object obj : sources) {
				// IMPORTANT: to avoid drag/drop into the SAME target, be
				// sure
				// all sources are from a different target
				if (getProject(obj) == null || !isFromDifferentTarget(obj)) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public void add(List<Object> sources, UserInteractions ui) throws Exception {

		Builder<IProject> projects = ImmutableSet.builder();
		if (sources != null) {
			for (Object obj : sources) {
				IProject project = getProject(obj);
				if (project != null) {
					projects.add(project);
				}
			}
			performDeployment(projects.build(), ui, RunState.RUNNING);
		}
	}

	protected IProject getProject(Object obj) {
		IProject project = null;
		if (obj instanceof IProject) {
			project = (IProject) obj;
		} else if (obj instanceof IJavaProject) {
			project = ((IJavaProject) obj).getProject();
		} else if (obj instanceof IAdaptable) {
			project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
		} else if (obj instanceof BootDashElement) {
			project = ((BootDashElement) obj).getProject();
		}
		return project;
	}

	protected boolean isFromDifferentTarget(Object dropSource) {
		if (dropSource instanceof BootDashElement) {
			return ((BootDashElement) dropSource).getBootDashModel() != this;
		}

		// If not a boot element that is being dropped, it is an element
		// external to the boot dash view (e.g. project from project explorer)
		return true;
	}

	public void performDeployment(
			final Set<IProject> projectsToDeploy,
			final UserInteractions ui,
			RunState runOrDebug
	) throws Exception {
		DebugSupport debugSuppport = getViewModel().getCfDebugSupport();
		getOperationsExecution(ui).runOpAsynch(
				new ProjectsDeployer(CloudFoundryBootDashModel.this, ui, projectsToDeploy, runOrDebug, debugSuppport));
	}

	public CloudAppDashElement addElement(CFApplicationDetail appDetail, IProject project) throws Exception {
		CloudAppDashElement addedElement = null;
		boolean changed = false;
		synchronized (this) {
			addedElement = applications.addApplication(appDetail.getName());
			addedElement.setDetailedData(appDetail);
			// Update the cache BEFORE updating the model, since the model
			// elements are handles to the cache
			changed = addedElement.setProject(project);

			//Should be okay to call inside synch block as the events are fired from a
			// a Job now.
		}
		if (changed) {
			notifyElementChanged(addedElement);
		}
		return addedElement;
	}

	public CloudAppDashElement getApplication(String appName) {
		Set<CloudAppDashElement> apps = getApplications().getValues();

		// Add any existing ones that weren't replaced by the new ones
		// Replace the existing one with a new one for the given Cloud
		// Application
		for (CloudAppDashElement element : apps) {
			if (appName.equals(element.getName())) {
				return element;
			}
		}
		return null;
	}

	private CloudAppDashElement getApplication(IProject project) {
		Set<CloudAppDashElement> apps = getApplications().getValues();
		boolean includeNonExistingProjects = !project.exists();
		for (CloudAppDashElement element : apps) {
			if (project.equals(element.getProject(includeNonExistingProjects))) {
				return element;
			}
		}
		return null;
	}

	public CloudAppDashElement ensureApplication(String appName) {
		synchronized (this) {
			return applications.addApplication(appName);
		}
	}

	/**
	 * Perform a 'shallow' update of the application elements in this model. This only
	 * ensures that elements with the right names exist, creating needed ones and
	 * disposing removed ones, but reusing existing ones. The state of the existing elements
	 * is not updated in any way.
	 */
	public void updateAppNames(Collection<String> names) {
		applications.setAppNames(names);
	}

	public void updateElements(Collection<CFApplicationDetail> apps) throws Exception {
		if (apps == null) {
			/*
			 * Error case: set empty list of BDEs don't modify state of local to CF artifacts mappings
			 */
			applications.setAppNames(ImmutableSet.<String>of());
		} else {
			synchronized (this) {
				applications.setAppNames(getNames(apps));
				for (CFApplicationDetail appDetails : apps) {
					CloudAppDashElement app = applications.getApplication(appDetails.getName());
					app.setDetailedData(appDetails);
				}
			}
		}
	}

	private ImmutableList<String> getNames(Collection<CFApplicationDetail> apps) {
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (CFApplicationDetail app : apps) {
			builder.add(app.getName());
		}
		return builder.build();
	}

	public OperationsExecution getOperationsExecution(UserInteractions ui) {
		return new OperationsExecution(ui);
	}

	public OperationsExecution getOperationsExecution() {
		return new OperationsExecution(null);
	}

	public void updateApplication(CFApplicationDetail appDetails) {
		CloudAppDashElement app = getApplication(appDetails.getName());
		if (app!=null) {
			app.setDetailedData(appDetails);
		}
	}

	@Override
	public void delete(Collection<BootDashElement> toRemove, UserInteractions ui) {

		if (toRemove == null || toRemove.isEmpty()) {
			return;
		}

		for (BootDashElement element : toRemove) {
			if (element instanceof CloudAppDashElement) {
				try {
					delete((CloudAppDashElement) element, ui);
				} catch (Exception e) {
					BootDashActivator.log(e);
				}
			}
		}
	}

	/**
	 * Remove one element at a time, which updates the model
	 *
	 * @param element
	 * @return
	 * @throws Exception
	 */
	protected void delete(final CloudAppDashElement cloudElement, final UserInteractions ui) throws Exception {
		cloudElement.cancelOperations();
		CancelationToken cancelToken = cloudElement.createCancelationToken();
		CloudApplicationOperation operation = new CloudApplicationOperation("Deleting: " + cloudElement.getName(), this,
				cloudElement.getName(), cancelToken) {

			@Override
			protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
				// Delete from CF first. Do it outside of synch block to avoid
				// deadlock
				model.getRunTarget().getClient().deleteApplication(appName);

				synchronized (CloudFoundryBootDashModel.this) {
					getElementConsoleManager().terminateConsole(cloudElement.getName());

					// Add any existing ones that weren't replaced by the new
					// ones
					// Replace the existing one with a new one for the given
					// Cloud
					// Application
					applications.removeApplication(cloudElement.getName());
					cloudElement.setProject(null);
				}
			}
		};

		// Allow deletions to occur concurrently with any other application
		// operation
		operation.setSchedulingRule(null);
		getOperationsExecution(ui).runOpAsynch(operation);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "(" + getRunTarget().getName() + ")";
	}

	@Override
	public BootDashModelConsoleManager getElementConsoleManager() {
		return this.consoleManager;
	}

	public ApplicationDeploymentOperations getApplicationDeploymentOperations() {
		return this.appDeploymentOperations;
	}

	/**
	 *
	 * @param project
	 * @param ui
	 * @param requests
	 * @param monitor
	 * @return non-null deployment properties for the application.
	 * @throws Exception
	 *             if error occurred while resolving the deployment properties
	 * @throws OperationCanceledException
	 *             if user canceled operation while resolving deployment
	 *             properties
	 */
	public CloudApplicationDeploymentProperties resolveDeploymentProperties(CloudAppDashElement cde, UserInteractions ui, IProgressMonitor monitor) throws Exception {
		/*
		 * Refresh the cloud application first to get the latest deployment properties changes
		 */
		IProject project = cde.getProject();
		CFApplication app = cde.getSummaryData();

		new RefreshSomeApplications(this, Collections.singletonList(app)).run(monitor);
		/*
		 * Now construct deployment properties object
		 */
		Map<String, Object> cloudData = buildOperationCloudData(monitor, project, app);

		CloudApplicationDeploymentProperties deploymentProperties = CloudApplicationDeploymentProperties.getFor(project, cloudData, app);
		CloudAppDashElement element = app == null ? null : getApplication(app.getName());
		final IFile manifestFile = element == null ? null : element.getDeploymentManifestFile();
		if (manifestFile != null) {
			if (manifestFile.exists()) {
				final String yamlContents = IOUtil.toString(manifestFile.getContents());
				String errorMessage = null;
				TextEdit edit = null;
				try {
					YamlGraphDeploymentProperties yamlGraph = new YamlGraphDeploymentProperties(yamlContents, deploymentProperties.getAppName(), cloudData);
					MultiTextEdit me = yamlGraph.getDifferences(deploymentProperties);
					edit = me != null && me.hasChildren() ? me : null;
					if (yamlGraph.getInheritFilePath() != null) {
						errorMessage = "'inherit' attribute is present in the manifest but is not supported. Merge with caution.";
					}
				} catch (MalformedTreeException e) {
					BootDashActivator.log(e);
					errorMessage = "Failed to create text differences between local manifest file and deployment properties on CF. Merge with caution.";
					edit = new ReplaceEdit(0, yamlContents.length(),
							new Yaml(YamlGraphDeploymentProperties.createDumperOptions())
									.dump(ApplicationManifestHandler.toYaml(deploymentProperties, cloudData)));
				} catch (Throwable t) {
					BootDashActivator.log(t);
					errorMessage = "Failed to parse local manifest file YAML contents. Merge with caution.";
					edit = new ReplaceEdit(0, yamlContents.length(),
							new Yaml(YamlGraphDeploymentProperties.createDumperOptions())
									.dump(ApplicationManifestHandler.toYaml(deploymentProperties, cloudData)));
				}

				/*
				 * If UI is available and there differences between manifest and
				 * current deployment properties on CF then prompt the user to
				 * perform the merge
				 */
				if (edit != null && ui != null) {
					final IDocument doc = new Document(yamlContents);
					edit.apply(doc);

					final YamlFileInput left = new YamlFileInput(manifestFile,
							BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CLOUD_ICON));
					final YamlInput right = new YamlInput("Current deployment properties from Cloud Foundry",
							BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CLOUD_ICON),
							doc.get());

					CompareConfiguration config = new CompareConfiguration();
					config.setLeftLabel(left.getName());
					config.setLeftImage(left.getImage());
					config.setRightLabel(right.getName());
					config.setRightImage(right.getImage());
					config.setLeftEditable(true);

					final String message = errorMessage;

					final CompareEditorInput input = new CompareEditorInput(config) {
						@Override
						protected Object prepareInput(IProgressMonitor arg0)
								throws InvocationTargetException, InterruptedException {
							setMessage(message);
							return new DiffNode(left, right);
						}
					};
					input.setTitle("Merge Local Deployment Manifest File");

					int result = ui.openManifestCompareDialog(input, null);
					switch (result) {
					case IDialogConstants.CANCEL_ID:
						throw new OperationCanceledException();
					case IDialogConstants.NO_ID:
						element.setDeploymentManifestFile(null);
						/*
						 * Use the current CF deployment properties, hence just break out of the switch
						 */
						break;
					case IDialogConstants.YES_ID:
						/*
						 * Load deployment properties from YAML text content
						 */
						final byte[] yamlBytes = left.getContent();
						List<CloudApplicationDeploymentProperties> props = new ApplicationManifestHandler(project,
								cloudData, manifestFile) {
							@Override
							protected InputStream getInputStream() throws Exception {
								return new ByteArrayInputStream(yamlBytes);
							}
						}.load(monitor);
						CloudApplicationDeploymentProperties found = null;
						for (CloudApplicationDeploymentProperties p : props) {
							if (deploymentProperties.getAppName().equals(p.getAppName())) {
								found = p;
								break;
							}
						}
						if (found == null) {
							throw ExceptionUtil.coreException(
									"Cannot load deployment properties for application '" + deploymentProperties.getAppName()
											+ "' from the manifest file '" + manifestFile.getFullPath() + "'");
						} else {
							deploymentProperties = found;
						}
						break;
						default:
					}


				}
			} else {
				deploymentProperties = createDeploymentProperties(project, ui, monitor);
			}
		}
		return deploymentProperties;

	}

	/**
	 * Creates deployment properties either based on user inout via the UI if UI context is available or generates default deployment properties
	 * @param project the workspace project
	 * @param ui UI context
	 * @param monitor progress monitor
	 * @return deployment properties
	 * @throws Exception
	 */
	public CloudApplicationDeploymentProperties createDeploymentProperties(IProject project, UserInteractions ui, IProgressMonitor monitor) throws Exception {
		Map<String, Object> cloudData = buildOperationCloudData(monitor, project, null);
		CloudApplicationDeploymentProperties props = CloudApplicationDeploymentProperties.getFor(project, cloudData, null);
		CloudAppDashElement element = getApplication(props.getAppName());
		if (ui != null) {
			Map<Object, Object> yaml = ApplicationManifestHandler.toYaml(props, cloudData);
			DumperOptions options = new DumperOptions();
			options.setExplicitStart(true);
			options.setCanonical(false);
			options.setPrettyFlow(true);
			options.setDefaultFlowStyle(FlowStyle.BLOCK);

			String defaultManifest = new Yaml(options).dump(yaml);

			props = ui.promptApplicationDeploymentProperties(cloudData, project,
					element == null ? DeploymentPropertiesDialog.findManifestYamlFile(project)
							: element.getDeploymentManifestFile(),
					defaultManifest, false, false);

			ICloudApplicationArchiver archiver = getArchiver(props, cloudData, ui, monitor);
			if (archiver!=null) {
				File archive = archiver.getApplicationArchive(monitor);
				props.setArchive(archive);
			}
		}
		return props;
	}

	protected ICloudApplicationArchiver getArchiver(
			CloudApplicationDeploymentProperties deploymentProperties,
			Map<String, Object> cloudData,
			UserInteractions ui,
			IProgressMonitor mon
	) {
		try {
			for (CloudApplicationArchiverStrategy s : getArchiverStrategies(deploymentProperties, cloudData, ui, mon)) {
				ICloudApplicationArchiver a = s.getArchiver(mon);
				if (a != null) {
					return a;
				}
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	protected CloudApplicationArchiverStrategy[] getArchiverStrategies(
			CloudApplicationDeploymentProperties deploymentProperties,
			Map<String, Object> cloudData,
			UserInteractions ui,
			IProgressMonitor mon
	) throws Exception {
		IProject project = deploymentProperties.getProject();

//		Map<String, Object> cloudData = model.buildOperationCloudData(mon, project, null);

		IFile manifestFile =  deploymentProperties.getManifestFile();
		String appName = deploymentProperties.getAppName();
		ApplicationManifestHandler parser = new ApplicationManifestHandler(project, cloudData, manifestFile);

		return new CloudApplicationArchiverStrategy[] {
				CloudApplicationArchiverStrategies.fromManifest(project, appName, parser),
				CloudApplicationArchiverStrategies.packageAsJar(project, ui)
		};
	}


	@Override
	public boolean canDelete(BootDashElement element) {
		//Can delete apps, but not services (at leats not yet)
		return element instanceof CloudAppDashElement;
	}

	@Override
	public String getDeletionConfirmationMessage(Collection<BootDashElement> value) {
		return "Are you sure that you want to delete the selected applications from: "
				+ getRunTarget().getName() + "? The applications will be permanently removed.";
	}

	public boolean isConnected() {
		return getRunTarget().isConnected();
	}

	public void setServices(Set<CloudServiceDashElement> newServices) {
		this.services.replaceAll(newServices);
	}

	public ObservableSet<CloudAppDashElement> getApplications() {
		return applications.getApplications();
	}

	public ImmutableSet<CloudAppDashElement> getApplicationValues() {
		return applications.getApplicationValues();
	}

	public ObservableSet<CloudServiceDashElement> getServices() {
		return services;
	}

	public Map<String, Object> buildOperationCloudData(IProgressMonitor monitor, IProject project, CFApplication app) throws Exception {
		Map<String, Object> cloudData = new HashMap<>();
		cloudData.put(ApplicationManifestHandler.DOMAINS_PROP, getRunTarget().getDomains(monitor));
		cloudData.put(ApplicationManifestHandler.BUILDPACK_PROP, getRunTarget().getBuildpack(project));
		cloudData.put(ApplicationManifestHandler.STACK_PROP, getRunTarget().getStacks(monitor));
		if (app != null) {
			cloudData.put(ApplicationManifestHandler.NAME_PROP, app.getName());
		}
		return cloudData;
	}

	public CloudDashElementFactory getElementFactory() {
		return elementFactory;
	}

	public IPropertyStore getPropertyStore() {
		return modelStore;
	}

	public void disconnect() {
		getRunTarget().disconnect();
	}

	public void connect() throws Exception {
		getRunTarget().connect();
	}

	public ClientRequests getClient() {
		return getRunTarget().getClient();
	}

	public List<CFCloudDomain> getCloudDomains(IProgressMonitor monitor) throws Exception {
		return getRunTarget().getDomains(monitor);
	}
}
