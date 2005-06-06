/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.ui.editor.dialogs;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;

public class ModelTableLabelProvider implements ITableLabelProvider {

    public void addListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof IProperty) {
            if (columnIndex == 0)
                return ((IProperty) element).getName();
            else if (columnIndex == 1)
                return ((IProperty) element).getValue();
            else if (columnIndex == 2) {
                if (((IProperty) element).getType() != null)
                    return ((IProperty) element).getType();
                else 
                    return "";  
            }
        }
        return "";
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }
}