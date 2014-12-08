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

import java.util.ArrayList;
import java.util.Collection;

/**
 * A collection of data that can be searched with a simple 'fuzzy' string
 * matching algorithm. Clients must override 'getKey' method to define how
 * a search 'key' is associated with each data item. 
 * <p>
 * The collection can then be searched for items who's key matches 
 * simple 'fuzzy' patterns.
 */
public abstract class FuzzyMap<E> {
	
	public static class Match<E> {
		public double score;
		public final E data;
		public Match(double score, E e) {
			this.score = score;
			this.data = e;
		}
	}

	private ArrayList<E> entries = new ArrayList<E>();
	
	protected abstract String getKey(E entry);
	
	public void add(E value) {
		entries.add(value);
	}
	
	/**
	 * Search for pattern. A pattern is just a sequence of characters which have to found in 
	 * an entrie's key in the same order as they are in the pattern. 
	 */
	public Collection<Match<E>> find(String pattern) {
		ArrayList<Match<E>> matches = new ArrayList<Match<E>>();
		for (E e : entries) {
			String key = getKey(e);
			double score = match(pattern, key);
			if (score!=0.0) {
				matches.add(new Match<E>(score, e));
			}
		}
		return matches;
	}
	
	/**
	 * Match given pattern with a given data. The data is considered a 'match' for the
	 * pattern if all characters in the pattern can be found in the data, in the
	 * same order but with possible 'gaps' in between. 
	 * <p>
	 * The function returns 0.o when the pattern doesn't match the data and a non-zero
	 * 'score' when it does. The higher the score, the better the match is considered to
	 * be.
	 */
	public static double match(String pattern, String data) {
		int ppos = 0; //pos of next char in pattern to look for
		int dpos = 0; //pos of next char in data not yet matched
		int gaps = 0; //number of 'gaps' in the match. A gap is any non-empty run of consecutive characters in the data that are not used by the match
		int skips = 0; //number of skipped characters. This is the sum of the length of all the gaps.
		int plen = pattern.length();
		int dlen = data.length();
		if (plen>dlen) {
			return 0.0;
		}
		while (ppos<plen) {
			if (dpos>=dlen) {
				//still chars left in pattern but no more data
				return 0.0;
			}
			char c = pattern.charAt(ppos++);
			int foundCharAt = data.indexOf(c, dpos);
			if (foundCharAt>=0) {
				if (foundCharAt>dpos) {
					gaps++;
					skips+=foundCharAt-dpos;
				}
				dpos = foundCharAt+1;
			} else {
				return 0.0;
			}
		}
		//end of pattern reached. All matched.
		if (dpos<dlen) {
			//data left over
			//gaps++; don't count end skipped chars as a real 'gap'. Otherwise we 
			//tend to favor matches at the end of the string over matches in the middle.
			skips+=dlen-dpos; //but do count the extra chars at end => more extra = worse score
		}
		return score(gaps, skips);
	}

	private static double score(int gaps, int skips) {
		double badness = 1+gaps + skips/1000.0; // higher is worse
		return -badness; //higher is better
	}

}
