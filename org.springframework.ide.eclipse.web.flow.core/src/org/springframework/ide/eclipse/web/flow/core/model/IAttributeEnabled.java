/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.web.flow.core.model;

import java.util.List;

public interface IAttributeEnabled extends IWebFlowModelElement {
    
    void addProperty(IAttribute property);

    void addProperty(IAttribute property, int index);

    void removeProperty(IAttribute property);

    void addProperty(String name, String value);

    List getProperties();
}
