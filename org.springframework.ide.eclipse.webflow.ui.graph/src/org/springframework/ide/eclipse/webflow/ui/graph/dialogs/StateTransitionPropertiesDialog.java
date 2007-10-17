/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import java.util.ArrayList;
import java.util.List;

import ognl.Ognl;
import ognl.OgnlException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog2;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowUtils;
import org.springframework.util.StringUtils;

/**
 * Properties {@link Dialog} implemantation that enables the edition of
 * {@link IStateTransition} elements.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class StateTransitionPropertiesDialog extends TitleAreaDialog implements
		IDialogValidator {

	/**
	 * 
	 */
	private static final String EXPRESSION_PREFIX = "${";

	/**
	 * 
	 */
	private static final String EXPRESSION_SUFFIX = "}";

	/**
	 * 
	 */
	private List<IActionElement> actions;

	/**
	 * 
	 */
	private Button ognlButton;

	/**
	 * 
	 */
	private Button okButton;

	/**
	 * 
	 */
	private Text onText;

	/**
	 * 
	 */
	private Text onExceptionText;

	/**
	 * 
	 */
	private Button browseExceptionButton;

	/**
	 * 
	 */
	private SelectionListener buttonListener = new SelectionAdapter() {

		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	/**
	 * 
	 */
	private IWebflowModelElement parent;

	/**
	 * 
	 */
	private IStateTransition transition;

	/**
	 * 
	 */
	private IStateTransition transitionClone;

	/**
	 * 
	 */
	private ActionComposite actionProperties;

	/**
	 * 
	 */
	private PropertiesComposite properties;

	private boolean displayToBlock = false;

	private Combo toStateText;

	/**
	 * @param parentShell
	 * @param state
	 * @param parent
	 */
	@SuppressWarnings("unchecked")
	public StateTransitionPropertiesDialog(Shell parentShell,
			IWebflowModelElement parent, IStateTransition state) {
		this(parentShell, parent, state, false);
	}

	@SuppressWarnings("unchecked")
	public StateTransitionPropertiesDialog(Shell parentShell,
			IWebflowModelElement parent, IStateTransition state,
			boolean displayToBlock) {
		super(parentShell);
		this.transition = state;
		this.parent = parent;
		this.transitionClone = ((ICloneableModelElement<IStateTransition>) this.transition)
				.cloneModelElement();

		actions = new ArrayList<IActionElement>();
		if (this.transitionClone.getActions() != null) {
			actions.addAll(this.transitionClone.getActions());
		}
		this.displayToBlock = displayToBlock;
	}

	protected void handleButtonPressed(Button widget) {
		try {
			IType throwable = JdtUtils.getJavaType(WebflowUtils
					.getActiveWebflowConfig().getProject().getProject(),
					"java.lang.Throwable");
			IJavaSearchScope searchScope = SearchEngine
					.createJavaSearchScope(throwable.newTypeHierarchy(
							new NullProgressMonitor())
							.getAllSubtypes(throwable));
			TypeSelectionDialog2 dialog = new TypeSelectionDialog2(getShell(),
					false, new ProgressMonitorDialog(getShell()), searchScope,
					IJavaSearchConstants.CLASS);
			dialog.setBlockOnOpen(true);
			dialog.setTitle("Type Selection");
			// dialog.setFilter("*");
			if (Dialog.OK == dialog.open()) {
				IType obj = (IType) dialog.getFirstResult();
				this.onExceptionText.setText(obj.getFullyQualifiedName());
			}
		}
		catch (JavaModelException e) {
		}

		this.validateInput();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@SuppressWarnings("unchecked")
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.transitionClone.setOn(trimString(this.onText.getText()));
			this.transitionClone.setOnException(trimString(this.onExceptionText
					.getText()));

			if (this.displayToBlock) {
				this.transitionClone.setToStateId(trimString(this.toStateText
						.getText()));
			}

			if (this.actions != null && this.actions.size() > 0) {
				transitionClone.removeAll();
				for (IActionElement a : this.actions) {
					transitionClone.addAction(a);
				}
			}
			else {
				transitionClone.removeAll();
			}

			((ICloneableModelElement<IStateTransition>) this.transition)
					.applyCloneValues(this.transitionClone);
		}
		super.buttonPressed(buttonId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getShellTitle());
		shell.setImage(getImage());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		// do this here because setting the text will set enablement on the
		// ok button
		onText.setFocus();
		if (this.transition != null
				&& (this.transition.getOn() != null || this.transition
						.getOnException() != null)) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(getTitle());
		setMessage(getMessage());
		return contents;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(parentComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabFolder folder = new TabFolder(composite, SWT.NULL);
		folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		TabItem item1 = new TabItem(folder, SWT.NULL);
		item1.setText("General");
		item1.setImage(WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_TRANSITION));
		TabItem item2 = new TabItem(folder, SWT.NULL);
		TabItem item4 = new TabItem(folder, SWT.NULL);

		Composite nameGroup = new Composite(folder, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 2;
		layout1.marginWidth = 0;
		layout1.marginHeight = 0;
		nameGroup.setLayout(layout1);

		Group groupActionType = new Group(nameGroup, SWT.NULL);
		groupActionType.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layoutAttMap = new GridLayout();
		layoutAttMap.numColumns = 3;
		layoutAttMap.marginWidth = 5;
		groupActionType.setLayout(layoutAttMap);
		groupActionType.setText(" Transition ");

		Label onLabel = new Label(groupActionType, SWT.NONE);
		onLabel.setText("On");
		onText = new Text(groupActionType, SWT.SINGLE | SWT.BORDER);
		if (this.transition != null && this.transition.getOn() != null) {
			this.onText.setText(this.transition.getOn());
		}
		onText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		onText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		new Label(groupActionType, SWT.NONE);
		new Label(groupActionType, SWT.NONE);
		ognlButton = new Button(groupActionType, SWT.CHECK);
		ognlButton.setText("Parse OGNL transition criteria");
		ognlButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				validateInput();
			}
		});
		new Label(groupActionType, SWT.NONE);

		Label onExceptionLabel = new Label(groupActionType, SWT.NONE);
		onExceptionLabel.setText("On Exception");
		onExceptionText = new Text(groupActionType, SWT.SINGLE | SWT.BORDER);
		if (this.transition != null && this.transition.getOnException() != null) {
			this.onExceptionText.setText(this.transition.getOnException());
		}
		onExceptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		onExceptionText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		browseExceptionButton = new Button(groupActionType, SWT.PUSH);
		browseExceptionButton.setText("...");
		browseExceptionButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_END));
		browseExceptionButton.addSelectionListener(buttonListener);

		if (this.displayToBlock) {
			Label toStateLabel = new Label(groupActionType, SWT.NONE);
			toStateLabel.setText("To State");
			toStateText = new Combo(groupActionType, SWT.DROP_DOWN
					| SWT.READ_ONLY);
			toStateText.setItems(WebflowUtils.getStateId(this.parent));
			if (this.transition != null
					&& this.transition.getToStateId() != null) {
				this.toStateText.setText(this.transition.getToStateId());
			}
			toStateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			toStateText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});
			new Label(groupActionType, SWT.NONE);
		}

		item1.setControl(nameGroup);

		actionProperties = new ActionComposite(this, item2, getShell(),
				this.actions, this.transitionClone,
				IActionElement.ACTION_TYPE.ACTION);
		item2.setControl(actionProperties.createDialogArea(folder));

		properties = new PropertiesComposite(this, item4, getShell(),
				(IAttributeEnabled) this.transitionClone);
		item4.setControl(properties.createDialogArea(folder));

		applyDialogFont(parentComposite);

		return parentComposite;
	}

	/**
	 * Cut the expression from given criteria string and return it.
	 * @param encodedCriteria
	 * @return
	 */
	private String cutExpression(String encodedCriteria) {
		return encodedCriteria.substring(EXPRESSION_PREFIX.length(),
				encodedCriteria.length() - EXPRESSION_SUFFIX.length());
	}

	/**
	 * @return
	 */
	protected String getMessage() {
		return "Enter the details for the state transition";
	}

	/**
	 * @return
	 */
	public IWebflowModelElement getModelElementParent() {
		return this.parent;
	}

	/**
	 * @return
	 */
	protected String getShellTitle() {
		return "Transition";
	}

	/**
	 * @return
	 */
	protected String getTitle() {
		return "Transition properties";
	}

	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_TRANSITION);
	}

	/**
	 * 
	 */
	protected void handleTableSelectionChanged() {
		// TODO Auto-generated method stub
	}

	/**
	 * @param error
	 */
	protected void showError(String error) {
		super.setErrorMessage(error);
	}

	/**
	 * @param string
	 * @return
	 */
	public String trimString(String string) {
		if (string != null && string == "") {
			string = null;
		}
		return string;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.dialogs.IDialogValidator#validateInput()
	 */
	public void validateInput() {
		String id = this.onText.getText();
		String onexception = this.onExceptionText.getText();
		boolean error = false;
		StringBuffer errorMessage = new StringBuffer();
		if (!StringUtils.hasText(id) && !StringUtils.hasText(onexception)) {
			errorMessage
					.append("A valid on or on-exception attribute is required. ");
			error = true;
		}
		if (this.ognlButton.getSelection()) {
			if (!id.startsWith(EXPRESSION_PREFIX)
					|| !id.endsWith(EXPRESSION_SUFFIX)) {
				errorMessage
						.append("A valid OGNL expression needs to start with '${' and ends with '}'. ");
				error = true;
			}
			else {
				try {
					Ognl.parseExpression(this.cutExpression(id));
				}
				catch (OgnlException e) {
					errorMessage.append("Malformed OGNL expression. ");
					error = true;
				}
			}
		}
		if (error) {
			getButton(OK).setEnabled(false);
			setErrorMessage(errorMessage.toString());
		}
		else {
			getButton(OK).setEnabled(true);
			setErrorMessage(null);
		}
	}
}
