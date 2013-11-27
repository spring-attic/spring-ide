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
package org.springframework.ide.eclipse.wizard.gettingstarted.guides;

import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.misc.StringMatcher;
import org.eclipse.ui.internal.misc.StringMatcher.Position;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.ContentManager;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.ContentType;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.Describable;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.DisplayNameable;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.GSContent;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
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

		private final ContentManager content;

		public ContentProvider(ContentManager content) {
			this.content = content;
		}

		//@Override
		public void dispose() {
		}

		//@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		//@Override
		public Object[] getElements(Object e) {
			if (e==content) {
				ContentType<?>[] types = content.getTypes();
				if (types!=null) {
					if (types.length==1) {
						//If there's only one type of content. Then it looks better
						//to just show those elements uncategorized.
						return getChildren(types[0]);
					} else {
						return types;
					}
				}
			}
			return null;
		}

		//@Override
		public Object[] getChildren(Object e) {
			try {
				if (e instanceof ContentType<?>) {
					return content.get((ContentType<?>)e);
				}
				return null;
			} catch (Throwable error) {
				WizardPlugin.log(error);
				return new Object[] {error};
			}
		}

		//@Override
		public Object getParent(Object e) {
			if (e instanceof GSContent) {
				return e.getClass();
			} else if (e instanceof Class<?>) {
				return content;
			}
			return null;
		}

		//@Override
		public boolean hasChildren(Object e) {
			Object[] c = getChildren(e);
			return c!=null && c.length>0;
		}
	}

	private static final LabelProvider labelProvider = new LabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof DisplayNameable) {
				DisplayNameable item = (DisplayNameable) element;
				return item.getDisplayName();
			} else if (element instanceof Throwable) {
				return ExceptionUtil.getMessage((Throwable) element);
			}
			return super.getText(element);
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof Throwable) {
				return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
			}
			return null;
		}
	};

	private class ChoicesFilter extends ViewerFilter {

		private StringMatcher matcher = null;
		private final HashMap<Object, Boolean> cache = new HashMap<Object, Boolean>();

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
			if (e instanceof ContentType<?>) {
				//Only search in the content (leaves). The contenttypes are selected if
				// any of their children (content) is selected.
				return matchChildren(viewer, e);
			} else if (match(label)) {
				return true;
			} else if (e instanceof Describable && match(((Describable) e).getDescription())) {
				return true;
			}
			return false;
		}

		private boolean matchChildren(Viewer viewer, Object e) {
			Object[] children=contentProvider.getChildren(e);
			if (children!=null) {
				for (Object c : children) {
					if (select(viewer, e, c)) {
						return true;
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
	private final SelectionModel<GSContent> selection;
	private Text searchBox;
	private ChoicesFilter filter;
	private final ContentManager content;
	private final ContentProvider contentProvider;
	private final LiveVariable<Object> rawSelection;
	private TreeViewer treeviewer;
	private String initialFilterText;
	private String category;

	public ChooseTypedContentSection(IPageWithSections owner, SelectionModel<GSContent> selection,
			LiveVariable<Object> rawSelection, ContentManager content) {
		super(owner);
		this.selection = selection;
		this.rawSelection = rawSelection;
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
		searchBox.setMessage("Type pattern to match");

		GridDataFactory.fillDefaults().grab(true, false).applyTo(searchBox);

		Label fieldNameLabel = null;
		if (sectionLabel!=null) {
			fieldNameLabel = new Label(field, SWT.NONE);
			fieldNameLabel.setText(sectionLabel);
		}

		treeviewer = new TreeViewer(field, SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL);
		treeviewer.addFilter(filter = new ChoicesFilter());
		treeviewer.setLabelProvider(labelProvider);
		treeviewer.setContentProvider(contentProvider);
		treeviewer.setInput(content);
		treeviewer.expandAll();
		if (category != null) {
			treeviewer.collapseAll();
			for (TreeItem item : treeviewer.getTree().getItems()) {
				if (item.getText().equals(category)) {
					item.setExpanded(true);
				}
			}
		} else {
			treeviewer.expandAll();
		}

		if (fieldNameLabel!=null) {
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(fieldNameLabel);
		}
		GridDataFactory grab = GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 150);
		grab.applyTo(field);
		grab.applyTo(treeviewer.getControl());

		whenVisible(treeviewer.getControl(), new Runnable() {
			public void run() {
				GSContent preSelect = selection.selection.getValue();
				if (preSelect!=null) {
					treeviewer.setSelection(new StructuredSelection(preSelect), true);
				} else {
					treeviewer.setSelection(StructuredSelection.EMPTY, true);
				}
				if (initialFilterText!=null) {
					searchBox.setText(initialFilterText);
					updateFilter();
				}
			}
		});

		treeviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = treeviewer.getSelection();
				if (sel.isEmpty()) {
					setSelection(null);
				} else if (sel instanceof IStructuredSelection){
					IStructuredSelection ss = (IStructuredSelection) sel;
					setSelection(ss.getFirstElement());
				} else {
					//Not expecting anything else. So ignore.
				}
			}

			private void setSelection(Object e) {
				if (e == null) {
					selection.selection.setValue(null);
					rawSelection.setValue(null);
				} else {
					rawSelection.setValue(e);
					if (e instanceof GSContent) {
						selection.selection.setValue((GSContent) e);
					}
				}
			}
		});

		searchBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateFilter();
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

	public void setFilterText(String text) {
		if (searchBox!=null) {
			searchBox.setText(text);
			updateFilter();
		} else {
			//UI isn't there yet so remember the text as intial filter text to be set when
			// ui is created later.
			this.initialFilterText = text;
		}
	}

	public void setCategory(String category) {
		this.category = category;
	}

	private void updateFilter() {
		filter.setSearchTerm(searchBox.getText());
		treeviewer.refresh();
		treeviewer.expandAll();
	}

//	private String[] getLabels() {
//		String[] labels = new String[options.length];
//		for (int i = 0; i < labels.length; i++) {
//			labels[i] = labelProvider.getText(options[i]);
//		}
//		return labels;
//	}

}
