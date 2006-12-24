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

package org.springframework.ide.eclipse.beans.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.BeansNamespaceAwareEditorContribution;

public class NamespaceEditorContributionRegistry {

    public static final String EXTENSION_POINT = BeansEditorPlugin.PLUGIN_ID
            + ".namespaceContribution";

    private static Map<String, INamespaceAwareEditorContribution> contributions = null;

    private static List<IHyperlinkDetector> hyperLinkDetectors = null;

    private static INamespaceAwareEditorContribution beansEditorContribution = new BeansNamespaceAwareEditorContribution();

    public static INamespaceAwareEditorContribution getNamespaceAwareEditorContribution(
            String namespace) {

        if (namespace == null) {
            return beansEditorContribution;
        }
        else {
            return (INamespaceAwareEditorContribution) contributions
                    .get(namespace);
        }
    }

    public static List<IHyperlinkDetector> getHyperLinkDetectors() {
        return hyperLinkDetectors;
    }

    public static Collection<INamespaceAwareEditorContribution> getNamespaceAwareEditorContributions() {
        return contributions.values();
    }

    public static void init() {

        contributions = new HashMap<String, INamespaceAwareEditorContribution>();
        hyperLinkDetectors = new ArrayList<IHyperlinkDetector>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();

        IExtension[] extensions = registry.getExtensionPoint(EXTENSION_POINT)
                .getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            IExtension extension = extensions[i];
            IConfigurationElement[] elements = extension
                    .getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                try {
                    IConfigurationElement element = elements[j];
                    Object namespaceContribution = element
                            .createExecutableExtension("class");
                    if (namespaceContribution instanceof INamespaceAwareEditorContribution) {
                        INamespaceAwareEditorContribution contribution = (INamespaceAwareEditorContribution) namespaceContribution;
                        contributions.put(contribution.getNamespaceUri(),
                                contribution);
                    }
                }
                catch (CoreException e) {
                }
            }
        }

        for (Map.Entry<String, INamespaceAwareEditorContribution> contribution : contributions
                .entrySet()) {
            if (contribution.getValue().getHyperLinkDetector() != null) {
                hyperLinkDetectors.add(contribution.getValue()
                        .getHyperLinkDetector());
            }
        }
    }
}
