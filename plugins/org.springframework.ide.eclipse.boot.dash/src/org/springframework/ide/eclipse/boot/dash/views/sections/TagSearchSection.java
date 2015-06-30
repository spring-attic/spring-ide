/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.boot.dash.views.BootDashLabelProvider;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;

/**
 * Tags searching section. Creates a search text box UI wise and allows to
 * register listeners to tags and search term changes.
 * 
 * @author Alex Boyko
 *
 */
public class TagSearchSection extends PageSection implements Disposable {
	
	private Text tagsSearchBox;

	private String[] searchTags = new String[0];
	
	private String tagSearchTerm = "";
	
	private List<TagSearchListener> listeners = new ArrayList<TagSearchListener>();

	public TagSearchSection(IPageWithSections owner) {
		super(owner);
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return OK_VALIDATOR;
	}

	@Override
	public void createContents(Composite page) {
		tagsSearchBox = new Text(page, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		tagsSearchBox.setMessage("Type tags to match");
		tagsSearchBox.setText(StringUtils.join(searchTags, BootDashLabelProvider.TAGS_SEPARATOR));
		tagsSearchBox.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		tagsSearchBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				internalSetSearchTags(tagsSearchBox.getText());
			}
		});
	}

	private void internalSetSearchTags(String s) {
		if (s.isEmpty()) {
			searchTags = new String[0];
			tagSearchTerm = "";
		} else {
			String[] splitSearchStr = s.split("\\s+");
			if (Pattern.matches("(.+)\\s+", s)) {
				searchTags = splitSearchStr;
				tagSearchTerm = "";
			} else {
				searchTags = Arrays.copyOfRange(splitSearchStr, 0, splitSearchStr.length - 1);
				tagSearchTerm = splitSearchStr[splitSearchStr.length - 1];
			}
		}
		notifySearchTagsChanged();
	}
	
	private void notifySearchTagsChanged() {
		for (TagSearchListener listener : listeners) {
			listener.searchTermsChanged(searchTags, tagSearchTerm);
		}
	}
	
	public String[] getSearchTags() {
		return searchTags;
	}
	
	public String getSearchTerm() {
		return tagSearchTerm;
	}
	
	public void addListener(TagSearchListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(TagSearchListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void dispose() {
		tagsSearchBox.dispose();
		listeners.clear();
	}

	public interface TagSearchListener {
		
		void searchTermsChanged(String[] tags, String term);
		
	}

}
