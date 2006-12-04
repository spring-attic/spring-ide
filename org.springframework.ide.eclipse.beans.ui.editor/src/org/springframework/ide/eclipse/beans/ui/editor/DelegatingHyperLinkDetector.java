/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

public class DelegatingHyperLinkDetector implements IHyperlinkDetector {

    public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
            IRegion region, boolean canShowMultipleHyperlinks) {
        
        List<IHyperlinkDetector> hyperLinkDetectors = NamespaceEditorContributionRegistry
                .getHyperLinkDetectors();
        List<IHyperlink> hyperLinks = new ArrayList<IHyperlink>();

        for (IHyperlinkDetector hyperLinkDetector : hyperLinkDetectors) {
            IHyperlink[] temp = hyperLinkDetector.detectHyperlinks(textViewer,
                    region, canShowMultipleHyperlinks);
            if (temp != null) {
                hyperLinks.addAll(Arrays.asList(temp));
            }
        }
        if (hyperLinks.size() > 0) {
            return hyperLinks.toArray(new IHyperlink[hyperLinks.size()]);
        }
        else {
            return null;
        }
    }
}
