/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudData;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentPropertiesDialog;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.ManifestDiffDialog;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlFileInput;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlGraphDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlInput;
import org.springframework.ide.eclipse.boot.dash.dialogs.CustomizeAppsManagerURLDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.CustomizeAppsManagerURLDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.DeploymentPropertiesDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.EditTemplateDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.EditTemplateDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.ManifestDiffDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.ManifestDiffDialogModel.Result;
import org.springframework.ide.eclipse.boot.dash.dialogs.PasswordDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.SelectRemoteEurekaDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.ToggleFiltersDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.ToggleFiltersDialogModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashTreeContentProvider;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * An implementation of 'UserInteractions' that uses real Dialogs, for use in
 * 'production'.
 *
 * @author Kris De Volder
 */
public class DefaultUserInteractions implements UserInteractions {

	public interface UIContext {
		Shell getShell();
	}

	private UIContext context;

	public DefaultUserInteractions(UIContext context) {
		this.context = context;
	}

	@Override
	public ILaunchConfiguration chooseConfigurationDialog(final String dialogTitle, final String message,
			final Collection<ILaunchConfiguration> configs) {
		final LiveVariable<ILaunchConfiguration> chosen = new LiveVariable<>();
		context.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
				try {
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
					dialog.setElements(configs.toArray());
					dialog.setTitle(dialogTitle);
					dialog.setMessage(message);
					dialog.setMultipleSelection(false);
					int result = dialog.open();
					labelProvider.dispose();
					if (result == Window.OK) {
						chosen.setValue((ILaunchConfiguration) dialog.getFirstResult());
					}
				} finally {
					labelProvider.dispose();
				}
			}
		});
		return chosen.getValue();
	}

	private Shell getShell() {
		return context.getShell();
	}

	@Override
	public IType chooseMainType(final IType[] mainTypes, final String dialogTitle, final String message) {
		if (mainTypes.length == 1) {
			return mainTypes[0];
		} else if (mainTypes.length > 0) {
			// Take care the UI interactions don't bork if called from non-ui
			// thread.
			final LiveVariable<IType> chosenType = new LiveVariable<>();
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
					try {
						ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
						dialog.setElements(mainTypes);
						dialog.setTitle(dialogTitle);
						dialog.setMessage(message);
						dialog.setMultipleSelection(false);
						int result = dialog.open();
						labelProvider.dispose();
						if (result == Window.OK) {
							chosenType.setValue((IType) dialog.getFirstResult());
						}
					} finally {
						labelProvider.dispose();
					}
				}
			});
			return chosenType.getValue();
		}
		return null;
	}

	@Override
	public void errorPopup(final String title, final String message) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(getShell(), title, message);
			}
		});
	}

	@Override
	public void openLaunchConfigurationDialogOnGroup(final ILaunchConfiguration conf, final String launchGroup) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IStructuredSelection selection = new StructuredSelection(new Object[] { conf });
				DebugUITools.openLaunchConfigurationDialogOnGroup(getShell(), selection, launchGroup);
			}
		});
	}

	@Override
	public void openUrl(final String url) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (url != null) {
					UiUtil.openUrl(url);
				}
			}
		});
	}

	@Override
	public boolean confirmOperation(final String title, final String message) {
		final boolean[] confirm = { false };
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				confirm[0] = MessageDialog.openConfirm(getShell(), title, message);
			}
		});
		return confirm[0];
	}

	@Override
	public void openDialog(final ToggleFiltersDialogModel model) {
		final Shell shell = getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				ToggleFiltersDialog dlg = new ToggleFiltersDialog("Select Filters", model, shell);
				dlg.open();
			}
		});
	}

	@Override
	public String chooseFile(String title, String file) {
		FileDialog fileDialog = new FileDialog(getShell());
		fileDialog.setText(title);
		fileDialog.setFileName(file);

		String result = fileDialog.open();
		return result;
	}

	@Override
	public String selectRemoteEureka(BootDashViewModel model, String title, String message, String initialValue, IInputValidator validator) {
		SelectRemoteEurekaDialog dialog = new SelectRemoteEurekaDialog(getShell(), new BootDashTreeContentProvider());
		dialog.setInput(model);

	    dialog.setTitle("Select Eureka instance");
	    dialog.setMessage("Select the Eureka instance this local app should be registered with");
	    int open = dialog.open();
	    if (open == Window.OK) {
	    		String result = dialog.getSelectedEurekaURL();
	    		return result;
	    }
		return null;
	}

	@Override
	public CloudApplicationDeploymentProperties promptApplicationDeploymentProperties(DeploymentPropertiesDialogModel model)
			throws Exception {
		final Shell shell = getShell();

		if (shell != null) {
			model.initFileModel();
			model.initManualModel();
			shell.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					new DeploymentPropertiesDialog(shell, model).open();
				}
			});
		}

		return model.getDeploymentProperties();
	}


	@Override
	public boolean yesNoWithToggle(final String propertyKey, final String title, final String message, final String toggleMessage) {
		final String ANSWER = propertyKey+".answer";
		final String TOGGLE = propertyKey+".toggle";
		final IPreferenceStore store = getPreferencesStore();
		store.setDefault(ANSWER, true);
		boolean toggleState = store.getBoolean(TOGGLE);
		boolean answer = store.getBoolean(ANSWER);
		if (toggleState) {
			return answer;
		}
		final boolean[] dialog = new boolean[2];
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialogWithToggle result = MessageDialogWithToggle.openYesNoQuestion(getShell(), title , message, toggleMessage, false, null, null);
				dialog[0] = result.getReturnCode()==IDialogConstants.YES_ID;
				dialog[1] = result.getToggleState();
			}
		});
		store.setValue(TOGGLE, dialog[1]);
		store.setValue(ANSWER, dialog[0]);
		return dialog[0];
	}

	@Override
	public boolean confirmWithToggle(final String propertyKey, final String title, final String message, final String toggleMessage) {
		final IPreferenceStore store = getPreferencesStore();
		boolean toggleState = store.getBoolean(propertyKey);
		if (toggleState) {
			return true;
		}
		final boolean[] dialog = new boolean[2];
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialogWithToggle result = MessageDialogWithToggle.openOkCancelConfirm(getShell(), title , message, toggleMessage, false, null, null);
				dialog[0] = result.getReturnCode()==IDialogConstants.OK_ID;
				dialog[1] = result.getToggleState();
			}
		});
		store.setValue(propertyKey, dialog[0] && dialog[1]);
		return dialog[0];
	}

	protected IPreferenceStore getPreferencesStore() {
		return BootDashActivator.getDefault().getPreferenceStore();
	}

	@Override
	public Result openManifestDiffDialog(ManifestDiffDialogModel model) throws CoreException {
		LiveVariable<Integer> resultCode = new LiveVariable<>();
		LiveVariable<Throwable> error = new LiveVariable<>();
		getShell().getDisplay().syncExec(() -> {
			try {
				resultCode.setValue(new ManifestDiffDialog(getShell(), model).open());
			} catch (Exception e) {
				error.setValue(e);
			}
		});
		if (error.getValue()!=null) {
			throw ExceptionUtil.coreException(error.getValue());
		} else {
			return ManifestDiffDialog.getResultForCode(resultCode.getValue());
		}
	}

	@Override
	public void openEditTemplateDialog(final EditTemplateDialogModel model) {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				new EditTemplateDialog(model, getShell()).open();
			}
		});
	}

	@Override
	public void openEditAppsManagerURLDialog(CustomizeAppsManagerURLDialogModel model) {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				new CustomizeAppsManagerURLDialog(model, getShell()).open();
			}
		});
	}

	@Override
	public int confirmOperation(String title, String message, String[] buttonLabels, int defaultButtonIndex) {
		return new MessageDialog(getShell(), title, null, message,
				MessageDialog.QUESTION, buttonLabels, defaultButtonIndex).open();
	}

	@Override
	public void openPasswordDialog(PasswordDialogModel model) {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				new UpdatePasswordDialog(getShell(), model).open();
			}
		});
	}

	@Override
	public void warningPopup(String title, String message) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openWarning(getShell(), title, message);
			}
		});
	}

	@Override
	public ManifestDiffDialogModel.Result confirmReplaceApp(String title, CloudData cloudData, IFile manifestFile, CloudApplicationDeploymentProperties deploymentProperties) throws  Exception {
		final Exception[] error = new Exception[1];
		final Result[] result = new Result[1];
		getShell().getDisplay().syncExec(() -> {
			try {
				result[0] = confirmReplaceApp(title, cloudData, manifestFile, deploymentProperties, new NullProgressMonitor());
			} catch (Exception e) {
				error[0] = e;
			}
		});

		if (error[0] != null) {
			throw error[0];
		}
		return result[0];
	}

	private ManifestDiffDialogModel.Result confirmReplaceApp(String title, CloudData cloudData, IFile manifestFile, CloudApplicationDeploymentProperties existingAppDeploymentProperties, IProgressMonitor monitor) throws Exception {


		Result result = confirmReplaceAppWithManifest(title, cloudData, manifestFile,
				existingAppDeploymentProperties, monitor);
		if (result == null) {
			String message = "Replace content of the existing Cloud application? Existing deployment properties including bound services will be retained.";
			if (MessageDialog.openConfirm(getShell(), title, message)) {
				// Not ideal, but using "Forget Manifest" to indicate to use existing Cloud Foundry app deployment properties
				return Result.FORGET_MANIFEST;
			} else {
				return Result.CANCELED;
			}
		} else {
			return result;
		}
	}

	private ManifestDiffDialogModel.Result confirmReplaceAppWithManifest(String title, CloudData cloudData, IFile manifestFile, CloudApplicationDeploymentProperties existingAppDeploymentProperties, IProgressMonitor monitor) throws Exception {

		if (manifestFile != null && manifestFile.isAccessible()) {


			String yamlContents = IOUtil.toString(manifestFile.getContents());

			YamlGraphDeploymentProperties newDeploymentProperties = new YamlGraphDeploymentProperties(yamlContents, existingAppDeploymentProperties.getAppName(), cloudData);
			TextEdit edit = null;
			String errorMessage = null;
			try {
				MultiTextEdit me = newDeploymentProperties.getDifferences(existingAppDeploymentProperties);
				edit = me != null && me.hasChildren() ? me : null;
			} catch (MalformedTreeException e) {
				errorMessage  = "Failed to create text differences between local manifest file and deployment properties on CF.";
			} catch (Throwable t) {
				errorMessage = "Failed to parse local manifest file YAML contents.";
			}
			if (errorMessage != null) {
				throw ExceptionUtil.coreException(errorMessage);
			}

			if (edit != null) {
				final IDocument doc = new Document(yamlContents);
				edit.apply(doc);

				final YamlFileInput left = new YamlFileInput(manifestFile,
						BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CLOUD_ICON));
				final YamlInput right = new YamlInput("Existing application in Cloud Foundry",
						BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CLOUD_ICON),
						doc.get());

				CompareConfiguration config = new CompareConfiguration();
				config.setLeftLabel(left.getName());
				config.setLeftImage(left.getImage());
				config.setRightLabel(right.getName());
				config.setRightImage(right.getImage());

				final CompareEditorInput input = new CompareEditorInput(config) {
					@Override
					protected Object prepareInput(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						if (hasDeletedService(newDeploymentProperties, existingAppDeploymentProperties)) {
							setMessage("WARNING: If using the manifest file, existing service bindings for the application that are not listed in the manifest will be removed.");
						}
						return new DiffNode(left, right);
					}

				};
				input.setTitle("Replacing existing application");

				input.run(monitor);

				// TODO: At the moment, this dialogue offers limited functionality to just compare manifest with existing app. Eventually
				// we want to have a "full feature" compare editor that allows users to forget manifest
				// or make changes
				config.setLeftEditable(false);

				ManifestDiffDialogModel model = new ManifestDiffDialogModel(input);

				int val = new ManifestDiffDialog(getShell(), model, title).open();
				return ManifestDiffDialog.getResultForCode(val);
			}
		}
		return null;
	}

	private boolean hasDeletedService(YamlGraphDeploymentProperties newDeployment, CloudApplicationDeploymentProperties oldDeployment) {
		return !newDeployment.getServices().containsAll(oldDeployment.getServices());
	}

}
