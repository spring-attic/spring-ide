package org.springframework.ide.eclipse.gettingstarted.wizard;

import java.net.URL;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

public class OpenUrlSection extends WizardPageSection {

	private LiveExpression<URL> url;
	private LiveVariable<Boolean> enableOpen;
	private String sectionLabel;

	public OpenUrlSection(WizardPageWithSections owner, String sectionLabel,
			LiveExpression<URL> url,
			LiveVariable<Boolean> enableOpenOnFinish) {
		super(owner);
		this.sectionLabel = sectionLabel;
		this.url = url;
		this.enableOpen = enableOpenOnFinish;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return Validator.OK;
	}

	@Override
	public void createContents(Composite page) {
		final Composite comp;
		if (sectionLabel!=null) {
			Group group = new Group(page, SWT.BORDER);
			group.setText(sectionLabel);
			comp = group;
		} else {
			comp = new Composite(page, SWT.NONE);
		}
		
		comp.setLayout(new GridLayout(3, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);
		
		final Label label = new Label(comp, SWT.NONE);
		final Button checkbox = new Button(comp, SWT.CHECK);
		checkbox.setText("Open");
		checkbox.setToolTipText("Open the url after finishing this wizard");
		checkbox.setSelection(enabled(enableOpen));
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(true, false).applyTo(label);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(checkbox);
		
		url.addListener(new ValueListener<URL>() {
			@Override
			public void gotValue(LiveExpression<URL> exp, URL value) {
				String txt = value==null ? "<no home page url>" : value.toString();
				label.setText(txt);
				checkbox.setEnabled(value!=null);
				//Size requirements for label may have changed:
				comp.layout(new Control[] {label}); 
			}
		});
		
		checkbox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				refresh();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				refresh();
			}
			private void refresh() {
				enableOpen.setValue(checkbox.getSelection());
			}
		});
	}

	public static boolean enabled(LiveExpression<Boolean> var) {
		Boolean val = var.getValue();
		return val!=null && val;
	}

}
