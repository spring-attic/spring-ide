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
import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUIUtils;

public class ActionProperties implements IPropertySource {

    public static final String A_AUTOWIRE = "Autowire";

    // Property unique keys
    public static final String A_BEAN = "Bean";

    public static final String A_CLASS = "Class";

    public static final String A_CLASSREF = "ClassRef";

    public static final String A_METHOD = "Method";

    public static final String A_NAME = "Name";

    private static final String[] AUTOWIRE_VALUES = new String[] { "no",
            "byName", "byType", "constructor", "autodetect", "default" };
    // Property descriptors
    private static List descriptors;

    private IAction property;

    public ActionProperties(IAction property) {
        this.property = property;
    }

    public Object getEditableValue() {
        return this;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return (IPropertyDescriptor[]) descriptors
                .toArray(new IPropertyDescriptor[descriptors.size()]);
    }

    public Object getPropertyValue(Object id) {
        if (A_BEAN.equals(id)) {
            return property.getBean();
        }
        else if (A_AUTOWIRE.equals(id)) {
            String autowire = WebFlowUIUtils.returnNotNullOnString(property
                    .getAutowire());
            List list = Arrays.asList(AUTOWIRE_VALUES);
            int i = list.indexOf(autowire);
            return new Integer(i);
        }
        else if (A_BEAN.equals(id)) {
            return WebFlowUIUtils.returnNotNullOnString(property.getBean());
        }
        else if (A_CLASS.equals(id)) {
            return WebFlowUIUtils.returnNotNullOnString(property.getBeanClass());
        }
        else if (A_CLASSREF.equals(id)) {
            return WebFlowUIUtils.returnNotNullOnString(property.getClassRef());
        }
        else if (A_METHOD.equals(id)) {
            return WebFlowUIUtils.returnNotNullOnString(property.getMethod());
        }
        else if (A_NAME.equals(id)) {
            return WebFlowUIUtils.returnNotNullOnString(property.getName());
        }
        return null;
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {
    }

    public void setPropertyValue(Object id, Object value) {
        if (A_BEAN.equals(id)) {
            property.setBean((String) value);
        }
        else if (A_AUTOWIRE.equals(id)) {
            int index = ((Integer) value).intValue();
            property.setAutowire(AUTOWIRE_VALUES[index]);
        }
        else if (A_BEAN.equals(id)) {
            property.setBean((String) value);
        }
        else if (A_CLASS.equals(id)) {
            property.setBeanClass((String) value);
        }
        else if (A_CLASSREF.equals(id)) {
            property.setClassRef((String) value);
        }
        else if (A_METHOD.equals(id)) {
            property.setMethod((String) value);
        }
        else if (A_NAME.equals(id)) {
            property.setName((String) value);
        }
    }
    
    static {
        descriptors = new ArrayList();
        PropertyDescriptor descriptor;

        descriptor = new PropertyDescriptor(A_BEAN, "bean");
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Action");
        descriptors.add(descriptor);
        descriptor = new PropertyDescriptor(A_CLASS, "class");
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Action");
        descriptors.add(descriptor);
        descriptor = new PropertyDescriptor(A_CLASSREF, "classref");
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Action");
        descriptors.add(descriptor);
        descriptor = new ComboBoxPropertyDescriptor(A_AUTOWIRE, "autowire",
                AUTOWIRE_VALUES);
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Action");
        descriptors.add(descriptor);
        descriptor = new PropertyDescriptor(A_METHOD, "method");
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Action");
        descriptors.add(descriptor);
        descriptor = new PropertyDescriptor(A_NAME, "name");
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Action");
        descriptors.add(descriptor);
    }
}