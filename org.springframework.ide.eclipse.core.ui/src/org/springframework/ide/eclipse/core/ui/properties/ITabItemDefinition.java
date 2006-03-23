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
package org.springframework.ide.eclipse.core.ui.properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Interface for a tab property extension
 * @author Pierre-Antoine Grégoire
 */
public interface ITabItemDefinition {

	public void createTabItem(TabFolder folder, IAdaptable selectedElement);

	public TabItem getTabItem();

	public boolean performOk();

	public void performDefaults();
}