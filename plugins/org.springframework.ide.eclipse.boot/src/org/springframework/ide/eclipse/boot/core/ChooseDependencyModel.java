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
package org.springframework.ide.eclipse.boot.core;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalType;
import org.springsource.ide.eclipse.commons.livexp.core.BooleanFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;

/**
 * Model for a ChooseDependencyDialog.
 *
 * @author Kris De Volder
 */
public class ChooseDependencyModel implements OkButtonHandler {

	public final MavenCoordinates[] availableChoices;
	public final LiveVariable<MavenCoordinates> selected = new LiveVariable<MavenCoordinates>();
	public final LiveExpression<ValidationResult> validator = Validator.notNull(selected, "No dependency selected");

	public final BooleanFieldModel disableJarTypeAssist = new BooleanFieldModel("disableJarTypeAssist", false);
	{
		disableJarTypeAssist.label("Don't ask again (disables Jar Type Search content assistant)");
	}

	public final LiveExpression<String> previewText = new LiveExpression<String>("") {
		{
			dependsOn(selected);
		}

		@Override
		protected String compute() {
			MavenCoordinates d = selected.getValue();
			if (d!=null) {
				return d.toXmlString();
			}
			return "";
		}
	};

	/**
	 * Records when performOk is called. This allows us to determine if the dialog was closed
	 * via ok button or via another means (i.e. cancel button).
	 */
	private boolean okPerformed = false;
	private String dependencyFileName;
	private ExternalType type;

	public ChooseDependencyModel(Collection<MavenCoordinates> availableChoices, String dependencyFileName, ExternalType type) {
		this.type = type;
		this.dependencyFileName = dependencyFileName;
		Assert.isLegal(!availableChoices.isEmpty());
		this.availableChoices = availableChoices.toArray(new MavenCoordinates[availableChoices.size()]);
		selected.setValue(this.availableChoices[0]);
	}

	public MavenCoordinates getResult() {
		if (okPerformed && !disableJarTypeAssist.getValue()) {
			return selected.getValue();
		}
		//We cannot distinguish between cancelation and no selection is this a problem?
		return null;
	}

	@Override
	public void performOk() throws Exception {
		okPerformed = true;
	}

	public String getTitle() {
		return "Confirm Changes to '"+dependencyFileName+"'";
	}

	public String getDependencyFileName() {
		return dependencyFileName;
	}

	public boolean isShowChoices() {
		return availableChoices.length>1;
	}

	public String getTypeName() {
		return type.getFullyQualifiedName();
	}

}
