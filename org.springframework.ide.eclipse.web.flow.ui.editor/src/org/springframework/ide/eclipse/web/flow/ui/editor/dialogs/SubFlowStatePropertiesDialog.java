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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCoreUtils;
import org.springframework.ide.eclipse.web.flow.core.internal.model.AttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IPropertyEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowEditorInput;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelDecorator;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelProvider;

public class SubFlowStatePropertiesDialog extends TitleAreaDialog implements
        IDialogValidator {

    private static int RADIOBEANREF_CHOICE = 0;

    private static int RADIOCLASS_CHOICE = 1;

    private static int RADIOCLASSREF_CHOICE = 2;

    private Button addButton;

    private Button attributeMapperButton;

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

    private TableViewer configsViewer2;

    private Text flowText;

    private int LABEL_WIDTH = 70;

    private Label nameLabel;

    private Text nameText;

    private Button okButton;

    private IWebFlowModelElement parent;

    private Button radioBeanRef;

    private Button radioClass;

    private Button radioClassRef;

    private Button removeButton;

    private ISubFlowState subFlowState;

    private ISubFlowState subFlowStateClone;

    private Label viewLabel;

    private BeanReferencePropertiesComposite beanProperties;

    private PropertiesComposite properties;

    public SubFlowStatePropertiesDialog(Shell parentShell,
            IWebFlowModelElement parent, ISubFlowState state) {
        super(parentShell);
        this.subFlowState = state;
        this.parent = parent;
        this.subFlowStateClone = (ISubFlowState) ((ICloneableModelElement) state)
                .cloneModelElement();

        WebFlowEditorInput input = WebFlowUtils.getActiveFlowEditorInput();
        beansConfig = input.getBeansConfigSet();
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.subFlowStateClone.setId(trimString(getId()));
            this.subFlowStateClone.setFlow(trimString(getView()));
            if (this.attributeMapperButton.getSelection()) {
                IAttributeMapper mapper = this.subFlowStateClone
                        .getAttributeMapper();
                if (mapper == null) {
                    mapper = new AttributeMapper();
                    this.subFlowStateClone.setAttributeMapper(mapper);
                }
                if (this.radioBeanRef.getSelection()) {
                    mapper.setBean(trimString(getBean()));
                    mapper.setBeanClass(null);
                    mapper.setAutowire(null);
                    mapper.setClassRef(null);
                } else if (this.radioClass.getSelection()) {
                    mapper.setBean(null);
                    mapper.setBeanClass(trimString(getBeanClass()));
                    mapper.setAutowire(trimString(getAutowire()));
                    mapper.setClassRef(null);
                } else if (this.radioClassRef.getSelection()) {
                    mapper.setBean(null);
                    mapper.setBeanClass(null);
                    mapper.setAutowire(null);
                    mapper.setClassRef(trimString(getClassRef()));
                }
            } else {
                this.subFlowStateClone.removeAttributeMapper();
            }
            if (this.beanProperties.useBeanReference()) {
                if (this.beanProperties.getRadioBeanRef()) {
                    this.subFlowStateClone.setBean(this.beanProperties
                            .getBeanText());
                    this.subFlowStateClone.setBeanClass(null);
                    this.subFlowStateClone.setAutowire(null);
                    this.subFlowStateClone.setClassRef(null);
                } else if (this.beanProperties.getRadioClass()) {
                    this.subFlowStateClone.setBean(null);
                    this.subFlowStateClone
                            .setBeanClass(trimString(this.beanProperties
                                    .getClassText()));
                    this.subFlowStateClone
                            .setAutowire(trimString(this.beanProperties
                                    .getAutowireText()));
                    this.subFlowStateClone.setClassRef(null);
                } else if (this.beanProperties.getRadioClassRef()) {
                    this.subFlowStateClone.setBean(null);
                    this.subFlowStateClone.setBeanClass(null);
                    this.subFlowStateClone.setAutowire(null);
                    this.subFlowStateClone.setClassRef(this.beanProperties
                            .getClassRefText());
                }
            } else {
                this.subFlowStateClone.setBean(null);
                this.subFlowStateClone.setBeanClass(null);
                this.subFlowStateClone.setAutowire(null);
                this.subFlowStateClone.setClassRef(null);
            }

            ((ICloneableModelElement) this.subFlowState)
                    .applyCloneValues((ICloneableModelElement) this.subFlowStateClone);
        }
        super.buttonPressed(buttonId);
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(getShellTitle());
        shell.setImage(getImage());
    }

    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        // do this here because setting the text will set enablement on the
        // ok button
        nameText.setFocus();
        if (this.subFlowState != null && this.subFlowState.getId() != null) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }

    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(getTitle());
        setMessage(getMessage());
        return contents;
    }

    protected Control createDialogArea(Composite parent) {
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        GridData gridData = null;
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        //layout.verticalSpacing = 10;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        TabFolder folder = new TabFolder(composite, SWT.NULL);
        TabItem item1 = new TabItem(folder, SWT.NULL);
        item1.setText("General");
        item1.setImage(getImage());
        TabItem item2 = new TabItem(folder, SWT.NULL);
        item2.setText("Attribute Mapper");
        item2.setImage(WebFlowImages
                .getImage(WebFlowImages.IMG_OBJS_ATTRIBUTE_MAPPER));
        TabItem item3 = new TabItem(folder, SWT.NULL);
        TabItem item4 = new TabItem(folder, SWT.NULL);

        Composite nameGroup = new Composite(folder, SWT.NULL);
        nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout1 = new GridLayout();
        layout1.numColumns = 2;
        layout1.marginWidth = 5;
        //layout1.horizontalSpacing = 10;
        //layout1.verticalSpacing = 10;
        nameGroup.setLayout(layout1);
        nameLabel = new Label(nameGroup, SWT.NONE);
        nameLabel.setText("Id");
        nameText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
        if (this.subFlowState != null && this.subFlowState.getId() != null) {
            this.nameText.setText(this.subFlowState.getId());
        }
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        viewLabel = new Label(nameGroup, SWT.NONE);
        viewLabel.setText("Flow");

        flowText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
        if (this.subFlowState != null && this.subFlowState.getFlow() != null) {
            this.flowText.setText(this.subFlowState.getFlow());
        }
        flowText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        flowText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        item1.setControl(nameGroup);

        //Group for attribute mapper settings.
        Group groupActionType = new Group(folder, SWT.NULL);
        GridLayout layoutAttMap = new GridLayout();
        layoutAttMap.marginWidth = 3;
        layoutAttMap.marginHeight = 3;
        groupActionType.setLayout(layoutAttMap);
        groupActionType.setText(" Attribute Mapper ");
        groupActionType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Create the radio button for no attribute mapper.
        attributeMapperButton = new Button(groupActionType, SWT.CHECK);
        if (this.subFlowState != null
                && this.subFlowState.getAttributeMapper() != null) {
            attributeMapperButton.setSelection(true);
        }
        attributeMapperButton.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
        attributeMapperButton.setText("Use Attribute Mapper");
        attributeMapperButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setAttributeMapperEnabled();
            }
        });
        //        // Create the radio button for no attribute mapper.
        radioBeanRef = new Button(groupActionType, SWT.RADIO);
        if (this.subFlowState != null
                && this.subFlowState.getAttributeMapper() != null
                && this.subFlowState.getAttributeMapper().getBean() != null) {
            radioBeanRef.setSelection(true);
        }
        radioBeanRef.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioBeanRef.setText("Locate attribute mapper by bean reference");
        radioBeanRef.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setAttributeMapperChoice(RADIOBEANREF_CHOICE);
            }
        });
        // Inset composite for classname.
        Composite inset1 = new Composite(groupActionType, SWT.NULL);
        GridLayout inset1Layout = new GridLayout();
        inset1Layout.numColumns = 3;
        inset1Layout.marginWidth = 20;
        inset1Layout.marginHeight = 2;
        inset1.setLayout(inset1Layout);
        inset1.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Label field.
        beanLabel = new Label(inset1, SWT.NONE);
        beanLabel.setText("Bean");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        beanLabel.setLayoutData(gridData);

        // Add the text box for action classname.
        beanText = new Text(inset1, SWT.SINGLE | SWT.BORDER);
        if (this.subFlowState != null
                && this.subFlowState.getAttributeMapper() != null
                && this.subFlowState.getAttributeMapper().getBean() != null) {
            beanText.setText(this.subFlowState.getAttributeMapper().getBean());
        }
        beanText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        beanText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        browseBeanButton = new Button(inset1, SWT.PUSH);
        browseBeanButton.setText("...");
        browseBeanButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_END));
        browseBeanButton.addSelectionListener(buttonListener);

        radioClassRef = new Button(groupActionType, SWT.RADIO);
        if (this.subFlowState != null
                && this.subFlowState.getAttributeMapper() != null
                && this.subFlowState.getAttributeMapper().getClassRef() != null) {
            radioClassRef.setSelection(true);
        }
        radioClassRef.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioClassRef.setText("Locate attribute mapper by class reference");
        radioClassRef.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setAttributeMapperChoice(RADIOCLASSREF_CHOICE);
            }
        });

        // Inset composite for classname.
        Composite inset3 = new Composite(groupActionType, SWT.NULL);
        GridLayout inset3Layout = new GridLayout();
        inset3Layout.numColumns = 3;
        inset3Layout.marginWidth = 20;
        inset3Layout.marginHeight = 2;
        inset3.setLayout(inset3Layout);
        inset3.setLayoutData(new GridData(GridData.FILL_BOTH));

        //      Label field.
        classRefLabel = new Label(inset3, SWT.NONE);
        classRefLabel.setText("Classref");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        classRefLabel.setLayoutData(gridData);

        // Add the text box for action classname.
        classRefText = new Text(inset3, SWT.SINGLE | SWT.BORDER);
        if (this.subFlowState != null
                && this.subFlowState.getAttributeMapper() != null
                && this.subFlowState.getAttributeMapper().getClassRef() != null) {
            classRefText.setText(this.subFlowState.getAttributeMapper()
                    .getClassRef());
        }
        classRefText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classRefText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        // Add the button for browsing types.
        browseClassRefButton = new Button(inset3, SWT.PUSH);
        browseClassRefButton.setText("...");
        browseClassRefButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_END));
        browseClassRefButton.addSelectionListener(buttonListener);

        radioClass = new Button(groupActionType, SWT.RADIO);
        if (this.subFlowState != null
                && this.subFlowState.getAttributeMapper() != null
                && this.subFlowState.getAttributeMapper().getBeanClass() != null) {
            radioClass.setSelection(true);
        }

        radioClass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioClass.setText("Locate attribute mapper by class");
        radioClass.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setAttributeMapperChoice(RADIOCLASS_CHOICE);
            }
        });

        // Inset composite for classname.
        Composite inset2 = new Composite(groupActionType, SWT.NULL);
        GridLayout inset2Layout = new GridLayout();
        inset2Layout.numColumns = 3;
        inset2Layout.marginWidth = 20;
        inset2Layout.marginHeight = 2;
        inset2.setLayout(inset2Layout);
        inset2.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Label field.
        classLabel = new Label(inset2, SWT.NONE);
        classLabel.setText("Class");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        classLabel.setLayoutData(gridData);

        // Add the text box for action classname.
        classText = new Text(inset2, SWT.SINGLE | SWT.BORDER);
        if (this.subFlowState != null
                && this.subFlowState.getAttributeMapper() != null
                && this.subFlowState.getAttributeMapper().getBeanClass() != null) {
            classText.setText(this.subFlowState.getAttributeMapper()
                    .getBeanClass());
        }
        classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
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
        if (this.subFlowState != null
                && this.subFlowState.getAttributeMapper() != null
                && this.subFlowState.getAttributeMapper().getAutowire() != null) {
            autowireText.setText(this.subFlowState.getAttributeMapper()
                    .getAutowire());
        }
        autowireText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        item2.setControl(groupActionType);

        beanProperties = new BeanReferencePropertiesComposite(this, item3,
                getShell(), (IBeanReference) this.subFlowStateClone, false);
        item3.setControl(beanProperties.createDialogArea(folder));

        properties = new PropertiesComposite(this, item4, getShell(),
                (IPropertyEnabled) this.subFlowStateClone);
        item4.setControl(properties.createDialogArea(folder));

        applyDialogFont(parentComposite);

        this.setAttributeMapperEnabled();

        return parentComposite;
    }

    /**
     * @return Returns the autowire.
     */
    public String getAutowire() {
        return this.autowireText.getText();
    }

    /**
     * @return Returns the bean.
     */
    public String getBean() {
        return this.beanText.getText();
    }

    /**
     * @return Returns the beanClass.
     */
    public String getBeanClass() {
        return this.classText.getText();
    }

    /**
     * @return Returns the classRef.
     */
    public String getClassRef() {
        return this.classRefText.getText();
    }

    public String getId() {
        return this.nameText.getText();
    }

    protected Image getImage() {
        return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_SUBFLOW_STATE);
    }

    protected String getMessage() {
        return "Enter the details for the sub flow state";
    }

    public String getName() {
        return this.nameText.getText();
    }

    protected String getShellTitle() {
        return "Sub Flow State";
    }

    protected String getTitle() {
        return "Sub Flow State properties";
    }

    public String getView() {
        return this.flowText.getText();
    }

    /**
     * One of the buttons has been pressed, act accordingly.
     */
    private void handleButtonPressed(Button button) {

        if (button.equals(browseBeanButton)) {
            WebFlowEditorInput input = WebFlowUtils.getActiveFlowEditorInput();
            IBeansConfigSet beansConfig = input.getBeansConfigSet();
            ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                    getShell(), new DecoratingLabelProvider(
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
            dialog.setElements(elements.toArray());
            dialog.setEmptySelectionMessage("Select a bean reference");
            dialog.setTitle("Bean reference");
            dialog.setMessage("Please select a bean reference");
            dialog.setMultipleSelection(false);
            if (Dialog.OK == dialog.open()) {
                this.beanText.setText(((IBean) dialog.getFirstResult())
                        .getElementName());
                this.setAttributeMapperChoice(RADIOBEANREF_CHOICE);
            }

        } else {
            IProject project = this.parent.getElementResource().getProject();
            IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
            try {
                if (project.hasNature(JavaCore.NATURE_ID)) {
                    IJavaProject javaProject = (IJavaProject) project
                            .getNature(JavaCore.NATURE_ID);
                    IType type = javaProject
                            .findType("org.springframework.binding.AttributeMapper");
                    if (type != null) {
                        searchScope = SearchEngine.createHierarchyScope(type);
                    }
                }
            } catch (JavaModelException e) {
            } catch (CoreException e) {
            }
            //TypeSelectionDialog2 dialog= new TypeSelectionDialog2(getShell(),
            // false,
            //        new ProgressMonitorDialog(getShell()), searchScope,
            // IJavaSearchConstants.TYPE);
            TypeSelectionDialog dialog = new TypeSelectionDialog(getShell(),
                    new ProgressMonitorDialog(getShell()),
                    IJavaSearchConstants.CLASS, searchScope);
            dialog
                    .setMessage("Select an attribute mapper implementation class"); //$NON-NLS-1$
            dialog.setBlockOnOpen(true);
            dialog.setTitle("Attribute Mapper Class");
            //dialog.setFilter("*");
            if (Dialog.OK == dialog.open()) {
                IType obj = (IType) dialog.getFirstResult();
                if (button.equals(browseClassButton)) {
                    this.classText.setText(obj.getFullyQualifiedName());
                    this.setAttributeMapperChoice(RADIOCLASS_CHOICE);
                } else if (button.equals(browseClassRefButton)) {
                    this.classRefText.setText(obj.getFullyQualifiedName());
                    this.setAttributeMapperChoice(RADIOCLASSREF_CHOICE);
                }
            }
        }
        this.validateInput();

    }

    /**
     *  
     */
    protected void handleTableSelectionChanged() {
        IStructuredSelection selection = (IStructuredSelection) configsViewer2
                .getSelection();
        if (selection.isEmpty()) {
            removeButton.setEnabled(false);
        } else {
            removeButton.setEnabled(true);
        }

    }

    protected void setAttributeMapperChoice(int choice) {
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

    protected void setAttributeMapperEnabled() {
        if (!this.attributeMapperButton.getSelection()) {
            this.radioBeanRef.setEnabled(false);
            this.radioClass.setEnabled(false);
            this.radioClassRef.setEnabled(false);

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
        } else {
            setAttributeMapperChoice(RADIOBEANREF_CHOICE);
            this.radioBeanRef.setEnabled(true);
            this.radioClass.setEnabled(true);
            this.radioClassRef.setEnabled(true);

            if (this.subFlowState.getAttributeMapper() != null) {
                if (this.subFlowState.getAttributeMapper().getBean() != null) {
                    this.setAttributeMapperChoice(RADIOBEANREF_CHOICE);
                } else if (this.subFlowState.getAttributeMapper()
                        .getBeanClass() != null) {
                    this.setAttributeMapperChoice(RADIOCLASS_CHOICE);
                } else if (this.subFlowState.getAttributeMapper().getClassRef() != null) {
                    this.setAttributeMapperChoice(RADIOCLASSREF_CHOICE);
                }
            }
        }
    }

    protected void showError(String error) {
        super.setErrorMessage(error);
    }

    public String trimString(String string) {
        if (string != null && string == "") {
            string = null;
        }
        return string;
    }

    public void validateInput() {
        String id = this.nameText.getText();
        String flow = this.flowText.getText();
        String bean = this.beanText.getText();
        String clazz = this.classText.getText();
        String autowire = this.autowireText.getText();
        String classRef = this.classRefLabel.getText();
        boolean error = false;
        StringBuffer errorMessage = new StringBuffer();

        if (id == null || "".equals(id)) {
            errorMessage.append("A valid id attribute is required. ");
            error = true;
        } else {
            if (WebFlowCoreUtils.isIdAlreadyChoosenByAnotherState(parent,
                    subFlowState, id)) {
                errorMessage
                        .append("The entered id attribute must be unique within a single web flow. ");
                error = true;
            }
        }
        if (flow == null || "".equals(flow)) {
            errorMessage.append("A valid flow attribute is required. ");
            error = true;
        }
        error = this.beanProperties.validateInput(errorMessage);
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
        if (error) {
            getButton(OK).setEnabled(false);
            setErrorMessage(errorMessage.toString());
        } else {
            getButton(OK).setEnabled(true);
            setErrorMessage(null);
        }
    }
}