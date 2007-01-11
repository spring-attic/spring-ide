/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.core.model;

import java.util.Set;

import org.springframework.beans.factory.parsing.Problem;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This interface provides information for a Spring beans configuration.
 * 
 * @author Torsten Juergeleit
 */
public interface IBeansConfig extends IResourceModelElement, IBeanClassAware {

	char EXTERNAL_FILE_NAME_PREFIX = '/';

	String DEFAULT_LAZY_INIT = "false";
	String DEFAULT_AUTO_WIRE = "no";
	String DEFAULT_DEPENDENCY_CHECK = "none";
	String DEFAULT_INIT_METHOD = "";
	String DEFAULT_DESTROY_METHOD = "";
	String DEFAULT_MERGE = "false";

	Set<Problem> getWarnings();

	Set<Problem> getErrors();

	String getDefaultLazyInit();

	String getDefaultAutowire();

	String getDefaultDependencyCheck();

	String getDefaultInitMethod();

	String getDefaultDestroyMethod();

	String getDefaultMerge();

	Set<IBeansImport> getImports();

	Set<IBeanAlias> getAliases();

	IBeanAlias getAlias(String name);

	Set<IBeansComponent> getComponents();
	
	Set<IBean> getBeans();
	
	IBean getBean(String name);

	boolean hasBean(String name);

	Set<IBean> getInnerBeans();
}
