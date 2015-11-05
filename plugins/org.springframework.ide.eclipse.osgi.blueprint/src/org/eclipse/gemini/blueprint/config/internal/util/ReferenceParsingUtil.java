/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.config.internal.util;

import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Reference related parsing utility.
 * 
 * @author Costin Leau
 */
public abstract class ReferenceParsingUtil {

	private static final String ONE = "1";
	private static final String M = "m";

	public static void checkAvailabilityAndCardinalityDuplication(Element element, String availabilityName,
			String cardinalityName, ParserContext context) {

		String avail = element.getAttribute(availabilityName);
		String cardinality = element.getAttribute(cardinalityName);

		if (StringUtils.hasText(avail) && StringUtils.hasText(cardinality)) {
			boolean availStatus = avail.startsWith(ONE);
			boolean cardStatus = cardinality.startsWith(M);

			if (availStatus != cardStatus) {
				context.getReaderContext().error(
						"Both '" + availabilityName + "' and '" + cardinalityName
								+ "' attributes have been specified but with contradictory values.", element);
			}
		}
	}

	public static Availability determineAvailabilityFromCardinality(String value) {
		return (value.startsWith(ONE) ? Availability.MANDATORY : Availability.OPTIONAL);
	}

	public static Availability determineAvailability(String value) {
		return (value.startsWith(M) ? Availability.MANDATORY : Availability.OPTIONAL);
	}
}
