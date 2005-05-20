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

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;

class TableCellModifier implements ICellModifier {

    public boolean canModify(Object element, String property) {
        return true;
    }

    public Object getValue(Object element, String property) {
        if (element instanceof IProperty) {
            if (property.equals("Name")) {
                return ((IProperty) element).getName();
            }
            else if (property.equals("Value")) {
                return ((IProperty) element).getValue();
            }
        }
        return new String("");
    }

    public void modify(Object element, String property, Object value) {
        if (element instanceof TableItem
                && ((TableItem) element).getData() instanceof IProperty) {
            if (property.equals("Name")) {
                ((IProperty) ((TableItem) element).getData())
                        .setName((String) value);
                ((TableItem) element).setText(0, (String) value);
            }
            else if (property.equals("Value")) {
                ((IProperty) ((TableItem) element).getData())
                        .setValue((String) value);
                ((TableItem) element).setText(1, (String) value);
            }
        }
    }
}