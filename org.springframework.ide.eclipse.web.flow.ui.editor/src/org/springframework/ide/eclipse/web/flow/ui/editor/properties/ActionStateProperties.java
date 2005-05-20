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

package org.springframework.ide.eclipse.web.flow.ui.editor.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;

public class ActionStateProperties implements IPropertySource {

    // Property unique keys
    public static final String A_ID = "Id";

    // Property descriptors
    private static List descriptors;
    static {
        descriptors = new ArrayList();
        PropertyDescriptor descriptor;

        descriptor = new PropertyDescriptor(A_ID, "id");
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Action State");
        descriptors.add(descriptor);
    }

    private IActionState property;

    public ActionStateProperties(IActionState property) {
        this.property = property;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return (IPropertyDescriptor[]) descriptors
                .toArray(new IPropertyDescriptor[descriptors.size()]);
    }

    public Object getPropertyValue(Object id) {
        if (A_ID.equals(id)) {
            return property.getId();
        }
        return null;
    }

    public Object getEditableValue() {
        return this;
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {
    }

    public void setPropertyValue(Object id, Object value) {
        if (A_ID.equals(id)) {
            property.setId((String) value);
        }
    }
}