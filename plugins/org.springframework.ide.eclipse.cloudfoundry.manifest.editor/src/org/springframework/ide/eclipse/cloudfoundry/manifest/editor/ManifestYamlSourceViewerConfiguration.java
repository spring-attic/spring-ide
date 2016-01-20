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
import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;
import org.springframework.ide.eclipse.editor.support.yaml.AbstractYamlSourceViewerConfiguration;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

public class ManifestYamlSourceViewerConfiguration extends AbstractYamlSourceViewerConfiguration {

	private ICompletionEngine completionEngine;
	private ManifestYmlSchema schema = new ManifestYmlSchema();
	private YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;

	public ManifestYamlSourceViewerConfiguration() {
	}

	@Override
	public ICompletionEngine getCompletionEngine() {
		if (completionEngine==null) {
			completionEngine = new ManifestYamlCompletionEngine(structureProvider, schema);
		}
		return completionEngine;
	}

	@Override
	protected IDialogSettings getPluginDialogSettings() {
		return ManifestEditorActivator.getDefault().getDialogSettings();
	}

}
