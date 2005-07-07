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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowEditorInput;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelDecorator;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelProvider;

public class BeanReferencePropertiesComposite {

    private static int RADIOBEANREF_CHOICE = 0;

    private static int RADIOCLASS_CHOICE = 1;

    private static int RADIOCLASSREF_CHOICE = 2;

    private Label autowireLabel;

    private Combo autowireText;

    private Label beanLabel;

    private IBeansConfigSet beansConfig;

    private Text beanText;

    private Button browseBeanButton;

    private Button browseClassButton;

    private Button browseClassRefButton;

    private SelectionListener buttonListener = new SelectionAdapter() {

        public void widgetSelected(SelectionEvent e) {
            handleButtonPressed((Button) e.widget);
        }
    };

    private Label classLabel;

    private Label classRefLabel;

    private Text classRefText;

    private Text classText;

    private int LABEL_WIDTH = 70;

    private Text methodText;

    private Shell parentShell;

    private Button radioBeanRef;

    private Button radioClass;

    private Button radioClassRef;

    private boolean showMethod;

    private IBeanReference state;

    private IDialogValidator validator;

    private Button beanReferenceButton;

    public BeanReferencePropertiesComposite(IDialogValidator validator,
            TabItem item, Shell parentShell, IBeanReference state,
            boolean showMethod) {
        this.state = state;
        this.showMethod = showMethod;
        WebFlowEditorInput input = WebFlowUtils.getActiveFlowEditorInput();
        beansConfig = input.getBeansConfigSet();
        this.parentShell = parentShell;
        this.validator = validator;
        item.setText("Bean Reference");
        item
                .setToolTipText("Define bean reference as exported in the bean registry");
        item.setImage(WebFlowImages.getImage(WebFlowImages.IMG_OBJS_JAVABEAN));
    }

