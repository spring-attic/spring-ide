package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

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
			String applicationName = deployedAppName == null ? selectedAppName : deployedAppName;
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

		private Set<TextFileDocumentProvider> docProviders;

		final public LiveVariable<IResource> selectedFile = new LiveVariable<>();

		final public LiveExpression<IDocument> document = new LiveExpression<IDocument>() {

			{
				dependsOn(editorInput);
			}

			@Override
			protected IDocument compute() {
				FileEditorInput input = editorInput.getValue();
				if (input != null) {
					TextFileDocumentProvider provider = getTextDocumentProvider(input.getFile());
					if (provider != null) {
						return provider.getDocument(input);
					}
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

		final public LiveExpression<FileEditorInput> editorInput = new LiveExpression<FileEditorInput>() {

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
			if (docProvider != null && file != null && file.exists() && docProvider.canSaveDocument(file)) {
				if (ui.confirmOperation("Changes Detected", "Masnifest file '" + file.getFile().getFullPath().toOSString()
								+ "' has been changed. Do you want to save changes or discard them?", "Save", "Discard")) {
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

		private IDocument document = new Document("");

		private boolean readOnly;

		private IAnnotationModel annotationModel = new AnnotationModel();

		ManualDeploymentPropertiesDialogModel(boolean readOnly) {
			super();
			this.readOnly = readOnly;
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

	}

	final public LiveVariable<ManifestType> type = new LiveVariable<>();

	final private String deployedAppName;

	final private Map<String, Object> cloudData;

	final IProject project;

	final private FileDeploymentPropertiesDialogModel fileModel;

	final private ManualDeploymentPropertiesDialogModel manualModel;

	public DeploymentPropertiesDialogModel(UserInteractions ui, Map<String, Object> cloudData, IProject project, String deployedAppName) {
		super();
		this.ui = ui;
		this.deployedAppName = deployedAppName;
		this.cloudData = cloudData;
		this.project = project;
		this.manualModel = new ManualDeploymentPropertiesDialogModel(deployedAppName != null);
		this.fileModel = new FileDeploymentPropertiesDialogModel();
	}

	public CloudApplicationDeploymentProperties getDeploymentProperties() throws Exception {
		switch (type.getValue()) {
		case FILE:
			return fileModel.getDeploymentProperties();
		case MANUAL:
			return manualModel.getDeploymentProperties();
		default:
			return null;
		}
	}

}
