/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.ide.eclipse.aop.core.model.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMember;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.util.ObjectUtils;

public class AopReference implements IAopReference {

    private ADVICE_TYPES type;

    private IMember source;

    private IMember target;

    private IAspectDefinition definition;

    private IResource file;

    private IBean bean;

    public AopReference(ADVICE_TYPES type, IMember source, IMember target,
            IAspectDefinition def, IResource file, IBean bean) {
        this.type = type;
        this.source = source;
        this.target = target;
        this.definition = def;
        this.file = file;
        this.bean = bean;
    }

    public IAspectDefinition getDefinition() {
        return definition;
    }

    public ADVICE_TYPES getAdviceType() {
        return this.type;
    }

    public IMember getSource() {
        return this.source;
    }

    public IMember getTarget() {
        return this.target;
    }

    public IResource getResource() {
        return file;
    }

    public boolean equals(Object obj) {
        if (obj instanceof AopReference) {
            AopReference other = (AopReference) obj;
            return getTarget().equals(other.getTarget())
                    && ((getSource() == null && other.getSource() == null)
                    || (getSource() != null && getSource().equals(other.getSource())))
                    && getResource().equals(other.getResource())
                    && getDefinition().getAspectLineNumber() == other
                            .getDefinition().getAspectLineNumber();
        }
        return false;
    }

    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(source);
        hashCode = 21 + ObjectUtils.nullSafeHashCode(target);
        hashCode = 24 + ObjectUtils.nullSafeHashCode(file);
        hashCode = 12 + ObjectUtils.nullSafeHashCode(definition
                .getAspectLineNumber());
        return hashCode;
    }

    public IBean getTargetBean() {
        return bean;
    }
}
