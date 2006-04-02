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
import org.springframework.ide.eclipse.web.flow.core.model.IInputMapping;
import org.springframework.ide.eclipse.web.flow.core.model.IOutputMapping;
import org.springframework.ide.eclipse.web.flow.core.model.IAttribute;

public class ModelTableLabelProvider implements ITableLabelProvider {

    public void addListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof IAttribute) {
            if (columnIndex == 0)
                return ((IAttribute) element).getName();
            else if (columnIndex == 1)
                return ((IAttribute) element).getValue();
            else if (columnIndex == 2) {
                if (((IAttribute) element).getType() != null)
                    return ((IAttribute) element).getType();
                else 
                    return "";  
            }
        }
        else if (element instanceof IInputMapping) {
            if (columnIndex == 0) {
                if (((IInputMapping) element).getName() != null)
                    return ((IInputMapping) element).getName();
                else 
                    return ""; 
            }
            else if (columnIndex == 1) {
                if (((IInputMapping) element).getValue() != null)
                    return ((IInputMapping) element).getValue();
                else 
                    return ""; 
            }
            else if (columnIndex == 2) {
                if (((IInputMapping) element).getAs() != null)
                    return ((IInputMapping) element).getAs();
                else 
                    return "";  
            }
            else if (columnIndex == 3) {
                if (((IInputMapping) element).getType() != null)
                    return ((IInputMapping) element).getType();
                else 
                    return "";  
            }
        }
        else if (element instanceof IOutputMapping) {
            if (columnIndex == 0) {
                if (((IOutputMapping) element).getName() != null)
                    return ((IOutputMapping) element).getName();
                else 
                    return ""; 
            }
            else if (columnIndex == 1) {
                if (((IOutputMapping) element).getValue() != null)
                    return ((IOutputMapping) element).getValue();
                else 
                    return ""; 
            }
            else if (columnIndex == 2) {
                if (((IOutputMapping) element).getAs() != null)
                    return ((IOutputMapping) element).getAs();
                else 
                    return "";  
            }
            else if (columnIndex == 3) {
                if (((IOutputMapping) element).getType() != null)
                    return ((IOutputMapping) element).getType();
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