    protected Control createDialogArea(Composite parent) {
        GridData gridData = null;
        //Group for attribute mapper settings.
        Group groupActionType = new Group(parent, SWT.NULL);
        GridLayout layoutAttMap = new GridLayout();
        layoutAttMap.marginWidth = 3;
        layoutAttMap.marginHeight = 3;
        groupActionType.setLayout(layoutAttMap);
        groupActionType.setText(" Bean Reference ");
        GridData grid = new GridData();
        groupActionType.setLayoutData(grid);

        //      Create the radio button for no attribute mapper.
        beanReferenceButton = new Button(groupActionType, SWT.CHECK);
        if (this.state.getBean() != null || this.state.getBeanClass() != null
                || this.state.getClassRef() != null) {
            beanReferenceButton.setSelection(true);
        }
        //beanReferenceButton.setLayoutData(new GridData(
        //        GridData.FILL_HORIZONTAL));
        beanReferenceButton.setText("Use Bean Reference");
        beanReferenceButton.setToolTipText("Enable to use bean reference");
        beanReferenceButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setActionImplementation();
            }
        });

        if (showMethod) {
            Composite methodComposite = new Composite(groupActionType, SWT.NONE);
            methodComposite
                    .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            GridLayout layout3 = new GridLayout();
            layout3.marginHeight = 3;
            layout3.marginWidth = 20;
            layout3.numColumns = 2;
            methodComposite.setLayout(layout3);

            Label methodLabel = new Label(methodComposite, SWT.NONE);
            gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            gridData.widthHint = LABEL_WIDTH;
            methodLabel.setLayoutData(gridData);
            methodLabel.setText("Method");

            methodText = new Text(methodComposite, SWT.SINGLE | SWT.BORDER);

            if (this.state != null && this.state.getMethod() != null) {
                methodText.setText(this.state.getMethod());
            }

            methodText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            methodText.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    validator.validateInput();
                }
            });
        }

        // Create the radio button for no attribute mapper.
        radioBeanRef = new Button(groupActionType, SWT.RADIO);
        if (this.state != null && this.state.getBean() != null
                && this.state.getBean() != null) {
            radioBeanRef.setSelection(true);
        }
        radioBeanRef.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioBeanRef.setText("Locate by bean reference");
        radioBeanRef.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                setActionImplementationChoice(RADIOBEANREF_CHOICE);
                validator.validateInput();
            }
        });
        // Inset composite for classname.
        Composite inset1 = new Composite(groupActionType, SWT.NULL);
        GridLayout inset1Layout = new GridLayout();
        inset1Layout.numColumns = 3;
        inset1Layout.marginWidth = 20;
        inset1Layout.marginHeight = 2;
        inset1.setLayout(inset1Layout);
        inset1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Label field.
        beanLabel = new Label(inset1, SWT.NONE);
        beanLabel.setText("Bean");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        beanLabel.setLayoutData(gridData);

        // Add the text box for action classname.
        beanText = new Text(inset1, SWT.SINGLE | SWT.BORDER);
        if (this.state != null && this.state.getBean() != null) {
            beanText.setText(this.state.getBean());
        }
        beanText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        beanText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validator.validateInput();
            }
        });
        browseBeanButton = new Button(inset1, SWT.PUSH);
        browseBeanButton.setText("...");
        browseBeanButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_END));
        browseBeanButton.addSelectionListener(buttonListener);

        radioClassRef = new Button(groupActionType, SWT.RADIO);
        if (this.state != null && this.state.getClassRef() != null) {
            radioClassRef.setSelection(true);
        }
        radioClassRef.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioClassRef.setText("Locate by class reference");
        radioClassRef.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                setActionImplementationChoice(RADIOCLASSREF_CHOICE);
                validator.validateInput();
            }
        });

        // Inset composite for classname.
        Composite inset3 = new Composite(groupActionType, SWT.NULL);
        GridLayout inset3Layout = new GridLayout();
        inset3Layout.numColumns = 3;
        inset3Layout.marginWidth = 20;
        inset3Layout.marginHeight = 2;
        inset3.setLayout(inset3Layout);
        inset3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        //      Label field.
        classRefLabel = new Label(inset3, SWT.NONE);
        classRefLabel.setText("Classref");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        classRefLabel.setLayoutData(gridData);

        // Add the text box for action classname.
        classRefText = new Text(inset3, SWT.SINGLE | SWT.BORDER);
        if (this.state != null && this.state.getClassRef() != null) {
            classRefText.setText(this.state.getClassRef());
        }
        classRefText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classRefText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validator.validateInput();
            }
        });
        // Add the button for browsing types.
        browseClassRefButton = new Button(inset3, SWT.PUSH);
        browseClassRefButton.setText("...");
        browseClassRefButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_END));
        browseClassRefButton.addSelectionListener(buttonListener);

        radioClass = new Button(groupActionType, SWT.RADIO);
        if (this.state != null && this.state.getBeanClass() != null) {
            radioClass.setSelection(true);
        }
        radioClass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioClass.setText("Locate by class");
        radioClass.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                setActionImplementationChoice(RADIOCLASS_CHOICE);
                validator.validateInput();
            }
        });

        // Inset composite for classname.
        Composite inset2 = new Composite(groupActionType, SWT.NULL);
        GridLayout inset2Layout = new GridLayout();
        inset2Layout.numColumns = 3;
        inset2Layout.marginWidth = 20;
        inset2Layout.marginHeight = 2;
        inset2.setLayout(inset2Layout);
        inset2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Label field.
        classLabel = new Label(inset2, SWT.NONE);
        classLabel.setText("Class");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        classLabel.setLayoutData(gridData);

        // Add the text box for action classname.
        classText = new Text(inset2, SWT.SINGLE | SWT.BORDER);
        if (this.state != null && this.state.getBeanClass() != null) {
            classText.setText(this.state.getBeanClass());
        }
        classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validator.validateInput();
            }
        });
        // Add the button for browsing types.
        browseClassButton = new Button(inset2, SWT.PUSH);
        browseClassButton.setText("...");
        browseClassButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_END));
        browseClassButton.addSelectionListener(buttonListener);

        //      Label field.
        autowireLabel = new Label(inset2, SWT.NONE);
        autowireLabel.setText("Autowire");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        autowireLabel.setLayoutData(gridData);

        // Add the text box for action classname.

        autowireText = new Combo(inset2, SWT.READ_ONLY);
        autowireText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        autowireText.setItems(new String[] { "no", "byName", "byType",
                "constructor", "autodetect", "default" });
        if (this.state != null && this.state.getAutowire() != null) {
            autowireText.setText(this.state.getAutowire());
        }
        autowireText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validator.validateInput();
            }
        });
        
        setActionImplementationEnabled();
        setActionImplementation();

        return groupActionType;
    }

    /**
     * @return Returns the autowireText.
     */
    public String getAutowireText() {
        return trimString(autowireText.getText());
    }

    /**
     * @return Returns the beanText.
     */
    public String getBeanText() {
        return trimString(beanText.getText());
    }

    /**
     * @return Returns the classRefText.
     */
    public String getClassRefText() {
        return trimString(classRefText.getText());
    }

    /**
     * @return Returns the classText.
     */
    public String getClassText() {
        return trimString(classText.getText());
    }

    /**
     * @return Returns the methodText.
     */
    public String getMethodText() {
        return trimString(methodText.getText());
    }

    /**
     * @return Returns the radioBeanRef.
     */
    public boolean getRadioBeanRef() {
        return radioBeanRef.getSelection();
    }

    /**
     * @return Returns the radioClass.
     */
    public boolean getRadioClass() {
        return radioClass.getSelection();
    }

    /**
     * @return Returns the radioClassRef.
     */
    public boolean getRadioClassRef() {
        return radioClassRef.getSelection();
    }

    private String trimString(String string) {
        if (string != null && string == "") {
            string = null;
        }
        return string;
    }

    /**
     * One of the buttons has been pressed, act accordingly.
     */
    private void handleButtonPressed(Button button) {

        if (button.equals(browseBeanButton)) {
            WebFlowEditorInput input = WebFlowUtils.getActiveFlowEditorInput();
            IBeansConfigSet beansConfig = input.getBeansConfigSet();
            ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                    parentShell, new DecoratingLabelProvider(
                            new WebFlowModelLabelProvider(),
                            new WebFlowModelLabelDecorator()));
            dialog.setBlockOnOpen(true);
            List elements = new ArrayList();
            Iterator iter = beansConfig.getConfigs().iterator();
            IBeansProject parent = (IBeansProject) beansConfig
                    .getElementParent();
            while (iter.hasNext()) {
                String config = (String) iter.next();
                elements.addAll(parent.getConfig(config).getBeans());
            }
            dialog.setSize(100, 20);
            dialog.setElements(elements.toArray());
            dialog.setEmptySelectionMessage("Select a bean reference");
            dialog.setTitle("Bean reference");
            dialog.setMessage("Please select a bean reference");
            dialog.setMultipleSelection(false);
            if (Dialog.OK == dialog.open()) {
                this.beanText.setText(((IBean) dialog.getFirstResult())
                        .getElementName());
                this.setActionImplementationChoice(RADIOBEANREF_CHOICE);
            }

        } else if (button.equals(browseClassRefButton)) {

            WebFlowEditorInput input = WebFlowUtils.getActiveFlowEditorInput();
            IBeansConfigSet beansConfig = input.getBeansConfigSet();
            ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                    parentShell, new DecoratingLabelProvider(
                            new WebFlowModelLabelProvider(),
                            new WebFlowModelLabelDecorator()));
            dialog.setBlockOnOpen(true);
            List elements = new ArrayList();
            Iterator iter = beansConfig.getConfigs().iterator();
            IBeansProject parent = (IBeansProject) beansConfig
                    .getElementParent();
            while (iter.hasNext()) {
                String config = (String) iter.next();
                elements.addAll(parent.getConfig(config).getBeans());
            }
            dialog.setSize(100, 20);
            dialog.setElements(elements.toArray());
            dialog.setEmptySelectionMessage("Select a class reference");
            dialog.setTitle("Class reference");
            dialog.setMessage("Please select a class reference");
            dialog.setMultipleSelection(false);
            if (Dialog.OK == dialog.open()) {
                this.classRefText.setText(((IBean) dialog.getFirstResult())
                        .getClassName());
                this.setActionImplementationChoice(RADIOCLASSREF_CHOICE);
            }
        } else {
            IProject project = WebFlowUtils.getActiveFlowEditorInput()
                    .getFile().getProject();
            IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
            try {
                if (project.hasNature(JavaCore.NATURE_ID)) {
                    IJavaProject javaProject = (IJavaProject) project
                            .getNature(JavaCore.NATURE_ID);
                    IType type = javaProject
                            .findType("org.springframework.web.flow.Action");
                    if (type != null) {
                        searchScope = SearchEngine.createHierarchyScope(type);
                    }
                }
            } catch (JavaModelException e) {
            } catch (CoreException e) {
            }
            /*
             * TypeSelectionDialog2 dialog= new TypeSelectionDialog2(getShell(),
             * false, new ProgressMonitorDialog(getShell()), searchScope,
             * IJavaSearchConstants.TYPE);
             * dialog.setMessage(JavaUIMessages.JavaUI_defaultDialogMessage);
             */

            TypeSelectionDialog dialog = new TypeSelectionDialog(parentShell,
                    new ProgressMonitorDialog(parentShell),
                    IJavaSearchConstants.CLASS, searchScope);
            dialog.setMessage("Select an action implementation class"); //$NON-NLS-1$
            dialog.setBlockOnOpen(true);
            dialog.setTitle("Action Class");
            //dialog.setFilter("*");
            if (Dialog.OK == dialog.open()) {
                IType obj = (IType) dialog.getFirstResult();
                if (button.equals(browseClassButton)) {
                    this.classText.setText(obj.getFullyQualifiedName());
                    this.setActionImplementationChoice(RADIOCLASS_CHOICE);
                } else if (button.equals(browseClassRefButton)) {
                    this.classRefText.setText(obj.getFullyQualifiedName());
                    this.setActionImplementationChoice(RADIOCLASSREF_CHOICE);
                }
            }
        }

    }

    protected void setActionImplementationChoice(int choice) {
        if (RADIOBEANREF_CHOICE == choice) {
            this.radioBeanRef.setSelection(true);
            this.radioClass.setSelection(false);
            this.radioClassRef.setSelection(false);

            this.beanText.setEnabled(true);
            this.browseBeanButton.setEnabled(true);
            this.beanLabel.setEnabled(true);

            this.classText.setEnabled(false);
            this.browseClassButton.setEnabled(false);
            this.autowireText.setEnabled(false);
            this.classLabel.setEnabled(false);
            this.autowireLabel.setEnabled(false);

            this.classRefText.setEnabled(false);
            this.browseClassRefButton.setEnabled(false);
            this.classRefLabel.setEnabled(false);
        } else if (RADIOCLASS_CHOICE == choice) {
            this.radioBeanRef.setSelection(false);
            this.radioClass.setSelection(true);
            this.radioClassRef.setSelection(false);

            this.beanText.setEnabled(false);
            this.browseBeanButton.setEnabled(false);
            this.beanLabel.setEnabled(false);

            this.classText.setEnabled(true);
            this.browseClassButton.setEnabled(true);
            this.autowireText.setEnabled(true);
            this.classLabel.setEnabled(true);
            this.autowireLabel.setEnabled(true);

            this.classRefText.setEnabled(false);
            this.browseClassRefButton.setEnabled(false);
            this.classRefLabel.setEnabled(false);
        } else if (RADIOCLASSREF_CHOICE == choice) {
            this.radioBeanRef.setSelection(false);
            this.radioClass.setSelection(false);
            this.radioClassRef.setSelection(true);

            this.beanText.setEnabled(false);
            this.browseBeanButton.setEnabled(false);
            this.beanLabel.setEnabled(false);

            this.classText.setEnabled(false);
            this.browseClassButton.setEnabled(false);
            this.autowireText.setEnabled(false);
            this.classLabel.setEnabled(false);
            this.autowireLabel.setEnabled(false);

            this.classRefText.setEnabled(true);
            this.browseClassRefButton.setEnabled(true);
            this.classRefLabel.setEnabled(true);
        } else {
            this.radioBeanRef.setSelection(false);
            this.radioClass.setSelection(false);
            this.radioClassRef.setSelection(false);

            this.beanText.setEnabled(false);
            this.browseBeanButton.setEnabled(false);
            this.beanLabel.setEnabled(false);

            this.classText.setEnabled(false);
            this.browseClassButton.setEnabled(false);
            this.autowireText.setEnabled(false);
            this.classLabel.setEnabled(false);
            this.autowireLabel.setEnabled(false);

            this.classRefText.setEnabled(false);
            this.browseClassRefButton.setEnabled(false);
            this.classRefLabel.setEnabled(false);
        }

        if (this.beansConfig == null) {
            this.browseBeanButton.setEnabled(false);
            this.browseClassButton.setEnabled(false);
            this.browseClassRefButton.setEnabled(false);
        }
    }

    protected void setActionImplementationEnabled() {
        this.radioBeanRef.setEnabled(true);
        this.radioClass.setEnabled(true);
        this.radioClassRef.setEnabled(true);

        if (this.state.getBean() != null) {
            this.setActionImplementationChoice(RADIOBEANREF_CHOICE);
        } else if (this.state.getBeanClass() != null) {
            this.setActionImplementationChoice(RADIOCLASS_CHOICE);
        } else if (this.state.getClassRef() != null) {
            this.setActionImplementationChoice(RADIOCLASSREF_CHOICE);
        } else {
            this.setActionImplementationChoice(-4);
        }
    }
    
    protected void setActionImplementation() {
        if (this.beanReferenceButton.getSelection()) {
            this.setActionImplementationEnabled();
        }
        else {
            this.radioBeanRef.setEnabled(false);
            this.radioClass.setEnabled(false);
            this.radioClassRef.setEnabled(false);
            this.setActionImplementationChoice(-4);
        }
    }

    public boolean validateInput(StringBuffer errorMessage) {
        String bean = this.getBeanText();
        String clazz = this.getClassText();
        String autowire = this.getAutowireText();
        String classRef = this.getClassRefText();
        boolean error = false;
        if (this.radioBeanRef.getSelection()
                && (bean == null || "".equals(bean))) {
            errorMessage
                    .append("A valid bean reference attribute is required. ");
            error = true;
        }
        if (this.radioClass.getSelection()
                && (clazz == null || "".equals(clazz) || clazz.indexOf(".") == -1)) {
            errorMessage.append("A valid bean class name is required. ");
            error = true;
        }
        if (this.radioClass.getSelection()
                && (autowire == null || "".equals(autowire))) {
            errorMessage.append("Please select an autowire type. ");
            error = true;
        }
        if (this.radioClassRef.getSelection()
                && (classRef == null || "".equals(classRef) || classRef
                        .indexOf(".") == -1)) {
            errorMessage.append("A valid bean class reference is required. ");
            error = true;
        }
        return error;
    }
    
    public boolean useBeanReference() {
        return this.beanReferenceButton.getSelection();
    }
}