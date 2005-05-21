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
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;

public class TransitionProperties implements IPropertySource {

    // Property unique keys
    public static final String T_ON = "On";

    public static final String T_TO = "To";

    public static final String T_FROM = "From";

    // Property descriptors
    private static List descriptors;
    static {
        descriptors = new ArrayList();
        PropertyDescriptor descriptor;
        descriptor = new TextPropertyDescriptor(T_ON, "on");
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Transition");
        descriptors.add(descriptor);
        descriptor = new PropertyDescriptor(T_TO, "to");
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Transition");
        descriptors.add(descriptor);
        descriptor = new PropertyDescriptor(T_FROM, "from");
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Transition");
        descriptors.add(descriptor);
    }

    private IStateTransition property;

    public TransitionProperties(IStateTransition property) {
        this.property = property;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return (IPropertyDescriptor[]) descriptors
                .toArray(new IPropertyDescriptor[descriptors.size()]);
    }

    public Object getPropertyValue(Object id) {
        if (T_ON.equals(id)) {
            return WebFlowUtils.returnNotNullOnString(property.getOn());
        }
        else if (T_TO.equals(id)) {
            return WebFlowUtils.returnNotNullOnString(property.getToState()
                    .getId());
        }
        else if (T_FROM.equals(id)) {
            return WebFlowUtils.returnNotNullOnString(property.getFromState()
                    .getId());
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
        if (T_ON.equals(id)) {
            property.setOn((String) value);
        }
    }
}