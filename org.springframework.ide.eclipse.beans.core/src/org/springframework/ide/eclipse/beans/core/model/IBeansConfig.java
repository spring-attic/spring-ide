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
 * @author Torsten Juergeleit
 */
public interface IBeansConfig extends IResourceModelElement, IBeanClassAware {

	char EXTERNAL_FILE_NAME_PREFIX = '/';

	Set<Problem> getWarnings();

	Set<Problem> getErrors();

	Set<IBeanAlias> getAliases();

	IBeanAlias getAlias(String name);

	Set<IBeansComponent> getComponents();
	
	Set<IBean> getBeans();
	
	IBean getBean(String name);

	boolean hasBean(String name);

	Set<IBean> getInnerBeans();
}
