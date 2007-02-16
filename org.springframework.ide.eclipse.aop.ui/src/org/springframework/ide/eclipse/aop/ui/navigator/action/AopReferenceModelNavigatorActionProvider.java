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
package org.springframework.ide.eclipse.aop.ui.navigator.action;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 */
public class AopReferenceModelNavigatorActionProvider
        extends CommonActionProvider {

    private OpenConfigFileAction openConfigAction;
    
    public AopReferenceModelNavigatorActionProvider() {
    }

    public void init(ICommonActionExtensionSite site) {
        openConfigAction = new OpenConfigFileAction(site);
    }

    public void fillContextMenu(IMenuManager menu) {
        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openConfigAction);
    }

    public void fillActionBars(IActionBars actionBars) {
        actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
                openConfigAction);
    }
}
