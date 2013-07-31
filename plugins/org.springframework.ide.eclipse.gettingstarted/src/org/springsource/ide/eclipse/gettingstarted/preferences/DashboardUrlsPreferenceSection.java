/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.gettingstarted.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.preferences.GettingStartedPreferences;
import org.springframework.ide.eclipse.gettingstarted.preferences.URLBookmark;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PrefsPageSection;


import org.eclipse.debug.internal.ui.MultipleInputDialog;
import org.eclipse.debug.internal.ui.SWTFactory;

/**
 * Inspired by {@link org.eclipse.debug.ui.EnvironmentTab}
 * 
 * @author Andrew Eisenberg (Grails System properties prefs, copied here and modified
 * @author Kris De Volder
 */

@SuppressWarnings("restriction")
public class DashboardUrlsPreferenceSection extends PrefsPageSection {
	
    private static final String P_VARIABLE = "Name";
	private static final String P_VALUE = "Url";

    private String[] envTableColumnHeaders = {
            P_VARIABLE, 
            P_VALUE, 
    };
	
	private TableViewer environmentTable;
    
    private Button envAddButton;
    private Button envEditButton;
    private Button envRemoveButton;
    private Button envUpButton;
    private Button envDownButton;

	public DashboardUrlsPreferenceSection(IPageWithSections owner) {
		super(owner);
	}
	

	@Override
	public boolean performOK() {
        TableItem[] items = environmentTable.getTable().getItems();
        URLBookmark[] bookmarks = new URLBookmark[items.length];
        for (int i = 0; i < bookmarks.length; i++) {
			bookmarks[i] = (URLBookmark) items[i].getData();
		}
        GettingStartedActivator.getDefault().getPreferences().setDashboardWebPages(bookmarks);
        return true;
    }

	@Override
	public void performDefaults() {
		environmentTable.setInput(GettingStartedPreferences.DEFAULT_DASH_WEB_PAGES);
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return org.springsource.ide.eclipse.commons.livexp.core.Validator.OK;
	}
	
