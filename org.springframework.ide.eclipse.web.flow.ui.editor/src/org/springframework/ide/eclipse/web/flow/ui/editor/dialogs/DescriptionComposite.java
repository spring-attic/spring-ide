/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.web.flow.core.model.IDescriptionEnabled;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;

public class DescriptionComposite {

    private IDescriptionEnabled state;

    private IDialogValidator validator;

    private Shell parentShell;
    
    private Text descriptionText;

    public DescriptionComposite(IDialogValidator validator, TabItem item,
            Shell parentShell, IDescriptionEnabled state) {
        this.state = state;
        item.setText("Description");
        item.setToolTipText("Define element's description");
        item
                .setImage(WebFlowImages
                        .getImage(WebFlowImages.IMG_OBJS_PROPERTIES));
        this.parentShell = parentShell;
    }

    protected Control createDialogArea(Composite parent) {
        Group groupPropertyType = new Group(parent, SWT.NULL);
        GridLayout layoutPropMap = new GridLayout();
        layoutPropMap.marginWidth = 3;
        layoutPropMap.marginHeight = 3;
        groupPropertyType.setLayout(layoutPropMap);
        groupPropertyType.setText(" Description ");
        groupPropertyType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.descriptionText = new Text(groupPropertyType, SWT.MULTI| SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 120;
        this.descriptionText.setLayoutData(data);
        
        if (this.state != null && this.state.hasDescription()) {
            this.descriptionText.setText(this.state.getDescription());
        }
        
        return groupPropertyType;
    }
    
    public String getDescription() {
        return this.descriptionText.getText();
    }
}
