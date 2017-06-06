/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor.lsp;

import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.editor.support.yaml.YamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.completions.YamlAssistContext;

/**
 * @author Martin Lippert
 */
public class LSPBasedYamlAssistContextProvider implements YamlAssistContextProvider {

	private ISourceViewer viewer;

	public LSPBasedYamlAssistContextProvider(ISourceViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public YamlAssistContext getGlobalAssistContext(YamlDocument doc) {
		return new LSPBasedYAssistContext(viewer);
	}
}
