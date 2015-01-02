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
package org.springframework.ide.eclipse.propertiesfileeditor;

import java.util.Collection;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.configurationmetadata.ConfigurationMetadataSource;

public class SpringPropertyIndex extends FuzzyMap<PropertyInfo> {

	public SpringPropertyIndex(IJavaProject jp) {
		try {
			StsConfigMetadataRepositoryJsonLoader loader = new StsConfigMetadataRepositoryJsonLoader();
			ConfigurationMetadataRepository metadata = loader.load(jp); //TODO: is this fast enough? Or should it be done in background?
			
			Collection<ConfigurationMetadataProperty> allEntries = metadata.getAllProperties().values();
			for (ConfigurationMetadataProperty item : allEntries) {
				add(new PropertyInfo(item));
			}
			
			for (ConfigurationMetadataGroup group : metadata.getAllGroups().values()) {
				for (ConfigurationMetadataSource source : group.getSources().values()) {
					for (ConfigurationMetadataProperty prop : source.getProperties().values()) {
						PropertyInfo info = get(prop.getId());
						info.addSource(source);
					}
				}
			}
			
	//		System.out.println(">>> spring properties metadata loaded "+index.size()+" items===");
	//		dumpAsTestData();
	//		System.out.println(">>> spring properties metadata loaded "+index.size()+" items===");
	
			// TODO Auto-generated method stub
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
	}

	public SpringPropertyIndex() {}

	@Override
	protected String getKey(PropertyInfo entry) {
		return entry.getId();
	}

}
