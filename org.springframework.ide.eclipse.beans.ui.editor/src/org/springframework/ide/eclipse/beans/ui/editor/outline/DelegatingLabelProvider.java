/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.outline;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.INamespaceAwareEditorContribution;
import org.springframework.ide.eclipse.beans.ui.editor.NamespaceEditorContributionRegistry;
import org.w3c.dom.Node;

public class DelegatingLabelProvider
        extends JFaceNodeLabelProvider {

    private static ILabelProvider xmlProvider;

    private static BeansContentOutlineConfiguration configuration;

    public DelegatingLabelProvider(
            BeansContentOutlineConfiguration configuration,
            ILabelProvider xmlProvider) {
        DelegatingLabelProvider.xmlProvider = xmlProvider;
        DelegatingLabelProvider.configuration = configuration;
    }

    public DelegatingLabelProvider() {
    }

    public Image getImage(Object object) {
        if (!BeansEditorUtils.isSpringStyleOutline()) {
            return xmlProvider.getImage(object);
        }

        Node node = (Node) object;
        String namespace = node.getNamespaceURI();

        INamespaceAwareEditorContribution contribution = NamespaceEditorContributionRegistry
                .getNamespaceAwareEditorContribution(namespace);
        if (contribution != null
                && contribution
                        .getLabelProvider(configuration, xmlProvider) != null) {
            return contribution
                    .getLabelProvider(configuration, xmlProvider).getImage(
                            object);
        }
        return xmlProvider.getImage(object);
    }

    public String getText(Object object) {
        if (!BeansEditorUtils.isSpringStyleOutline()) {
            return xmlProvider.getText(object);
        }

        Node node = (Node) object;
        String namespace = node.getNamespaceURI();

        INamespaceAwareEditorContribution contribution = NamespaceEditorContributionRegistry
                .getNamespaceAwareEditorContribution(namespace);
        if (contribution != null
                && contribution
                        .getLabelProvider(configuration, xmlProvider) != null) {
            return contribution
                    .getLabelProvider(configuration, xmlProvider).getText(
                            object);
        }
        return xmlProvider.getText(object);
    }
}