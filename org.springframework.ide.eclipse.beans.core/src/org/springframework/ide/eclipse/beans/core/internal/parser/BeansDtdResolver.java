/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.core.internal.parser;

import java.io.IOException;

import org.xml.sax.InputSource;

public class BeansDtdResolver extends
						org.springframework.beans.factory.xml.BeansDtdResolver {
	public InputSource resolveEntity(String publicId, String systemId)
														    throws IOException {
		InputSource input = super.resolveEntity(publicId, systemId);
		if (input == null) {
			throw new IOException("Unsupported DTD [" + systemId + ']');
		}
		return input;
	}
}
