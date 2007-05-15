/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.javaconfig.core.model;

import java.util.ArrayList;
import java.util.List;

public class BeanCreationMethod {

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

	public BeanCreationMethod(String beanName, String beanClassName) {
		this.name = beanName;
		this.returnTypeName = beanClassName;
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

}