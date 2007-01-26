/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.util;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;

public class BeansJavaDocUtils {

    private final IMember element;

    public BeansJavaDocUtils(IMember element) {
        this.element = element;
    }

    public String getJavaDoc() {

        try {
            StringBuffer buf = new StringBuffer();
            Reader reader = JavadocContentAccess.getHTMLContentReader(element,
                    false, false);
            if (reader != null) {
                int charValue = 0;
                while ((charValue = reader.read()) != -1) {
                    buf.append((char) charValue);
                }
                return buf.toString();
            }
        }
        catch (JavaModelException e) {
        }
        catch (IOException e) {
        }

        return "";
    }
}
