/*******************************************************************************
 *  Copyright (c) 2012 - 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.config.ui.hyperlinks.AdviceMethodHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.BeanHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.ClassHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.FactoryMethodHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.FieldHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.ImportHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.InitDestroyMethodHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.ListenerMethodHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.LookupReplaceMethodHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.PointcutReferenceHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.PropertyNameHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.ToolAnnotationBasedHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.hyperlinks.XmlBackedHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.widgets.ButtonAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.ComboAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.HyperlinkedComboAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.HyperlinkedTextAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAreaAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttribute;
import org.springframework.ide.eclipse.config.ui.wizards.ExtendedNewClassCreationWizard;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @since 2.3.4
 */
@SuppressWarnings("restriction")
public abstract class AbstractConfigDetailsSectionPart extends AbstractConfigSectionPart {

	/**
	 * An abstract implementation of {@link HyperlinkedComboAttribute} designed
	 * to operate on an XML attribute. Clients must implement the
	 * <code>openHyperlink</code> method.
	 */
	protected abstract class XmlBackedHyperlinkComboAttribute extends HyperlinkedComboAttribute {
		public XmlBackedHyperlinkComboAttribute(Composite client, FormToolkit toolkit, String attrName) {
			this(client, toolkit, attrName, false);
		}

		public XmlBackedHyperlinkComboAttribute(Composite client, FormToolkit toolkit, String attrName, boolean required) {
			super(client, toolkit, attrName, null, required);
		}

		@Override
		public void modifyAttribute() {
			editAttribute(attr, combo.getItem(combo.getSelectionIndex()));
		}

		@Override
		public void update() {
			setComboSelection(combo, getAttributeValue(attr));
		}
	}

	/**
	 * An abstract implementation of {@link HyperlinkedTextAttribute} designed
	 * to operate on an XML attribute. Clients must implement the
	 * <code>openHyperlink</code> method.
	 */
	protected abstract class XmlBackedHyperlinkTextAttribute extends HyperlinkedTextAttribute {
		public XmlBackedHyperlinkTextAttribute(Composite client, FormToolkit toolkit, String attrName) {
			this(client, toolkit, attrName, false);
		}

		public XmlBackedHyperlinkTextAttribute(Composite client, FormToolkit toolkit, String attrName, boolean required) {
			super(client, toolkit, attrName, required);
		}

		@Override
		public void modifyAttribute() {
			editAttribute(attr, text.getText());
		}

		@Override
		public void update() {
			setTextValue(text, getAttributeValue(attr));
		}
	}

	private final FormToolkit toolkit;

	private final SpringConfigInputAccessor delegate;

