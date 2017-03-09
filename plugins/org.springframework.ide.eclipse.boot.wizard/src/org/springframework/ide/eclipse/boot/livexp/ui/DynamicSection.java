package org.springframework.ide.eclipse.boot.livexp.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.ReflowableSection;

public class DynamicSection extends ReflowableSection {

	private LiveExpression<IPageSection> content;
	private Composite composite;

	public DynamicSection(IPageWithSections owner, LiveExpression<IPageSection> content) {
		super(owner);
		this.content = content;
	}

	@Override
	public void createContents(Composite page) {
		composite = new Composite(page, SWT.NONE);
		Layout l = GridLayoutFactory.fillDefaults().margins(0, 0).create();
		composite.setLayout(l);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		content.addListener((e, newContents) -> updateContent(newContents));
	}

	private void updateContent(IPageSection newContents) {
		if (composite!=null && !composite.isDisposed()) {
			for (Control oldWidget : composite.getChildren()) {
				oldWidget.dispose();
			}
		}
		newContents.createContents(composite);
		reflow(owner, composite);
	}

}
