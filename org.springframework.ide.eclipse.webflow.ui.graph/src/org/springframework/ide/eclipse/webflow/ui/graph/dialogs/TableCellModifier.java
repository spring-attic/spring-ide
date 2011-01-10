/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;

/**
 * @author Christian Dupuis
 */
public class TableCellModifier implements ICellModifier {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
     */
    public boolean canModify(Object element, String property) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
     */
    public Object getValue(Object element, String property) {
        if (element instanceof IAttribute) {
            if (property.equals("Name")) {
                return ((IAttribute) element).getName();
            }
            else if (property.equals("Value")) {
                return ((IAttribute) element).getValue();
            }
        }
        return new String("");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void modify(Object element, String property, Object value) {
        if (element instanceof TableItem
                && ((TableItem) element).getData() instanceof IAttribute) {
            if (property.equals("Name")) {
                ((IAttribute) ((TableItem) element).getData())
                        .setName((String) value);
                ((TableItem) element).setText(0, (String) value);
            }
            else if (property.equals("Value")) {
                ((IAttribute) ((TableItem) element).getData())
                        .setValue((String) value);
                ((TableItem) element).setText(1, (String) value);
            }
        }
    }
}
