/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.ide.eclipse.core.ui.dialogs.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class TabFolderLayout extends Layout {

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
            return new Point(wHint, hHint);

        Control[] children = composite.getChildren();
        int count = children.length;
        int maxWidth = 0, maxHeight = 0;
        for (int i = 0; i < count; i++) {
            Control child = children[i];
            Point pt = child.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
            maxWidth = Math.max(maxWidth, pt.x);
            maxHeight = Math.max(maxHeight, pt.y);
        }

        if (wHint != SWT.DEFAULT)
            maxWidth = wHint;
        if (hHint != SWT.DEFAULT)
            maxHeight = hHint;

        return new Point(maxWidth, maxHeight);

    }

    protected void layout(Composite composite, boolean flushCache) {
        Rectangle rect = composite.getClientArea();

        Control[] children = composite.getChildren();
        for (int i = 0; i < children.length; i++) {
            children[i].setBounds(rect);
        }
    }
}