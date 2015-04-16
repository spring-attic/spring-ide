package org.springframework.ide.eclipse.boot.properties.editor.util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public abstract class PrefixFinder {
	public String getPrefix(IDocument doc, int offset) {
		try {
			if (doc == null || offset > doc.getLength())
				return null;
			int prefixStart= offset;
			while (prefixStart > 0 && isPrefixChar(doc.getChar(prefixStart-1))) {
				prefixStart--;
			}
			return doc.get(prefixStart, offset-prefixStart);
		} catch (BadLocationException e) {
			return null;
		}
	}
	protected abstract boolean isPrefixChar(char c);
}