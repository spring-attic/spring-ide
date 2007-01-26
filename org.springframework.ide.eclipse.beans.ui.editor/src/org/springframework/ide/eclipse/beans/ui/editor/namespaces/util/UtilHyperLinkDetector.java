/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.editor.namespaces.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractHyperLinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean
 * properties in attribute values. Resolves bean references (including
 * references to parent beans or factory beans).
 * 
 * @author Christian Dupuis
 */
public class UtilHyperLinkDetector
        extends AbstractHyperLinkDetector implements IHyperlinkDetector {

    /**
     * Returns <code>true</code> if given attribute is openable.
     */
    protected boolean isLinkableAttr(Attr attr) {
        String attrName = attr.getName();
        if ("list-class".equals(attrName)) {
            return true;
        }
        else if ("map-class".equals(attrName)) {
            return true;
        }
        else if ("set-class".equals(attrName)) {
            return true;
        }
        else if ("value-type".equals(attrName)) {
            return true;
        }
        else if ("key-type".equals(attrName)) {
            return true;
        }
        return false;
    }

    protected IHyperlink createHyperlink(String name, String target,
            Node parentNode, IRegion hyperlinkRegion, IDocument document,
            Node node, ITextViewer textViewer, IRegion cursor) {
        if (name == null) {
            return null;
        }
        IFile file = BeansEditorUtils.getFile(document);
        IType type = BeansModelUtils.getJavaType(file.getProject(), target);
        if (type != null) {
            return new JavaElementHyperlink(hyperlinkRegion, type);
        }
        return null;
    }
}
