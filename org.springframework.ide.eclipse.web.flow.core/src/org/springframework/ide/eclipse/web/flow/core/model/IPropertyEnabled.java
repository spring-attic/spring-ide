/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.web.flow.core.model;

import java.util.List;

public interface IPropertyEnabled extends IWebFlowModelElement {
    
    void addProperty(IProperty property);

    void addProperty(IProperty property, int index);

    void removeProperty(IProperty property);

    void addProperty(String name, String value);

    List getProperties();
}
