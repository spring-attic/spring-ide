package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
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
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class DeploymentPropertiesDialogModel extends AbstractDisposable {

	public static enum ManifestType {
		FILE,
		MANUAL
	}

	private UserInteractions ui;

	private abstract class AbstractSubModel {

		String selectedAppName = null;

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
			String applicationName = deployedApp == null ? selectedAppName : getDeployedAppName();
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
//				docProvider.disconnect(file);
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

	final public LiveVariable<ManifestType> type = new LiveVariable<>();

	final private CFApplication deployedApp;

	final private Map<String, Object> cloudData;

	final IProject project;

	final private FileDeploymentPropertiesDialogModel fileModel;

	final private ManualDeploymentPropertiesDialogModel manualModel;

	private boolean isCancelled = false;

	public DeploymentPropertiesDialogModel(UserInteractions ui, Map<String, Object> cloudData, IProject project, CFApplication deployedApp) {
		super();
		this.ui = ui;
		this.deployedApp = deployedApp;
		this.cloudData = cloudData;
		this.project = project;
		this.manualModel = new ManualDeploymentPropertiesDialogModel(deployedApp != null);
		this.fileModel = new FileDeploymentPropertiesDialogModel();
	}

	public CloudApplicationDeploymentProperties getDeploymentProperties() throws Exception {
		if (isCancelled) {
			throw new OperationCanceledException();
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

	public IProject getProject() {
		return project;
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
}
