/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.metadata;

import java.util.List;

import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypedProperty;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlNavigable;

/**
 * @author Kris De Volder
 */
public interface HintProvider extends YamlNavigable<HintProvider> {
	List<ValueHint> getValueHints(String query);
	List<TypedProperty> getPropertyHints(String query);
}
