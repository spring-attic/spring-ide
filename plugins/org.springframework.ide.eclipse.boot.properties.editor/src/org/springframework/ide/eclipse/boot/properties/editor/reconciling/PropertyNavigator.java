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
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyAnnotation.ERROR_TYPE;
import static org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.isBracketable;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesCompletionEngine;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine.IProblemCollector;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.ValueParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypedProperty;
import org.springframework.ide.eclipse.boot.util.StringUtil;

/**
 * Helper class for {@link SpringPropertiesReconcileEngine} and {@link SpringPropertiesCompletionEngine}.
 * <p>
 * This class provides a means to 'navigate' a chain of bracket and dot navigation operations down
 * from a typed property into its value type.
 *
 * @author Kris De Volder
 */
public class PropertyNavigator {

	private static final char EOF = 0;

	/**
	 * If problem collector is not null, then problems detected in the navigation chain are added to the
	 * collector.
	 */
	private IProblemCollector problemCollector;

	/**
	 * Document in which navigation chain text is contained.
	 */
	private IDocument doc;

	private TypeUtil typeUtil;

	private IRegion region;

	private String regionText;

	public PropertyNavigator(IDocument doc, IProblemCollector problemCollector, TypeUtil typeUtil, IRegion region) throws BadLocationException {
		this.doc = doc;
		this.problemCollector = problemCollector==null?IProblemCollector.NULL:problemCollector;
		this.typeUtil = typeUtil;
		this.region = region;
		this.regionText = doc.get(region.getOffset(), region.getLength());
	}

	/**
	 * @param offset current position in the nav chain. Text before this offset is already 'processed'.
	 * @param type The type at the end of the already processed nav chain. The next nav op in the chain
	 *             should go deeper into this type.
	 * @param r The entire region of the navchain, including both the already processed portion as well
	 *        as the remaining text.
	 * @return Type at the end of the whole nav chain, or null if the type could not be determined.
	 */
	public Type navigate(int offset, Type type) {
		if (type!=null) {
			if (offset<getEnd(region)) {
				char navOp = getChar(offset);
				if (navOp=='.') {
					if (typeUtil.isDotable(type)) {
						return dotNavigate(offset, type);
					} else {
						problemCollector.accept(new SpringPropertyProblem(ERROR_TYPE,
								"Can't use '.' navigation for property '"+textBetween(region.getOffset(), offset)+"' of type "+type,
								offset, getEnd(region)-offset));
					}
				} else if (navOp=='[') {
					if (isBracketable(type)) {
						return bracketNavigate(offset, type);
					} else {
						problemCollector.accept(new SpringPropertyProblem(ERROR_TYPE,
								"Can't use '[..]' navigation for property '"+textBetween(region.getOffset(), offset)+"' of type "+type,
								offset, getEnd(region)-offset));
					}
				} else {
					problemCollector.accept(new SpringPropertyProblem(ERROR_TYPE, "Expecting either a '.' or '['", offset, getEnd(region)-offset));
				}
			} else {
				//end of nav chain
				return type;
			}
		}
		//Something we can't handle...
		return null;
	}

	private String textBetween(int start, int end) {
		try {
			if (end>start) {
				return doc.get(start, end-start);
			}
		} catch (BadLocationException e) {
			//ignore
		}
		return "";
	}

	private int indexOf(char c, int from) {
		int offset = region.getOffset();
		int found = regionText.indexOf(c, from-offset);
		if (found>=0) {
			return found+offset;
		}
		return -1;
	}

	/**
	 * Handle bracket navigation into given type, after a bracket at
	 * was found at given offset. Assumes the type has already been checked to
	 * be 'bracketable'.
	 */
	private Type bracketNavigate(int offset, Type type) {
		int lbrack = offset;
		int rbrack = indexOf(']', lbrack);
		if (rbrack<0) {
			problemCollector.accept(new SpringPropertyProblem(ERROR_TYPE,
					"No matching ']'",
					offset, 1));
		} else {
			String indexStr = textBetween(lbrack+1, rbrack);
			if (!indexStr.contains("${")) {
				try {
					Integer.parseInt(indexStr);
				} catch (Exception e) {
					problemCollector.accept(new SpringPropertyProblem(ERROR_TYPE,
						"Expecting 'Integer' for '[...]' notation '"+textBetween(region.getOffset(), lbrack)+"'",
						lbrack+1, rbrack-lbrack-1
					));
				}
			}
			Type domainType = TypeUtil.getDomainType(type);
			return navigate(rbrack+1, domainType);
		}
		return null;
	}


	/**
	 * Handle dot navigation into given type, after a '.' was
	 * was found at given offset. Assumes the type has already been
	 * checked to be 'dotable'.
	 */
	private Type dotNavigate(int offset, Type type) {
		if (typeUtil.isMap(type)) {
			int keyStart = offset+1;
			Type domainType = TypeUtil.getDomainType(type);
			int keyEnd = -1;
			if (typeUtil.isDotable(domainType)) {
				//'.' should be interpreted as navigation.
				keyEnd = nextNavOp(".[", offset+1);
			} else {
				//'.' should *not* be interpreted as navigation.
				keyEnd = nextNavOp("[", offset+1);
			}
			String key = textBetween(keyStart, keyEnd);
			Type keyType = TypeUtil.getKeyType(type);
			if (keyType!=null) {
				ValueParser keyParser = typeUtil.getValueParser(keyType);
				if (keyParser!=null) {
					try {
						keyParser.parse(key);
					} catch (Exception e) {
						problemCollector.accept(new SpringPropertyProblem(ERROR_TYPE,
								"Expecting "+typeUtil.niceTypeName(keyType),
								keyStart, keyEnd-keyStart));
					}
				}
			}
			return navigate(keyEnd, domainType);
		} else {
			// dot navigation into object properties
			int keyStart = offset+1;
			int	keyEnd = nextNavOp(".[", offset+1);
			if (keyEnd<0) {
				keyEnd = getEnd(region);
			}
			String key = StringUtil.camelCaseToHyphens(textBetween(keyStart, keyEnd));

			List<TypedProperty> properties = typeUtil.getProperties(type);
			if (properties!=null) {
				TypedProperty prop = null;
				for (TypedProperty p : properties) {
					if (p.getName().equals(key)) {
						prop = p;
						break;
					}
				}
				if (prop==null) {
					problemCollector.accept(new SpringPropertyProblem(ERROR_TYPE,
							"Type '"+typeUtil.niceTypeName(type)+"' has no property '"+key+"'",
							keyStart, keyEnd-keyStart));
				} else {
					return navigate(keyEnd, prop.getType());
				}
			}
		}
		return null;
	}

	/**
	 * Skip ahead from give position until reaching the next 'navigation' operator (or the end
	 * of the navigation chain region).
	 *
	 * @param navops Each character in this string is considered a 'navigation operator'.
	 * @param pos current position in the document.
	 * @return position of next navop if found, or the position at the end of the region if not found.
	 */
	private int nextNavOp(String navops, int pos) {
		int end = getEnd(region);
		while (pos < end && navops.indexOf(getChar(pos))<0) {
			pos++;
		}
		return Math.min(pos, end); //ensure never past the end
	}

	private char getChar(int offset) {
		try {
			return doc.getChar(offset);
		} catch (BadLocationException e) {
			//outside doc, return something anyways.
			return EOF;
		}
	}

	private int getEnd(IRegion region) {
		return region.getOffset()+region.getLength();
	}
}
