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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * LiveExpression that computes a URL String based on a number of input fields.
 */
public class UrlMaker extends LiveExpression<String> {

	private final List<FieldModel<String>> inputs = new ArrayList<FieldModel<String>>();
	private final List<MultiSelectionFieldModel<String>> multiInputs = new ArrayList<MultiSelectionFieldModel<String>>();

	private final LiveExpression<String> baseUrl;

	public UrlMaker(String baseUrl) {
		this(LiveExpression.constant(baseUrl));
	}

	public UrlMaker(LiveExpression<String> baseUrl) {
		this.baseUrl = baseUrl;
		dependsOn(baseUrl);
	}

	public UrlMaker addField(FieldModel<String> param) {
		inputs.add(param);
		dependsOn(param.getVariable()); //Recompute my value when the input changes.
		return this;
	}

	public UrlMaker addField(MultiSelectionFieldModel<String> param) {
		multiInputs.add(param);
		dependsOn(param.getSelecteds()); //Recompute my value when the input changes.
		return this;
	}

	@Override
	protected String compute() {
		String baseUrl = this.baseUrl.getValue();
		if (baseUrl==null) {
			baseUrl = "";
		} else {
			baseUrl = baseUrl.trim();
		}
		try {
			URIBuilder builder = new URIBuilder(baseUrl);
			for (FieldModel<String> f : inputs) {
				String paramValue = f.getValue();
				if (paramValue!=null) {
					builder.addParameter(f.getName(), paramValue);
				}
			}

			for (MultiSelectionFieldModel<String> mf : multiInputs) {
				String name = mf.getName();
				for (String selectedValue : mf.getSelecteds().getValues()) {
					builder.addParameter(name, selectedValue);
				}
			}

			return builder.toString();
		} catch (URISyntaxException e) {
			//most likely baseUrl is unparseable. Can't add params then.
			WizardPlugin.log(e);
			return baseUrl;
		}
	}

}
