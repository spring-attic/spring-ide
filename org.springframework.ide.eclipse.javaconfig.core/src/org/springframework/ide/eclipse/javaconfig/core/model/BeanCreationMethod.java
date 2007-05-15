/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.javaconfig.core.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;

/**
 * Holds information about a single {@link Bean} annotated method in a
 * {@link Configuration} class.
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanCreationMethod {

	private String owningClassName;

	private List<String> aliases = new ArrayList<String>();

	private boolean allowsOverriding;

	private String returnTypeName;

	private List<String> parameterTypes = new ArrayList<String>();

	private String name;

	private List<String> dependsOn = new ArrayList<String>();

	private String destoryMethodName;

	private String initMethodName;

	boolean isBeanCreationMethod = false;

	private String scope;

	private boolean isPublic;

	public BeanCreationMethod(String beanName, String beanClassName,
			String owningClassName) {
		this.name = beanName;
		this.returnTypeName = beanClassName;
		this.owningClassName = owningClassName;
	}

	public void addAlias(String alias) {
		this.aliases.add(alias);
	}

	public void addDependsOn(String alias) {
		this.dependsOn.add(alias);
	}

	public List<String> getAliases() {
		return aliases;
	}

	public String getReturnTypeName() {
		return returnTypeName;
	}

	public String getName() {
		return name;
	}

	public List<String> getDependsOn() {
		return dependsOn;
	}

	public String getDestoryMethodName() {
		return destoryMethodName;
	}

	public String getInitMethodName() {
		return initMethodName;
	}

	public String getScope() {
		return scope;
	}

	public boolean isAllowsOverriding() {
		return allowsOverriding;
	}

	public boolean isBeanCreationMethod() {
		return isBeanCreationMethod;
	}

	public void setAllowsOverriding(boolean allowsOverriding) {
		this.allowsOverriding = allowsOverriding;
	}

	public void setReturnTypeName(String beanClassName) {
		this.returnTypeName = beanClassName;
	}

	public void setBeanCreationMethod(boolean isBeanCreationMethod) {
		this.isBeanCreationMethod = isBeanCreationMethod;
	}

	public void setName(String beanName) {
		this.name = beanName;
	}

	public void setDestoryMethodName(String destoryMethodName) {
		this.destoryMethodName = destoryMethodName;
	}

	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public List<String> getParameterTypes() {
		return parameterTypes;
	}

	public void addParameterTypes(String parameterType) {
		this.parameterTypes.add(parameterType);
	}

	public String getOwningClassName() {
		return owningClassName;
	}

}