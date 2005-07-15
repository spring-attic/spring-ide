/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class NodeElementHyperlink implements IHyperlink {

    private final IRegion region;

    private final IRegion targetRegion;
    
    private final ITextViewer viewer;

    /**
     * Creates a new Java element hyperlink.
     */
    public NodeElementHyperlink(IRegion region, IRegion targetRegion, ITextViewer viewer) {
        this.region = region;
        this.targetRegion = targetRegion;
        this.viewer = viewer;
    }
    
    public IRegion getHyperlinkRegion() {
        return this.region;
    }

    public String getTypeLabel() {
        return null;
    }

    public String getHyperlinkText() {
        return null;
    }

    public void open() {
        //viewer.removeRangeIndication();
        //viewer.setRangeIndication(this.targetRegion.getOffset(), this.targetRegion.getLength(), true);
        viewer.setSelectedRange(this.targetRegion.getOffset(), 0);
        viewer.revealRange(this.targetRegion.getOffset(), this.targetRegion.getLength());
    }

}
