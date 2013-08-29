/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.boot;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

/**
 * Model for a UI widget that offers multiple choices. Could be represented
 * by a set of Checkboxes or multi-selection enabled list/tree viewer.
 * 
 * @author Kris De Volder
 */
public class MultiSelectionFieldModel<T> {

	private Class<T> type; //Type of data stored in the field.
	private String name; // used to submit value to some service that handles the form
	private String label; // Label to display in forms
	private LiveSet<T> variable = new LiveSet<T>();
	private LiveExpression<ValidationResult> validator;
	
	private Map<T,String> labelMap = new LinkedHashMap<T, String>();
	
	public MultiSelectionFieldModel(Class<T> type, String name) {
		this.type = type;
		this.name  = name;
		this.label = name;
		this.variable = new LiveSet<T>();
		this.validator = Validator.OK;
	}
	
	public Class<T> getType() {
		return type;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getLabel(T value) {
		return labelMap.get(value);
	}

	public String getName() {
		return name;
	}

	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}
	
	public MultiSelectionFieldModel<T> validator(LiveExpression<ValidationResult> v) {
		this.validator = v;
		return this;
	}

	public LiveSet<T> getSelecteds() {
		return variable;
	}
	
	public void add(T v) {
		getSelecteds().add(v);
	}
	public void remove(T v) {
		getSelecteds().remove(v);
	}

	/**
	 * Add a valid choice to the multi selection model. 
	 * @param label String to show the choice to a user
	 * @param key  Value added to the set when user selects this choice. 
	 */
	public MultiSelectionFieldModel<T> choice(String label, T value) {
		Assert.isLegal(labelMap.get(value)==null, "Duplicate choice "+value+" already added");
		labelMap.put(value, label);
		return this;
	}

	public MultiSelectionFieldModel<T> label(String label) {
		this.label = label;
		return this;
	}

	@SuppressWarnings("unchecked")
	public synchronized T[] getChoices() {
		Collection<T> values = labelMap.keySet();
		return values.toArray((T[]) Array.newInstance(getType(), values.size()));
	}

}
