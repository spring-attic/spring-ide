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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCoreUtils;
import org.springframework.ide.eclipse.web.flow.core.internal.model.Property;
import org.springframework.ide.eclipse.web.flow.core.internal.model.Setup;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IDescriptionEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IPropertyEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.ISetup;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IViewState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowEditorInput;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelDecorator;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelProvider;

public class ViewStatePropertiesDialog extends TitleAreaDialog implements
        IDialogValidator {
    
    private class SetupContentProvider implements IStructuredContentProvider {

        private IViewState project;

        public SetupContentProvider(IViewState project) {
            this.project = project;
        }

        public void dispose() {
        }

        public Object[] getElements(Object obj) {
            if (project.getSetup() != null)
                return new Object[] {project.getSetup() };
            else 
                return null;
        }

        public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
        }
    }
    
    private static int RADIOBEANREF_CHOICE = 0;

    private static int RADIOCLASS_CHOICE = 1;

    private static int RADIOCLASSREF_CHOICE = 2;
    
    private Button addButton;

    private Label autowireLabel;

    private Combo autowireText;

    private Label beanLabel;

    private BeanReferencePropertiesComposite beanProperties;
    
    private IBeansConfigSet beansConfig;

    private Text beanText;

    private Button browseBeanButton;

    private Button browseClassButton;

    private Button browseClassRefButton;
    
    private Button browseOnerrorButton;

    private SelectionListener buttonListener = new SelectionAdapter() {

        public void widgetSelected(SelectionEvent e) {
            handleButtonPressed((Button) e.widget);
        }
    };

    private Label classLabel;

    private Label classRefLabel;

    private Text classRefText;

    private Text classText;
    
    private Table configsTable;
    
    private TableViewer configsViewer;
    
    private DescriptionComposite description;

    private Button editButton;
    
    private int LABEL_WIDTH = 70;
    
    private Label methodLabel;
    
    private Text methodText;

    private Text nameText;

    private Button okButton;
    
    private Label onerrorLabel;
    
    private Text onerrorText;

    private IWebFlowModelElement parent;
    
    private Shell parentShell;

    private PropertiesComposite properties;

    private Button radioBeanRef;

    private Button radioClass;

    private Button radioClassRef;

    private Button removeButton;
    
    private Button setupButton;
    
    private ISetup setupClone;

    private IViewState viewState;

    private IViewState viewStateClone;

    private Text viewText;

    public ViewStatePropertiesDialog(Shell parentShell,
            IWebFlowModelElement parent, IViewState state) {
        super(parentShell);
        this.viewState = state;
        this.parent = parent;
        this.viewStateClone = (IViewState) ((ICloneableModelElement) state)
                .cloneModelElement();
        this.setupClone = this.viewStateClone.getSetup();
        if (this.setupClone == null) {
            this.setupClone = new Setup();
        }
        this.parentShell = parentShell;
        WebFlowEditorInput input = WebFlowUtils.getActiveFlowEditorInput();
        beansConfig = input.getBeansConfigSet();
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.viewStateClone.setId(trimString(getId()));
            this.viewStateClone.setView(trimString(getView()));
            if (this.beanProperties.useBeanReference()) {
                if (this.beanProperties.getRadioBeanRef()) {
                    this.viewStateClone.setBean(this.beanProperties
                            .getBeanText());
                    this.viewStateClone.setBeanClass(null);
                    this.viewStateClone.setAutowire(null);
                    this.viewStateClone.setClassRef(null);
                } else if (this.beanProperties.getRadioClass()) {
                    this.viewStateClone.setBean(null);
                    this.viewStateClone
                            .setBeanClass(trimString(this.beanProperties
                                    .getClassText()));
                    this.viewStateClone
                            .setAutowire(trimString(this.beanProperties
                                    .getAutowireText()));
                    this.viewStateClone.setClassRef(null);
                } else if (this.beanProperties.getRadioClassRef()) {
                    this.viewStateClone.setBean(null);
                    this.viewStateClone.setBeanClass(null);
                    this.viewStateClone.setAutowire(null);
                    this.viewStateClone.setClassRef(this.beanProperties
                            .getClassRefText());
                }
            } else {
                this.viewStateClone.setBean(null);
                this.viewStateClone.setBeanClass(null);
                this.viewStateClone.setAutowire(null);
                this.viewStateClone.setClassRef(null);
            }
            if (this.setupButton.getSelection()) {
                if (this.radioBeanRef.getSelection()) {
                    this.setupClone.setBean(this.beanText.getText());
                    this.setupClone.setBeanClass(null);
                    this.setupClone.setAutowire(null);
                    this.setupClone.setClassRef(null);
                } else if (this.radioClass.getSelection()) {
                    this.setupClone.setBean(null);
                    this.setupClone
                            .setBeanClass(trimString(this.classText.getText()));
                    this.setupClone
                            .setAutowire(trimString(this.autowireText.getText()));
                    this.setupClone.setClassRef(null);
                } else if (this.radioClassRef.getSelection()) {
                    this.setupClone.setBean(null);
                    this.setupClone.setBeanClass(null);
                    this.setupClone.setAutowire(null);
                    this.setupClone.setClassRef(this.classRefText.getText());
                }
                this.setupClone.setMethod(trimString(this.methodText.getText()));
                this.setupClone.setOnErrorId(trimString(this.onerrorText.getText()));
                if (this.viewStateClone.getSetup() == null) {
                    this.viewStateClone.setSetup(setupClone);
                }
            }
            else {
                if (this.viewStateClone.getSetup() != null) {
                    this.viewStateClone.removeSetup();
                }
            }
            
            if (this.viewStateClone instanceof IDescriptionEnabled) {
                ((IDescriptionEnabled) this.viewStateClone)
                        .setDescription(description.getDescription());
            }
            
            ((ICloneableModelElement) this.viewState)
                    .applyCloneValues((ICloneableModelElement) this.viewStateClone);
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
        if (this.viewState != null && this.viewState.getId() != null) {
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
        Composite composite = new Composite(parentComposite, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabFolder folder = new TabFolder(composite, SWT.NULL);
        TabItem item1 = new TabItem(folder, SWT.NULL);
        item1.setText("General");
        item1.setImage(getImage());
        TabItem item4 = new TabItem(folder, SWT.NULL);
        item4.setText("Setup");
        item4.setImage(WebFlowImages.getImage(WebFlowImages.IMG_OBJS_SETUP));
        TabItem item2 = new TabItem(folder, SWT.NULL);
        TabItem item3 = new TabItem(folder, SWT.NULL);
        TabItem item5 = new TabItem(folder, SWT.NULL);

        Composite nameGroup = new Composite(folder, SWT.NULL);
        nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout1 = new GridLayout();
        layout1.numColumns = 2;
        layout1.marginWidth = 5;
        nameGroup.setLayout(layout1);
        Label nameLabel = new Label(nameGroup, SWT.NONE);
        nameLabel.setText("Id");
        nameText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
        if (this.viewState != null && this.viewState.getId() != null) {
            this.nameText.setText(this.viewState.getId());
        }
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Label viewLabel = new Label(nameGroup, SWT.NONE);
        viewLabel.setText("View");

        viewText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
        if (this.viewState != null && this.viewState.getView() != null) {
            this.viewText.setText(this.viewState.getView());
        }
        viewText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        viewText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        item1.setControl(nameGroup);
        
//      Group for attribute mapper settings.
        Group groupActionType = new Group(folder, SWT.NULL);
        GridLayout layoutAttMap = new GridLayout();
        layoutAttMap.marginWidth = 3;
        layoutAttMap.marginHeight = 3;
        groupActionType.setLayout(layoutAttMap);
        groupActionType.setText(" Setup ");
        groupActionType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Create the radio button for no attribute mapper.
        setupButton = new Button(groupActionType, SWT.CHECK);
        if (this.viewState != null
                && this.viewState.getSetup() != null) {
            setupButton.setSelection(true);
        }
        setupButton.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
        setupButton.setText("Use Setup");
        setupButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setSetupEnabled();
                validateInput();
            }
        });
        
        Composite methodComposite = new Composite(groupActionType, SWT.NONE);
        methodComposite
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout3 = new GridLayout();
        layout3.marginHeight = 3;
        layout3.marginWidth = 20;
        layout3.numColumns = 3;
        methodComposite.setLayout(layout3);
        
        onerrorLabel = new Label(methodComposite, SWT.NONE);
        GridData gridData1 = new GridData(
                GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData1.widthHint = LABEL_WIDTH;
        onerrorLabel.setLayoutData(gridData1);
        onerrorLabel.setText("On Error");

        onerrorText = new Text(methodComposite, SWT.SINGLE | SWT.BORDER);
        if (this.setupClone != null && this.setupClone.getOnErrorId() != null) {
            onerrorText.setText(this.setupClone.getOnErrorId());
        }
        onerrorText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        onerrorText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        browseOnerrorButton = new Button(methodComposite, SWT.PUSH);
        browseOnerrorButton.setText("...");
        browseOnerrorButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_END));
        browseOnerrorButton.addSelectionListener(buttonListener);
        
        

        methodLabel = new Label(methodComposite, SWT.NONE);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        methodLabel.setLayoutData(gridData);
        methodLabel.setText("Method");

        methodText = new Text(methodComposite, SWT.SINGLE | SWT.BORDER);

        if (this.viewState != null && this.viewState.getSetup() != null && this.viewState.getSetup().getMethod() != null) {
            methodText.setText(this.viewState.getSetup().getMethod());
        }

        methodText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        methodText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        
        //        // Create the radio button for no attribute mapper.
        radioBeanRef = new Button(groupActionType, SWT.RADIO);
        if (this.viewState != null
                && this.viewState.getSetup() != null
                && this.viewState.getSetup().getBean() != null) {
            radioBeanRef.setSelection(true);
        }
        radioBeanRef.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioBeanRef.setText("Locate setup by bean reference");
        radioBeanRef.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setSetupChoice(RADIOBEANREF_CHOICE);
                validateInput();
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
        if (this.viewState != null
                && this.viewState.getSetup() != null
                && this.viewState.getSetup().getBean() != null) {
            beanText.setText(this.viewState.getSetup().getBean());
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
        if (this.viewState != null
                && this.viewState.getSetup() != null
                && this.viewState.getSetup().getClassRef() != null) {
            radioClassRef.setSelection(true);
        }
        radioClassRef.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioClassRef.setText("Locate setup by class reference");
        radioClassRef.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setSetupChoice(RADIOCLASSREF_CHOICE);
                validateInput();
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
        if (this.viewState != null
                && this.viewState.getSetup() != null
                && this.viewState.getSetup().getClassRef() != null) {
            classRefText.setText(this.viewState.getSetup()
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
        if (this.viewState != null
                && this.viewState.getSetup() != null
                && this.viewState.getSetup().getBeanClass() != null) {
            radioClass.setSelection(true);
        }

        radioClass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioClass.setText("Locate setup by class");
        radioClass.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setSetupChoice(RADIOCLASS_CHOICE);
                validateInput();
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
        if (this.viewState != null
                && this.viewState.getSetup() != null
                && this.viewState.getSetup().getBeanClass() != null) {
            classText.setText(this.viewState.getSetup()
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
        if (this.viewState != null
                && this.viewState.getSetup() != null
                && this.viewState.getSetup().getAutowire() != null) {
            autowireText.setText(this.viewState.getSetup()
                    .getAutowire());
        }
        autowireText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Group groupPropertyType = new Group(groupActionType, SWT.NULL);
        GridLayout layoutPropMap = new GridLayout();
        layoutPropMap.marginWidth = 3;
        layoutPropMap.marginHeight = 3;
        groupPropertyType.setLayout(layoutPropMap);
        groupPropertyType.setText(" Properties ");
        groupPropertyType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite tableAndButtons = new Composite(groupPropertyType, SWT.NONE);
        tableAndButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout2 = new GridLayout();
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        layout2.numColumns = 2;
        tableAndButtons.setLayout(layout2);

        configsTable = new Table(tableAndButtons, SWT.MULTI
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        //data.widthHint = 250;
        data.heightHint = 50;
        configsTable.setLayoutData(data);
        TableColumn columnName = new TableColumn(configsTable, SWT.NONE);
        columnName.setText("Name");
        columnName.setWidth(150);
        TableColumn columnValue = new TableColumn(configsTable, SWT.NONE);
        columnValue.setText("Value");
        columnValue.setWidth(120);
        TableColumn columnType = new TableColumn(configsTable, SWT.NONE);
        columnType.setText("Type");
        columnType.setWidth(80);
        configsTable.setHeaderVisible(true);

        configsViewer = new TableViewer(configsTable);
        String[] columnNames = new String[] { "Name", "Value", "Type" };
        configsViewer.setColumnProperties(columnNames);
        configsViewer.setContentProvider(new PropertiesContentProvider(
                setupClone, configsViewer));
        
        configsViewer.setLabelProvider(new ModelTableLabelProvider());
        configsViewer.setCellModifier(new TableCellModifier());
        configsViewer.setInput(this.setupClone);
        configsTable.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                handleTableSelectionChanged();
            }
        });
        Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
        GridLayout layout30 = new GridLayout();
        layout30.marginHeight = 0;
        layout30.marginWidth = 0;
        buttonArea.setLayout(layout30);
        buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        addButton = new Button(buttonArea, SWT.PUSH);
        addButton.setText("Add");
        GridData data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data1.widthHint = 40;
        addButton.setLayoutData(data1);
        addButton.addSelectionListener(new SelectionAdapter() {

            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e) {
                IProperty property = new Property(setupClone, "<name>", "<value>");
                PropertyEditorDialog dialog = new PropertyEditorDialog(parentShell, property);
                dialog.open();
                configsViewer.refresh(true);
            }
        });
        editButton = new Button(buttonArea, SWT.PUSH);
        editButton.setText("Edit");
        data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data1.widthHint = 40;
        editButton.setLayoutData(data1);
        editButton.addSelectionListener(new SelectionAdapter() {

            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) configsViewer
                        .getSelection();
                if (selection.getFirstElement() != null) {
                    if (selection.getFirstElement() instanceof IProperty) {
                        PropertyEditorDialog dialog = new PropertyEditorDialog(parentShell, (IProperty) selection
                                .getFirstElement());
                        dialog.open();
                        configsViewer.refresh(true);
                    }
                }
            }
        });

        removeButton = new Button(buttonArea, SWT.PUSH);
        removeButton.setText("Delete");
        GridData data2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data1.widthHint = 40;
        removeButton.setLayoutData(data2);
        removeButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) configsViewer
                        .getSelection();
                if (selection.getFirstElement() != null) {
                    if (selection.getFirstElement() instanceof IProperty) {
                        setupClone.removeProperty((IProperty) selection
                                .getFirstElement());
                    }
                }
            }
        });
        removeButton.setEnabled(false);
        editButton.setEnabled(false);
        
        item4.setControl(groupActionType);

        beanProperties = new BeanReferencePropertiesComposite(this, item2,
                getShell(), (IBeanReference) this.viewStateClone, false);
        item2.setControl(beanProperties.createDialogArea(folder));

        properties = new PropertiesComposite(this, item3, getShell(),
                (IPropertyEnabled) this.viewStateClone);
        item3.setControl(properties.createDialogArea(folder));

        description = new DescriptionComposite(this, item5, getShell(),
                (IDescriptionEnabled) this.viewStateClone);
        item5.setControl(description.createDialogArea(folder));
        
        this.setSetupEnabled();
        
        applyDialogFont(parentComposite);
        
        return parentComposite;
    }

    public String getId() {
        return this.nameText.getText();
    }

    protected Image getImage() {
        return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_VIEW_STATE);
    }

    protected String getMessage() {
        return "Enter the details for the view state";
    }
    
    public IWebFlowModelElement getModelElementParent() {
        return this.parent;
    }

    protected String getShellTitle() {
        return "View State";
    }

    protected String getTitle() {
        return "View State properties";
    }

    public String getView() {
        return this.viewText.getText();
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
                this.setSetupChoice(RADIOBEANREF_CHOICE);
            }

        } 
        else  if (button.equals(browseOnerrorButton)) {
            ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                    getShell(), new DecoratingLabelProvider(
                            new WebFlowModelLabelProvider(),
                            new WebFlowModelLabelDecorator()));
            dialog.setBlockOnOpen(true);
            dialog.setElements(WebFlowCoreUtils.getStatesWithoutParent(
                    this.parent).toArray());
            dialog.setEmptySelectionMessage("Enter a valid state id");
            dialog.setTitle("State reference");
            dialog.setMessage("Please select a state reference");
            dialog.setMultipleSelection(false);
            if (Dialog.OK == dialog.open()) {
                this.onerrorText.setText(((IState) dialog.getFirstResult())
                        .getId());
            }
        }
        else {
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
                    .setMessage("Select an setup implementation class"); //$NON-NLS-1$
            dialog.setBlockOnOpen(true);
            dialog.setTitle("Setup Class");
            //dialog.setFilter("*");
            if (Dialog.OK == dialog.open()) {
                IType obj = (IType) dialog.getFirstResult();
                if (button.equals(browseClassButton)) {
                    this.classText.setText(obj.getFullyQualifiedName());
                    this.setSetupChoice(RADIOCLASS_CHOICE);
                } else if (button.equals(browseClassRefButton)) {
                    this.classRefText.setText(obj.getFullyQualifiedName());
                    this.setSetupChoice(RADIOCLASSREF_CHOICE);
                }
            }
        }
        this.validateInput();

    }
    
    protected void handleTableSelectionChanged() {
        IStructuredSelection selection = (IStructuredSelection) configsViewer
                .getSelection();
        if (selection.isEmpty()) {
            this.editButton.setEnabled(false);
        } else {
            this.editButton.setEnabled(true);
        }
    }
    
    protected void setSetupChoice(int choice) {
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

    protected void setSetupEnabled() {
        if (!this.setupButton.getSelection()) {
            this.methodLabel.setEnabled(false);
            this.methodText.setEnabled(false);
            
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
            
            this.configsTable.setEnabled(false);
            this.addButton.setEnabled(false);
            this.editButton.setEnabled(false);
            this.removeButton.setEnabled(false);
             
            this.onerrorText.setEnabled(false);
            this.onerrorLabel.setEnabled(false);
            this.browseOnerrorButton.setEnabled(false);

        } else {
            setSetupChoice(RADIOBEANREF_CHOICE);
            this.radioBeanRef.setEnabled(true);
            this.radioClass.setEnabled(true);
            this.radioClassRef.setEnabled(true);
            
            this.methodLabel.setEnabled(true);
            this.methodText.setEnabled(true);
            
            this.configsTable.setEnabled(true);
            this.addButton.setEnabled(true);
            this.editButton.setEnabled(true);
            this.removeButton.setEnabled(true);
            
            this.onerrorText.setEnabled(true);
            this.onerrorLabel.setEnabled(true);
            this.browseOnerrorButton.setEnabled(true);

            if (this.viewState.getSetup() != null) {
                if (this.viewState.getSetup().getBean() != null) {
                    this.setSetupChoice(RADIOBEANREF_CHOICE);
                } else if (this.viewState.getSetup()
                        .getBeanClass() != null) {
                    this.setSetupChoice(RADIOCLASS_CHOICE);
                } else if (this.viewState.getSetup().getClassRef() != null) {
                    this.setSetupChoice(RADIOCLASSREF_CHOICE);
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
        String bean = this.beanText.getText();
        String clazz = this.classText.getText();
        String autowire = this.autowireText.getText();
        String classRef = this.classRefLabel.getText();
        boolean error = false;
        StringBuffer errorMessage = new StringBuffer();
        error = this.beanProperties.validateInput(errorMessage);
        if (id == null || "".equals(id)) {
            errorMessage.append("A valid id attribute is required. ");
            error = true;
        } else {
            if (WebFlowCoreUtils.isIdAlreadyChoosenByAnotherState(parent,
                    viewState, id)) {
                errorMessage
                        .append("The entered id attribute must be unique within a single web flow. ");
                error = true;
            }
        }
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