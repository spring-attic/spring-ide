/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.web.flow.core.model;

public interface IBeanReference extends IWebFlowModelElement {

    String getBean();

    void setBean(String bean);
    
    boolean hasBeanReference();
    
    void setMethod(String method);
    
    String getMethod();
}
