/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.editor.support.reconcile.IReconcileEngine;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileStrategy;
import org.springframework.ide.eclipse.editor.support.yaml.AbstractYamlSourceViewerConfiguration;
import org.springframework.ide.eclipse.editor.support.yaml.YamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.completions.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

/**
 * @author Kris De Volder
 */
public class ManifestYamlSourceViewerConfiguration extends AbstractYamlSourceViewerConfiguration {

	private ManifestYmlSchema schema = new ManifestYmlSchema();
	private YamlAssistContextProvider assistContextProvider = new SchemaBasedYamlAssistContextProvider(schema);

	public ManifestYamlSourceViewerConfiguration() {
	}

	@Override
	protected YamlAssistContextProvider getAssistContextProvider() {
		return assistContextProvider;
	}

	@Override
	protected YamlStructureProvider getStructureProvider() {
		return YamlStructureProvider.DEFAULT;
	}

	@Override
	protected IDialogSettings getPluginDialogSettings() {
		return ManifestEditorActivator.getDefault().getDialogSettings();
	}

	@Override
	protected ITextHover getTextAnnotationHover(ISourceViewer sourceViewer) {
		return null;
	}

	@Override
	protected IReconcilingStrategy createReconcilerStrategy(ISourceViewer viewer) {
		IReconcileEngine engine = createReconcileEngine();
		return new ReconcileStrategy(viewer, engine);
	}

	private IReconcileEngine createReconcileEngine() {
		return new YamlSchemaBasedReconcileEngine(getAstProvider(), schema);
	}

}
