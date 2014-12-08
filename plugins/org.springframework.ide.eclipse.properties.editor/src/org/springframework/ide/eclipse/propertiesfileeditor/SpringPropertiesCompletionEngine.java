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

import java.util.Collection;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.configurationmetadata.ConfigurationMetadataRepository;
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
	
	/** 
	 * Create an empty completion engine. Meant for unit testing. Real clients should use the
	 * constructor that accepts an {@link IJavaProject}. 
	 */
	public SpringPropertiesCompletionEngine() {
	}
	
	/** 
	 * Create a completion engine and poplulate it with metadata parsed from given 
	 * {@link IJavaProject}'s classpath.
	 */
	public SpringPropertiesCompletionEngine(IJavaProject jp) throws Exception {
		StsConfigMetadataRepositoryJsonLoader loader = new StsConfigMetadataRepositoryJsonLoader();
		ConfigurationMetadataRepository metadata = loader.load(jp); //TODO: is this fast enough? Or should it be done in background?
		
		Collection<ConfigurationMetadataProperty> allEntries = metadata.getAllProperties().values();
		for (ConfigurationMetadataProperty item : allEntries) {
			add(item);
		}
		
//		System.out.println(">>> spring properties metadata loaded ===");
//		int i = 0;
//		for (Match<ConfigurationMetadataProperty> entry : index.find("")) {
//			System.out.println(String.format("%3d", ++i)+":"+ entry.data.getId());
//		}
//		System.out.println("<<< spring properties metadata loaded ===");
	}

	/**
	 * Add a ConfigurationMetadataProperty item to the CompletionEngine. Normal clients don't really need to
	 * call this, the data will be parsed from project's classpath. This mostly here to allow the engine to
	 * be more easily unit tested with controlled test data.
	 */
	public void add(ConfigurationMetadataProperty item) {
		index.add(item);
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
