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
import org.springframework.ide.eclipse.web.flow.core.model.IViewState;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;

public class ViewStateProperties implements IPropertySource {

    // Property unique keys
    public static final String V_ID = "Id";

    public static final String V_VIEW = "View";

    // Property descriptors
    private static List descriptors;
    static {
        descriptors = new ArrayList();
        PropertyDescriptor descriptor;

        descriptor = new PropertyDescriptor(V_ID, "id");
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("View State");
        descriptors.add(descriptor);
        descriptor = new PropertyDescriptor(V_VIEW, "view");
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("View State");
        descriptors.add(descriptor);
    }

    private IViewState property;

    public ViewStateProperties(IViewState property) {
        this.property = property;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return (IPropertyDescriptor[]) descriptors
                .toArray(new IPropertyDescriptor[descriptors.size()]);
    }

    public Object getPropertyValue(Object id) {
        if (V_ID.equals(id)) {
            return WebFlowUtils.returnNotNullOnString(property.getId());
        }
        else if (V_VIEW.equals(id)) {
            return WebFlowUtils.returnNotNullOnString(property.getView());
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
        if (V_ID.equals(id)) {
            property.setId((String) value);
        }
        else if (V_VIEW.equals(id)) {
            property.setView((String) value);
        }
    }

}