	public AbstractConfigDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input, Composite parent,
			FormToolkit toolkit) {
		super(editor, input, parent, toolkit, Section.TITLE_BAR | Section.DESCRIPTION);
		this.toolkit = toolkit;
		delegate = new SpringConfigInputAccessor(editor, input);
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to an AOP advice method. Clicking the hyperlink
	 * will open the class file at the method displayed in the text field.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createAdviceMethodAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new AdviceMethodHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	protected abstract void createAttributes(Composite client);

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to another Spring bean. Clicking the hyperlink will
	 * open the configuration file containing the bean definition.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createBeanAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new BeanHyperlinkProvider(getConfigEditor().getTextViewer(),
						getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link ButtonAttribute} widget set for displaying an attribute
	 * representing a Java class type. Clicking the hyperlink will open the
	 * class file displayed in the text field, or open a class creation wizard
	 * if the text field is empty. Clicking the button will invoke the open type
	 * dialog.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param includeInterfaces include interfaces in the proposals
	 * @param required denotes whether this is a required field
	 * @return {@link ButtonAttribute} widget set
	 */
	protected ButtonAttribute createClassAttribute(Composite client, String attr, final boolean includeInterfaces,
			boolean required) {
		ButtonAttribute buttonAttr = new ButtonAttribute(client, toolkit, attr, required) {
			@Override
			public void browse() {
				doOpenTypeDialog(attr, text.getText(), includeInterfaces);
			}

			@Override
			public void modifyAttribute() {
				editAttribute(attr, text.getText());
			}

			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new ClassHyperlinkProvider(getConfigEditor().getTextViewer(),
						getInput(), attr);
				if (!provider.open(text.getText())) {
					openNewClassWizard(attr, text.getText());
				}
			}

			@Override
			public void update() {
				setTextValue(text, getAttributeValue(attr));
			}
		};
		buttonAttr.createAttribute();
		return buttonAttr;
	}

	/**
	 * Creates a {@link ComboAttribute} widget set for displaying an attribute
	 * that can have multiple known values.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param values an array of known values
	 * @param required denotes whether this is a required field
	 * @return {@link ComboAttribute} widget set
	 */
	protected ComboAttribute createComboAttribute(Composite client, String attr, String[] values, boolean required) {
		ComboAttribute comboAttr = new ComboAttribute(client, toolkit, attr, values, required) {
			@Override
			public void modifyAttribute() {
				editAttribute(attr, combo.getItem(combo.getSelectionIndex()));
			}

			@Override
			public void update() {
				setComboSelection(combo, getAttributeValue(attr));
			}
		};
		comboAttr.createAttribute(2);
		return comboAttr;
	}

	@Override
	public void createContent() {
		Section detailsSection = getSection();
		detailsSection.setLayout(new GridLayout());
		detailsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		detailsSection.setText(Messages.getString("ToolingAwareDetailsPart.DETAILS_SECTION_TITLE")); //$NON-NLS-1$
		detailsSection.setDescription(Messages.getString("ToolingAwareDetailsPart.DETAILS_SECTION_DESCRIPTION")); //$NON-NLS-1$)

		Composite detailsClient = toolkit.createComposite(detailsSection);
		detailsClient.setLayout(new GridLayout(3, false));
		detailsClient.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		detailsSection.setClient(detailsClient);

		createAttributes(detailsClient);

		toolkit.paintBordersFor(detailsClient);
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a Java method. Clicking the hyperlink will open
	 * the class file at the method displayed in the text field.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createFactoryMethodAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new FactoryMethodHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a Java method. Clicking the hyperlink will open
	 * the class file at the method displayed in the text field.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param referenceNode the name of the factory bean reference node
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createFactoryMethodAttribute(Composite client, String attr,
			final String referenceNode, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new FactoryMethodHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr, referenceNode);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a field inside a class. Clicking the hyperlink
	 * will open the class file specified by the bean.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createFieldAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new FieldHyperlinkProvider(getConfigEditor().getTextViewer(),
						getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to an external resource. Clicking the hyperlink
	 * will open the referenced resource.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createImportAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new ImportHyperlinkProvider(getConfigEditor().getTextViewer(),
						getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a Java method. Clicking the hyperlink will open
	 * the class file at the method displayed in the text field.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createInitDestroyMethodAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new InitDestroyMethodHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a Java method. Clicking the hyperlink will open
	 * the class file at the method displayed in the text field.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createListenerMethodAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new ListenerMethodHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a Java method. Clicking the hyperlink will open
	 * the class file at the method displayed in the text field.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createLookupReplaceMethodAttribute(Composite client, String attr,
			boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new LookupReplaceMethodHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to an AOP pointcut. Clicking the hyperlink will
	 * open the configuration file containing the pointcut definition.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createPointcutAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new PointcutReferenceHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a Java method. Clicking the hyperlink will open
	 * the class file at the method displayed in the text field.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createPropertyNameAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new PropertyNameHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link TextAreaAttribute} widget set for displaying a block of
	 * text nested between an element's start and end tags.
	 * 
	 * @param client the parent composite
	 * @param elem the element name
	 * @return {@link TextAreaAttribute} widget set
	 */
	protected TextAreaAttribute createTextArea(Composite client, String elem) {
		TextAreaAttribute textElem = new TextAreaAttribute(client, toolkit, elem) {
			@Override
			public void modifyAttribute() {
				editElement(text.getText());
			}

			@Override
			public void update() {
				setTextValue(text, getElementValue());
			}
		};
		textElem.createAttribute(2);
		return textElem;
	}

	/**
	 * Creates a {@link TextAttribute} widget set for displaying an attribute
	 * that can have any text value.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link TextAttribute} widget set
	 */
	protected TextAttribute createTextAttribute(Composite client, String attr, boolean required) {
		TextAttribute textAttr = new TextAttribute(client, toolkit, attr, required) {
			@Override
			public void modifyAttribute() {
				editAttribute(attr, text.getText());
			}

			@Override
			public void update() {
				setTextValue(text, getAttributeValue(attr));
			}
		};
		textAttr.createAttribute(2);
		return textAttr;
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute whose reference is defined through a tool annotation. Clicking
	 * the hyperlink will open to an appropriate file (ie. a class file, or a
	 * configuration file) containing the reference displayed in the text field.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createToolAnnotationAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new ToolAnnotationBasedHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	private void doOpenTypeDialog(String attr, String filter, boolean includeInterfaces) {
		filter = filter.replace('$', '.');
		try {
			if (filter == null) {
				filter = ""; //$NON-NLS-1$
			}

			int scope;
			if (includeInterfaces) {
				scope = IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES;
			}
			else {
				scope = IJavaElementSearchConstants.CONSIDER_CLASSES;
			}

			SelectionDialog dialog = JavaUI.createTypeDialog(getConfigEditor().getSite().getShell(), PlatformUI
					.getWorkbench().getProgressService(), null, scope, false, filter);
			dialog.setTitle(Messages.getString("AbstractNamespaceDetailsPart.TYPE_SELECTION_DIALOG_TITLE")); //$NON-NLS-1$

			if (dialog.open() == Window.OK) {
				IType type = (IType) dialog.getResult()[0];
				String newValue = type.getFullyQualifiedName('$');
				editAttribute(attr, newValue);
			}
		}
		catch (JavaModelException e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
					.getString("AbstractNamespaceDetailsPart.ERROR_OPENING_DIALOG"), e)); //$NON-NLS-1$
		}
	}

	private void editAttribute(String attrName, String newValue) {
		delegate.editAttribute(attrName, newValue);
	}

	private void editElement(String elemValue) {
		delegate.editElement(elemValue);
	}

	private String getAttributeValue(String attr) {
		return delegate.getAttributeValue(attr);
	}

	private String getElementValue() {
		return delegate.getElementValue();
	}

	private void openNewClassWizard(String attr, String name) {
		name = name.replace('$', '.');
		IProject project = getConfigEditor().getResourceFile().getProject();
		ExtendedNewClassCreationWizard wizard = new ExtendedNewClassCreationWizard(project, name, true);
		wizard.init(PlatformUI.getWorkbench(), null);
		WizardDialog dialog = new WizardDialog(getConfigEditor().getSite().getShell(), wizard);
		dialog.create();
		if (dialog.open() == Window.OK) {
			String newValue = wizard.getQualifiedName();
			editAttribute(attr, newValue);
		}
	}

	/**
	 * Sets the selection on the given combo to the given value.
	 * 
	 * @param combo the combo to modify
	 * @param value the new combo value
	 */
	protected void setComboSelection(Combo combo, String value) {
		for (int i = 0; i < combo.getItemCount(); i++) {
			String item = combo.getItem(i);
			if (item.equals(value) && combo.getSelectionIndex() != i) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * Sets the text on the given text field to the given value.
	 * 
	 * @param text the text field to modify
	 * @param value the new text field value
	 */
	protected void setTextValue(Text text, String value) {
		if (!text.getText().equals(value)) {
			text.setText(value);
			text.setSelection(text.getText().length());
		}
	};

}
