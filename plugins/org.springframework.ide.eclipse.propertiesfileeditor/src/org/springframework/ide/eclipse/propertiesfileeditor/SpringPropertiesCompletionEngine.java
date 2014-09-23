package org.springframework.ide.eclipse.propertiesfileeditor;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.IDocument;

public class SpringPropertiesCompletionEngine {
	
	private String[] WORDS = {
		"bar",
		"bartentender",
		"bartering",
		"barren",
		"banana",
		"bar.mitswa"
	};

	public Collection<String> getCompletions(IDocument document, String prefix, int offset) {
		ArrayList<String> completions = new ArrayList<>();
		for (String word : WORDS) {
			if (word.startsWith(prefix)) {
				completions.add(word.substring(prefix.length()));
			}
		}
		return completions;
	}

}
