package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.AppNameAnnotation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.AppNameAnnotationModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class DeploymentPropertiesDialogModel extends AbstractDisposable {

	public static final String UNKNOWN_DEPLOYMENT_MANIFEST_TYPE_MUST_BE_EITHER_FILE_OR_MANUAL = "Unknown deployment manifest type. Must be either 'File' or 'Manual'.";
	public static final String NO_SUPPORT_TO_DETERMINE_APP_NAMES = "Support for determining application names is unavailable";
	public static final String MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME = "Manifest does not contain deployment properties for application with name ''{0}''.";
	public static final String APPLICATION_NAME_NOT_SELECTED = "Application name not selected";
	public static final String MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED = "Manifest does not have any application defined.";
	public static final String ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY = "Enter deployment manifest YAML manually.";
	public static final String CURRENT_GENERATED_DEPLOYMENT_MANIFEST = "Current generated deployment manifest.";
	public static final String CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM = "Choose an existing deployment manifest YAML file from the local file system.";
	public static final String DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED = "Deployment manifest file not selected.";


	public static enum ManifestType {
		FILE,
		MANUAL
	}

	private UserInteractions ui;

	private abstract class AbstractSubModel {

		LiveVariable<AppNameAnnotationModel> appNameAnnotationModel = new LiveVariable<>();

		LiveExpression<List<String>> applicationNames = new LiveExpression<List<String>>() {

			private AppNameAnnotationModel attachedTo = null;
			private AppNameAnnotationModelListener listener = new AppNameAnnotationModelListener() {
				public void modelChanged(AnnotationModelEvent event) {
					refresh();
				}
			};

			{
				dependsOn(appNameAnnotationModel);
			}

			@Override
			protected List<String> compute() {
				AppNameAnnotationModel annotationModel = appNameAnnotationModel.getValue();
				attachListener(annotationModel);
				if (annotationModel != null) {
					List<String> applicationNames = new ArrayList<>();
					for (Iterator<Annotation> itr = annotationModel.getAnnotationIterator(); itr.hasNext();) {
						Annotation next = itr.next();
						if (next instanceof AppNameAnnotation) {
							AppNameAnnotation a = (AppNameAnnotation) next;
							applicationNames.add(a.getText());
						}
					}
					return applicationNames;
				}
				return Collections.emptyList();
			}

			synchronized private void attachListener(AppNameAnnotationModel annotationModel) {
				if (attachedTo == annotationModel) {
					return;
				}
				if (attachedTo != null) {
					attachedTo.removeAnnotationModelListener(listener);
				}
				if (annotationModel != null) {
					annotationModel.addAnnotationModelListener(listener);
				}
				attachedTo = annotationModel;
			}

		};

		LiveExpression<String> selectedAppName = new LiveExpression<String>() {

			private AppNameAnnotationModel attachedTo = null;
			private AppNameAnnotationModelListener listener = new AppNameAnnotationModelListener() {
				public void modelChanged(AnnotationModelEvent event) {
					refresh();
				}
			};

			{
				dependsOn(appNameAnnotationModel);
			}

			@Override
			protected String compute() {
				AppNameAnnotationModel annotationModel = appNameAnnotationModel.getValue();
				attachListener(annotationModel);
				if (annotationModel != null) {
					AppNameAnnotation a = annotationModel.getSelectedAppAnnotation();
					if (a != null) {
						return a.getText();
					}
				}
				return null;
			}

			synchronized private void attachListener(AppNameAnnotationModel annotationModel) {
				if (attachedTo == annotationModel) {
					return;
				}
				if (attachedTo != null) {
					attachedTo.removeAnnotationModelListener(listener);
				}
				if (annotationModel != null) {
					annotationModel.addAnnotationModelListener(listener);
				}
				attachedTo = annotationModel;
			}

		};

		abstract String getManifestContents();

		/**
		 * Return manifest from which contents are takes as an {@link IFile}
		 * Return null if manifest content doesn't come from a file
		 * @return
		 */
		abstract IFile getManifest();

		CloudApplicationDeploymentProperties getDeploymentProperties() throws Exception {
			List<CloudApplicationDeploymentProperties> propsList = new ApplicationManifestHandler(project, cloudData, getManifest()) {
				@Override
				protected InputStream getInputStream() throws Exception {
					return new ByteArrayInputStream(getManifestContents().getBytes());
				}
			}.load(new NullProgressMonitor());
			/*
			 * If "Select Manifest..." action is invoked appName is not null,
			 * but we should allow for any manifest file selected for now. Hence
			 * set the applicationName var to null in that case
			 */
			CloudApplicationDeploymentProperties deploymentProperties = null;
			String applicationName = deployedApp == null ? selectedAppName.getValue() : getDeployedAppName();
			if (applicationName == null) {
				deploymentProperties = propsList.get(0);
			} else {
				for (CloudApplicationDeploymentProperties p : propsList) {
					if (applicationName.equals(p.getAppName())) {
						deploymentProperties = p;
						break;
					}
				}
			}
			return deploymentProperties;
		}

	}

	public class FileDeploymentPropertiesDialogModel extends AbstractSubModel {

		{
			onDispose((d) -> {
				saveOrDiscardIfNeeded();
			});
		}

		private final Set<TextFileDocumentProvider> docProviders = new HashSet<>();

		private final LiveVariable<IResource> selectedFile = new LiveVariable<>();

		private final LiveExpression<FileEditorInput> editorInput = new LiveExpression<FileEditorInput>() {

			{
				dependsOn(selectedFile);
			}

			@Override
			protected FileEditorInput compute() {
				IFile file = getFile();
				if (file != null) {
					FileEditorInput currentInput = getValue();
					if (currentInput == null || !currentInput.getFile().equals(file)) {
						saveOrDiscardIfNeeded(currentInput);
						return file == null ? null : new FileEditorInput(file);
					}
				}
				return null;
			}

		};

		private final LiveExpression<IDocument> document = new LiveExpression<IDocument>(new Document("")) {
			{
				dependsOn(editorInput);
			}

			@Override
			protected IDocument compute() {
				try {
					FileEditorInput input = editorInput.getValue();
					if (input != null) {
						TextFileDocumentProvider provider = getTextDocumentProvider(input.getFile());
						if (provider != null) {
							IDocument doc = provider.getDocument(input);
							if (doc == null) {
								provider.connect(input);
								doc = provider.getDocument(input);
							}
							return doc;
						}
					}
				} catch (Exception e) {
					Log.log(e);
				}
				return new Document("");
			}
		};

		final private LiveExpression<String> fileLabel = new LiveExpression<String>() {
			{
				dependsOn(editorInput);
			}

			@Override
			protected String compute() {
				FileEditorInput input = editorInput.getValue();
				if (input != null) {
					boolean dirty = getTextDocumentProvider(input.getFile()).canSaveDocument(input);
					return editorInput.getValue().getFile().getFullPath().toOSString() + (dirty ? "*" : "");
				}
				return "";
			}

		};

		Validator validator = new Validator() {

			{
				dependsOn(editorInput);
				dependsOn(appNameAnnotationModel);
				dependsOn(applicationNames);
				dependsOn(selectedAppName);
			}

			@Override
			protected ValidationResult compute() {
				ValidationResult result = ValidationResult.OK;

				if (editorInput.getValue() == null) {
					result = ValidationResult.error(DEPLOYMENT_MANIFEST_FILE_NOT_SELECTED);
				}

				if (result.isOk()) {
					AppNameAnnotationModel appNamesModel = appNameAnnotationModel.getValue();
					if (appNamesModel == null) {
						result = ValidationResult.error(NO_SUPPORT_TO_DETERMINE_APP_NAMES);
					}
					if (result.isOk()) {
						String appName = getDeployedAppName();
						if (applicationNames.getValue().isEmpty()) {
							result = ValidationResult.error(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED);
						} else {
							String selectedAnnotation = selectedAppName.getValue();
							if (appName == null) {
								if (selectedAnnotation == null) {
									result = ValidationResult.error(APPLICATION_NAME_NOT_SELECTED);
								}
							} else {
								if (selectedAnnotation == null || !appName.equals(selectedAnnotation)) {
									result = ValidationResult.error(MessageFormat.format(
											MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME,
											appName));
								}
							}
						}
					}
				}

				if (result.isOk()) {
					result = ValidationResult.info(CHOOSE_AN_EXISTING_DEPLOYMENT_MANIFEST_YAML_FILE_FROM_THE_LOCAL_FILE_SYSTEM);
				}

				return result;
			}

		};

		private IElementStateListener dirtyStateListener = new IElementStateListener() {

			@Override
			public void elementMoved(Object arg0, Object arg1) {
			}

			@Override
			public void elementDirtyStateChanged(final Object file, final boolean dirty) {
				FileEditorInput editorInputValue = editorInput.getValue();
				if (editorInputValue != null && editorInputValue.equals(file)) {
					fileLabel.refresh();
				}
			}

			@Override
			public void elementDeleted(Object arg0) {
			}

			@Override
			public void elementContentReplaced(Object file) {
				if (file.equals(editorInput.getValue())) {
					document.refresh();
				}
			}

			@Override
			public void elementContentAboutToBeReplaced(Object arg0) {
			}
		};

		private void saveOrDiscardIfNeeded() {
			FileEditorInput input = editorInput.getValue();
			if (input != null) {
				saveOrDiscardIfNeeded(input);
			}
		}

		private void saveOrDiscardIfNeeded(FileEditorInput file) {
			TextFileDocumentProvider docProvider = file == null ? null : getTextDocumentProvider(file.getFile());
			if (docProvider != null && file != null && file.exists()) {
				if (docProvider.canSaveDocument(file) && ui.confirmOperation("Changes Detected", "Manifest file '" + file.getFile().getFullPath().toOSString()
								+ "' has been changed. Do you want to save changes or discard them?", new String[] {"Save", "Discard"}, 0) == 0) {
					try {
						docProvider.saveDocument(new NullProgressMonitor(), file, docProvider.getDocument(file), true);
					} catch (CoreException e) {
						Log.log(e);
						ui.errorPopup("Failed Saving File", ExceptionUtil.getMessage(e));
					}
				} else {
					try {
						docProvider.resetDocument(file);
					} catch (CoreException e) {
						Log.log(e);
					}
				}
				docProvider.disconnect(file);
			}
		}

		private TextFileDocumentProvider getTextDocumentProvider(IFile file) {
			if (file != null) {
				IDocumentProvider docProvider = DocumentProviderRegistry.getDefault().getDocumentProvider(new FileEditorInput(file));
				if (docProvider instanceof TextFileDocumentProvider) {
					TextFileDocumentProvider textDocProvider = (TextFileDocumentProvider) docProvider;
					if (!docProviders.contains(textDocProvider)) {
						textDocProvider.addElementStateListener(dirtyStateListener);
						onDispose((d) -> {
							textDocProvider.removeElementStateListener(dirtyStateListener);
						});
						docProviders.add(textDocProvider);
					}
					return textDocProvider;
				}
			}
			return null;
		}

		public IAnnotationModel getAnnotationModel() {
			FileEditorInput input = editorInput.getValue();
			if (input != null) {
				TextFileDocumentProvider provider = getTextDocumentProvider(input.getFile());
				if (provider != null) {
					return provider.getAnnotationModel(input);
				}
			}
			return null;
		}

		@Override
		String getManifestContents() {
			return document.getValue().get();
		}

		private IFile getFile() {
			IResource r = selectedFile.getValue();
			return r instanceof IFile ? (IFile) r : null;
		}

		@Override
		IFile getManifest() {
			return getFile();
		}

	}

	public class ManualDeploymentPropertiesDialogModel extends AbstractSubModel {

		private IDocument document;

		private boolean readOnly;

		private IAnnotationModel annotationModel = new AnnotationModel();

		Validator validator = new Validator() {

			{
				dependsOn(appNameAnnotationModel);
				dependsOn(applicationNames);
				dependsOn(selectedAppName);
			}

			@Override
			protected ValidationResult compute() {
				ValidationResult result = ValidationResult.OK;

				AppNameAnnotationModel appNamesModel = appNameAnnotationModel.getValue();
				if (appNamesModel == null) {
					result = ValidationResult.error(NO_SUPPORT_TO_DETERMINE_APP_NAMES);
				}
				if (result.isOk()) {
					String appName = getDeployedAppName();
					if (applicationNames.getValue().isEmpty()) {
						result = ValidationResult.error(MANIFEST_DOES_NOT_HAVE_ANY_APPLICATION_DEFINED);
					} else {
						String selectedAnnotation = selectedAppName.getValue();
						if (appName == null) {
							if (selectedAnnotation == null) {
								result = ValidationResult.error(APPLICATION_NAME_NOT_SELECTED);
							}
						} else {
							if (selectedAnnotation == null || !appName.equals(selectedAnnotation)) {
								result = ValidationResult.error(MessageFormat.format(
										MANIFEST_DOES_NOT_CONTAIN_DEPLOYMENT_PROPERTIES_FOR_APPLICATION_WITH_NAME,
										appName));
							}
						}
					}
				}

				if (result.isOk()) {
					result = ValidationResult.info(readOnly ? CURRENT_GENERATED_DEPLOYMENT_MANIFEST : ENTER_DEPLOYMENT_MANIFEST_YAML_MANUALLY);
				}

				return result;
			}
		};

		ManualDeploymentPropertiesDialogModel(boolean readOnly) {
			super();
			this.readOnly = readOnly;
			this.document = new Document(generateDefaultContent());
		}

		public void setText(String s) {
			if (readOnly) {
				throw new IllegalStateException("The model is read-only!");
			}
			document.set(s);
		}

		public String getText() {
			return document.get();
		}

		public IAnnotationModel getAnnotationModel() {
			return annotationModel;
		}

		@Override
		String getManifestContents() {
			return getText();
		}

		@Override
		IFile getManifest() {
			return null;
		}

		private String generateDefaultContent() {
			CloudApplicationDeploymentProperties props = CloudApplicationDeploymentProperties.getFor(project, cloudData,
					getDeployedApp());
			Map<Object, Object> yaml = ApplicationManifestHandler.toYaml(props, cloudData);
			DumperOptions options = new DumperOptions();
			options.setExplicitStart(true);
			options.setCanonical(false);
			options.setPrettyFlow(true);
			options.setDefaultFlowStyle(FlowStyle.BLOCK);
			return new Yaml(options).dump(yaml);
		}

	}

	private abstract class AppNameAnnotationModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {

		@Override
		public void modelChanged(IAnnotationModel model) {
			// Leave empty. AnnotationModelEvent method is the one that will be called
		}

		@Override
		abstract public void modelChanged(AnnotationModelEvent event);

	}


	final public LiveVariable<ManifestType> type = new LiveVariable<>();

	final private CFApplication deployedApp;

	final private Map<String, Object> cloudData;

	final IProject project;

	final private FileDeploymentPropertiesDialogModel fileModel;

	final private ManualDeploymentPropertiesDialogModel manualModel;

	private boolean isCancelled = false;

	final private Validator validator;

	public DeploymentPropertiesDialogModel(UserInteractions ui, Map<String, Object> cloudData, IProject project, CFApplication deployedApp) {
		super();
		this.ui = ui;
		this.deployedApp = deployedApp;
		this.cloudData = cloudData;
		this.project = project;
		this.manualModel = new ManualDeploymentPropertiesDialogModel(deployedApp != null);
		this.fileModel = new FileDeploymentPropertiesDialogModel();

		this.validator = new Validator() {

			{
				dependsOn(type);
				dependsOn(fileModel.validator);
				dependsOn(manualModel.validator);
			}

			@Override
			protected ValidationResult compute() {
				if (isFileManifestType()) {
					return fileModel.validator.getValue();
				} else if (isManualManifestType()) {
					return manualModel.validator.getValue();
				} else {
					return ValidationResult.error(UNKNOWN_DEPLOYMENT_MANIFEST_TYPE_MUST_BE_EITHER_FILE_OR_MANUAL);
				}
			}

		};
	}

	public CloudApplicationDeploymentProperties getDeploymentProperties() throws Exception {
		if (isCancelled) {
			throw new OperationCanceledException();
		}
		if (type.getValue() == null) {
			return null;
		}
		switch (type.getValue()) {
		case FILE:
			return fileModel.getDeploymentProperties();
		case MANUAL:
			return manualModel.getDeploymentProperties();
		default:
			return null;
		}
	}

	public void cancelPressed() {
		fileModel.saveOrDiscardIfNeeded();
		isCancelled = true;
	}

	public boolean okPressed() {
		fileModel.saveOrDiscardIfNeeded();
		isCancelled = false;
		try {
			return getDeploymentProperties()!=null;
		} catch (Exception e) {
			ui.errorPopup("Invalid YAML content", ExceptionUtil.getMessage(e));
			return false;
		}
	}

	public void setSelectedManifest(IResource manifest) {
		fileModel.selectedFile.setValue(manifest);
	}

	public void setManualManifest(String manifestText) {
		manualModel.setText(manifestText);
	}

	public void setManifestType(ManifestType type) {
		this.type.setValue(type);
	}

	public LiveVariable<IResource> getSelectedManifestVar() {
		return fileModel.selectedFile;
	}

	public String getProjectName() {
		return project.getName();
	}

	public boolean isFileManifestType() {
		return type.getValue() == ManifestType.FILE;
	}

	public boolean isManualManifestType() {
		return type.getValue() == ManifestType.MANUAL;
	}

	public IResource getSelectedManifest() {
		return getSelectedManifestVar().getValue();
	}

	public String getDeployedAppName() {
		return deployedApp == null ? null : deployedApp.getName();
	}

	public IDocument getManualDocument() {
		return manualModel.document;
	}

	public IAnnotationModel getManualAnnotationModel() {
		return manualModel.annotationModel;
	}

	public boolean isManualManifestReadOnly() {
		return deployedApp!=null;
	}

	public LiveExpression<IDocument> getFileDocument() {
		return fileModel.document;
	}

	public IAnnotationModel getFileAnnotationModel() {
		return fileModel.getAnnotationModel();
	}

	public LiveExpression<String> getFileLabel() {
		return fileModel.fileLabel;
	}

	private CFApplication getDeployedApp() {
		return deployedApp;
	}

	public void setFileAppNameAnnotationModel(AppNameAnnotationModel appNameAnnotationModel) {
		fileModel.appNameAnnotationModel.setValue(appNameAnnotationModel);
	}

	public void setManualAppNameAnnotationModel(AppNameAnnotationModel appNameAnnotationModel) {
		manualModel.appNameAnnotationModel.setValue(appNameAnnotationModel);
	}

	public boolean isCanceled() {
		return isCancelled;
	}

	public Validator getValidator() {
		return validator;
	}

	public LiveExpression<AppNameAnnotationModel> getManualAppNameAnnotationModel() {
		return manualModel.appNameAnnotationModel;
	}

}
