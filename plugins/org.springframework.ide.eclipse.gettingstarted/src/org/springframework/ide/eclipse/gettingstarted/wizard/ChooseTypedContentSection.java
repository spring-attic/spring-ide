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
package org.springframework.ide.eclipse.gettingstarted.wizard;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.misc.StringMatcher;
import org.eclipse.ui.internal.misc.StringMatcher.Position;
import org.springframework.ide.eclipse.gettingstarted.content.ContentManager;
import org.springframework.ide.eclipse.gettingstarted.content.Describable;
import org.springframework.ide.eclipse.gettingstarted.content.GSContent;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * Allow choosing a guide in pull-down style combo box or table viewer.
 * 
 * TODO: this was copied from the GuidesWizard but it should be changed
 * into or replaced by something that allows picking content from
 * a tree where the first level are the types of content available
 * and the second level are elements of that content type.
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class ChooseTypedContentSection extends WizardPageSection {

	private static class ContentProvider implements ITreeContentProvider {

		private ContentManager content;
		
		public ContentProvider(ContentManager content) {
			this.content = content;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object e) {
			if (e==content) {
				return content.getTypes();
			}
			return null;
		}

		@Override
		public Object[] getChildren(Object e) {
			if (e instanceof Class<?>) {
				return content.get((Class<?>)e);
			}
			return null;
		}

		@Override
		public Object getParent(Object e) {
			if (e instanceof GSContent) {
				return e.getClass();
			} else if (e instanceof Class<?>) {
				return content;
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object e) {
			Object[] c = getChildren(e);
			return c!=null && c.length>0;
		}
	}

	private static final LabelProvider labelProvider = new LabelProvider() {
		public String getText(Object element) {
			if (element instanceof Class<?>) {
				return beatifyClassName(((Class<?>) element).getSimpleName());
			}
			if (element instanceof GSContent) {
				GSContent item = (GSContent) element;
				return item.getDisplayName();
			}
			return super.getText(element);
		}

		private String beatifyClassName(String simpleName) {
			//Assume class name is camel case. Just split it up at capital letters and
			// insert spaces there.
			StringBuilder result = new StringBuilder();
			Matcher m = Pattern.compile("[A-Z]").matcher(simpleName);
			int pos1 = 0;
			boolean found = m.find();
			while (found) {
				int pos2 = m.start();
				if (pos2>pos1) {
					result.append(simpleName.substring(pos1, pos2)+" ");
				}
				pos1 = pos2;
				found = m.find();
			}
			//Don't forget the last bit
			if (pos1>=0) {
				result.append(simpleName.substring(pos1));
			}
			return result.toString();
		};
	};
	
	private class ChoicesFilter extends ViewerFilter {
		
		private StringMatcher matcher = null;
		private HashMap<Object, Boolean> cache = new HashMap<Object, Boolean>();
		
		public ChoicesFilter() {
			if (searchBox!=null) {
				setSearchTerm(searchBox.getText());
			}
		}
		
		public void setSearchTerm(String text) {
			matcher = new StringMatcher(text, true, false);
			cache.clear();
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (matcher==null) {
				return true;
			}
			Boolean v = cache.get(element);
			if (v==null) {
				v = compute(viewer, parentElement, element);
				cache.put(element, v);
			}
			return v;
		}
		
		public boolean compute(Viewer viewer, Object parentElement, Object e) {
			String label = labelProvider.getText(e);
			if (match(label)) {
				return true;
			} else if (e instanceof Describable && match(((Describable) e).getDescription())) {
				return true;
			} else {
				Object[] children=contentProvider.getChildren(e);
				if (children!=null) {
					for (Object c : children) {
						if (select(viewer, e, c)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		private boolean match(String text) {
			if (matcher==null) {
				return true; // Search term not set... anything is acceptable.
			} else if (text==null) {
				return false;
			} else {
				Position x = matcher.find(text, 0, text.length());
				return x!=null;
			}
		}

	}
	
	
	private String sectionLabel;
	private SelectionModel<GSContent> selection;
	private Text searchBox;
	private ChoicesFilter filter;
	private ContentManager content;
	private ContentProvider contentProvider;

	public ChooseTypedContentSection(IPageWithSections owner, SelectionModel<GSContent> selection, ContentManager content) {
		super(owner);
		this.selection = selection;
		this.content = content;
		this.contentProvider = new ContentProvider(content);
	}
	

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return selection.validator;
	}

	@Override
	public void createContents(Composite page) {
		Composite field = new Composite(page, SWT.NONE);
		int cols = sectionLabel==null ? 1 : 2;
		GridLayout layout = GridLayoutFactory.fillDefaults().numColumns(cols).create();
		field.setLayout(layout);
		
		searchBox = new Text(field, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(searchBox);
		
		Label fieldNameLabel = null;
		if (sectionLabel!=null) {
			fieldNameLabel = new Label(field, SWT.NONE);
			fieldNameLabel.setText(sectionLabel);
		}
		
		final TreeViewer tv = new TreeViewer(field, SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL);
		tv.addFilter(filter = new ChoicesFilter());
		tv.setLabelProvider(labelProvider);
		tv.setContentProvider(contentProvider);
		tv.setInput(content);
		tv.expandAll();
		
		if (fieldNameLabel!=null) {
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(fieldNameLabel);
		}
		GridDataFactory grab = GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 150);
		grab.applyTo(field);
		grab.applyTo(tv.getControl());
		
		whenVisible(tv.getControl(), new Runnable() {
			public void run() {
				GSContent preSelect = selection.selection.getValue();
				if (preSelect!=null) {
					tv.setSelection(new StructuredSelection(preSelect), true);
				} else {
					tv.setSelection(StructuredSelection.EMPTY, true);
				}
			}
		});
		
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = tv.getSelection();
				if (sel.isEmpty()) {
					selection.selection.setValue(null);
				} else if (sel instanceof IStructuredSelection){
					IStructuredSelection ss = (IStructuredSelection) sel;
					Object el = ss.getFirstElement();
					if (el instanceof GSContent) {
						selection.selection.setValue((GSContent) el);
					} else {
						selection.selection.setValue(null);
					}
				}
			}
		});
		
		searchBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				filter.setSearchTerm(searchBox.getText());
				tv.refresh();
				tv.expandAll();
			}
		});
	}

	private void whenVisible(final Control control, final Runnable runnable) {
		PaintListener l = new PaintListener() {
			public void paintControl(PaintEvent e) {
				runnable.run();
				control.removePaintListener(this);
			}
		};
		control.addPaintListener(l);
	}

//	private String[] getLabels() {
//		String[] labels = new String[options.length]; 
//		for (int i = 0; i < labels.length; i++) {
//			labels[i] = labelProvider.getText(options[i]);
//		}
//		return labels;
//	}
	
}
