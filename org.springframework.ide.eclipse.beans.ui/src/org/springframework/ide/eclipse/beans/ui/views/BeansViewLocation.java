/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.views;

/**
 * This class is used for selecting a specific node within the BeansView.
 * <p>
 * Sample:
 * <code>
 * BeansViewLocation location = new BeansViewLocation();
 * location.setProjectName("myproject");
 * location.setConfigName("myconfig");
 * location.setBeanName("mybean");
 * location.setPropertyName("myproperty");
 * IViewPart view = BeansView.showView();
 * ((IShowInTarget) view).show(new ShowInContext(location, null));
 * </code>
 */
public class BeansViewLocation {

	private String projectName;
	private String configName;
	private String beanName;
	private String constructorArgumentName;
	private String propertyName;

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	public boolean hasProjectName() {
		return (projectName != null && projectName.length() > 0);
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public String getConfigName() {
		return configName;
	}

	public boolean hasConfigName() {
		return (configName != null && configName.length() > 0);
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getBeanName() {
		return beanName;
	}

	public boolean hasBeanName() {
		return (beanName != null && beanName.length() > 0);
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public boolean hasPropertyName() {
		return (propertyName != null && propertyName.length() > 0);
	}
}
