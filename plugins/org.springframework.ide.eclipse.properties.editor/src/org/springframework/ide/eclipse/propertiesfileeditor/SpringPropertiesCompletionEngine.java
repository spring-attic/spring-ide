/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.springframework.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.configurationmetadata.ConfigurationMetadataSource;
import org.springframework.ide.eclipse.propertiesfileeditor.FuzzyMap.Match;

/**
 * @author Kris De Volder
 */
public class SpringPropertiesCompletionEngine {
	
	private FuzzyMap<ConfigurationMetadataProperty> index = new FuzzyMap<ConfigurationMetadataProperty>() {
		protected String getKey(ConfigurationMetadataProperty entry) {
			return entry.getId();
		}
	};
	public SpringPropertiesCompletionEngine(IJavaProject jp) throws Exception {
		StsConfigMetadataRepositoryJsonLoader loader = new StsConfigMetadataRepositoryJsonLoader();
		ConfigurationMetadataRepository metadata = loader.load(jp); //TODO: is this fast enough? Or should it be done in background?
		
		Collection<ConfigurationMetadataProperty> allEntries = metadata.getAllProperties().values();
		for (ConfigurationMetadataProperty item : allEntries) {
			index.add(item);
		}
		
//		System.out.println(">>> spring properties metadata loaded ===");
//		int i = 0;
//		for (Match<ConfigurationMetadataProperty> entry : index.find("")) {
//			System.out.println(String.format("%3d", ++i)+":"+ entry.data.getId());
//		}
//		System.out.println("<<< spring properties metadata loaded ===");
	}

	public Collection<Match<ConfigurationMetadataProperty>> getCompletions(IDocument doc, final String prefix, int offset) {
		return index.find(prefix);
	}

}

//Mock implementation (makes suggestions from a hard-coded list of words.
//
//public class SpringPropertiesCompletionEngine {
//	
//	private String[] WORDS = {
//		"bar",
//		"bartentender",
//		"bartering",
//		"barren",
//		"banana",
//		"bar.mitswa"
//	};
//	
//	private IJavaProject javaProject;
//
//	public SpringPropertiesCompletionEngine(IJavaProject jp) {
//		this.javaProject = jp;
//	}
//
//	public Collection<String> getCompletions(IJavaProject javaProject, IDocument doc, String prefix, int offset) {
//		ArrayList<String> completions = new ArrayList<>();
//		for (String word : WORDS) {
//			if (word.startsWith(prefix)) {
//				completions.add(word.substring(prefix.length()));
//			}
//		}
//		return completions;
//	}
//
//}
