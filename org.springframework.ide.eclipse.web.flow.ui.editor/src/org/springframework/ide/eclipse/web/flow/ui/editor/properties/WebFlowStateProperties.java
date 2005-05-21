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

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;

public class WebFlowStateProperties implements IPropertySource {

    // Property unique keys
    public static final String W_ID = "Id";

    public static final String W_STARTSTATE = "StartState";

    public static final String W_FILE = "File";

    // Property descriptors
    private static List descriptors;
    
    static {
        descriptors = new ArrayList();
        PropertyDescriptor descriptor;
        descriptor = new TextPropertyDescriptor(W_ID, "id");
        descriptor.setValidator(CellEditorValidator.getInstance());
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Web Flow");
        descriptors.add(descriptor);
        descriptor = new PropertyDescriptor(W_STARTSTATE, "start-state");
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Web Flow");
        descriptors.add(descriptor);
        descriptor = new PropertyDescriptor(W_FILE, "config file");
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory("Web Flow");
        descriptors.add(descriptor);
    }

    private IWebFlowState property;

    public WebFlowStateProperties(IWebFlowState property) {
        this.property = property;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return (IPropertyDescriptor[]) descriptors
                .toArray(new IPropertyDescriptor[descriptors.size()]);
    }

    public Object getPropertyValue(Object id) {
        if (W_ID.equals(id)) {
            return WebFlowUtils.returnNotNullOnString(property.getId());
        }
        else if (W_STARTSTATE.equals(id)) {
            return WebFlowUtils
                    .returnNotNullOnString((property.getStartState() != null ? property
                            .getStartState().getId()
                            : null));
        }
        else if (W_FILE.equals(id)) {
            return new ConfigFilePropertySource(property.getConfig()
                    .getConfigFile());
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
        if (W_ID.equals(id)) {
            property.setId((String) value);
        }
    }

    private class ConfigFilePropertySource extends FilePropertySource {

        private IFile file;

        public ConfigFilePropertySource(IFile file) {
            super(file);
            this.file = file;
        }

        public String toString() {
            return file.getFullPath().toString();
        }
    }
}