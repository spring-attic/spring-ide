/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import java.io.IOException;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.ProjectSpecificLanguageServerWrapper;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.lsp.LSBasedSourceViewerConfiguration;
import org.springframework.ide.eclipse.editor.support.util.ShellProviders;
import org.springframework.ide.eclipse.editor.support.yaml.AbstractYamlEditor;

@SuppressWarnings("restriction")
public class ManifestYamlEditor extends AbstractYamlEditor {

	@Override
	protected YEditSourceViewerConfiguration createSourceViewerConfiguration() {
		if (ManifestEditorActivator.getDefault().isLanguageServerEnabled()) {
			return new LSBasedSourceViewerConfiguration(ShellProviders.from(this));
		}
		else {
			return new ManifestYamlSourceViewerConfiguration(ShellProviders.from(this));
		}
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		
		if (ManifestEditorActivator.getDefault().isLanguageServerEnabled() && input instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput) input;
			
			IFile file = fileInput.getFile();
			IProject project = file.getProject();
		
			IContentType contentType = Platform.getContentTypeManager().getContentType("org.springframework.ide.eclipse.manifest.yml");
			for (LanguageServerDefinition serverDefinition : LanguageServersRegistry.getInstance().findProviderFor(contentType)) {
				if (serverDefinition != null) {
					try {
						ProjectSpecificLanguageServerWrapper lsWrapperForConnection = LanguageServiceAccessor.getLSWrapperForConnection(project, serverDefinition);
						if (lsWrapperForConnection != null) {
							IPath fileLocation = file.getLocation();
							if (fileLocation != null) {
								lsWrapperForConnection.connect(fileLocation, null);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
//		SpringPropertiesEditorPlugin.getIndexManager().addListener(this);
//		SpringPropertiesEditorPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

//	@Override
//	public void changed(SpringPropertiesIndexManager info) {
//		if (sourceViewerConf!=null) {
//			sourceViewerConf.forceReconcile();
//		}
//	}

	@Override
	public void dispose() {
		super.dispose();
//		SpringPropertiesEditorPlugin.getIndexManager().removeListener(this);
//		SpringPropertiesEditorPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

//	@Override
//	public void propertyChange(PropertyChangeEvent event) {
//		if (event.getProperty().startsWith(ProblemSeverityPreferencesUtil.PREFERENCE_PREFIX)) {
//			if (sourceViewerConf!=null) {
//				sourceViewerConf.forceReconcile();
//			}
//		}
//	}
}
