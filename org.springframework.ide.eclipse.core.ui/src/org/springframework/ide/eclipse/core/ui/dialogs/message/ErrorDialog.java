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
package org.springframework.ide.eclipse.core.ui.dialogs.message;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin;
import org.springframework.ide.eclipse.core.ui.images.PluginImages;


/**
 * Basic Error dialog
 * @author Pierre-Antoine Grégoire
 */
public class ErrorDialog extends AbstractInformationDialog {

    /**
     * This constructor use the active Shell for the org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin.<br>
     * If this plugin is not available in the context of the call (e.g. not started) use a constructor allowing the specification of the shell.<br>
     * This constructor also uses a default image from this plugin.<br>
     * If this plugin is not available in the context of the call (e.g. not started) use a constructor allowing the specification of the image.<br>
     * @param subTitle The SubTitle displayed in the dialog
     * @param dialogMessage THe message to display
     */
    public ErrorDialog(String subTitle, String dialogMessage) {
        this(SpringCoreUIPlugin.getActiveShell(),SpringCoreUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_BASIC_ERROR_64), subTitle, dialogMessage);
    }

    /**
     * This constructor use the active Shell for the org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin
     * If this plugin is not available in the context of the call (e.g. not started) use a constructor allowing the specification of the shell. 
     * This constructor also uses a default image from this plugin.<br>
     * If this plugin is not available in the context of the call (e.g. not started) use a constructor allowing the specification of the image.<br>
     * 
     * Note that the details are formatted using the toString() method of the parameterized Object, but if the Object is a Throwable, the message and stacktrace are rendered.<br>
     * @param subTitle The SubTitle displayed in the dialog
     * @param dialogMessage THe message to display
     * @param detail The expandable details
     */
    public ErrorDialog(String subTitle, String dialogMessage, Object detail) {
    	this(SpringCoreUIPlugin.getActiveShell(),SpringCoreUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_BASIC_ERROR_64), subTitle, dialogMessage,detail);
    }
    /**
     * This constructor allows you to use your own shell and image.<br>
     * @param parentShell the parent shell
     * @param image the image to display
     * @param subTitle The SubTitle displayed in the dialog
     * @param dialogMessage THe message to display
     */
    public ErrorDialog(Shell parentShell,Image image,String subTitle, String dialogMessage) {
        this(SpringCoreUIPlugin.getActiveShell(), "Basic Error Dialog", subTitle, null, dialogMessage, null, new String[] { IDialogConstants.OK_LABEL }, 0, -1,image);
    }

    /**
     * This constructor allows you to use your own shell and image.<br>
     * 
     * @param parentShell the parent shell
     * @param image the image to display
     * @param subTitle The SubTitle displayed in the dialog
     * @param dialogMessage THe message to display
     * @param detail The expandable details
     */
    public ErrorDialog(Shell parentShell,Image image,String subTitle, String dialogMessage, Object detail) {
        this(SpringCoreUIPlugin.getActiveShell(), "Basic Error Dialog", subTitle, null, dialogMessage, detail, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.SHOW_DETAILS_LABEL }, 0, 1,image);
    }
    /**
     * @param parentShell
     * @param dialogTitle
     * @param dialogSubTitle
     * @param dialogTitleImage
     * @param dialogMessage
     * @param detail
     * @param dialogButtonLabels
     * @param defaultIndex
     * @param detailIndex
     * @param image
     */
    public ErrorDialog(Shell parentShell, String dialogTitle, String dialogSubTitle, Image dialogTitleImage, String dialogMessage, Object detail, String[] dialogButtonLabels, int defaultIndex, int detailIndex,Image image) {
        super(parentShell, dialogTitle, dialogSubTitle, dialogTitleImage, dialogMessage, dialogButtonLabels, defaultIndex, detailIndex);
        setDetail(detail);
        setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
        setImage(image);
    }
    
}
