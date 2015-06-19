package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;
import org.springframework.ide.eclipse.boot.dash.livexp.DelegatingLiveSet;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSets;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelectionSource;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;

public class DynamicCompositeSection<M, T> extends PageSection implements MultiSelectionSource<T>, Disposable {

	public static interface SectionFactory<M, T> {
		MultiSelectionSource<T> create(M model);
	}

	/**
	 * A dynamically changing collection of models. When this changes the DynamicComposite will react by
	 * creating or deleting sections corresponding to the corresponding added/deleted models.
	 */
	private LiveExpression<Set<M>> models;

	/**
	 * Keeps track of sections per model.
	 */
	private Map<M, SubSection> sectionsMap = new LinkedHashMap<M, SubSection>();

	/**
	 * Keeps track of the selected elements
	 */
	private DelegatingLiveSet<T> elements;
	private MultiSelection<T> selection;
	private Composite composite = null; //Set once UI is created

	private SectionFactory<M, T> sectionFactory;

	private ValueListener<Set<M>> modelListener = new ValueListener<Set<M>>() {
		public void gotValue(LiveExpression<Set<M>> exp, Set<M> value) {
			updateSections();
		}
	};

	private class SubSection {
		Collection<Control> ui;
		MultiSelectionSource<T> section;
	}

	public DynamicCompositeSection(Class<T> selectionType, IPageWithSections owner, LiveExpression<Set<M>> models, SectionFactory<M,T> sectionFactory) {
		super(owner);
		this.models = models;
		this.elements = new DelegatingLiveSet<T>();
		this.selection = new MultiSelection<T>(selectionType, elements);
		this.sectionFactory = sectionFactory;
	}

	@Override
	public MultiSelection<T> getSelection() {
		return selection;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return OK_VALIDATOR;
	}

	@Override
	public void createContents(Composite page) {
//		composite = new Composite(page, SWT.NONE);
//		Layout l = new GridLayout();
//		composite.setLayout(l);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		composite = page;

		models.addListener(modelListener);
	}

	/**
	 * Called when we need to update the sections based on the models
	 */
	private synchronized void updateSections() {
		Set<M> currentModels = models.getValue();
		System.out.println("Update sections: "+currentModels);
		System.out.println("Already have: "+sectionsMap.keySet());

		//Missing: current models for which we have no section
		Set<M> missing = new HashSet<M>(currentModels);
		missing.removeAll(sectionsMap.keySet());
		System.out.println("Missing: "+missing);

		//Extra: current sections for which there is no more model
		Set<M> extra = new HashSet<M>();
		extra.addAll(sectionsMap.keySet());
		extra.removeAll(currentModels);
		System.out.println("Extra: "+extra);

		for (M m : extra) {
			System.out.println("Delete: "+m);
			deleteSectionFor(m);
		}

		for (M m : missing) {
			System.out.println("Create: "+m);
			createSectionFor(m);
		}

		boolean dirty = !missing.isEmpty() || !extra.isEmpty();
		if (dirty) {
			reflow();
			updateSelectionDelegate();
		}
	}

	private void updateSelectionDelegate() {
		LiveExpression<Set<T>> newSelection = LiveSets.emptySet(selection.getElementType());
		for (SubSection s : sectionsMap.values()) {
			newSelection = LiveSets.union(newSelection, s.section.getSelection().getElements());
		}
		elements.setDelegate(newSelection);
	}

	private void createSectionFor(M m) {
		Set<Control> oldWidgets = new HashSet<Control>(Arrays.asList(composite.getChildren()));
		SubSection s = new SubSection();
		s.section = sectionFactory.create(m);
		s.section.createContents(composite);
		s.ui = new HashSet<Control>(Arrays.asList(composite.getChildren()));
		s.ui.removeAll(oldWidgets);
		sectionsMap.put(m, s);
	}

	private void reflow() {
		if (owner instanceof Reflowable) {
			((Reflowable) owner).reflow();
		}
	}

	private void deleteSectionFor(M m) {
		SubSection s = sectionsMap.get(m);
		sectionsMap.remove(m);
		if (s.section instanceof Disposable) {
			((Disposable) s.section).dispose();
		}
		if (s.ui!=null) {
			for (Control widget : s.ui) {
				widget.dispose();
			}
		}
	}

	@Override
	public void dispose() {
		if (modelListener!=null) {
			models.removeListener(modelListener);
		}
		if (sectionsMap!=null) {
			for (SubSection s : sectionsMap.values()) {
				if (s.section instanceof Disposable) {
					((Disposable)s.section).dispose();
				}
			}
		}
	}

}
