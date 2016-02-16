/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package com.test;

import java.util.List;

/**
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class BeanInheritanceTestClass {

	public BeanInheritanceTestClass() {
		
	}
	
    public BeanInheritanceTestClass(String constructorArg) {

    }

    public void setFoo(String foo) {

    }

    public void setFooReference(BeanInheritanceTestClass referencedClass) {

    }

    public void setParam(String param) {

    }
    
    public void setParam2(String param) {

    }

    public void initFoo() {

    }

    public void initFoo(String fooParam) {

    }

    public void setTheList(List<?> foo) {

    }

}