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
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.springframework.ide.eclipse.editor.support.util.DocumentUtil;

/**
 * A non-sucky alternative to {@link IRegion}. Represents a region of text in a document.
 * <p>
 * Caution: assumes the underlying document is not mutated during the lifetime of the
 * region object (otherwise start/end positions may no longer be valid).
 * <p>
 * Implements {@link CharSequence} for convenience (e.g you can use {@link DocumentRegion} as
 * input to a {@link Pattern} and other standard JRE functions which expect a {@link CharSequence}.
 *
 * @author Kris De Volder
 */
public class DocumentRegion implements CharSequence {
	final IDocument doc;
	final int start;
	final int end;
	public DocumentRegion(IDocument doc, IRegion r) {
		this(doc,
			r.getOffset(),
			r.getOffset()+r.getLength()
		);
	}

	/**
	 * Constructs a {@link DocumentRegion} on a given document. Tries its
	 * best to behave sensibly when passed 'strange' coordinates by
	 * adjusting them logically rather than throw an Exception.
	 * <p>
	 * A position before the start of the document is moved to be the start
	 * of the document.
	 * <p>
	 * A position after the end of the document is moved to the end
	 * of the document.
	 * <p>
	 * If 'end' position is before the start position it is moved be
	 * exactly at the start position (this avoids region with
	 * negative length).
	 */
	public DocumentRegion(IDocument doc, int start, int end) {
		this.doc = doc;
		this.start = limitRange(start, 0, doc.getLength());
		this.end = limitRange(end, start, doc.getLength());
	}

	private int limitRange(int offset, int min, int max) {
		if (offset<min) {
			return min;
		}
		if (offset>max) {
			return max;
		}
		return offset;
	}

	@Override
	public String toString() {
		return DocumentUtil.textBetween(doc, start, end);
	}

	public DocumentRegion trim() {
		return trimEnd().trimStart();
	}

	public DocumentRegion trimStart() {
		int howMany = 0;
		int len = length();
		while (howMany<len && Character.isWhitespace(charAt(howMany))) {
			howMany++;
		}
		return subSequence(howMany, len);
	}

	public DocumentRegion trimEnd() {
		int howMany = 0; //how many chars to remove from the end
		int len = length();
		int lastChar = len-1;
		while (howMany<len && Character.isWhitespace(charAt(lastChar-howMany))) {
			howMany++;
		}
		if (howMany>0) {
			return subSequence(0, len-howMany);
		}
		return this;
	}

	/**
	 * Gets character from the region, offset from the start of the region
	 * @return the character from the document (char)0 if the offset is outside the region.
	 */
	@Override
	public char charAt(int offset) {
		if (offset<0 || offset>=length()) {
			throw new IndexOutOfBoundsException(""+offset);
		}
		try {
			return doc.getChar(start+offset);
		} catch (BadLocationException e) {
			throw new IndexOutOfBoundsException(""+offset);
		}
	}

	@Override
	public int length() {
		return end-start;
	}

	@Override
	public DocumentRegion subSequence(int start, int end) {
		int len = length();
		Assert.isLegal(start>=0);
		Assert.isLegal(end<=len);
		if (this.start==0 && this.end==len) {
			return this;
		}
		return new DocumentRegion(doc, this.start+start, this.start+end);
	}

	public boolean isEmpty() {
		return length()==0;
	}

	public DocumentRegion subSequence(int start) {
		return subSequence(start, length());
	}

	public IRegion asRegion() {
		return new Region(start, end-start);
	}
}