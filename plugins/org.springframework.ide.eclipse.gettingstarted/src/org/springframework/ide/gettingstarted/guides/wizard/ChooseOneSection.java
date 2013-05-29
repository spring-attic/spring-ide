package org.springframework.ide.gettingstarted.guides.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public class ChooseOneSection<T> extends WizardPageSection {

	private SelectionModel<T> selection;
	private String label; //Descriptive Label for this section
	private T[] options; //The elements to choose from

	public ChooseOneSection(IPageWithSections owner, String label, SelectionModel<T> selection, T[] options) {
		super(owner);
		this.label = label;
		this.selection = selection;
		this.options = options;
	}
	
	private LabelProvider labelProvider = new LabelProvider();
	
	public ChooseOneSection<T> setLabelProvider(LabelProvider p) {
		this.labelProvider = p;
		return this;
	}
	
	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return selection.validator;
	}

	@Override
	public void createContents(Composite page) {
		Composite field = new Composite(page, SWT.NONE);
		GridLayout layout = GridLayoutFactory.fillDefaults().numColumns(2).create();
		field.setLayout(layout);
		Label fieldNameLabel = new Label(field, SWT.NONE);
		fieldNameLabel.setText(label);
		
		final Combo combo = new Combo(field, SWT.READ_ONLY);
		combo.setItems(getLabels());
		T preselect = selection.selection.getValue();
		if (preselect!=null) {
			combo.setText(labelProvider.getText(preselect));
		}
		GridDataFactory.fillDefaults().applyTo(combo);
		
		combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				int selected = combo.getSelectionIndex();
				if (selected>=0) {
					selection.selection.setValue(options[selected]);
				} else {
					selection.selection.setValue(null);
				}
			}
		});
		
	}

	private String[] getLabels() {
		String[] labels = new String[options.length]; 
		for (int i = 0; i < labels.length; i++) {
			labels[i] = labelProvider.getText(options[i]);
		}
		return labels;
	}

}
