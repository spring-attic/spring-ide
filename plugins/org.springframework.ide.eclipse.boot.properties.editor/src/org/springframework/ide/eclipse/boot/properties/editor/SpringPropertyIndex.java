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
package org.springframework.ide.eclipse.boot.properties.editor;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.configurationmetadata.ConfigurationMetadataSource;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo.PropertySource;

public class SpringPropertyIndex extends FuzzyMap<PropertyInfo> {

	public SpringPropertyIndex(IJavaProject jp) {
		try {
			StsConfigMetadataRepositoryJsonLoader loader = new StsConfigMetadataRepositoryJsonLoader();
			ConfigurationMetadataRepository metadata = loader.load(jp);
			//^^^ Should be done in bg? It seems fast enough for now.

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

//			System.out.println(">>> spring properties metadata loaded "+this.size()+" items===");
//			dumpAsTestData();
//			System.out.println(">>> spring properties metadata loaded "+this.size()+" items===");
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
	}
	/**
	 * Dumps out 'test data' based on the current contents of the index. This is not meant to be
	 * used in 'production' code. The idea is to call this method during development to dump a
	 * 'snapshot' of the index onto System.out. The data is printed in a forma so that it can be easily
	 * pasted/used into JUNit testing code.
	 */
	public void dumpAsTestData() {
		List<Match<PropertyInfo>> allData = this.find("");
		for (Match<PropertyInfo> match : allData) {
			PropertyInfo d = match.data;
			System.out.println("data("
					+dumpString(d.getId())+", "
					+dumpString(d.getType())+", "
					+dumpString(d.getDefaultValue())+", "
					+dumpString(d.getDescription()) +");"
			);
//			for (PropertySource source : d.getSources()) {
//				String st = source.getSourceType();
//				String sm = source.getSourceMethod();
//				if (sm!=null) {
//					System.out.println(d.getId() +" from: "+st+"::"+sm);
//				}
//			}
		}
	}

	private String dumpString(Object v) {
		if (v==null) {
			return "null";
		}
		return dumpString(""+v);
	}

	private String dumpString(String s) {
		if (s==null) {
			return "null";
		} else {
			StringBuilder buf = new StringBuilder("\"");
			for (char c : s.toCharArray()) {
				switch (c) {
				case '\r':
					buf.append("\\r");
					break;
				case '\n':
					buf.append("\\n");
					break;
				case '\\':
					buf.append("\\\\");
					break;
				case '\"':
					buf.append("\\\"");
					break;
				default:
					buf.append(c);
					break;
				}
			}
			buf.append("\"");
			return buf.toString();
		}
	}

	public SpringPropertyIndex() {}

	@Override
	protected String getKey(PropertyInfo entry) {
		return entry.getId();
	}

}