    /**
     * Creates and configures the table that displayed the key/value
     * pairs that comprise the environment.
     * @param parent the composite in which the table should be created
     */
    private void createEnvironmentTable(Composite parent) {
    	GridDataFactory grab = GridDataFactory.fillDefaults().grab(true, true);
    	
        Font font = parent.getFont();
        // Create label, add it to the parent to align the right side buttons with the top of the table
        SWTFactory.createLabel(parent, "Urls to show in the Spring Tool Suite Dashboard.", 2);
        
        // Create table composite
        Composite tableComposite = SWTFactory.createComposite(parent, font, 1, 1, GridData.FILL_BOTH, 0, 0);
        grab.applyTo(tableComposite);
        grab.applyTo(parent);
        
        // Create table
        environmentTable = new TableViewer(tableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        Table table = environmentTable.getTable();
        table.setLayout(new GridLayout());
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setFont(font);
        environmentTable.setContentProvider(new URLBookmarkContentProvider());
        environmentTable.setLabelProvider(new URLBookmarkLabelProvider());
        environmentTable.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                handleTableSelectionChanged();
            }
        });
        environmentTable.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                if (!environmentTable.getSelection().isEmpty()) {
                    handleEnvEditButtonSelected();
                }
            }
        });
        // Create columns
        final TableColumn tc1 = new TableColumn(table, SWT.NONE, 0);
        tc1.setText(envTableColumnHeaders[0]);
        final TableColumn tc2 = new TableColumn(table, SWT.NONE, 1);
        tc2.setText(envTableColumnHeaders[1]);
        final Table tref = table;
        final Composite comp = tableComposite;
        tableComposite.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                Rectangle area = comp.getClientArea();
                Point size = tref.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                ScrollBar vBar = tref.getVerticalBar();
                int width = area.width - tref.computeTrim(0,0,0,0).width - 2;
                if (size.y > area.height + tref.getHeaderHeight()) {
                    Point vBarSize = vBar.getSize();
                    width -= vBarSize.x;
                }
                Point oldSize = tref.getSize();
                if (oldSize.x > area.width) {
                    tc1.setWidth(width/2-1);
                    tc2.setWidth(width - tc1.getWidth());
                    tref.setSize(area.width, area.height);
                } else {
                    tref.setSize(area.width, area.height);
                    tc1.setWidth(width/2-1);
                    tc2.setWidth(width - tc1.getWidth());
                }
            }
        });
    }
	

	@Override
	public void createContents(Composite parent) {
        // Create main composite
        Composite mainComposite = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_HORIZONTAL);
        
        createEnvironmentTable(mainComposite);
        createTableButtons(mainComposite);
        environmentTable.setInput(GettingStartedActivator.getDefault().getPreferences().getDashboardWebPages());

	}

    /**
     * Creates the add/edit/remove buttons for the environment table
     * @param parent the composite in which the buttons should be created
     */
    private void createTableButtons(Composite parent) {
        // Create button composite
        Composite buttonComposite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END, 0, 0);

        // Create buttons
        envAddButton = createPushButton(buttonComposite, "Add", null); 
        envAddButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleEnvAddButtonSelected();
            }
        });
        
        envEditButton = createPushButton(buttonComposite, "Edit", null); 
        envEditButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleEnvEditButtonSelected();
            }
        });
        envEditButton.setEnabled(false);
        
        envRemoveButton = createPushButton(buttonComposite, "Remove", null); 
        envRemoveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleEnvRemoveButtonSelected();
            }
        });
        envRemoveButton.setEnabled(false);
        
        envUpButton = createPushButton(buttonComposite, "Up", null); 
        envUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleDirectionButtonSelected(-1);
            }
		});
        envUpButton.setEnabled(false);

        envDownButton = createPushButton(buttonComposite, "Down", null); 
        envDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleDirectionButtonSelected(+1);
            }
		});
        envDownButton.setEnabled(false);
    }
 	

	/**
     * Creates and returns a new push button with the given
     * label and/or image.
     * 
     * @param parent parent control
     * @param label button label or <code>null</code>
     * @param image image of <code>null</code>
     * 
     * @return a new push button
     */
    private Button createPushButton(Composite parent, String label, Image image) {
        return SWTFactory.createPushButton(parent, label, image);   
    }

    
	
    /**
     * Content provider for the environment table
     */
    private class URLBookmarkContentProvider implements IStructuredContentProvider {
        public void dispose() {
        }
        public Object[] getElements(Object inputElement) {
        	if (inputElement instanceof Object[]) {
//	        	GettingStartedPreferences prefs = (GettingStartedPreferences) inputElement;
//	            URLBookmark[] elements = prefs.getDashboardWebPages();
	            return (Object[]) inputElement;
        	} 
        	return new Object[0];
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput == null){
                return;
            }
            if (viewer instanceof TableViewer){
                TableViewer tableViewer= (TableViewer) viewer;
                if (tableViewer.getTable().isDisposed()) {
                    return;
                }
            }
        }
    }
    
    
    /**
     * Responds to a selection changed event in the environment table
     * @param event the selection change event
     */
    private void handleTableSelectionChanged() {
    	int[] selecteds = environmentTable.getTable().getSelectionIndices();
        int size = selecteds==null ? 0 : selecteds.length;
        envEditButton.setEnabled(size == 1);
        envRemoveButton.setEnabled(size > 0);
        envUpButton.setEnabled(canMoveSelection(-1));
        envDownButton.setEnabled(canMoveSelection(+1));
    }
    
	private boolean canMoveSelection(int dir) {
    	int[] selecteds = environmentTable.getTable().getSelectionIndices();
    	if (selecteds!=null && selecteds.length>0) {
        	int size = environmentTable.getTable().getItemCount();
    		for (int i = 0; i < selecteds.length; i++) {
				int moveTo = selecteds[i] + dir;
				if (moveTo<0 || moveTo>=size) {
					//not allowed to move out of range!
					return false;
				}
			}
    		//all selected elements are movable
    		return true;
    	}
    	//nothing to move, empty selection
		return false;
	}


	/**
     * Adds a new environment variable to the table.
     */
    private void handleEnvAddButtonSelected() {
        MultipleInputDialog dialog = new MultipleInputDialog(owner.getShell(), "System property") {
            @Override
            public void createBrowseField(String labelText,
                    String initialValue, boolean allowEmpty) {
               super.createBrowseField(labelText, initialValue, allowEmpty);
            }
            @SuppressWarnings("unchecked")
			@Override
            public void createVariablesField(String labelText,
                    String initialValue, boolean allowEmpty) {
                Label label = new Label(panel, SWT.NONE);
                label.setText(labelText);
                label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
                
                Composite comp = new Composite(panel, SWT.NONE);
                GridLayout layout = new GridLayout();
                layout.marginHeight=0;
                layout.marginWidth=0;
                comp.setLayout(layout);
                comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                
                final Text text = new Text(comp, SWT.SINGLE | SWT.BORDER);
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                data.widthHint = 200;
                text.setLayoutData(data);
                text.setData(FIELD_NAME, labelText);

                // make sure rows are the same height on both panels.
                label.setSize(label.getSize().x, text.getSize().y); 
                
                if (initialValue != null) {
                    text.setText(initialValue);
                }

                if (!allowEmpty) {
                    validators.add(new Validator() {
                        public boolean validate() {
                            return !text.getText().equals("");
                        }
                    });

                    text.addModifyListener(new ModifyListener() {
                        public void modifyText(ModifyEvent e) {
                            validateFields();
                        }
                    });
                }
                controlList.add(text);
            }
        }; 
        dialog.addTextField(P_VARIABLE, null, false);
        dialog.addTextField(P_VALUE, null, true);
        
        if (dialog.open() != Window.OK) {
            return;
        }
        
        String name = dialog.getStringValue(P_VARIABLE);
        String value = dialog.getStringValue(P_VALUE);
        
        if (name != null && value != null && name.length() > 0 && value.length() >0) {
            addBookmark(new URLBookmark(name.trim(), value.trim()));
        }
    }
    
    private void addBookmark(URLBookmark urlBookmark) {
    	ArrayList<URLBookmark> elements = new ArrayList<URLBookmark>(Arrays.asList((URLBookmark[])environmentTable.getInput()));
    	int insertAt = environmentTable.getTable().getSelectionIndex();
    	if (insertAt<0) {
    		elements.add(urlBookmark);
    	} else {
    		elements.add(insertAt, urlBookmark);
    	}
    	environmentTable.setInput(elements.toArray(new URLBookmark[elements.size()]));
    	if (insertAt>=0) {
    		environmentTable.getTable().setSelection(insertAt);
    	}
    	handleTableSelectionChanged();
	}


	/**
     * Creates an editor for the value of the selected environment variable.
     */
    private void handleEnvEditButtonSelected() {
        IStructuredSelection sel= (IStructuredSelection) environmentTable.getSelection();
        URLBookmark var= (URLBookmark) sel.getFirstElement();
        if (var == null) {
            return;
        }
        String originalName= var.getName();
        String value= var.getUrl();
        MultipleInputDialog dialog= new MultipleInputDialog(getShell(), "Edit"); 
        dialog.addTextField(P_VARIABLE, originalName, false);
        dialog.addTextField(P_VALUE, value, true);
        
        if (dialog.open() != Window.OK) {
            return;
        }
        String name= dialog.getStringValue(P_VARIABLE);
        value= dialog.getStringValue(P_VALUE);
        var.setName(name);
        var.setUrl(value);
        environmentTable.update(var, null);
    }

    private Shell getShell() {
		return owner.getShell();
	}


	/**
     * Removes the selected environment variable from the table.
     */
    private void handleEnvRemoveButtonSelected() {
    	Table table = environmentTable.getTable();
        
        int[] _selecteds = table.getSelectionIndices();
        Set<Integer> selecteds = new HashSet<Integer>();
        for (Integer s : _selecteds) {
			selecteds.add(s);
		}
        
        
        URLBookmark[] elements = (URLBookmark[]) environmentTable.getInput();
        URLBookmark[] newElements = new URLBookmark[elements.length - selecteds.size()];
        int dst=0;
        for (int src = 0; src < elements.length; src++) {
        	if (selecteds.contains(src)) {
        		//skip (so it is deleted)
        	} else {
        		//keep (so it is not deleted)
        		newElements[dst++] = elements[src];
        	}
		}
        environmentTable.setInput(newElements);
        environmentTable.setSelection(StructuredSelection.EMPTY);
    	handleTableSelectionChanged(); //programatic selection changes don't fire events we must trigger this ourselves.
    }
    
    protected void handleDirectionButtonSelected(int dir) {
    	Table table = environmentTable.getTable();
        
        int[] selecteds = table.getSelectionIndices();
        if (selecteds!=null && selecteds.length>0) {
        	URLBookmark[] elements = (URLBookmark[]) environmentTable.getInput();
        	int size = elements.length;
        	for (int i = 0; i < selecteds.length; i++) {
				int src = selecteds[i];
				int dst = src+dir;
				if (dst>=0 && dst<size) {
					URLBookmark temp = elements[dst];
					elements[dst] = elements[src];
					elements[src] = temp;
					selecteds[i] = dst;
				}
			}
        	environmentTable.setInput(elements);
        	table.setSelection(selecteds);
        	handleTableSelectionChanged();
        }
	}
      
    /**
     * Label provider for the environment table
     */
    private class URLBookmarkLabelProvider extends LabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
//            if (columnIndex == 0) {
//                return GrailsUiImages.getImage(GrailsUiImages.IMG_OBJ_GRAILS);
//            }
            return null;
        }
        public String getColumnText(Object element, int columnIndex)    {
            String result = null;
            if (element instanceof URLBookmark) {
                switch (columnIndex) {
                    case 0: // variable
                        result = ((URLBookmark) element).getName();
                        break;
                    case 1: // value
                        result = ((URLBookmark) element).getUrl();
                        break;
                }
            }
            return result;
        }
    }
	
}
