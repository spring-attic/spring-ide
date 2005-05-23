/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.web.flow.core.model;

public interface ISetup extends IBeanReference {
    
    String getMethod();
    
    void setMethod(String method);
    
    String getOnErrorId();
    
    ITransitionableTo getOnError();
    
    void setOnErrorId(String id);
    
    void setOnError(ITransitionableTo onerror);
    
}
